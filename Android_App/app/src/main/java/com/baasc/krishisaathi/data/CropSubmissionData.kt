package com.baasc.krishisaathi.data

data class CropSubmissionData(
    val mainCropImageUri: String? = null,
    val additionalImageUris: List<String> = emptyList(),
    val selectedCropKey: String = "",
    val customCropName: String = "",
    val selectedSymptomKeys: List<String> = emptyList(),
    val problemDescription: String = "",
    val growthDuration: String = "",
    val growthDurationUnit: String = "Days",
    val selectedRegion: String = "",
    val selectedLanguage: String = "",
    val visionPrediction: VisionPrediction? = null
) {
    val displayCropName: String
        get() = if (selectedCropKey.equals("other", ignoreCase = true) && customCropName.isNotBlank()) {
            customCropName.trim()
        } else if (selectedCropKey.isNotBlank()) {
            selectedCropKey.replaceFirstChar { it.uppercase() }
        } else {
            visionPrediction?.crop.orEmpty()
        }
}
