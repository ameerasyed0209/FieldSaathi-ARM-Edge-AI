package com.baasc.cameratest.ui

import android.graphics.Bitmap
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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.baasc.cameratest.validation.PlantPartResult

/**
 * ValidationWarningScreen:
 * Displayed when an image fails Plant-Part Validation.
 *
 * CRITICAL REQUIREMENTS:
 * - Shows: ⚠️ Invalid image
 * - Shows: "Please capture a leaf, stem, or fruit."
 * - Provides: [ RETAKE ] button
 * - NO ROI is generated
 * - NO 224x224 image is produced
 * - The user must retake the photo
 */
@Composable
fun ValidationWarningScreen(
    capturedBitmap: Bitmap,
    validationResult: PlantPartResult,
    onRetake: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF141414))
            .verticalScroll(scrollState)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Warning Icon Badge
        Surface(
            shape = CircleShape,
            color = Color(0xFFD32F2F).copy(alpha = 0.2f),
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = Color(0xFFEF5350),
                    modifier = Modifier.size(38.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Primary Warning Title
        Text(
            text = "Invalid image",
            color = Color(0xFFFF5252),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Mandatory Message
        Text(
            text = "Please capture a leaf, stem, or fruit.",
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Model status / Reason
        Text(
            text = validationResult.reasonMessage,
            color = Color(0xFFFFB74D),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Rejected Image Thumbnail
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(2.dp, Color(0xFFD32F2F), RoundedCornerShape(14.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = capturedBitmap.asImageBitmap(),
                contentDescription = "Rejected Photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            // Red translucent banner across rejected photo
            Surface(
                color = Color.Black.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = null,
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Image rejected — No ROI generated",
                        color = Color(0xFFFF8A80),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Validation Details Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF212121))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Validation Breakdown",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                ValidationRow(
                    label = "AI Model Status:",
                    value = if (validationResult.isModelLoaded) "Loaded" else "Not Installed",
                    valueColor = if (validationResult.isModelLoaded) Color(0xFF81C784) else Color(0xFFFFB74D)
                )

                ValidationRow(
                    label = "Detected Subject:",
                    value = validationResult.className.displayName,
                    valueColor = if (validationResult.className.isPlantPart) Color(0xFF81C784) else Color(0xFFFF5252)
                )

                ValidationRow(
                    label = "Confidence:",
                    value = "${(validationResult.confidence * 100).toInt()}% (Required: ≥ 70%)",
                    valueColor = if (validationResult.confidence >= 0.70f) Color(0xFF81C784) else Color(0xFFFFB74D)
                )

                ValidationRow(
                    label = "Decision:",
                    value = "REJECTED",
                    valueColor = Color(0xFFFF5252)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = validationResult.reasonMessage,
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Rejection explanation callout
        Surface(
            color = Color(0xFF263238),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB74D).copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFFFFB74D),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Allowed subjects are Leaf, Stem, or Fruit. Backgrounds, walls, hands, floors, tables, phones, and random objects are strictly rejected.",
                    color = Color(0xFFFFE082),
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Mandatory Retake Button
        Button(
            onClick = onRetake,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "RETAKE",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ValidationRow(
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 13.sp
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
