package com.example.mycameraenhancer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.camera.core.ImageProxy
import java.io.File
import java.io.FileOutputStream

object ImageProcessor {

    fun proxyToBitmap(image: ImageProxy): Bitmap {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    fun enhanceImage(input: Bitmap): Bitmap {
        // TODO: Add OpenCV / TFLite here
        // Example simple enhancement
        val enhanced = input.copy(input.config, true)
        // In real app: call OpenCV denoise, sharpen, or TFLite super-res
        return enhanced
    }

    fun saveBitmap(context: Context, bitmap: Bitmap, fileName: String) {
        val file = File(context.getExternalFilesDir(null), fileName)
        FileOutputStream(file).use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it)
        }
    }
}
