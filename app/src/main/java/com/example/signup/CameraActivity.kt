/*
package com.example.signup

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import coil.load

open class BaseActivity : ComponentActivity() {

    var capturedPhotoUri: Uri? = null



    private val cameraPermissionRequestLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                startDefaultCamera()
            } else {
                Toast.makeText(
                    this,
                    "Go to settings and enable camera permission to use this feature",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private val takePictureLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // Retrieve the photo URI from the result intent
                val photoUri = result.data?.data
                // Store the photo URI in the variable
                capturedPhotoUri = photoUri
                Toast.makeText(this, "Photo taken", Toast.LENGTH_SHORT).show()
            }
        }

    fun handleCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                startDefaultCamera()
            }

            else -> {
                cameraPermissionRequestLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun startDefaultCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                takePictureLauncher.launch(takePictureIntent)
            } ?: run {
                Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun displayCapturedPhoto(imageView: ImageView, capturedPhotoUri: Uri?) {
        capturedPhotoUri?.let { uri ->

            imageView.load(uri)
        }
    }


    fun someFunction() {
        val imageView: ImageView = findViewById(org.koin.android.R.id.image)
        displayCapturedPhoto(imageView, capturedPhotoUri)
    }
}
*/
