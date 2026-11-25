package com.baasc.krishisaathi.camera

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

data class ProcessedRoiResult(
    val originalBitmap: Bitmap,
    val croppedRoiBitmap: Bitmap,
    val modelInputBitmap: Bitmap,
    val validationResult: PlantPartResult,
    val processedImageUri: Uri? = null
)

/**
 * ROIProcessor:
 * Extracts the center guide-box ROI and resizes to 224x224.
 * Only executes for VALID plant images.
 */
object ROIProcessor {

    const val TARGET_WIDTH = 224
    const val TARGET_HEIGHT = 224

    fun extractCenterRoi(
        capturedBitmap: Bitmap,
        previewWidth: Float,
        previewHeight: Float,
        guideBoxSizePx: Float
    ): Bitmap {
        val imgW = capturedBitmap.width.toFloat()
        val imgH = capturedBitmap.height.toFloat()

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

    fun resizeTo224x224(
        roiBitmap: Bitmap,
        targetWidth: Int = TARGET_WIDTH,
        targetHeight: Int = TARGET_HEIGHT
    ): Bitmap {
        return Bitmap.createScaledBitmap(roiBitmap, targetWidth, targetHeight, true)
    }

    fun processValidImage(
        context: Context,
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

        // Save processed 224x224 image to cache for downstream screens
        val processedUri = try {
            val cacheDir = File(context.cacheDir, "processed_crops").apply { mkdirs() }
            val outputFile = File(cacheDir, "roi_224_${System.currentTimeMillis()}.jpg")
            FileOutputStream(outputFile).use { out ->
                modelInput224.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                outputFile
            )
        } catch (e: Exception) {
            null
        }

        return ProcessedRoiResult(
            originalBitmap = capturedBitmap,
            croppedRoiBitmap = croppedRoi,
            modelInputBitmap = modelInput224,
            validationResult = validationResult,
            processedImageUri = processedUri
        )
    }
}
