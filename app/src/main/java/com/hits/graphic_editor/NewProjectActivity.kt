package com.hits.graphic_editor

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.hits.graphic_editor.custom_api.getSimpleImage
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.BottomMenuBinding
import com.hits.graphic_editor.databinding.ExtraTopMenuBinding
import com.hits.graphic_editor.databinding.TopMenuBinding
import com.hits.graphic_editor.face_detection.FaceDetection
import com.hits.graphic_editor.rotation.Rotation
import com.hits.graphic_editor.scaling.Scaling
import com.hits.graphic_editor.ui.color_correction.ColorCorrection
import org.opencv.android.OpenCVLoader


class NewProjectActivity : AppCompatActivity() {

    private val binding: ActivityNewProjectBinding by lazy {
        ActivityNewProjectBinding.inflate(layoutInflater)
    }

    private var pickedPhoto: Uri? = null
    val topMenu: TopMenuBinding by lazy {
        TopMenuBinding.inflate(layoutInflater)
    }
    val bottomMenu: BottomMenuBinding by lazy {
        BottomMenuBinding.inflate(layoutInflater)
    }
    val extraTopMenu: ExtraTopMenuBinding by lazy {
        ExtraTopMenuBinding.inflate(layoutInflater)
    }
    private var processedImage: ProcessedImage = ProcessedImage()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (OpenCVLoader.initLocal()) {
            Log.i("TEST", "OpenCV loaded successfully")
        } else {
            Log.e("TEST", "OpenCV initialization failed!")
            (Toast.makeText(this, "OpenCV initialization failed!", Toast.LENGTH_LONG)).show()
            return
        }

        setContentView(binding.root)

        // ------------get photo from MainActivity------------
        val photo = intent?.getStringExtra("photo")

        pickedPhoto = photo?.toUri()
        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, pickedPhoto)

        binding.imageView.setImageURI(pickedPhoto)

        // -------------------add main menus-------------------
        addTopMenu(binding, topMenu)
        addBottomMenu(binding, bottomMenu)

        // --------------create necessary fields---------------
        processedImage.image = getSimpleImage(bitmap)
        val newScaling = Scaling(binding, layoutInflater)
        val newRotation = Rotation(binding, layoutInflater)
        val newFaceDetection = FaceDetection(this, binding, layoutInflater)
        val newColorCorrection = ColorCorrection(binding, layoutInflater, newFaceDetection)

        // --------------add listeners to menus----------------
        setListenersToTopMenu(this, binding, this, topMenu, processedImage)
        setListenersToExtraTopMenu(
            binding,
            topMenu,
            bottomMenu,
            extraTopMenu,
            processedImage,
            newScaling,
            newRotation,
            newColorCorrection,
            newFaceDetection
        )

        // ------------add listener to bottom menu-------------
        bottomMenu.root.addOnTabSelectedListener(object : OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab) {

                removeTopMenu(binding, topMenu)
                removeBottomMenu(binding, bottomMenu)
                addExtraTopMenu(binding, extraTopMenu)

                when (bottomMenu.root.selectedTabPosition) {
                    FilterMode.SCALING.ordinal -> {
                        newScaling.simpleImage = processedImage.image
                        newScaling.showBottomMenu()
                    }

                    FilterMode.ROTATION.ordinal -> {
                        newRotation.simpleImage = processedImage.image
                        newRotation.showBottomMenu()
                    }

                    FilterMode.COLOR_CORRECTION.ordinal -> {
                        newColorCorrection.simpleImage = processedImage.image
                        newColorCorrection.showBottomMenu()
                    }

                    FilterMode.RETOUCH.ordinal -> {

                    }

                    FilterMode.SPLINE.ordinal -> {

                    }

                    FilterMode.AFFINE_TRANSFORMATION.ordinal -> {

                    }

                    FilterMode.UNSHARP_MASKING.ordinal -> {

                    }

                    FilterMode.CUBE.ordinal -> {

                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        supportActionBar?.hide()
    }
}