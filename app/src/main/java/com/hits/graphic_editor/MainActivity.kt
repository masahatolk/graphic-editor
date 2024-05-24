package com.hits.graphic_editor

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.hits.graphic_editor.databinding.ActivityMainBinding
import com.hits.graphic_editor.databinding.BottomSheetBinding
import java.io.File

class MainActivity : AppCompatActivity() {

    private var fab: View? = null
    private val bottomSheetBinding: BottomSheetBinding by lazy {
        BottomSheetBinding.inflate(layoutInflater)
    }
    private var pickedPhoto: Uri? = null
    private var tempImageUri: Uri? = null

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        fab = binding.fab

        val dialog = BottomSheetDialog(this)
        dialog.setContentView(bottomSheetBinding.root)

        fab?.setOnClickListener {

            dialog.show()

            bottomSheetBinding.gallery.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermission(Manifest.permission.READ_MEDIA_IMAGES, 1)
                }
                else requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, 1)
            }

            bottomSheetBinding.camera.setOnClickListener {
                requestPermission(Manifest.permission.CAMERA, 2)
            }
        }

        tempImageUri = initTempUri()

        supportActionBar?.hide()
    }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri ?: return@registerForActivityResult
            pickedPhoto = uri
            val message: String = pickedPhoto.toString()
            val photoIntent = Intent(this, NewProjectActivity::class.java)
            photoIntent.putExtra("photo", message)
            startActivity(photoIntent)
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) {
            if(it){
                val message: String = tempImageUri.toString()
                val photoIntent = Intent(this, NewProjectActivity::class.java)
                photoIntent.putExtra("photo", message)
                startActivity(photoIntent)
            }
        }

    private val permissionToGalleryResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                galleryLauncher.launch("image/*")
            }
        }

    private val permissionToCameraResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                cameraLauncher.launch(tempImageUri)
            }
        }

    private fun requestPermission(permission: String, code: Int) {
        if (ContextCompat.checkSelfPermission(
                this,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (code == 1) permissionToGalleryResultLauncher.launch(permission)
            else permissionToCameraResultLauncher.launch(permission)
        } else {
            if (code == 1) galleryLauncher.launch("image/*")
            else cameraLauncher.launch(tempImageUri)
        }
    }

    private fun initTempUri(): Uri {
        val tempImagesDir = File(
            applicationContext.filesDir,
            getString(R.string.temp_images_dir))

        tempImagesDir.mkdir()

        val tempImage = File(
            tempImagesDir,
            getString(R.string.temp_image))

        return FileProvider.getUriForFile(
            applicationContext,
            "com.hits.graphic-editor",
            tempImage)
    }
}