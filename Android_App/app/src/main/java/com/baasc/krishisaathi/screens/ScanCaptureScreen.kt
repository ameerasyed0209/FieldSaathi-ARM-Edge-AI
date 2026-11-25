package com.baasc.krishisaathi.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

import com.baasc.krishisaathi.PlantCityClassifier
import com.baasc.krishisaathi.R
import com.baasc.krishisaathi.camera.PlantPartResult
import com.baasc.krishisaathi.camera.PlantPartValidatorImpl
import com.baasc.krishisaathi.camera.ProcessedRoiResult
import com.baasc.krishisaathi.camera.ROIProcessor
import com.baasc.krishisaathi.camera.ui.CameraPreviewScreen
import com.baasc.krishisaathi.camera.ui.PhotoConfirmationScreen
import com.baasc.krishisaathi.camera.ui.ProcessedResultScreen
import com.baasc.krishisaathi.camera.ui.ValidationWarningScreen
import com.baasc.krishisaathi.data.VisionPrediction
import com.baasc.krishisaathi.ui.theme.ForestGreen
import com.baasc.krishisaathi.ui.theme.TextSecondary

private enum class CameraStage {
    LIVE_PREVIEW,
    CONFIRMATION,
    VALIDATION_WARNING,
    PROCESSED_RESULT
}

private data class CapturedData(
    val bitmap: Bitmap,
    val previewWidth: Float,
    val previewHeight: Float,
    val guideBoxSizePx: Float
)

