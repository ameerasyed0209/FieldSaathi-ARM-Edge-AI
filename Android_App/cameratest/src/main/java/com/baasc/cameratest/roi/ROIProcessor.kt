package com.baasc.cameratest.roi

import android.graphics.Bitmap
import com.baasc.cameratest.validation.PlantPartResult

/**
 * Result data class for valid processed ROI images:
 * - [originalBitmap]: Full captured camera photo in correct upright orientation.
 * - [croppedRoiBitmap]: Bitmap cropped to the exact region delineated by the Guide Box.
 * - [modelInputBitmap]: Scaled 224 x 224 bitmap suitable for subsequent disease detection models.
 * - [validationResult]: Result from [com.baasc.cameratest.validation.PlantPartValidator].
 */
data class ProcessedRoiResult(
    val originalBitmap: Bitmap,
    val croppedRoiBitmap: Bitmap,
    val modelInputBitmap: Bitmap,
    val validationResult: PlantPartResult
)

/**
 * ROIProcessor:
 * Dedicated modular component for extracting the guide box ROI and resizing to 224 x 224.
 *
 * CRITICAL RULE:
 * This component is ONLY invoked AFTER Plant-Part Validation has succeeded.
 * If validation fails, NO ROI is extracted and NO 224x224 image is created.
 */
object ROIProcessor {

    const val TARGET_WIDTH = 224
    const val TARGET_HEIGHT = 224

    /**
     * Extracts the Center ROI corresponding to the guide box overlay shown on screen.
     */
    fun extractCenterRoi(
        capturedBitmap: Bitmap,
        previewWidth: Float,
        previewHeight: Float,
        guideBoxSizePx: Float
    ): Bitmap {
        val imgW = capturedBitmap.width.toFloat()
        val imgH = capturedBitmap.height.toFloat()

        // PreviewView with ScaleType.FILL_CENTER scales uniformly to fill the view
        val scale = maxOf(previewWidth / imgW, previewHeight / imgH)
        val scaledW = imgW * scale
        val scaledH = imgH * scale

        val offsetX = (scaledW - previewWidth) / 2f
        val offsetY = (scaledH - previewHeight) / 2f

        val boxLeft = (previewWidth - guideBoxSizePx) / 2f
        val boxTop = (previewHeight - guideBoxSizePx) / 2f

        var cropLeft = (boxLeft + offsetX) / scale
        var cropTop = (boxTop + offsetY) / scale
        var cropWidth = guideBoxSizePx / scale
        var cropHeight = guideBoxSizePx / scale

        // Clamp to bitmap boundaries
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
     * Resizes the cropped ROI bitmap to exactly 224 x 224 with high quality bilinear filtering.
     */
    fun resizeTo224x224(
        roiBitmap: Bitmap,
        targetWidth: Int = TARGET_WIDTH,
        targetHeight: Int = TARGET_HEIGHT
    ): Bitmap {
        return Bitmap.createScaledBitmap(roiBitmap, targetWidth, targetHeight, true)
    }

    /**
     * Executes the ROI extraction and 224x224 resize pipeline for a VALID image.
     * Throws IllegalArgumentException if called with an invalid validation result.
     */
    fun processValidImage(
        capturedBitmap: Bitmap,
        previewWidth: Float,
        previewHeight: Float,
        guideBoxSizePx: Float,
        validationResult: PlantPartResult
    ): ProcessedRoiResult {
        require(validationResult.isValid) {
            "Cannot generate ROI for an invalid image. Plant-Part validation must pass first."
        }

        val croppedRoi = extractCenterRoi(
            capturedBitmap = capturedBitmap,
            previewWidth = previewWidth,
            previewHeight = previewHeight,
            guideBoxSizePx = guideBoxSizePx
        )

        val modelInput224 = resizeTo224x224(croppedRoi)

        return ProcessedRoiResult(
            originalBitmap = capturedBitmap,
            croppedRoiBitmap = croppedRoi,
            modelInputBitmap = modelInput224,
            validationResult = validationResult
        )
    }
}
