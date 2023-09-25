package com.example.qrscanner

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.view.View
import android.widget.TextView
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
    private lateinit var textView : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)
        textView = findViewById(R.id.text_qrname)
        mainBinding.startScanner.setOnClickListener {
            requestCameraAndStartScanner()
        }
        textView.setOnClickListener{
            val urlIntent = Intent(Intent.ACTION_VIEW , Uri.parse(textView.text.toString()))
            startActivity(urlIntent)
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
                    mainBinding.textQrname.text = barcode.url!!.url!!
                }
                Barcode.TYPE_CONTACT_INFO -> {
                    val contactInfo = barcode.contactInfo
                    if(contactInfo != null){
                        mainBinding.textQrname.text = contactInfo.name?.formattedName
                        mainBinding.textQremail.text = contactInfo.emails.firstOrNull()?.address
                        mainBinding.textQrphone.text = contactInfo.phones.firstOrNull()?.number
                    }
                }
                Barcode.TYPE_EMAIL -> {
                    mainBinding.textQrname.text = barcode.email?.address
                }

                Barcode.TYPE_PHONE -> {
                    mainBinding.textQrname.text = barcode.phone?.number
                }

                Barcode.TYPE_SMS -> {
                    mainBinding.textQrname.text = barcode.sms?.message
                }

                else -> {
                    mainBinding.textQrname.text = barcode.rawValue.toString()
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