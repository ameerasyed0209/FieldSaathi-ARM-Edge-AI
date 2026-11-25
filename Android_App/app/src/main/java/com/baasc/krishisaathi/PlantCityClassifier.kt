package com.baasc.krishisaathi

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import com.baasc.krishisaathi.camera.PlantPartValidatorImpl
import com.baasc.krishisaathi.camera.ValidationStatus
import com.baasc.krishisaathi.data.VisionPrediction
import org.json.JSONArray
import org.tensorflow.lite.Interpreter
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.exp

/**
 * PlantCityClassifier
 *
 * Dedicated dual-model on-device Vision AI classifier for KrishiSaathi.
 *
 * Architecture:
 * 1. Image Quality & Plant-Likelihood Validation (multi-attribute heuristic rejection).
 * 2. Closed-set Out-of-Distribution Rejection (evaluates top-1 confidence, top-2 margin,
 *    and crop-level probability distribution to reject unsupported crops or ambiguous foliage).
 * 3. Model 1 (PlantCity MobileNetV2 FP32): 52 classes across 12 crops:
 *    Apple, Apricot, Bean, Cherry, Corn, Fig, Grape, Lokat, Pear, Persimmon, Tomato, Walnut.
 * 4. Model 2 (Tomato Multi-Organ MobileNetV2 FP32): Activated when Tomato is confidently identified.
 *    Analyzes plant part (Leaf, Stem, Fruit) and tomato disease (Viral, Wilt, Gray Mold).
 *
 * Model Specs:
 * - PlantCity Input: [1, 224, 224, 3] Float32 RGB (0.0..255.0, MobileNetV2 preprocessing included in graph)
 * - PlantCity Output: [1, 52] Float32 logits/probabilities
 */
class PlantCityClassifier(private val context: Context) {

    companion object {
        private const val TAG = "PlantCityClassifier"
        const val MODEL_FILE = "PlantCity_MobileNetV2_FP32.tflite"
        const val LABELS_FILE = "class_names.json"
        const val INPUT_WIDTH = 224
        const val INPUT_HEIGHT = 224
        const val INPUT_CHANNELS = 3
        const val NUM_CLASSES = 52

        // Calibrated rejection thresholds
        const val MIN_CLASS_CONFIDENCE = 0.65f
        const val MIN_CROP_PROBABILITY = 0.50f
        const val MIN_CROP_MARGIN = 0.15f
        const val MIN_CLASS_MARGIN = 0.10f
    }

    private val interpreter: Interpreter
    private val labels: List<String>
    private var tomatoClassifier: TomatoMultiOrganClassifier? = null
    val isInitialized: Boolean

    init {
        var initialized = false
        try {
            // 1. Load the PlantCity TFLite model from assets
            val modelBuffer = loadModelFile(context, MODEL_FILE)
            val options = Interpreter.Options().apply {
                setNumThreads(4)
            }
            interpreter = Interpreter(modelBuffer, options)

            // 2. Load the 52 class names from class_names.json
            val json = context.assets
                .open(LABELS_FILE)
                .bufferedReader()
                .use { it.readText() }

            val jsonArray = JSONArray(json)
            labels = List(jsonArray.length()) { index ->
                jsonArray.getString(index)
            }

            // 3. Allocate and validate tensors
            interpreter.allocateTensors()
            validateTensors()

            // 4. Initialize Tomato Multi-Organ classifier
            try {
                tomatoClassifier = TomatoMultiOrganClassifier(context)
                Log.d(TAG, "TomatoMultiOrganClassifier initialized successfully")
            } catch (e: Exception) {
                Log.w(TAG, "TomatoMultiOrganClassifier initialization deferred or failed: ${e.message}")
            }

            initialized = true
            Log.d(TAG, "Successfully initialized PlantCityClassifier with ${labels.size} classes")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize PlantCityClassifier: ${e.message}", e)
            throw IllegalStateException("PlantCityClassifier initialization failed: ${e.message}", e)
        }
        isInitialized = initialized
    }

    private fun validateTensors() {
        val inputTensor = interpreter.getInputTensor(0)
        val inputShape = inputTensor.shape()
        val outputTensor = interpreter.getOutputTensor(0)
        val outputShape = outputTensor.shape()

        require(inputShape.size == 4 && inputShape[1] == INPUT_WIDTH && inputShape[2] == INPUT_HEIGHT && inputShape[3] == INPUT_CHANNELS) {
            "Unexpected input shape: ${inputShape.contentToString()}. Expected [1, 224, 224, 3]"
        }
        require(outputShape.size == 2 && outputShape[1] == NUM_CLASSES) {
            "Unexpected output shape: ${outputShape.contentToString()}. Expected [1, 52]"
        }
    }

