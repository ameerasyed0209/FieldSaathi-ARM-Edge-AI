package com.baasc.krishisaathi.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baasc.krishisaathi.PlantCityClassifier
import com.baasc.krishisaathi.R
import com.baasc.krishisaathi.components.CropImagePreview
import com.baasc.krishisaathi.components.StepProgressIndicator
import com.baasc.krishisaathi.data.AgronomicAdvisoryGenerator
import com.baasc.krishisaathi.data.CropSubmissionData
import com.baasc.krishisaathi.data.VisionPrediction
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.baasc.krishisaathi.ui.theme.ButtonGreen
import com.baasc.krishisaathi.ui.theme.CardBackground
import com.baasc.krishisaathi.ui.theme.ForestGreen
import com.baasc.krishisaathi.ui.theme.InputBackground
import com.baasc.krishisaathi.ui.theme.InputBorder
import com.baasc.krishisaathi.ui.theme.KrishiSaathiTheme
import com.baasc.krishisaathi.ui.theme.TextPrimary
import com.baasc.krishisaathi.ui.theme.TextSecondary

@Composable
fun GetAdviceScreen(
    submissionData: CropSubmissionData?,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    BackHandler {
        onNavigateBack()
    }

    val context = LocalContext.current

    // State for Vision AI prediction
    var prediction by remember { mutableStateOf<VisionPrediction?>(submissionData?.visionPrediction) }

    // Fallback on-device inference if prediction wasn't pre-computed
    val mainUriStr = submissionData?.mainCropImageUri
    LaunchedEffect(mainUriStr) {
        if (prediction == null && !mainUriStr.isNullOrEmpty()) {
            withContext(Dispatchers.IO) {
                try {
                    val classifier = PlantCityClassifier(context)
                    val result = classifier.predictFromUri(context, Uri.parse(mainUriStr))
                    classifier.close()
                    withContext(Dispatchers.Main) {
                        prediction = result
                    }
                } catch (e: Exception) {
                    android.util.Log.e("GetAdviceScreen", "Fallback Vision AI inference failed: ${e.message}", e)
                }
            }
        }
    }

    val advisoryGenerator = remember { AgronomicAdvisoryGenerator() }
    val advisory = remember(prediction, submissionData) {
        advisoryGenerator.generateAdvisory(prediction, submissionData)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CardBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // TOP BAR
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onNavigateBack,
                    shape = CircleShape,
                    color = InputBackground,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back),
                            tint = ForestGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = stringResource(id = R.string.get_advice_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )
                    Text(
                        text = stringResource(id = R.string.get_advice_subtitle),
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // STEP 3 PROGRESS INDICATOR
            StepProgressIndicator(currentStep = 3)

            Spacer(modifier = Modifier.height(18.dp))

            // 1. AI CROP ANALYSIS CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (prediction?.isUncertain == true) Color(0xFFFFF8E1) else Color(0xFFF1F8F2)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (prediction?.isUncertain == true) Color(0xFFFFB74D) else Color(0xFF81C784)
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (prediction?.isUncertain == true) Color(0xFFE65100) else ForestGreen,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI CROP ANALYSIS",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (prediction?.isUncertain == true) Color(0xFFBF360C) else ForestGreen
                            )
                        }

                        if (prediction != null) {
                            Surface(
                                color = if (prediction!!.isUncertain) Color(0xFFE65100) else ButtonGreen,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = prediction!!.formattedConfidence,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (prediction != null) {
                        val pred = prediction!!

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Crop: ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = pred.crop,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = ForestGreen
                            )
                        }

                        if (pred.plantPart != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Plant Part: ",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = pred.plantPart,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = ForestGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Detected: ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = if (pred.isHealthy) "Healthy / Normal" else pred.disease,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = if (pred.isHealthy) ButtonGreen else Color(0xFFC62828)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Confidence: ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = pred.formattedConfidence,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Model Source: ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = if (pred.modelSource == "Tomato_MultiOrgan") "Tomato Multi-Organ AI" else if (pred.modelSource == "PlantCity") "PlantCity Vision AI" else "Validation Guard",
                                fontWeight = FontWeight.Normal,
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }

                        if (pred.isUncertain) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFFE0B2),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color(0xFFE65100),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    val warningMsg = when {
                                        pred.crop.contains("No valid plant") ->
                                            "No valid plant detected. Please capture a clear image of a plant leaf."
                                        pred.crop.contains("Crop not supported") ->
                                            "Crop not supported by the trained models. KrishiSaathi supports 12 crops."
                                        else ->
                                            "Uncertain result. Please capture another image with better lighting and focus."
                                    }
                                    Text(
                                        text = warningMsg,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFFBF360C)
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "Analyzing crop image...",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. ADVISORY CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "ADVISORY",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = advisory.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = advisory.summary,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        lineHeight = 18.sp
                    )

                    if (advisory.recommendations.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Recommended Actions:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        advisory.recommendations.forEach { rec ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "• ",
                                    color = ButtonGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = rec,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }

                    if (!advisory.regionalNotes.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = advisory.regionalNotes,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Source: ${advisory.sourceLabel}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // SUBMISSION SUMMARY CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = stringResource(id = R.string.summary_heading),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Main Photo Preview
                    val mainUri = submissionData?.mainCropImageUri?.let { Uri.parse(it) }
                    if (mainUri != null) {
                        CropImagePreview(
                            imageUri = mainUri,
                            onRemoveImage = null
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Regional Context Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ForestGreen.copy(alpha = 0.08f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = ForestGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${stringResource(id = R.string.farm_location_label)}: ${submissionData?.selectedRegion.orEmpty()}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ForestGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Selected Crop Info
                    SummaryRow(
                        icon = Icons.Default.Eco,
                        label = stringResource(id = R.string.summary_crop),
                        value = submissionData?.displayCropName?.ifEmpty {
                            stringResource(id = R.string.none_selected)
                        } ?: stringResource(id = R.string.none_selected)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Growth Duration Info
                    SummaryRow(
                        icon = Icons.Default.CalendarToday,
                        label = stringResource(id = R.string.summary_duration),
                        value = submissionData?.growthDuration?.ifEmpty {
                            stringResource(id = R.string.none_selected)
                        } ?: stringResource(id = R.string.none_selected)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Symptoms List
                    val symptoms = submissionData?.selectedSymptomKeys.orEmpty()
                    SummaryRow(
                        icon = Icons.Default.Warning,
                        label = stringResource(id = R.string.summary_symptoms),
                        value = if (symptoms.isNotEmpty()) {
                            symptoms.joinToString(", ") { key ->
                                key.replace("_", " ").replaceFirstChar { it.uppercase() }
                            }
                        } else {
                            stringResource(id = R.string.none_selected)
                        }
                    )

                    // Additional Photos Summary (if any)
                    val additionalPhotos = submissionData?.additionalImageUris.orEmpty()
                    if (additionalPhotos.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "${stringResource(id = R.string.summary_additional_photos)} (${additionalPhotos.size})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(additionalPhotos) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(1.dp, InputBorder, RoundedCornerShape(10.dp))
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.bg_tropical_leaves),
                                        contentDescription = "Photo thumbnail",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }

                    // Problem Description Notes (if provided)
                    val desc = submissionData?.problemDescription.orEmpty()
                    if (desc.isNotBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = stringResource(id = R.string.summary_notes),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = desc,
                            fontSize = 13.sp,
                            color = TextPrimary,
                            lineHeight = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Edit Details Button
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = ForestGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = R.string.edit_details_btn),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ForestGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SummaryRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ForestGreen,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = TextSecondary
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GetAdviceScreenPreview() {
    KrishiSaathiTheme {
        GetAdviceScreen(
            submissionData = CropSubmissionData(
                mainCropImageUri = null,
                selectedCropKey = "tomato",
                selectedSymptomKeys = listOf("spots", "yellow_leaves"),
                growthDuration = "1–2 months",
                selectedRegion = "Karnataka",
                selectedLanguage = "kn"
            ),
            onNavigateBack = {},
            onNavigateToHome = {}
        )
    }
}
