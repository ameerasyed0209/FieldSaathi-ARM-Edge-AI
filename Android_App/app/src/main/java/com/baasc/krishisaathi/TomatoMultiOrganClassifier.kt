package com.baasc.krishisaathi

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.exp

/**
 * TomatoMultiOrganClassifier
 *
 * Dedicated on-device TensorFlow Lite classifier for Tomato Multi-Organ analysis.
 * Analyzes plant part (Leaf, Stem, Fruit) and specific tomato disease (Viral, Wilt, GrayMold).
 *
 * Valid combinations according to Tomato_MultiOrgan_Metadata.json:
 * - Leaf + Viral
 * - Leaf + Wilt
 * - Leaf + GrayMold
 * - Stem + Wilt
 * - Fruit + GrayMold
 *
 * Model Specs:
 * - Input: [1, 224, 224, 3] Float32 RGB (0.0..255.0 range, MobileNetV2 preprocessing included in graph)
 * - Output 0: [1, 3] Disease logits (Viral, Wilt, GrayMold)
 * - Output 1: [1, 3] Organ logits (Leaf, Stem, Fruit)
 */
class TomatoMultiOrganClassifier(private val context: Context) {

    companion object {
        private const val TAG = "TomatoMultiOrganClassifier"
        const val MODEL_FILE = "Tomato_MultiOrgan_MobileNetV2_FP32.tflite"
        const val METADATA_FILE = "Tomato_MultiOrgan_Metadata.json"
        const val INPUT_WIDTH = 224
        const val INPUT_HEIGHT = 224
        const val INPUT_CHANNELS = 3
        const val CONFIDENCE_THRESHOLD = 0.65f
    }

    data class TomatoResult(
        val organ: String,
        val disease: String,
        val organConfidence: Float,
        val diseaseConfidence: Float,
        val combinedConfidence: Float,
        val isValidCombination: Boolean,
        val isUncertain: Boolean,
        val organProbabilities: Map<String, Float>,
        val diseaseProbabilities: Map<String, Float>
    )

    private val interpreter: Interpreter
    private val organLabels = mutableMapOf<Int, String>()
    private val diseaseLabels = mutableMapOf<Int, String>()
    private val validCombinations = mutableSetOf<Pair<String, String>>()

    private val organOutputIndex: Int
    private val diseaseOutputIndex: Int
    val isInitialized: Boolean

    init {
        var initialized = false
        try {
            // 1. Load metadata
            val metaJsonStr = context.assets.open(METADATA_FILE).bufferedReader().use { it.readText() }
            val metaJson = JSONObject(metaJsonStr)

            val organObj = metaJson.getJSONObject("organ_output")
            val organKeys = organObj.keys()
            while (organKeys.hasNext()) {
                val key = organKeys.next()
                organLabels[key.toInt()] = organObj.getString(key)
            }

            val diseaseObj = metaJson.getJSONObject("disease_output")
            val diseaseKeys = diseaseObj.keys()
            while (diseaseKeys.hasNext()) {
                val key = diseaseKeys.next()
                diseaseLabels[key.toInt()] = diseaseObj.getString(key)
            }

            val validArr = metaJson.getJSONArray("valid_combinations")
            for (i in 0 until validArr.length()) {
                val pair = validArr.getJSONArray(i)
                validCombinations.add(Pair(pair.getString(0), pair.getString(1)))
            }

            // 2. Load TFLite Model
            val modelBuffer = loadModelFile(context, MODEL_FILE)
            val options = Interpreter.Options().apply {
                setNumThreads(4)
            }
            interpreter = Interpreter(modelBuffer, options)
            interpreter.allocateTensors()

            // 3. Resolve output tensor indices dynamically
            val t0Name = interpreter.getOutputTensor(0).name()
            if (t0Name.contains(":0") || t0Name.lowercase().contains("organ")) {
                organOutputIndex = 0
                diseaseOutputIndex = 1
            } else {
                organOutputIndex = 1
                diseaseOutputIndex = 0
            }

            validateTensors()
            initialized = true
            Log.d(TAG, "TomatoMultiOrganClassifier successfully initialized. " +
                    "OrganIndex=$organOutputIndex, DiseaseIndex=$diseaseOutputIndex, " +
                    "ValidCombos=${validCombinations.size}")
        } catch (e: Exception) {
            Log.e(TAG, "Initialization failed: ${e.message}", e)
            throw IllegalStateException("TomatoMultiOrganClassifier init failed: ${e.message}", e)
        }
        isInitialized = initialized
    }