    private fun loadModelFile(context: Context, fileName: String): ByteBuffer {
        val bytes = context.assets.open(fileName).use { it.readBytes() }
        return ByteBuffer.allocateDirect(bytes.size)
            .order(ByteOrder.nativeOrder())
            .apply {
                put(bytes)
                rewind()
            }
    }

    /**
     * Run Dual-Model Vision AI Inference on a 224x224 RGB image (values in 0.0..255.0).
     */
    fun predict(bitmap: Bitmap): VisionPrediction {
        // Step 1: Pre-inference Plant-Likelihood & Image Quality validation
        val validator = PlantPartValidatorImpl()
        val validationResult = validator.validate(bitmap)
        if (!validationResult.isValid) {
            Log.w(TAG, "Image rejected by PlantPartValidator: ${validationResult.status} (${validationResult.reasonMessage})")
            return VisionPrediction(
                classIndex = -1,
                rawLabel = "REJECTED_NON_PLANT",
                crop = "No valid plant detected",
                disease = "Please capture a clear image of a plant leaf.",
                confidence = validationResult.confidence,
                isHealthy = false,
                isUncertain = true,
                modelSource = "Rejected",
                rejectionReason = validationResult.status.name
            )
        }

        // Ensure exact 224 x 224 dimensions
        val scaledBitmap = if (bitmap.width == INPUT_WIDTH && bitmap.height == INPUT_HEIGHT) {
            bitmap
        } else {
            Bitmap.createScaledBitmap(bitmap, INPUT_WIDTH, INPUT_HEIGHT, true)
        }

        // Prepare input buffer: [1, 224, 224, 3] Float32 RGB (0.0f..255.0f)
        val inputBuffer = ByteBuffer
            .allocateDirect(1 * INPUT_WIDTH * INPUT_HEIGHT * INPUT_CHANNELS * 4)
            .order(ByteOrder.nativeOrder())

        val pixels = IntArray(INPUT_WIDTH * INPUT_HEIGHT)
        scaledBitmap.getPixels(pixels, 0, INPUT_WIDTH, 0, 0, INPUT_WIDTH, INPUT_HEIGHT)

        for (pixel in pixels) {
            val red = ((pixel shr 16) and 0xFF).toFloat()
            val green = ((pixel shr 8) and 0xFF).toFloat()
            val blue = (pixel and 0xFF).toFloat()

            inputBuffer.putFloat(red)
            inputBuffer.putFloat(green)
            inputBuffer.putFloat(blue)
        }
        inputBuffer.rewind()

        // 52-class output buffer
        val output = Array(1) { FloatArray(NUM_CLASSES) }
        interpreter.run(inputBuffer, output)

        // Convert logits to normalized probabilities via softmax if not already normalized
        val rawScores = output[0]
        val sumScores = rawScores.sum()
        val probabilities = if (sumScores in 0.98f..1.02f) {
            rawScores
        } else {
            softmax(rawScores)
        }

        // Find top-1 and top-2 class indices
        var bestIndex = 0
        var maxConfidence = probabilities[0]
        var secondConfidence = 0.0f

        for (i in 1 until NUM_CLASSES) {
            val prob = probabilities[i]
            if (prob > maxConfidence) {
                secondConfidence = maxConfidence
                maxConfidence = prob
                bestIndex = i
            } else if (prob > secondConfidence) {
                secondConfidence = prob
            }
        }

        val classMargin = maxConfidence - secondConfidence
        val rawLabel = labels.getOrElse(bestIndex) { "Unknown" }
        val parsed = parseLabel(rawLabel)

        // Step 2: Crop-level probability aggregation
        val cropProbabilities = mutableMapOf<String, Float>()
        for (i in 0 until NUM_CLASSES) {
            val label = labels.getOrElse(i) { "" }
            val crop = parseLabel(label).crop
            cropProbabilities[crop] = (cropProbabilities[crop] ?: 0f) + probabilities[i]
        }

        val sortedCrops = cropProbabilities.entries
            .map { Pair(it.key, it.value) }
            .sortedByDescending { it.second }
        val topCropEntry = sortedCrops.firstOrNull() ?: Pair(parsed.crop, maxConfidence)
        val secondCropEntry = sortedCrops.getOrNull(1)
        val topCropName: String = topCropEntry.first
        val topCropProb: Float = topCropEntry.second
        val secondCropProb: Float = secondCropEntry?.second ?: 0.0f
        val cropMargin: Float = topCropProb - secondCropProb

        Log.d(TAG, "Inference Analysis: TopClass='$rawLabel' (${"%.3f".format(maxConfidence)}), " +
                "TopCrop='$topCropName' (${"%.3f".format(topCropProb)}), " +
                "RunnerUpCrop='${secondCropEntry?.first}' (${"%.3f".format(secondCropProb)}), " +
                "CropMargin=${"%.3f".format(cropMargin)}, ClassMargin=${"%.3f".format(classMargin)}")

        // Step 3: Out-of-Distribution & Unsupported Crop Rejection
        // A closed-set classifier always predicts a top class even for unsupported crops (e.g. Potato, Mango).
        // If crop probability is below 50% or crop margin is under 15%, reject as unsupported crop.
        val isUnsupportedCrop = (topCropProb < MIN_CROP_PROBABILITY) || ((cropMargin < MIN_CROP_MARGIN) && (topCropProb < 0.70f))
        if (isUnsupportedCrop) {
            Log.w(TAG, "Rejected: Crop not supported by trained models (TopCrop='$topCropName', Prob=${"%.2f".format(topCropProb)}, Margin=${"%.2f".format(cropMargin)})")
            return VisionPrediction(
                classIndex = -1,
                rawLabel = "REJECTED_UNSUPPORTED_CROP",
                crop = "Crop not supported by the trained models",
                disease = "Unable to identify crop — KrishiSaathi supports Apple, Apricot, Bean, Cherry, Corn, Fig, Grape, Lokat, Pear, Persimmon, Tomato, and Walnut.",
                confidence = topCropProb,
                isHealthy = false,
                isUncertain = true,
                modelSource = "Rejected",
                rejectionReason = "UNSUPPORTED_CROP"
            )
        }

        // Step 4: Confidence & Ambiguity Check
        // If crop is recognized but disease is ambiguous (low confidence or narrow margin)
        val isUncertain = (maxConfidence < MIN_CLASS_CONFIDENCE) || (classMargin < MIN_CLASS_MARGIN && maxConfidence < 0.80f)
        if (isUncertain) {
            Log.w(TAG, "Uncertain diagnosis: Confidence=${"%.2f".format(maxConfidence)}, Margin=${"%.2f".format(classMargin)}")
            return VisionPrediction(
                classIndex = bestIndex,
                rawLabel = rawLabel,
                crop = topCropName,
                disease = "Uncertain prediction — please capture another image",
                confidence = maxConfidence,
                isHealthy = false,
                isUncertain = true,
                plantPart = "Leaf",
                modelSource = "PlantCity",
                rejectionReason = "LOW_CONFIDENCE"
            )
        }

        // Step 5: Dual-Model Routing for Confirmed Tomato
        if (topCropName.equals("Tomato", ignoreCase = true) && tomatoClassifier != null) {
            try {
                Log.d(TAG, "Tomato crop confidently identified. Routing to Tomato Multi-Organ Classifier...")
                val tomatoResult = tomatoClassifier!!.predict(scaledBitmap)

                // If PlantCity confidently indicates healthy leaf and tomato multi-organ is ambiguous, respect healthy
                val isHealthy = parsed.isHealthy && tomatoResult.diseaseConfidence < 0.60f
                val finalDisease = if (isHealthy) "Healthy / Normal" else tomatoResult.disease

                return VisionPrediction(
                    classIndex = bestIndex,
                    rawLabel = "Tomato_${tomatoResult.organ}_${tomatoResult.disease}",
                    crop = "Tomato",
                    disease = finalDisease,
                    confidence = if (isHealthy) maxConfidence else tomatoResult.diseaseConfidence,
                    isHealthy = isHealthy,
                    isUncertain = tomatoResult.isUncertain && !isHealthy,
                    plantPart = tomatoResult.organ,
                    modelSource = "Tomato_MultiOrgan",
                    organConfidence = tomatoResult.organConfidence,
                    diseaseConfidence = tomatoResult.diseaseConfidence
                )
            } catch (e: Exception) {
                Log.e(TAG, "Tomato multi-organ inference failed, falling back to PlantCity: ${e.message}", e)
            }
        }

        // Step 6: Confirmed prediction from PlantCity for other supported crops (or tomato fallback)
        return VisionPrediction(
            classIndex = bestIndex,
            rawLabel = rawLabel,
            crop = topCropName,
            disease = parsed.disease,
            confidence = maxConfidence,
            isHealthy = parsed.isHealthy,
            isUncertain = false,
            plantPart = "Leaf",
            modelSource = "PlantCity"
        )
    }