@Composable
fun ScanCaptureScreen(
    onNavigateBack: () -> Unit,
    onPhotoCaptured: (imageUri: String?, prediction: VisionPrediction?) -> Unit
) {
    val context = LocalContext.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            hasCameraPermission = isGranted
        }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    /*
     * Load the PlantCity model once.
     */
    val plantCityClassifier = remember {
        PlantCityClassifier(context)
    }

    /*
     * Close TFLite interpreter when this screen leaves composition.
     */
    DisposableEffect(Unit) {
        onDispose {
            plantCityClassifier.close()
        }
    }

    if (!hasCameraPermission) {
        CameraPermissionDeniedView(
            onRequestPermission = {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            },
            onNavigateBack = onNavigateBack
        )
        return
    }

    val validator = remember {
        PlantPartValidatorImpl()
    }

    var stage by remember {
        mutableStateOf(CameraStage.LIVE_PREVIEW)
    }

    var currentCaptureData by remember {
        mutableStateOf<CapturedData?>(null)
    }

    var validationResult by remember {
        mutableStateOf<PlantPartResult?>(null)
    }

    var processedRoiResult by remember {
        mutableStateOf<ProcessedRoiResult?>(null)
    }

    /*
     * Store the AI prediction.
     */
    var aiPrediction by remember {
        mutableStateOf<VisionPrediction?>(null)
    }

    BackHandler {
        when (stage) {

            CameraStage.LIVE_PREVIEW -> {
                onNavigateBack()
            }

            CameraStage.CONFIRMATION,
            CameraStage.VALIDATION_WARNING,
            CameraStage.PROCESSED_RESULT -> {

                stage = CameraStage.LIVE_PREVIEW
                currentCaptureData = null
                validationResult = null
                processedRoiResult = null
                aiPrediction = null
            }
        }
    }

    when (stage) {

        // ---------------------------------------------------------
        // CAMERA
        // ---------------------------------------------------------

        CameraStage.LIVE_PREVIEW -> {

            CameraPreviewScreen(
                onPhotoCaptured = {
                        bitmap,
                        previewW,
                        previewH,
                        boxSizePx ->

                    currentCaptureData = CapturedData(
                        bitmap = bitmap,
                        previewWidth = previewW,
                        previewHeight = previewH,
                        guideBoxSizePx = boxSizePx
                    )

                    stage = CameraStage.CONFIRMATION
                },

                onNavigateBack = onNavigateBack
            )
        }

        // ---------------------------------------------------------
        // PHOTO CONFIRMATION
        // ---------------------------------------------------------

        CameraStage.CONFIRMATION -> {

            val captureData = currentCaptureData

            if (captureData != null) {

                PhotoConfirmationScreen(

                    capturedBitmap = captureData.bitmap,

                    onRetake = {

                        stage = CameraStage.LIVE_PREVIEW
                        currentCaptureData = null
                        validationResult = null
                        processedRoiResult = null
                        aiPrediction = null
                    },

                    onConfirm = {

                        // -------------------------------------------------
                        // 1. VALIDATE PLANT PART
                        // -------------------------------------------------

                        val vResult =
                            validator.validate(captureData.bitmap)

                        validationResult = vResult

                        if (vResult.isValid) {

                            // ---------------------------------------------
                            // 2. CREATE THE EXISTING ROI
                            // ---------------------------------------------

                            val roiResult =
                                ROIProcessor.processValidImage(
                                    context = context,
                                    capturedBitmap = captureData.bitmap,
                                    previewWidth = captureData.previewWidth,
                                    previewHeight = captureData.previewHeight,
                                    guideBoxSizePx = captureData.guideBoxSizePx,
                                    validationResult = vResult
                                )

                            processedRoiResult = roiResult

                            // ---------------------------------------------
                            // 3. RUN PLANTCITY AI ON 224x224 ROI
                            // ---------------------------------------------
                            try {
                                val prediction =
                                    plantCityClassifier.predict(
                                        roiResult.modelInputBitmap
                                    )

                                aiPrediction = prediction

                                val confidencePercent =
                                    prediction.confidence * 100f

                                Toast.makeText(
                                    context,
                                    "Crop: ${prediction.crop}\n" +
                                            "Condition: ${if (prediction.isHealthy) "Healthy / Normal" else prediction.disease}\n" +
                                            "Confidence: ${
                                                "%.1f".format(
                                                    confidencePercent
                                                )
                                            }%",
                                    Toast.LENGTH_LONG
                                ).show()

                            } catch (e: Exception) {

                                Toast.makeText(
                                    context,
                                    "AI inference failed: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                            // ---------------------------------------------
                            // 4. SHOW EXISTING PROCESSED RESULT SCREEN
                            // ---------------------------------------------

                            stage =
                                CameraStage.PROCESSED_RESULT

                        } else {

                            // Invalid image
                            stage =
                                CameraStage.VALIDATION_WARNING
                        }
                    }
                )
            } else {

                stage = CameraStage.LIVE_PREVIEW
            }
        }

        // ---------------------------------------------------------
        // INVALID IMAGE
        // ---------------------------------------------------------

        CameraStage.VALIDATION_WARNING -> {

            val captureData = currentCaptureData
            val vResult = validationResult

            if (captureData != null && vResult != null) {

                ValidationWarningScreen(

                    capturedBitmap = captureData.bitmap,

                    validationResult = vResult,

                    onRetake = {

                        stage =
                            CameraStage.LIVE_PREVIEW

                        currentCaptureData = null
                        validationResult = null
                        processedRoiResult = null
                        aiPrediction = null
                    }
                )

            } else {

                stage =
                    CameraStage.LIVE_PREVIEW
            }
        }

        // ---------------------------------------------------------
        // PROCESSED RESULT
        // ---------------------------------------------------------

        CameraStage.PROCESSED_RESULT -> {

            val roiResult = processedRoiResult

            if (roiResult != null) {

                ProcessedResultScreen(

                    result = roiResult,

                    prediction = aiPrediction,

                    onContinue = { uri ->

                        onPhotoCaptured(
                            uri?.toString(),
                            aiPrediction
                        )
                    },

                    onTestAgain = {

                        stage =
                            CameraStage.LIVE_PREVIEW

                        currentCaptureData = null
                        validationResult = null
                        processedRoiResult = null
                        aiPrediction = null
                    }
                )

            } else {

                stage =
                    CameraStage.LIVE_PREVIEW
            }
        }
    }
}

@Composable
private fun CameraPermissionDeniedView(
    onRequestPermission: () -> Unit,
    onNavigateBack: () -> Unit
) {

    Box(
        modifier = Modifier
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

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Camera",
                        tint = Color(0xFFEF5350),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Text(
                text = stringResource(
                    id = R.string.camera_permission_title
                ),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = stringResource(
                    id = R.string.camera_permission_desc
                ),
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(
                modifier = Modifier.height(32.dp)
            )

            Button(
                onClick = onRequestPermission,

                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(50.dp),

                shape = RoundedCornerShape(25.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = ForestGreen
                )
            ) {

                Text(
                    text = stringResource(
                        id = R.string.grant_permission
                    ),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Button(
                onClick = onNavigateBack,

                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(50.dp),

                shape = RoundedCornerShape(25.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.DarkGray
                )
            ) {

                Text(
                    text = stringResource(
                        id = R.string.back
                    ),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}