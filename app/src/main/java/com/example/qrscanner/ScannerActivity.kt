package com.example.qrscanner
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.qrscanner.databinding.ActivityScannerBinding
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class ScannerActivity : AppCompatActivity() {

    private lateinit var scannerBinding: ActivityScannerBinding
    private lateinit var cameraSelector: CameraSelector
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var processCameraProvider: ProcessCameraProvider
    private lateinit var cameraPreview: Preview
    private lateinit var imageAnalysis: ImageAnalysis

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scannerBinding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(scannerBinding.root)
        cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({processCameraProvider = cameraProviderFuture.get()
            bindCameraPreview()
            bindImageAnalysis()
        }, ContextCompat.getMainExecutor(this))
    }

    fun bindCameraPreview() {
        cameraPreview =
            Preview.Builder().setTargetRotation(scannerBinding.previewView.display.rotation).build()
        cameraPreview.setSurfaceProvider(scannerBinding.previewView.surfaceProvider)
        processCameraProvider.bindToLifecycle(this, cameraSelector, cameraPreview)
    }

    fun bindImageAnalysis()
    {
        val barcodeScanner : BarcodeScanner = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .build()
        )

        imageAnalysis = ImageAnalysis.Builder()
            .setTargetRotation(scannerBinding.previewView.display.rotation)
            .build()

        val cameraExecutor = Executors.newSingleThreadExecutor()

        imageAnalysis.setAnalyzer(cameraExecutor){
            imageProxy ->
            processImageProxy(barcodeScanner , imageProxy)
        }
        processCameraProvider.bindToLifecycle(this , cameraSelector , imageAnalysis)
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun processImageProxy(barcodeScanner : BarcodeScanner, imageProxy: ImageProxy)
    {
        val inputImage = InputImage.fromMediaImage(imageProxy.image!! , imageProxy.imageInfo.rotationDegrees)
        barcodeScanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                if(barcodes.isNotEmpty()){
                    onScan?.invoke(barcodes)
                    onScan = null
                    finish()
                }
            }
            .addOnFailureListener{
                it.printStackTrace()
            }
            .addOnCompleteListener{
                imageProxy.close()
            }
    }

    companion object {
        private var onScan: ((barcodes : List<Barcode>) -> Unit)? = null
        fun startScanner(context: Context, onScan: (barcodes : List<Barcode>) -> Unit) {
            this.onScan = onScan
            Intent(context, ScannerActivity::class.java).also {
                context.startActivity(it)
            }
        }
    }
}