    /**
     * Helper for Gallery / URI input:
     * Decodes the image, applies EXIF rotation, checks plant-likelihood validation,
     * center-crops to square, scales to 224x224 RGB, and runs dual-model inference.
     */
    fun predictFromUri(context: Context, imageUri: Uri): VisionPrediction {
        val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
            ?: throw IllegalArgumentException("Could not decode image from URI: $imageUri")
        inputStream?.close()

        val rotatedBitmap = fixExifOrientation(context, imageUri, bitmap)

        // Validate plant-likelihood on the full gallery image first
        val validator = PlantPartValidatorImpl()
        val fullValidation = validator.validate(rotatedBitmap)
        if (!fullValidation.isValid) {
            return VisionPrediction(
                classIndex = -1,
                rawLabel = "REJECTED_NON_PLANT",
                crop = "No valid plant detected",
                disease = "Please capture a clear image of a plant leaf.",
                confidence = fullValidation.confidence,
                isHealthy = false,
                isUncertain = true,
                modelSource = "Rejected",
                rejectionReason = fullValidation.status.name
            )
        }

        val squareBitmap = centerCropToSquare(rotatedBitmap)
        val scaled224 = Bitmap.createScaledBitmap(squareBitmap, INPUT_WIDTH, INPUT_HEIGHT, true)

        return predict(scaled224)
    }

