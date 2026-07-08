package com.example.mycameraenhancer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private val cameraHelper = CameraHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        previewView = findViewById(R.id.previewView)
        val btnCapture = findViewById<Button>(R.id.btnCapture)

        cameraHelper.startCamera(previewView)

        btnCapture.setOnClickListener {
            cameraHelper.takeEnhancedPhoto { enhancedBitmap ->
                // Save or show the enhanced image
                ImageProcessor.saveBitmap(this, enhancedBitmap, "enhanced_photo.jpg")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraHelper.shutdown()
    }
}
