package com.baasc.cameratest.validation

import android.graphics.Bitmap

/**
 * Classes supported by the Plant-Part Validation architecture:
 * - [LEAF]: Valid plant leaf / foliage
 * - [STEM]: Valid plant stem, stalk, or branch
 * - [FRUIT]: Valid plant fruit, flower, or produce
 * - [BACKGROUND]: Invalid non-plant subject (wall, hand, floor, table, sky, phone, clothing, random objects, etc.)
 */
enum class PlantPartClass(val displayName: String, val isPlantPart: Boolean) {
    LEAF("Leaf", true),
    STEM("Stem", true),
    FRUIT("Fruit", true),
    BACKGROUND("Background / Non-Plant", false)
}

/**
 * Result data class for Plant-Part Validation.
 *
 * @property className The predicted class ([PlantPartClass.LEAF], [PlantPartClass.STEM], [PlantPartClass.FRUIT], or [PlantPartClass.BACKGROUND]).
 * @property confidence Float score from 0.0 to 1.0.
 * @property isValid True only if [className] is an allowed plant part AND [confidence] >= threshold.
 * @property reasonMessage User-facing reason for decision.
 * @property isModelLoaded Indicates whether a real trained model was executed or a test-harness simulation was used.
 */
data class PlantPartResult(
    val className: PlantPartClass,
    val confidence: Float,
    val isValid: Boolean,
    val reasonMessage: String,
    val isModelLoaded: Boolean = false
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
 * Implementation of [PlantPartValidator].
 * Uses a configurable threshold (default: 0.70).
 *
 * Enforces strict validation:
 * - No hardcoded predictions
 * - No fake confidence values
 * - If no trained model is loaded, reports isModelLoaded = false, isValid = false,
 *   rejects the image, and prompts the user to retake.
 */
class PlantPartValidatorImpl(
    override val confidenceThreshold: Float = VALIDATION_THRESHOLD
) : PlantPartValidator {

    companion object {
        const val VALIDATION_THRESHOLD = 0.70f
    }

    override val isModelLoaded: Boolean = false

    override fun validate(bitmap: Bitmap): PlantPartResult {
        return if (isModelLoaded) {
            runModelInference(bitmap)
        } else {
            // No trained model file is currently loaded in the project.
            // Strict rejection without dummy inference or simulated results:
            PlantPartResult(
                className = PlantPartClass.BACKGROUND,
                confidence = 0.0f,
                isValid = false,
                reasonMessage = "Plant-part AI model is not installed yet.",
                isModelLoaded = false
            )
        }
    }

    /**
     * Dedicated model loading and execution slot.
     *
     * Once a trained plant-part validation model (plant_part_classifier.tflite) is provided:
     * 1. Load interpreter: Interpreter(loadModelFile(context, "plant_part_classifier.tflite"))
     * 2. Preprocess bitmap to 224x224 RGB normalized tensor
     * 3. Run inference to obtain probabilities for [LEAF, STEM, FRUIT, BACKGROUND]
     * 4. Accept only if predicted class in {LEAF, STEM, FRUIT} and confidence >= confidenceThreshold
     */
    private fun runModelInference(bitmap: Bitmap): PlantPartResult {
        // Reserved for future trained model execution
        return PlantPartResult(
            className = PlantPartClass.BACKGROUND,
            confidence = 0.0f,
            isValid = false,
            reasonMessage = "Plant-part AI model is not installed yet.",
            isModelLoaded = false
        )
    }
}
