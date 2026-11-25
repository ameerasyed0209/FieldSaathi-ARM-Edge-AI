package com.baasc.krishisaathi.data

/**
 * Clean interface for agricultural advisory generation.
 *
 * Designed to cleanly accept real Vision AI predictions and user crop context.
 * Future Llama models will plug directly into this interface once model binaries are available.
 */
interface AdvisoryGenerator {
    fun generateAdvisory(
        prediction: VisionPrediction?,
        submissionData: CropSubmissionData?
    ): AdvisoryResult
}

data class AdvisoryResult(
    val title: String,
    val summary: String,
    val recommendations: List<String>,
    val regionalNotes: String?,
    val isLlamaGenerated: Boolean = false,
    val sourceLabel: String = "Agronomic Knowledge Base (Llama model integration pending)"
)

/**
 * Standard agronomic rule-based advisory implementation.
 * Provides practical guidance for the 52 crop/disease conditions detected by PlantCity
 * as well as Tomato Multi-Organ conditions (Leaf, Stem, Fruit; Viral, Wilt, Gray Mold).
 */
class AgronomicAdvisoryGenerator : AdvisoryGenerator {

    override fun generateAdvisory(
        prediction: VisionPrediction?,
        submissionData: CropSubmissionData?
    ): AdvisoryResult {
        if (prediction == null) {
            return AdvisoryResult(
                title = "Crop Assessment Incomplete",
                summary = "Please provide or capture a clearer image of the affected plant leaf to receive specific advice.",
                recommendations = listOf(
                    "Capture a sharp, well-lit photo of the upper leaf surface.",
                    "Ensure the diseased area fills the camera guide box.",
                    "Avoid strong backlight or blur."
                ),
                regionalNotes = null,
                isLlamaGenerated = false
            )
        }

        // Case 1: Non-plant image rejected
        if (prediction.crop.contains("No valid plant", ignoreCase = true)) {
            return AdvisoryResult(
                title = "No Valid Plant Detected",
                summary = "The captured image did not pass plant-likelihood validation. Please capture a clear, focused photograph of a crop leaf or organ.",
                recommendations = listOf(
                    "Hold the phone camera 15–25 cm away from the leaf.",
                    "Ensure natural daytime lighting; avoid extreme glare, shadows, or pitch-dark environments.",
                    "Keep the plant leaf steady inside the central guide box."
                ),
                regionalNotes = null,
                isLlamaGenerated = false
            )
        }

        // Case 2: Unsupported crop rejected (out-of-distribution)
        if (prediction.crop.contains("Crop not supported", ignoreCase = true)) {
            return AdvisoryResult(
                title = "Crop Not Supported by Trained Models",
                summary = "The AI model evaluated the image and determined that this plant is outside the 12 trained crop varieties (Apple, Apricot, Bean, Cherry, Corn, Fig, Grape, Lokat, Pear, Persimmon, Tomato, and Walnut).",
                recommendations = listOf(
                    "Verify if the photographed crop is among the 12 supported species.",
                    "If it is a supported crop, capture a clearer close-up of a representative leaf with distinct symptoms.",
                    "For crops outside this list (e.g. Potato, Rice, Wheat, Mango), consult your nearest Krishi Vigyan Kendra (KVK) or agricultural extension officer."
                ),
                regionalNotes = submissionData?.selectedRegion?.let { "Region: $it. Consider local university agricultural advisories for unlisted crops." },
                isLlamaGenerated = false
            )
        }

        // Case 3: Uncertain prediction (low confidence / ambiguous symptoms)
        if (prediction.isUncertain) {
            return AdvisoryResult(
                title = "Uncertain Crop Diagnosis",
                summary = "The AI model confidence (${prediction.formattedConfidence}) is below the required diagnostic threshold. We recommend capturing a clearer photo before applying treatments.",
                recommendations = listOf(
                    "Retake a close-up photo showing visible leaf or plant symptoms in good lighting.",
                    "Check both upper and lower leaf surfaces for signs of spores, discoloration, or pests.",
                    "Consult your local Krishi Vigyan Kendra (KVK) or extension officer if symptoms spread rapidly."
                ),
                regionalNotes = submissionData?.selectedRegion?.let { "Region: $it - Observe local weather patterns for disease escalation." },
                isLlamaGenerated = false
            )
        }

        // Case 4: Healthy foliage
        if (prediction.isHealthy) {
            val organLabel = prediction.plantPart?.let { " ($it)" } ?: ""
            return AdvisoryResult(
                title = "Healthy ${prediction.crop}$organLabel Foliage",
                summary = "The analyzed ${prediction.plantPart?.lowercase() ?: "leaf"} appears normal and healthy with ${prediction.formattedConfidence} confidence.",
                recommendations = listOf(
                    "Continue balanced irrigation without waterlogging the root zone.",
                    "Maintain scheduled organic or recommended fertilizer applications.",
                    "Monitor regularly for early signs of seasonal pests or foliar spots."
                ),
                regionalNotes = submissionData?.selectedRegion?.let { "Favorable growing conditions in $it." },
                isLlamaGenerated = false
            )
        }

        // Case 5: Diagnosed Disease
        val diseaseLower = prediction.disease.lowercase()
        val cropName = prediction.crop
        val organPart = prediction.plantPart
        val organLabel = organPart?.let { " ($it)" } ?: ""

        val recs = mutableListOf<String>()
        val summaryText: String

        when {
            // Tomato Multi-Organ Specific Advice
            cropName.equals("Tomato", ignoreCase = true) && organPart != null -> {
                when {
                    diseaseLower.contains("gray") || diseaseLower.contains("mold") -> {
                        summaryText = "Detected Gray Mold (Botrytis cinerea) on Tomato $organPart with ${prediction.formattedConfidence} confidence. Thrives in cool, damp, humid environments."
                        if (organPart.equals("Fruit", ignoreCase = true)) {
                            recs.add("Harvest and destroy infected fruit promptly to prevent spore spread to healthy fruit clusters.")
                            recs.add("Ensure tomato vines are staked and trellised so fruit does not touch moist soil.")
                        } else {
                            recs.add("Prune affected leaves/branches using clean, sanitized shears.")
                            recs.add("Improve air circulation by pruning dense interior foliage.")
                        }
                        recs.add("Apply bio-fungicides like Trichoderma or registered Botrytis fungicides.")
                        recs.add("Avoid overhead sprinkler irrigation; irrigate directly at the base of the plant.")
                    }
                    diseaseLower.contains("viral") -> {
                        summaryText = "Detected Viral Disease (Tomato Leaf Curl / Mosaic) on Tomato $organPart with ${prediction.formattedConfidence} confidence. Transmitted by insect vectors like whiteflies."
                        recs.add("Control whitefly and aphid populations using yellow sticky cards and neem oil sprays.")
                        recs.add("Rogue out and burn or bury severely stunted, curling plants to protect nearby tomatoes.")
                        recs.add("Avoid handling healthy plants after touching viral-infected plants.")
                        recs.add("Apply balanced micronutrients (zinc, boron) to build plant vigor.")
                    }
                    diseaseLower.contains("wilt") -> {
                        summaryText = "Detected Wilt Disease (Fusarium/Verticillium/Bacterial) on Tomato $organPart with ${prediction.formattedConfidence} confidence. Vascular blockage restricts water flow."
                        if (organPart.equals("Stem", ignoreCase = true)) {
                            recs.add("Check stem collar at ground level for brown vascular discoloration or soft lesions.")
                            recs.add("Avoid mulching directly against the stem collar to prevent stem rot.")
                        }
                        recs.add("Drench root zone and surrounding soil with Trichoderma viride or Pseudomonas fluorescens.")
                        recs.add("Avoid overwatering and ensure furrow or drip irrigation drainage.")
                        recs.add("Rotate with non-solanaceous crops (e.g. corn, pulses) for at least 2 seasons.")
                    }
                    else -> {
                        summaryText = "Detected ${prediction.disease} on Tomato $organPart (${prediction.formattedConfidence} confidence)."
                        recs.add("Inspect affected plant parts and remove heavily symptomatic tissue.")
                        recs.add("Maintain proper canopy ventilation and soil moisture balance.")
                    }
                }
            }

            // General Crop Diseases (PlantCity)
            diseaseLower.contains("blight") -> {
                summaryText = "Detected ${prediction.disease} on $cropName$organLabel (${prediction.formattedConfidence} confidence). Blight spreads rapidly in warm, humid conditions."
                recs.add("Prune and remove infected lower leaves to restrict fungal spore spread.")
                recs.add("Apply appropriate copper-based or Mancozeb fungicide as per local agricultural recommendations.")
                recs.add("Avoid overhead sprinkler irrigation; irrigate at the soil base to keep foliage dry.")
                recs.add("Ensure adequate plant spacing for proper airflow.")
            }
            diseaseLower.contains("spot") || diseaseLower.contains("shot_hole") -> {
                summaryText = "Detected ${prediction.disease} on $cropName$organLabel (${prediction.formattedConfidence} confidence). Foliar spots indicate fungal or bacterial infection."
                recs.add("Remove heavily spotted leaves and dispose of them away from the cultivation area.")
                recs.add("Avoid working in the field when foliage is wet to avoid transmitting pathogens.")
                recs.add("Apply protective bio-fungicide (Trichoderma viride or neem oil spray) or recommended chemical spray.")
                recs.add("Maintain field sanitation and clear fallen leaf debris.")
            }
            diseaseLower.contains("wilt") -> {
                summaryText = "Detected ${prediction.disease} on $cropName$organLabel (${prediction.formattedConfidence} confidence). Vascular wilting affects nutrient and water uptake."
                recs.add("Check soil drainage; avoid over-watering and poorly drained waterlogged soil.")
                recs.add("Drench root zone with Trichoderma or recommended bio-control agent.")
                recs.add("Practice crop rotation with non-host crops in subsequent seasons.")
            }
            diseaseLower.contains("curl") -> {
                summaryText = "Detected ${prediction.disease} on $cropName$organLabel (${prediction.formattedConfidence} confidence). Leaf curl is typically viral and transmitted by whiteflies or thrips."
                recs.add("Control vector populations (whiteflies, aphids) using yellow sticky traps and neem-based insecticides.")
                recs.add("Uproot and safely destroy severely infected plants to prevent transmission to healthy crops.")
                recs.add("Maintain balanced micronutrient support to strengthen plant immunity.")
            }
            diseaseLower.contains("mildew") || diseaseLower.contains("mold") -> {
                summaryText = "Detected ${prediction.disease} on $cropName$organLabel (${prediction.formattedConfidence} confidence). Mildew and molds thrive in humid microclimates."
                recs.add("Spray wettable sulfur or systemic fungicide recommended for mildew control.")
                recs.add("Thin canopy foliage to enhance sunlight penetration and aeration.")
                recs.add("Avoid excess nitrogen fertilization, which produces overly lush, susceptible leaves.")
            }
            diseaseLower.contains("rust") -> {
                summaryText = "Detected ${prediction.disease} on $cropName$organLabel (${prediction.formattedConfidence} confidence). Rust pustules consume plant energy and inhibit photosynthesis."
                recs.add("Apply sulfur-based or triazole fungicide at first appearance of rust pustules.")
                recs.add("Clean all farming equipment after handling infected plants.")
                recs.add("Plant certified disease-resistant varieties in future planting cycles.")
            }
            diseaseLower.contains("mite") || diseaseLower.contains("miner") -> {
                summaryText = "Detected ${prediction.disease} on $cropName$organLabel (${prediction.formattedConfidence} confidence). Pest infestation causing foliar damage."
                recs.add("Apply acaricide/miticide or botanical sprays such as 5% neem seed kernel extract (NSKE).")
                recs.add("Ensure sufficient moisture; spider mites thrive in dry, dusty conditions.")
                recs.add("Encourage beneficial predatory insects like ladybird beetles and lacewings.")
            }
            else -> {
                summaryText = "Detected ${prediction.disease} on $cropName$organLabel (${prediction.formattedConfidence} confidence)."
                recs.add("Isolate symptomatic plants or prune damaged foliage.")
                recs.add("Apply preventive broad-spectrum organic or recommended agronomic foliar spray.")
                recs.add("Monitor crops daily for symptom progression or recovery.")
            }
        }

        val regionText = submissionData?.selectedRegion?.takeIf { it.isNotBlank() }?.let {
            "Regional Advisory for $it: Monitor temperature and moisture fluctuations typical for this region during crop growth ($cropName, ${submissionData.growthDuration.ifEmpty { "unspecified duration" }})."
        }

        return AdvisoryResult(
            title = "${prediction.crop}$organLabel: ${prediction.disease}",
            summary = summaryText,
            recommendations = recs,
            regionalNotes = regionText,
            isLlamaGenerated = false,
            sourceLabel = "Agronomic Advisory Engine (Llama model integration pending)"
        )
    }
}
