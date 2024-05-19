package com.hits.graphic_editor

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.hits.graphic_editor.color_correction.ColorCorrection
import com.hits.graphic_editor.custom_api.getSimpleImage
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.BottomMenuBinding
import com.hits.graphic_editor.databinding.ExtraTopMenuBinding
import com.hits.graphic_editor.databinding.TopMenuBinding
import com.hits.graphic_editor.face_detection.FaceDetection
import com.hits.graphic_editor.rotation.Rotation
import com.hits.graphic_editor.scaling.Scaling
import com.hits.graphic_editor.ui.addBottomMenu
import com.hits.graphic_editor.ui.addExtraTopMenu
import com.hits.graphic_editor.ui.addTopMenu
import com.hits.graphic_editor.ui.removeBottomMenu
import com.hits.graphic_editor.ui.removeTopMenu
import com.hits.graphic_editor.ui.setListenersToExtraTopMenu
import com.hits.graphic_editor.ui.setListenersToTopMenu
import com.hits.graphic_editor.utils.ColorCorrectionMode
import com.hits.graphic_editor.utils.ProcessedImage
import kotlinx.coroutines.runBlocking
import org.opencv.android.OpenCVLoader

class NewProjectActivity : AppCompatActivity() {

    // ------------ UI elements ------------
    private val binding: ActivityNewProjectBinding by lazy {
        ActivityNewProjectBinding.inflate(layoutInflater)
    }
    private val topMenu: TopMenuBinding by lazy {
        TopMenuBinding.inflate(layoutInflater)
    }
    private val bottomMenu: BottomMenuBinding by lazy {
        BottomMenuBinding.inflate(layoutInflater)
    }
    private val extraTopMenu: ExtraTopMenuBinding by lazy {
        ExtraTopMenuBinding.inflate(layoutInflater)
    }
    private lateinit var processedImage: ProcessedImage

    private lateinit var newScaling: Scaling
    private lateinit var newFaceDetection: FaceDetection
    private lateinit var newColorCorrection: ColorCorrection
    private lateinit var newRotation: Rotation



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

        // ------------ get photo from MainActivity ------------
        val photo = intent.getStringExtra("photo")
        val selectedPhotoUri = photo!!.toUri()
        val selectedPhotoBitmap: Bitmap

        runBlocking {
            selectedPhotoBitmap = ImageDecoder.decodeBitmap(
                ImageDecoder.createSource(applicationContext.contentResolver, selectedPhotoUri)
            ) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = true
            }
        }

        binding.imageView.setImageURI(selectedPhotoUri)

        // ------------------- add main menus -------------------
        addTopMenu(binding, topMenu)
        addBottomMenu(binding, bottomMenu)

        // -------------- create necessary fields ---------------
        processedImage = ProcessedImage(getSimpleImage(selectedPhotoBitmap))
        newScaling = Scaling(binding, layoutInflater, processedImage)
        newRotation = Rotation(binding, layoutInflater, processedImage)
        newFaceDetection = FaceDetection(this, binding, layoutInflater)
        newColorCorrection = ColorCorrection(binding, layoutInflater, processedImage, newFaceDetection)

        // -------------- add listeners to top menus ----------------
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

        // ------------ add listener to bottom menu -------------
        bottomMenu.root.addOnTabSelectedListener(object : OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab) {
                onTabSelectedProcess()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {
                onTabSelectedProcess()
            }
        })

        supportActionBar?.hide()
    }

    fun onTabSelectedProcess () {
        removeTopMenu(binding, topMenu)
        removeBottomMenu(binding, bottomMenu)
        addExtraTopMenu(binding, extraTopMenu)

                processedImage.switchStackMode()
                when (bottomMenu.root.selectedTabPosition) {
                    ColorCorrectionMode.SCALING.ordinal -> {
                        newScaling.showBottomMenu()
                    }

                    ColorCorrectionMode.ROTATION.ordinal -> {
                        newRotation.showBottomMenu()
                    }

                    ColorCorrectionMode.COLOR_CORRECTION.ordinal -> {
                        newColorCorrection.showBottomMenu()
                    }

                    ColorCorrectionMode.RETOUCH.ordinal -> {

                    }

                    ColorCorrectionMode.SPLINE.ordinal -> {

                    }

                    ColorCorrectionMode.AFFINE_TRANSFORMATION.ordinal -> {

                    }

                    ColorCorrectionMode.UNSHARP_MASKING.ordinal -> {

                    }

                    ColorCorrectionMode.CUBE.ordinal -> {

            }
        }
    }
}