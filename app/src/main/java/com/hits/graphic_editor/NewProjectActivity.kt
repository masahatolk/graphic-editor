package com.hits.graphic_editor

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.hits.graphic_editor.custom_api.getSimpleImage
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.BottomMenuBinding
import com.hits.graphic_editor.databinding.ExtraTopMenuBinding
import com.hits.graphic_editor.databinding.TopMenuBinding
import com.hits.graphic_editor.rotation.Rotation
import com.hits.graphic_editor.scaling.Scaling
import com.hits.graphic_editor.color_correction.ColorCorrection
import com.hits.graphic_editor.ui.addBottomMenu
import com.hits.graphic_editor.ui.addExtraTopMenu
import com.hits.graphic_editor.ui.addTopMenu
import com.hits.graphic_editor.ui.removeBottomMenu
import com.hits.graphic_editor.ui.removeTopMenu
import com.hits.graphic_editor.ui.setListenersToExtraTopMenu
import com.hits.graphic_editor.ui.setListenersToTopMenu
import com.hits.graphic_editor.utils.FilterMode
import com.hits.graphic_editor.utils.ProcessedImage
import kotlinx.coroutines.runBlocking


class NewProjectActivity : AppCompatActivity() {

    // ------------ UI elements ------------
    private val binding: ActivityNewProjectBinding by lazy {
        ActivityNewProjectBinding.inflate(layoutInflater)
    }
    val topMenu: TopMenuBinding by lazy {
        TopMenuBinding.inflate(layoutInflater)
    }
    val bottomMenu: BottomMenuBinding by lazy {
        BottomMenuBinding.inflate(layoutInflater)
    }
    val extraTopMenu: ExtraTopMenuBinding by lazy {
        ExtraTopMenuBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        val processedImage = ProcessedImage(getSimpleImage(selectedPhotoBitmap))
        val newScaling = Scaling(binding, layoutInflater, processedImage)
        val newRotation = Rotation(binding, layoutInflater, processedImage)
        val newColorCorrection = ColorCorrection(binding, layoutInflater, processedImage)

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
            newColorCorrection
        )

        // ------------ add listener to bottom menu -------------
        bottomMenu.root.addOnTabSelectedListener(object : OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab) {

                removeTopMenu(binding, topMenu)
                removeBottomMenu(binding, bottomMenu)
                addExtraTopMenu(binding, extraTopMenu)

                processedImage.switchStackMode()
                when (bottomMenu.root.selectedTabPosition) {
                    FilterMode.SCALING.ordinal -> {
                        newScaling.showBottomMenu()
                    }

                    FilterMode.ROTATION.ordinal -> {
                        newRotation.showBottomMenu()
                    }

                    FilterMode.COLOR_CORRECTION.ordinal -> {
                        newColorCorrection.showBottomMenu()
                    }

                    FilterMode.RETOUCH.ordinal -> {

                    }

                    FilterMode.FACE_DETECTION.ordinal -> {

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