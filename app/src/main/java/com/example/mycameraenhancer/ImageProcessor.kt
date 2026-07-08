package com.example.mycameraenhancer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.camera.core.ImageProxy
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.io.File
import java.io.FileOutputStream

object ImageProcessor {

    init {
        System.loadLibrary("opencv_java4")  // Important for OpenCV
    }

    fun proxyToBitmap(image: ImageProxy): Bitmap {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    fun enhanceBurst(bitmaps: List<Bitmap>): Bitmap {
        if (bitmaps.isEmpty()) return bitmaps.firstOrNull() ?: throw IllegalArgumentException()

        // Simple averaging for noise reduction (expand with alignment later)
        var result = bitmaps[0].copy(bitmaps[0].config, true)
        for (i in 1 until bitmaps.size) {
            // Basic blend - improve with OpenCV alignment
            result = blendBitmaps(result, bitmaps[i])
        }

        return enhanceSingle(result)  // Final polish
    }

    private fun blendBitmaps(b1: Bitmap, b2: Bitmap): Bitmap {
        val result = b1.copy(b1.config, true)
        // Simple average (replace with proper merge)
        for (x in 0 until result.width) {
            for (y in 0 until result.height) {
                val p1 = b1.getPixel(x, y)
                val p2 = b2.getPixel(x, y)
                result.setPixel(x, y, averagePixels(p1, p2))
            }
        }
        return result
    }

    private fun averagePixels(p1: Int, p2: Int): Int {
        val a = (android.graphics.Color.alpha(p1) + android.graphics.Color.alpha(p2)) / 2
        val r = (android.graphics.Color.red(p1) + android.graphics.Color.red(p2)) / 2
        val g = (android.graphics.Color.green(p1) + android.graphics.Color.green(p2)) / 2
        val b = (android.graphics.Color.blue(p1) + android.graphics.Color.blue(p2)) / 2
        return android.graphics.Color.argb(a, r, g, b)
    }

    fun enhanceSingle(input: Bitmap): Bitmap {
        val mat = Mat()
        Utils.bitmapToMat(input, mat)

        // Noise reduction
        Imgproc.GaussianBlur(mat, mat, Size(3.0, 3.0), 0.0)

        // Sharpening
        val kernel = Mat(3, 3, org.opencv.core.CvType.CV_32F)
        kernel.put(0, 0, 0.0, -1.0, 0.0, -1.0, 5.0, -1.0, 0.0, -1.0, 0.0)
        Imgproc.filter2D(mat, mat, -1, kernel)

        val result = Bitmap.createBitmap(input.width, input.height, input.config)
        Utils.matToBitmap(mat, result)
        return result
    }

    fun saveBitmap(context: Context, bitmap: Bitmap, fileName: String) {
        val file = File(context.getExternalFilesDir(null), fileName)
        FileOutputStream(file).use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it)
        }
    }
}
