package com.baasc.cameratest

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.baasc.cameratest.roi.ProcessedRoiResult
import com.baasc.cameratest.roi.ROIProcessor
import com.baasc.cameratest.ui.CameraPreviewScreen
import com.baasc.cameratest.ui.PhotoConfirmationScreen
import com.baasc.cameratest.ui.ProcessedResultScreen
import com.baasc.cameratest.ui.ValidationWarningScreen
import com.baasc.cameratest.ui.theme.CameraTestTheme
import com.baasc.cameratest.validation.PlantPartResult
import com.baasc.cameratest.validation.PlantPartValidatorImpl

/**
 * Stages in the camera test pipeline:
 * - [LIVE_PREVIEW]: Live CameraX preview with Guide Box
 * - [CONFIRMATION]: Review captured photo + select validation test mode
 * - [VALIDATION_WARNING]: Rejection screen when image is not a leaf, stem, or fruit
 * - [PROCESSED_RESULT]: Acceptance screen showing 224x224 ROI + original photo
 */
enum class CameraTestStage {
    LIVE_PREVIEW,
    CONFIRMATION,
    VALIDATION_WARNING,
    PROCESSED_RESULT
}

/**
 * Data holder for captured photo metadata to enable ROI crop during confirm.
 */
data class CapturedPhotoData(
    val bitmap: Bitmap,
    val previewWidth: Float,
    val previewHeight: Float,
    val guideBoxSizePx: Float
)

/**
 * CameraTestActivity:
 * Completely separate test activity for testing the camera-to-ROI pipeline with Plant-Part Validation.
 *
 * Pipeline Flow:
 * Camera Capture
 *   ↓
 * Review Photo
 *   ↓
 * Plant-Part Validation (Leaf, Stem, Fruit vs Background)
 *   ↓                          ↓
 * Valid (>= 0.70)            Invalid (Background / Low Confidence)
 *   ↓                          ↓
 * Extract Center ROI         Show ⚠️ Invalid Image Warning
 *   ↓                          ↓
 * Resize to 224 x 224        [ RETAKE ] (No ROI generated)
 *   ↓
 * Display Processed Image
 */
class CameraTestActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CameraTestTheme {
                CameraTestApp()
            }
        }
    }
}

@Composable
fun CameraTestApp() {
    val context = LocalContext.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (!hasCameraPermission) {
        CameraPermissionDeniedScreen(
            onRequestPermission = {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        )
    } else {
        CameraPipelineFlow()
    }
}

@Composable
fun CameraPipelineFlow() {
    val validator = remember { PlantPartValidatorImpl() }

    var stage by remember { mutableStateOf(CameraTestStage.LIVE_PREVIEW) }
    var currentCaptureData by remember { mutableStateOf<CapturedPhotoData?>(null) }
    var validationResult by remember { mutableStateOf<PlantPartResult?>(null) }
    var processedRoiResult by remember { mutableStateOf<ProcessedRoiResult?>(null) }

    when (stage) {
        CameraTestStage.LIVE_PREVIEW -> {
            CameraPreviewScreen(
                onPhotoCaptured = { bitmap, previewW, previewH, boxSizePx ->
                    currentCaptureData = CapturedPhotoData(bitmap, previewW, previewH, boxSizePx)
                    stage = CameraTestStage.CONFIRMATION
                }
            )
        }

        CameraTestStage.CONFIRMATION -> {
            val captureData = currentCaptureData
            if (captureData != null) {
                PhotoConfirmationScreen(
                    capturedBitmap = captureData.bitmap,
                    onRetake = {
                        stage = CameraTestStage.LIVE_PREVIEW
                        currentCaptureData = null
                    },
                    onConfirm = {
                        // 1. Run Plant-Part Validation FIRST
                        val vResult = validator.validate(captureData.bitmap)
                        validationResult = vResult

                        if (vResult.isValid) {
                            // 2a. VALID PATH: Extract ROI and resize to 224x224
                            val roiResult = ROIProcessor.processValidImage(
                                capturedBitmap = captureData.bitmap,
                                previewWidth = captureData.previewWidth,
                                previewHeight = captureData.previewHeight,
                                guideBoxSizePx = captureData.guideBoxSizePx,
                                validationResult = vResult
                            )
                            processedRoiResult = roiResult
                            stage = CameraTestStage.PROCESSED_RESULT
                        } else {
                            // 2b. INVALID PATH: Do NOT generate ROI. Show Warning.
                            stage = CameraTestStage.VALIDATION_WARNING
                        }
                    }
                )
            } else {
                stage = CameraTestStage.LIVE_PREVIEW
            }
        }

        CameraTestStage.VALIDATION_WARNING -> {
            val captureData = currentCaptureData
            val vResult = validationResult
            if (captureData != null && vResult != null) {
                ValidationWarningScreen(
                    capturedBitmap = captureData.bitmap,
                    validationResult = vResult,
                    onRetake = {
                        stage = CameraTestStage.LIVE_PREVIEW
                        currentCaptureData = null
                        validationResult = null
                        processedRoiResult = null
                    }
                )
            } else {
                stage = CameraTestStage.LIVE_PREVIEW
            }
        }

        CameraTestStage.PROCESSED_RESULT -> {
            val roiResult = processedRoiResult
            if (roiResult != null) {
                ProcessedResultScreen(
                    result = roiResult,
                    onTestAgain = {
                        stage = CameraTestStage.LIVE_PREVIEW
                        currentCaptureData = null
                        validationResult = null
                        processedRoiResult = null
                    }
                )
            } else {
                stage = CameraTestStage.LIVE_PREVIEW
            }
        }
    }
}

/**
 * Screen displayed when camera permission is denied, allowing graceful retry without crashing.
 */
@Composable
fun CameraPermissionDeniedScreen(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFFC62828).copy(alpha = 0.2f),
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Camera",
                        tint = Color(0xFFEF5350),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Camera Permission Required",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "KrishiSaathi Camera Test requires camera access to capture leaves, stems, or fruits for testing.",
                color = Color.LightGray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onRequestPermission,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Text(
                    text = "Grant Camera Permission",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
