package com.baasc.cameratest

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageProxy

/**
 * Result data class holding pipeline artifacts:
 * - [originalBitmap]: Full captured camera photo in correct upright orientation.
 * - [croppedRoiBitmap]: Bitmap cropped to the exact region delineated by the Leaf Guide Box.
 * - [modelInputBitmap]: Scaled 224 x 224 bitmap directly suitable for model tensor feeding.
 * - [statusMessage]: Pipeline completion status.
 */
data class CameraPipelineResult(
    val originalBitmap: Bitmap,
    val croppedRoiBitmap: Bitmap,
    val modelInputBitmap: Bitmap,
    val statusMessage: String
)

/**
 * CameraPipelineManager:
 * Manages the camera-to-model preprocessing pipeline:
 *
 * Camera Capture -> Center ROI Extraction (Guide Box) -> 224x224 Resize -> processImageForModel()
 *
 * Designed to be modular so that leaf/background segmentation or MobileNetV2 can be plugged in seamlessly.
 */
object CameraPipelineManager {

    const val MODEL_INPUT_WIDTH = 224
    const val MODEL_INPUT_HEIGHT = 224

    /**
     * Converts an [ImageProxy] from CameraX into an upright [Bitmap], taking rotation degrees into account.
     */
    fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
        val originalBitmap = imageProxy.toBitmap()
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees

        return if (rotationDegrees != 0) {
            val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
            val rotatedBitmap = Bitmap.createBitmap(
                originalBitmap,
                0,
                0,
                originalBitmap.width,
                originalBitmap.height,
                matrix,
                true
            )
            // Recycle original if a new rotated bitmap was allocated
            if (rotatedBitmap != originalBitmap) {
                originalBitmap.recycle()
            }
            rotatedBitmap
        } else {
            originalBitmap
        }
    }

    /**
     * Extracts the Center ROI corresponding to the guide box overlay.
     *
     * Maps the guide box's on-screen coordinate frame (from PreviewView with ScaleType.FILL_CENTER)
     * onto the underlying captured photo without distortion or stretching.
     *
     * @param capturedBitmap Upright captured camera bitmap.
     * @param previewWidth Width of the camera PreviewView in pixels.
     * @param previewHeight Height of the camera PreviewView in pixels.
     * @param guideBoxSizePx Dimension (width and height) of the square guide box in pixels.
     */
    fun extractCenterRoi(
        capturedBitmap: Bitmap,
        previewWidth: Float,
        previewHeight: Float,
        guideBoxSizePx: Float
    ): Bitmap {
        val imgW = capturedBitmap.width.toFloat()
        val imgH = capturedBitmap.height.toFloat()

        // PreviewView with ScaleType.FILL_CENTER scales the image uniformly to fill the view
        val scale = maxOf(previewWidth / imgW, previewHeight / imgH)
        val scaledW = imgW * scale
        val scaledH = imgH * scale

        // Calculate offsets of the scaled image relative to the view center
        val offsetX = (scaledW - previewWidth) / 2f
        val offsetY = (scaledH - previewHeight) / 2f

        // The guide box is centered on the preview screen
        val boxLeft = (previewWidth - guideBoxSizePx) / 2f
        val boxTop = (previewHeight - guideBoxSizePx) / 2f

        // Map the screen coordinates back into bitmap coordinates
        var cropLeft = (boxLeft + offsetX) / scale
        var cropTop = (boxTop + offsetY) / scale
        var cropWidth = guideBoxSizePx / scale
        var cropHeight = guideBoxSizePx / scale

        // Clamp to bitmap boundaries to prevent out-of-bounds exceptions
        if (cropLeft < 0f) cropLeft = 0f
        if (cropTop < 0f) cropTop = 0f
        if (cropLeft + cropWidth > imgW) cropWidth = imgW - cropLeft
        if (cropTop + cropHeight > imgH) cropHeight = imgH - cropTop

        val finalCropW = cropWidth.toInt().coerceAtLeast(1)
        val finalCropH = cropHeight.toInt().coerceAtLeast(1)

        return Bitmap.createBitmap(
            capturedBitmap,
            cropLeft.toInt(),
            cropTop.toInt(),
            finalCropW,
            finalCropH
        )
    }

    /**
     * Resizes the cropped ROI to exactly 224 x 224 with high-quality bilinear filtering.
     */
    fun resizeRoiToModelInput(
        roiBitmap: Bitmap,
        targetWidth: Int = MODEL_INPUT_WIDTH,
        targetHeight: Int = MODEL_INPUT_HEIGHT
    ): Bitmap {
        return Bitmap.createScaledBitmap(roiBitmap, targetWidth, targetHeight, true)
    }

    /**
     * Executes the full pipeline on a captured image.
     *
     * 1. Preserves the original captured image.
     * 2. Extracts the center ROI corresponding to the guide box.
     * 3. Resizes the ROI to exactly 224 × 224.
     * 4. Passes the 224 × 224 bitmap to [processImageForModel].
     */
    fun executePipeline(
        capturedBitmap: Bitmap,
        previewWidth: Float,
        previewHeight: Float,
        guideBoxSizePx: Float
    ): CameraPipelineResult {
        // Step 1: Crop center ROI
        val croppedRoi = extractCenterRoi(
            capturedBitmap = capturedBitmap,
            previewWidth = previewWidth,
            previewHeight = previewHeight,
            guideBoxSizePx = guideBoxSizePx
        )

        // Step 2: Resize to 224 x 224
        val modelInputBitmap = resizeRoiToModelInput(croppedRoi)

        // Step 3: Call modular processImageForModel hook
        val message = processImageForModel(modelInputBitmap)

        return CameraPipelineResult(
            originalBitmap = capturedBitmap,
            croppedRoiBitmap = croppedRoi,
            modelInputBitmap = modelInputBitmap,
            statusMessage = message
        )
    }

    /**
     * Conceptual entry point for model inference.
     *
     * Later integration point:
     * Camera -> ROI -> 224x224 -> MobileNetV2 -> Disease prediction
     *
     * In this first test version, NO ML models (MobileNetV2/Llama) are invoked.
     * It validates that the image input satisfies the exact 224x224 tensor format.
     */
    fun processImageForModel(modelInputBitmap: Bitmap): String {
        require(modelInputBitmap.width == MODEL_INPUT_WIDTH && modelInputBitmap.height == MODEL_INPUT_HEIGHT) {
            "Model input must be exactly ${MODEL_INPUT_WIDTH}x${MODEL_INPUT_HEIGHT}"
        }

        // =========================================================================
        // FUTURE MOBILENETV2 INTEGRATION POINT:
        // val tensorImage = TensorImage.fromBitmap(modelInputBitmap)
        // val prediction = mobileNetV2Classifier.classify(tensorImage)
        // return prediction.label
        // =========================================================================

        return "Image ready for disease detection"
    }
}