    private fun fixExifOrientation(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        var orientation = ExifInterface.ORIENTATION_NORMAL
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exifInterface = ExifInterface(stream)
                orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not read EXIF orientation: ${e.message}")
        }

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            else -> return bitmap
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun centerCropToSquare(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val minDim = minOf(width, height)
        val startX = (width - minDim) / 2
        val startY = (height - minDim) / 2
        return Bitmap.createBitmap(bitmap, startX, startY, minDim, minDim)
    }

    private fun softmax(logits: FloatArray): FloatArray {
        var max = Float.NEGATIVE_INFINITY
        for (v in logits) {
            if (v > max) max = v
        }
        var sum = 0f
        val expArray = FloatArray(logits.size)
        for (i in logits.indices) {
            expArray[i] = exp((logits[i] - max).toDouble()).toFloat()
            sum += expArray[i]
        }
        if (sum > 0f) {
            for (i in expArray.indices) {
                expArray[i] /= sum
            }
        }
        return expArray
    }

    private data class ParsedLabel(
        val crop: String,
        val disease: String,
        val isHealthy: Boolean
    )

    private fun parseLabel(rawLabel: String): ParsedLabel {
        val normalized = rawLabel.replace("_", " ").trim()
        val lower = normalized.lowercase()

        val matchedCrop = when {
            lower.startsWith("apple") -> "Apple"
            lower.startsWith("apricot") -> "Apricot"
            lower.startsWith("bean") -> "Bean"
            lower.startsWith("cherry") -> "Cherry"
            lower.startsWith("corn") -> "Corn"
            lower.startsWith("fig") -> "Fig"
            lower.startsWith("grape") -> "Grape"
            lower.startsWith("lokat") -> "Lokat"
            lower.startsWith("pear") -> "Pear"
            lower.startsWith("walnut") -> "Walnut"
            lower.startsWith("persimmon") -> "Persimmon"
            lower.startsWith("tomato") -> "Tomato"
            else -> normalized.split(" ").firstOrNull()?.replaceFirstChar { it.uppercase() } ?: "Unknown"
        }

        val isHealthy = lower.contains("normal") || lower.contains("healthy")

        var conditionPart = if (normalized.startsWith(matchedCrop, ignoreCase = true)) {
            normalized.substring(matchedCrop.length).trim()
        } else {
            normalized
        }

        conditionPart = conditionPart
            .replace(Regex("^[-_\\s]+"), "")
            .replace(Regex("(?i)leaf disease"), "")
            .replace(Regex("(?i)disease"), "")
            .replace(Regex("(?i)image"), "")
            .replace(Regex("(?i)leaf"), "")
            .trim()

        val displayDisease = when {
            isHealthy -> "Healthy / Normal"
            conditionPart.isBlank() -> "General Infection"
            else -> conditionPart.split(" ")
                .filter { it.isNotBlank() }
                .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }
        }

        return ParsedLabel(
            crop = matchedCrop,
            disease = displayDisease,
            isHealthy = isHealthy
        )
    }

    fun close() {
        try {
            interpreter.close()
            tomatoClassifier?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing classifier: ${e.message}")
        }
    }
}
