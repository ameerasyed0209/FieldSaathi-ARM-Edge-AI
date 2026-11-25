package com.baasc.krishisaathi.camera

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.abs

/**
 * Explicit validation status categories.
 */
enum class ValidationStatus {
    VALID_PLANT,
    NON_PLANT,
    LOW_QUALITY,
    UNCERTAIN
}

/**
 * Classes supported by the Plant-Part Validation architecture:
 * - [LEAF]: Valid plant foliage
 * - [STEM]: Valid plant stem/branch
 * - [FRUIT]: Valid plant fruit
 * - [BACKGROUND]: Invalid non-plant (wall, hand, floor, table, sky, phone, clothing, random object, etc.)
 */
enum class PlantPartClass(val displayName: String, val isPlantPart: Boolean) {
    LEAF("Leaf", true),
    STEM("Stem", true),
    FRUIT("Fruit", true),
    BACKGROUND("Background / Non-Plant", false)
}

/**
 * Result data class for Plant-Part Validation.
 */
data class PlantPartResult(
    val status: ValidationStatus = ValidationStatus.VALID_PLANT,
    val className: PlantPartClass,
    val confidence: Float,
    val isValid: Boolean,
    val reasonMessage: String,
    val isModelLoaded: Boolean = false,
    val sharpnessScore: Float = 0f,
    val brightnessScore: Float = 0f,
    val plantLikelihoodScore: Float = 0f
)

/**
 * Modular interface for Plant-Part Validation.
 */
interface PlantPartValidator {
    val confidenceThreshold: Float
    val isModelLoaded: Boolean
    fun validate(bitmap: Bitmap): PlantPartResult
}

/**
 * Multi-criteria plant & image-quality rejection validator.
 *
 * Implements practical heuristic validation based on:
 * - Image sharpness & edge texture variance
 * - Exposure & luminance balance
 * - Saturation & organic foliage/lesion color profile distribution
 * - Unnatural non-plant color rejection (blues, cyans, magentas, monochromes)
 *
 * Prevents non-plant images (desks, keyboards, hands, walls, electronics)
 * from being fed to the closed-set 52-class PlantCity classifier.
 */
