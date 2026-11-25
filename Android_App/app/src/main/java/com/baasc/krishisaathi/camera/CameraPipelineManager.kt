package com.baasc.krishisaathi.camera

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageProxy

/**
 * Utility functions for camera frame conversion and rotation.
 */
object CameraPipelineManager {

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
            if (rotatedBitmap != originalBitmap) {
                originalBitmap.recycle()
            }
            rotatedBitmap
        } else {
            originalBitmap
        }
    }
}
