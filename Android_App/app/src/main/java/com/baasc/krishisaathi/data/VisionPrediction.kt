package com.baasc.krishisaathi.data

/**
 * Structured result model for KrishiSaathi Vision AI inference.
 * Supports both PlantCity (52 crop/disease classes) and Tomato Multi-Organ models.
 *
 * @param classIndex The index (0..51) matching class_names.json, or -1 if non-PlantCity.
 * @param rawLabel The exact class label string as loaded from class_names.json or metadata.
 * @param crop Cleanly formatted crop name (e.g., "Tomato", "Apple", or "Crop not supported by the trained models").
 * @param disease Cleanly formatted disease or condition (e.g., "Early Blight", "Healthy / Normal", "Gray Mold (Botrytis)").
 * @param confidence Model prediction confidence between 0.0f and 1.0f.
 * @param isHealthy True if the prediction corresponds to a healthy/normal crop class.
 * @param isUncertain True if confidence is below minimum threshold or image was rejected.
 * @param plantPart Optional organ/plant part identified by the multi-organ model ("Leaf", "Stem", "Fruit").
 * @param modelSource The model or pipeline that produced this prediction ("PlantCity", "Tomato_MultiOrgan", "Rejected").
 * @param organConfidence Optional confidence score specifically for the plant organ (0.0f..1.0f).
 * @param diseaseConfidence Optional confidence score specifically for the disease (0.0f..1.0f).
 * @param rejectionReason Optional rejection code if rejected ("NON_PLANT", "LOW_QUALITY", "UNSUPPORTED_CROP", "LOW_CONFIDENCE").
 */
data class VisionPrediction(
    val classIndex: Int,
    val rawLabel: String,
    val crop: String,
    val disease: String,
    val confidence: Float,
    val isHealthy: Boolean = false,
    val isUncertain: Boolean = false,
    val plantPart: String? = null,
    val modelSource: String = "PlantCity",
    val organConfidence: Float? = null,
    val diseaseConfidence: Float? = null,
    val rejectionReason: String? = null
) {
    val confidencePercentage: Float
        get() = confidence * 100f

    val formattedConfidence: String
        get() = "%.1f%%".format(confidencePercentage)

    val displayTitle: String
        get() = when {
            isUncertain -> when {
                crop.startsWith("No valid plant") -> "No valid plant detected"
                crop.startsWith("Crop not supported") -> "Unsupported Crop"
                else -> "Uncertain result: $crop"
            }
            plantPart != null && isHealthy -> "$crop ($plantPart) - Healthy / Normal"
            plantPart != null -> "$crop ($plantPart) - $disease"
            isHealthy -> "$crop (Healthy / Normal)"
            else -> "$crop - $disease"
        }
}