class PlantPartValidatorImpl(
    override val confidenceThreshold: Float = VALIDATION_THRESHOLD
) : PlantPartValidator {

    companion object {
        const val VALIDATION_THRESHOLD = 0.70f
        private const val SAMPLE_DIMENSION = 112
    }

    override val isModelLoaded: Boolean = false

    override fun validate(bitmap: Bitmap): PlantPartResult {
        return evaluateImageQualityAndPlantLikelihood(bitmap)
    }

    private fun evaluateImageQualityAndPlantLikelihood(bitmap: Bitmap): PlantPartResult {
        // Downsample for consistent, fast evaluation
        val scaled = Bitmap.createScaledBitmap(bitmap, SAMPLE_DIMENSION, SAMPLE_DIMENSION, true)
        val numPixels = SAMPLE_DIMENSION * SAMPLE_DIMENSION
        val pixels = IntArray(numPixels)
        scaled.getPixels(pixels, 0, SAMPLE_DIMENSION, 0, 0, SAMPLE_DIMENSION, SAMPLE_DIMENSION)

        var totalLuminance = 0.0
        var totalSaturation = 0.0
        var plantColorPixels = 0
        var unnaturalColorPixels = 0
        var lowSaturationPixels = 0

        val hsv = FloatArray(3)

        // Pass 1: Pixel color & luminance distribution
        for (pixel in pixels) {
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF

            val luminance = 0.299 * r + 0.587 * g + 0.114 * b
            totalLuminance += luminance

            Color.colorToHSV(pixel, hsv)
            val hue = hsv[0]
            val sat = hsv[1]
            val value = hsv[2]

            totalSaturation += sat

            if (sat < 0.12f) {
                lowSaturationPixels++
            }

            // Unnatural plant colors: strong blues, cyans, magentas (e.g., clothes, screens, sky)
            if ((hue in 185f..260f || hue in 280f..330f) && sat > 0.25f && value > 0.20f) {
                unnaturalColorPixels++
            }

            // Organic plant color profiles:
            // 1. Green / olive foliage: Hue 55°..175°, sat > 0.14, val > 0.12
            // 2. Yellow / chlorosis / rust: Hue 32°..55°, sat > 0.18, val > 0.15
            // 3. Necrotic brown / blight lesions: Hue 10°..32°, sat 0.15..0.75, val 0.12..0.75
            // 4. Dark shadowed leaf: sat > 0.18, val in 0.08..0.45, Hue 45°..175°
            val isGreenFoliage = (hue in 55f..175f) && sat >= 0.14f && value >= 0.12f
            val isYellowRust = (hue in 32f..55f) && sat >= 0.18f && value >= 0.15f
            val isNecroticBrown = (hue in 10f..32f) && sat in 0.15f..0.75f && value in 0.12f..0.75f
            val isDarkLeaf = (hue in 45f..175f) && sat >= 0.18f && value in 0.08f..0.50f

            if (isGreenFoliage || isYellowRust || isNecroticBrown || isDarkLeaf) {
                plantColorPixels++
            }
        }

        val avgLuminance = (totalLuminance / numPixels).toFloat()
        val avgSaturation = (totalSaturation / numPixels).toFloat()
        val plantRatio = plantColorPixels.toFloat() / numPixels
        val unnaturalRatio = unnaturalColorPixels.toFloat() / numPixels
        val lowSatRatio = lowSaturationPixels.toFloat() / numPixels

        // Pass 2: Texture / sharpness variance (gradient magnitude)
        var totalGradient = 0.0
        var edgeSamples = 0
        for (y in 1 until SAMPLE_DIMENSION - 1 step 2) {
            for (x in 1 until SAMPLE_DIMENSION - 1 step 2) {
                val p = pixels[y * SAMPLE_DIMENSION + x]
                val pRight = pixels[y * SAMPLE_DIMENSION + (x + 1)]
                val pDown = pixels[(y + 1) * SAMPLE_DIMENSION + x]

                val lum = 0.299 * ((p shr 16) and 0xFF) + 0.587 * ((p shr 8) and 0xFF) + 0.114 * (p and 0xFF)
                val lumR = 0.299 * ((pRight shr 16) and 0xFF) + 0.587 * ((pRight shr 8) and 0xFF) + 0.114 * (pRight and 0xFF)
                val lumD = 0.299 * ((pDown shr 16) and 0xFF) + 0.587 * ((pDown shr 8) and 0xFF) + 0.114 * (pDown and 0xFF)

                val grad = abs(lum - lumR) + abs(lum - lumD)
                totalGradient += grad
                edgeSamples++
            }
        }
        val sharpnessScore = (totalGradient / edgeSamples.coerceAtLeast(1)).toFloat()

        if (scaled != bitmap) {
            scaled.recycle()
        }

        // --- Quality Rejection Checks ---

        // 1. Underexposed / Pitch Dark
        if (avgLuminance < 22f) {
            return PlantPartResult(
                status = ValidationStatus.LOW_QUALITY,
                className = PlantPartClass.BACKGROUND,
                confidence = 0.10f,
                isValid = false,
                reasonMessage = "Image is too dark. Please capture the plant in natural daylight.",
                isModelLoaded = false,
                sharpnessScore = sharpnessScore,
                brightnessScore = avgLuminance,
                plantLikelihoodScore = 0f
            )
        }

        // 2. Overexposed / Blown Out White Glare
        if (avgLuminance > 240f) {
            return PlantPartResult(
                status = ValidationStatus.LOW_QUALITY,
                className = PlantPartClass.BACKGROUND,
                confidence = 0.10f,
                isValid = false,
                reasonMessage = "Image is overexposed and washed out. Avoid harsh direct flash or glare.",
                isModelLoaded = false,
                sharpnessScore = sharpnessScore,
                brightnessScore = avgLuminance,
                plantLikelihoodScore = 0f
            )
        }

        // 3. Severe Blur or Blank Uniform Surface (walls, paper, screen)
        if (sharpnessScore < 4.0f) {
            return PlantPartResult(
                status = ValidationStatus.LOW_QUALITY,
                className = PlantPartClass.BACKGROUND,
                confidence = 0.20f,
                isValid = false,
                reasonMessage = "Image is too blurry or lacks texture. Please ensure the leaf is in focus.",
                isModelLoaded = false,
                sharpnessScore = sharpnessScore,
                brightnessScore = avgLuminance,
                plantLikelihoodScore = plantRatio
            )
        }

        // 4. Monochrome / Grayscale (Paper, keyboard, floor, concrete, metal)
        if (lowSatRatio > 0.88f) {
            return PlantPartResult(
                status = ValidationStatus.NON_PLANT,
                className = PlantPartClass.BACKGROUND,
                confidence = 0.15f,
                isValid = false,
                reasonMessage = "No valid plant detected (monochrome/non-organic surface). Please capture a living plant leaf.",
                isModelLoaded = false,
                sharpnessScore = sharpnessScore,
                brightnessScore = avgLuminance,
                plantLikelihoodScore = 0.05f
            )
        }

        // 5. Dominated by unnatural non-plant colors (blues, purples, cyans)
        if (unnaturalRatio > 0.35f) {
            return PlantPartResult(
                status = ValidationStatus.NON_PLANT,
                className = PlantPartClass.BACKGROUND,
                confidence = 0.15f,
                isValid = false,
                reasonMessage = "No valid plant detected. Artificial background colors dominate the frame.",
                isModelLoaded = false,
                sharpnessScore = sharpnessScore,
                brightnessScore = avgLuminance,
                plantLikelihoodScore = plantRatio
            )
        }

        // 6. Insufficient organic foliage/lesion pixel proportion
        if (plantRatio < 0.12f) {
            return PlantPartResult(
                status = ValidationStatus.NON_PLANT,
                className = PlantPartClass.BACKGROUND,
                confidence = 0.25f,
                isValid = false,
                reasonMessage = "No valid plant detected. Please place a plant leaf, stem, or fruit inside the guide box.",
                isModelLoaded = false,
                sharpnessScore = sharpnessScore,
                brightnessScore = avgLuminance,
                plantLikelihoodScore = plantRatio
            )
        }

        // 7. Marginal quality check
        if (plantRatio < 0.20f && sharpnessScore < 6.5f) {
            return PlantPartResult(
                status = ValidationStatus.UNCERTAIN,
                className = PlantPartClass.BACKGROUND,
                confidence = 0.45f,
                isValid = false,
                reasonMessage = "Subject clarity is uncertain. Please take a closer, sharper photo of the leaf.",
                isModelLoaded = false,
                sharpnessScore = sharpnessScore,
                brightnessScore = avgLuminance,
                plantLikelihoodScore = plantRatio
            )
        }

        // Passed quality & plant-likelihood checks!
        val computedConfidence = (0.70f + (plantRatio * 0.20f) + (sharpnessScore / 100f * 0.10f)).coerceIn(0.70f, 0.98f)

        return PlantPartResult(
            status = ValidationStatus.VALID_PLANT,
            className = PlantPartClass.LEAF,
            confidence = computedConfidence,
            isValid = true,
            reasonMessage = "Plant foliage detected and verified for disease analysis.",
            isModelLoaded = false,
            sharpnessScore = sharpnessScore,
            brightnessScore = avgLuminance,
            plantLikelihoodScore = plantRatio
        )
    }
}
