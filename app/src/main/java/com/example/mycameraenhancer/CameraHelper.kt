package com.example.mycameraenhancer

import android.content.Context
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService

class CameraHelper(private val context: Context) {
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    fun startCamera(previewView: PreviewView) {
        cameraExecutor = Executors.newSingleThreadExecutor()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(context as MainActivity, cameraSelector, preview, imageCapture)
        }, ContextCompat.getMainExecutor(context))
    }

    fun takeEnhancedPhoto(callback: (android.graphics.Bitmap) -> Unit) {
        // For simplicity: capture one image. Expand to burst later.
        val imageCapture = imageCapture ?: return

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val bitmap = ImageProcessor.proxyToBitmap(image)
                    val enhanced = ImageProcessor.enhanceImage(bitmap)
                    callback(enhanced)
                    image.close()
                }
            }
        )
    }

    fun shutdown() {
        cameraExecutor.shutdown()
    }
}
