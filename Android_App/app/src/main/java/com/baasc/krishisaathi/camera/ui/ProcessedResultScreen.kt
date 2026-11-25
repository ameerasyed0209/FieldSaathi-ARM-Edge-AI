package com.baasc.krishisaathi.camera.ui

import android.net.Uri
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baasc.krishisaathi.camera.ProcessedRoiResult
import com.baasc.krishisaathi.data.VisionPrediction

/**
 * ProcessedResultScreen:
 * Displays accepted valid plant images.
 *
 * VALID IMAGE FLOW:
 * Captured image -> Plant-part validation -> Leaf/Stem/Fruit -> Center ROI -> Crop -> Resize 224x224
 *
 * Displays:
 * 1. "Plant image accepted"
 * 2. Processed 224 x 224 ROI
 * 3. Original captured image separately
 * 4. Actions: "Continue with Photo" (moves forward) or "Retake Photo"
 */
@Composable
fun ProcessedResultScreen(
    result: ProcessedRoiResult,
    prediction: VisionPrediction? = null,
    onContinue: (Uri?) -> Unit,
    onTestAgain: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF141716))
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Mandatory Acceptance Title Banner: Analysis & Comparison
        Surface(
            color = Color(0xFF1B5E20),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF81C784),
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Analysis & Comparison",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (prediction != null) {
                            "Plant image accepted • ${prediction.crop} (${prediction.formattedConfidence})"
                        } else {
                            "Plant image accepted • ROI & AI verification"
                        },
                        color = Color(0xFFA5D6A7),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        if (prediction != null) {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (prediction.isUncertain) Color(0xFF332014) else Color(0xFF1E2F23)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (prediction.isUncertain) Color(0xFFFF9800) else Color(0xFF4CAF50)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Vision AI Crop Analysis",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = if (prediction.isUncertain) Color(0xFFE65100) else Color(0xFF2E7D32),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = prediction.formattedConfidence,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Crop: ${prediction.crop}",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (prediction.plantPart != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Plant Part: ${prediction.plantPart}",
                            color = Color(0xFF81D4FA),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Condition: ${if (prediction.isHealthy) "Healthy / Normal" else prediction.disease}",
                        color = if (prediction.isHealthy) Color(0xFF81C784) else Color(0xFFFFB74D),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Model: ${if (prediction.modelSource == "Tomato_MultiOrgan") "Tomato Multi-Organ AI" else if (prediction.modelSource == "PlantCity") "PlantCity Vision AI" else "Validation Guard"}",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )

                    if (prediction.isUncertain) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val warningMsg = when {
                            prediction.crop.contains("No valid plant") ->
                                "⚠️ No valid plant detected: Please capture a clear image of a plant leaf."
                            prediction.crop.contains("Crop not supported") ->
                                "⚠️ Crop not supported: KrishiSaathi's trained models support Apple, Apricot, Bean, Cherry, Corn, Fig, Grape, Lokat, Pear, Persimmon, Tomato, and Walnut."
                            else ->
                                "⚠️ Uncertain prediction: Please capture another image with better lighting and focus."
                        }
                        Text(
                            text = warningMsg,
                            color = Color(0xFFFFCC80),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Processed 224x224 ROI Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2421))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Crop,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Processed 224 × 224 ROI",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Surface(
                        color = Color(0xFF2E7D32),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${result.modelInputBitmap.width} × ${result.modelInputBitmap.height} px",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .size(224.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(2.dp, Color(0xFF4CAF50), RoundedCornerShape(12.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = result.modelInputBitmap.asImageBitmap(),
                        contentDescription = "224x224 ROI Model Input",
                        modifier = Modifier.size(224.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Center guide-box ROI cropped and resized to exact 224×224 model dimensions.",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Original Captured Image Card (Kept separately)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2421))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = Color(0xFF64B5F6),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Original Captured Photo",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Surface(
                        color = Color(0xFF1E3A5F),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${result.originalBitmap.width} × ${result.originalBitmap.height} px",
                            color = Color(0xFF90CAF9),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color.Gray.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = result.originalBitmap.asImageBitmap(),
                        contentDescription = "Original Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "ROI crop size before 224x224 resize: ${result.croppedRoiBitmap.width} × ${result.croppedRoiBitmap.height} px",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Pipeline Stages Checklist
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2421))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Pipeline Stages Verified",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                PipelineStepItem(
                    title = "1. Plant-Part Guide Centering",
                    subtitle = "Leaf, stem, or fruit placed inside UI guide box",
                    completed = true
                )
                PipelineStepItem(
                    title = "2. Plant-Part Validation",
                    subtitle = "${result.validationResult.className.displayName} accepted (≥ 70% threshold)",
                    completed = true
                )
                PipelineStepItem(
                    title = "3. Center ROI Extraction",
                    subtitle = "Guide box coordinates mapped to photo pixels",
                    completed = true
                )
                PipelineStepItem(
                    title = "4. Resize to 224 × 224",
                    subtitle = "Exact 224×224 bitmap generated for disease model",
                    completed = true
                )
                PipelineStepItem(
                    title = "5. PlantCity Disease MobileNetV2",
                    subtitle = if (prediction != null) "${prediction.crop}: ${prediction.disease} (${prediction.formattedConfidence})" else "Awaiting inference",
                    completed = (prediction != null)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action Buttons: Continue with photo or Retake
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onTestAgain,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White.copy(alpha = 0.7f))
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Retake",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                onClick = { onContinue(result.processedImageUri) },
                enabled = result.validationResult.isValid,
                modifier = Modifier
                    .weight(1.3f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E7D32),
                    disabledContainerColor = Color(0xFF424242)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = if (result.validationResult.isValid) Color.White else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Continue to Details",
                    color = if (result.validationResult.isValid) Color.White else Color.Gray,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun PipelineStepItem(
    title: String,
    subtitle: String,
    completed: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = if (completed) Color(0xFF2E7D32) else Color(0xFF424242),
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (completed) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = title,
                color = if (completed) Color.White else Color.LightGray,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = Color.Gray,
                fontSize = 11.sp
            )
        }
    }
}
