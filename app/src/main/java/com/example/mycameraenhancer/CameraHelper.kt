package com.example.mycameraenhancer

import android.content.Context
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraHelper(private val context: Context) {
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    fun startCamera(previewView: PreviewView) {
        cameraExecutor = Executors.newSingleThreadExecutor()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                context as MainActivity,
                cameraSelector,
                preview,
                imageCapture
            )
        }, ContextCompat.getMainExecutor(context))
    }

    fun takeBurstAndEnhance(callback: (android.graphics.Bitmap) -> Unit) {
        val burstSize = 5  // Adjust based on your phone's performance
        val images = mutableListOf<ImageProxy>()

        fun captureNext(index: Int) {
            imageCapture?.takePicture(
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        images.add(image)
                        if (index < burstSize - 1) {
                            captureNext(index + 1)
                        } else {
                            processBurst(images, callback)
                        }
                    }
                }
            )
        }
        captureNext(0)
    }

    private fun processBurst(images: List<ImageProxy>, callback: (android.graphics.Bitmap) -> Unit) {
        val bitmaps = images.map { ImageProcessor.proxyToBitmap(it) }
        val enhanced = ImageProcessor.enhanceBurst(bitmaps)
        
        // Clean up
        images.forEach { it.close() }
        callback(enhanced)
    }

    fun shutdown() {
        cameraExecutor.shutdown()
    }
}