    private fun validateTensors() {
        val input = interpreter.getInputTensor(0)
        val shape = input.shape()
        require(shape.size == 4 && shape[1] == INPUT_WIDTH && shape[2] == INPUT_HEIGHT && shape[3] == INPUT_CHANNELS) {
            "Unexpected input shape: ${shape.contentToString()}"
        }
        require(interpreter.outputTensorCount == 2) {
            "Expected 2 output tensors, got ${interpreter.outputTensorCount}"
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
     * Run Tomato Multi-Organ inference on a 224x224 RGB image (values in 0.0..255.0).
     */
    fun predict(bitmap: Bitmap): TomatoResult {
        val scaledBitmap = if (bitmap.width != INPUT_WIDTH || bitmap.height != INPUT_HEIGHT) {
            Bitmap.createScaledBitmap(bitmap, INPUT_WIDTH, INPUT_HEIGHT, true)
        } else {
            bitmap
        }

        val inputBuffer = ByteBuffer.allocateDirect(1 * INPUT_WIDTH * INPUT_HEIGHT * INPUT_CHANNELS * 4)
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

        // Prepare outputs
        val diseaseOutput = Array(1) { FloatArray(3) }
        val organOutput = Array(1) { FloatArray(3) }

        val outputMap = HashMap<Int, Any>()
        outputMap[diseaseOutputIndex] = diseaseOutput
        outputMap[organOutputIndex] = organOutput

        val inputs = arrayOf<Any>(inputBuffer)
        interpreter.runForMultipleInputsOutputs(inputs, outputMap)

        // Convert logits to probabilities
        val organProbs = softmax(organOutput[0])
        val diseaseProbs = softmax(diseaseOutput[0])

        val organProbMap = organLabels.entries.associate { (idx, name) -> name to organProbs[idx] }
        val diseaseProbMap = diseaseLabels.entries.associate { (idx, name) -> name to diseaseProbs[idx] }

        // Find raw top organ and disease
        var topOrganIdx = 0
        var maxOrganConf = organProbs[0]
        for (i in 1 until 3) {
            if (organProbs[i] > maxOrganConf) {
                maxOrganConf = organProbs[i]
                topOrganIdx = i
            }
        }

        var topDiseaseIdx = 0
        var maxDiseaseConf = diseaseProbs[0]
        for (i in 1 until 3) {
            if (diseaseProbs[i] > maxDiseaseConf) {
                maxDiseaseConf = diseaseProbs[i]
                topDiseaseIdx = i
            }
        }

        val rawOrgan = organLabels[topOrganIdx] ?: "Leaf"
        val rawDisease = diseaseLabels[topDiseaseIdx] ?: "Wilt"
        val rawCombo = Pair(rawOrgan, rawDisease)
        val isRawValid = validCombinations.contains(rawCombo)

        // Select chosen organ and disease respecting valid combinations
        val chosenOrgan: String
        val chosenDisease: String
        val chosenOrganConf: Float
        val chosenDiseaseConf: Float
        val combinedConfidence: Float

        if (isRawValid) {
            chosenOrgan = rawOrgan
            chosenDisease = rawDisease
            chosenOrganConf = maxOrganConf
            chosenDiseaseConf = maxDiseaseConf
            combinedConfidence = maxOrganConf * maxDiseaseConf
        } else {
            // Find valid combination with highest joint probability
            var bestCombo: Pair<String, String>? = null
            var bestJointProb = -1f

            for (combo in validCombinations) {
                val oProb = organProbMap[combo.first] ?: 0f
                val dProb = diseaseProbMap[combo.second] ?: 0f
                val joint = oProb * dProb
                if (joint > bestJointProb) {
                    bestJointProb = joint
                    bestCombo = combo
                }
            }

            val fallback = bestCombo ?: Pair("Leaf", "Wilt")
            chosenOrgan = fallback.first
            chosenDisease = fallback.second
            chosenOrganConf = organProbMap[chosenOrgan] ?: 0.5f
            chosenDiseaseConf = diseaseProbMap[chosenDisease] ?: 0.5f
            combinedConfidence = chosenOrganConf * chosenDiseaseConf
        }

        val isUncertain = (chosenDiseaseConf < CONFIDENCE_THRESHOLD) || (chosenOrganConf < 0.50f)

        return TomatoResult(
            organ = chosenOrgan,
            disease = formatDiseaseName(chosenDisease),
            organConfidence = chosenOrganConf,
            diseaseConfidence = chosenDiseaseConf,
            combinedConfidence = combinedConfidence,
            isValidCombination = isRawValid || validCombinations.contains(Pair(chosenOrgan, chosenDisease)),
            isUncertain = isUncertain,
            organProbabilities = organProbMap,
            diseaseProbabilities = diseaseProbMap
        )
    }

    private fun formatDiseaseName(rawDisease: String): String {
        return when (rawDisease.lowercase()) {
            "viral" -> "Viral Disease (Tomato Curl/Mosaic)"
            "wilt" -> "Wilt Disease (Fusarium/Bacterial)"
            "graymold", "gray mold" -> "Gray Mold (Botrytis)"
            else -> rawDisease
        }
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

    fun close() {
        try {
            interpreter.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing interpreter: ${e.message}")
        }
    }
}
