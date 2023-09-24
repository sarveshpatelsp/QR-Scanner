package com.example.qrscanner

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.qrscanner.databinding.ActivityMainBinding
import com.google.mlkit.vision.barcode.common.Barcode

class MainActivity : AppCompatActivity() {

    private lateinit var mainBinding : ActivityMainBinding
    private val permission = android.Manifest.permission.CAMERA
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            isGranted ->
            if(isGranted)
            {
                startScanner()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)
        mainBinding.startScanner.setOnClickListener {
            requestCameraAndStartScanner()
        }
    }

    private fun requestCameraAndStartScanner()
    {
        if(isPermissionGranted(permission))
        {
           startScanner()
        }
        else
        {
            requestCameraPermission();
        }
    }

    private fun isPermissionGranted (permission:String) : Boolean
    {
        return (ContextCompat.checkSelfPermission(this, permission)
                == PackageManager.PERMISSION_GRANTED)
    }

    fun startScanner()
    {
        ScannerActivity.startScanner(this)
        {barcodes ->
        barcodes.forEach{barcode ->
            when (barcode.valueType){
                Barcode.TYPE_URL -> {
                    mainBinding.textQrcontent.text = barcode.url!!.url!!
                }
                Barcode.TYPE_CONTACT_INFO -> {
                    mainBinding.textQrcontent.text = barcode.contactInfo.toString()
                }
                Barcode.TYPE_EMAIL -> {
                    mainBinding.textQrcontent.text = barcode.email.toString()
                }
                else -> {
                    mainBinding.textQrcontent.text = barcode.rawValue.toString()
                }
            }
        }

        }
    }

    private fun requestCameraPermission()
    {
        when {
            shouldShowRequestPermissionRationale(permission)->
            {
                cameraPermissionRequest{
                    openPermissionSettings()
                }
            }
            else->{
                requestPermissionLauncher.launch(permission)
            }
        }
    }

   private inline fun cameraPermissionRequest(crossinline positive: () -> Unit){
       AlertDialog.Builder(this)
           .setTitle("Camera Permission Required")
           .setMessage("Without camera permission you did not access the scanner")
           .setPositiveButton("Allow"){dialog , which ->
           positive.invoke()
           }
           .setNegativeButton("Deny"){dialog , which ->
           }.show()
   }

    fun openPermissionSettings()
    {
        Intent(ACTION_APPLICATION_DETAILS_SETTINGS).also {
            val uri : Uri = Uri.fromParts("package" , packageName , null)
            it.data = uri
            startActivity(it)
        }
    }

}