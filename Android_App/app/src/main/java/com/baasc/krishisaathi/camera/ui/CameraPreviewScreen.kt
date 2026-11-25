package com.baasc.krishisaathi.camera.ui

import android.graphics.Bitmap
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.baasc.krishisaathi.camera.CameraPipelineManager

/**
 * CameraPreviewScreen:
 * Shows live CameraX preview with back camera by default.
 * Displays a center leaf/stem/fruit guide box overlay: "Place the leaf, stem, or fruit inside the box".
 * The guide box is strictly a UI overlay and is NOT embedded in the captured photo.
 */
@Composable
fun CameraPreviewScreen(
    onPhotoCaptured: (bitmap: Bitmap, previewW: Float, previewH: Float, boxSizePx: Float) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var previewView: PreviewView? by remember { mutableStateOf(null) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var isCapturing by remember { mutableStateOf(false) }

    // Measured Preview dimensions & Guide Box size in px
    var measuredWidthPx by remember { mutableStateOf(0f) }
    var measuredHeightPx by remember { mutableStateOf(0f) }
    var guideBoxSizePx by remember { mutableStateOf(0f) }

    BoxWithConstraints(modifier = modifier.fillMaxSize().background(Color.Black)) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val boxDimensionPx = minOf(widthPx * 0.72f, 850f)

        measuredWidthPx = widthPx
        measuredHeightPx = heightPx
        guideBoxSizePx = boxDimensionPx

        // 1. CameraX PreviewView
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.PERFORMANCE
                    previewView = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Bind CameraX Lifecycle
        LaunchedEffect(previewView) {
            val pv = previewView ?: return@LaunchedEffect
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(pv.surfaceProvider)
                }

                val capture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .build()

                imageCapture = capture

                // Default to back camera as required
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        capture
                    )
                } catch (e: Exception) {
                    Toast.makeText(context, "Camera bind error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }, ContextCompat.getMainExecutor(context))
        }

        // 3. Leaf Guide Box Overlay (UI only, never written to image)
        GuideBoxOverlay(
            boxSizePx = boxDimensionPx,
            modifier = Modifier.fillMaxSize()
        )

        // 4. Top Navigation Bar (Back button + Instructions)
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onNavigateBack,
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Surface(
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF81C784),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(
                            text = "Place the leaf, stem, or fruit inside the box",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                color = Color(0xFF263238).copy(alpha = 0.9f),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB74D).copy(alpha = 0.7f))
            ) {
                Text(
                    text = "Requires Leaf, Stem, or Fruit validation (Non-plant rejected)",
                    color = Color(0xFFFFE082),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }

        // 5. Capture Control at Bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isCapturing) {
                CircularProgressIndicator(
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.size(64.dp)
                )
            } else {
                // Shutter Button
                Surface(
                    onClick = {
                        val capture = imageCapture
                        if (capture != null && !isCapturing) {
                            isCapturing = true
                            capture.takePicture(
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageCapturedCallback() {
                                    override fun onCaptureSuccess(image: ImageProxy) {
                                        try {
                                            val uprightBitmap = CameraPipelineManager.imageProxyToBitmap(image)
                                            onPhotoCaptured(
                                                uprightBitmap,
                                                measuredWidthPx,
                                                measuredHeightPx,
                                                guideBoxSizePx
                                            )
                                        } finally {
                                            image.close()
                                            isCapturing = false
                                        }
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        isCapturing = false
                                        Toast.makeText(
                                            context,
                                            "Capture failed: ${exception.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            )
                        }
                    },
                    modifier = Modifier.size(76.dp),
                    shape = CircleShape,
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(4.dp, Color(0xFF4CAF50)),
                    shadowElevation = 8.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Surface(
                            modifier = Modifier.size(60.dp),
                            shape = CircleShape,
                            color = Color(0xFF2E7D32)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Camera,
                                    contentDescription = "Capture",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GuideBoxOverlay(
    boxSizePx: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val viewW = size.width
        val viewH = size.height

        val left = (viewW - boxSizePx) / 2f
        val top = (viewH - boxSizePx) / 2f
        val right = left + boxSizePx
        val bottom = top + boxSizePx
        val cornerRadius = 16.dp.toPx()

        val cutoutPath = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = Rect(left, top, right, bottom),
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                )
            )
        }

        clipPath(cutoutPath, clipOp = ClipOp.Difference) {
            drawRect(color = Color.Black.copy(alpha = 0.55f))
        }

        val strokeWidth = 2.dp.toPx()
        val cornerStrokeWidth = 4.dp.toPx()
        val cornerLen = 28.dp.toPx()
        val guideColor = Color(0xFF4CAF50)

        drawRoundRect(
            color = guideColor.copy(alpha = 0.5f),
            topLeft = Offset(left, top),
            size = Size(boxSizePx, boxSizePx),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )

        // Corner indicators
        drawLine(guideColor, Offset(left, top + cornerRadius), Offset(left, top + cornerRadius + cornerLen), cornerStrokeWidth)
        drawLine(guideColor, Offset(left + cornerRadius, top), Offset(left + cornerRadius + cornerLen, top), cornerStrokeWidth)

        drawLine(guideColor, Offset(right, top + cornerRadius), Offset(right, top + cornerRadius + cornerLen), cornerStrokeWidth)
        drawLine(guideColor, Offset(right - cornerRadius, top), Offset(right - cornerRadius - cornerLen, top), cornerStrokeWidth)

        drawLine(guideColor, Offset(left, bottom - cornerRadius), Offset(left, bottom - cornerRadius - cornerLen), cornerStrokeWidth)
        drawLine(guideColor, Offset(left + cornerRadius, bottom), Offset(left + cornerRadius + cornerLen, bottom), cornerStrokeWidth)

        drawLine(guideColor, Offset(right, bottom - cornerRadius), Offset(right, bottom - cornerRadius - cornerLen), cornerStrokeWidth)
        drawLine(guideColor, Offset(right - cornerRadius, bottom), Offset(right - cornerRadius - cornerLen, bottom), cornerStrokeWidth)
    }
}
