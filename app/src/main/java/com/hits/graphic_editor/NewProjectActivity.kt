package com.hits.graphic_editor

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.hits.graphic_editor.custom_api.getSimpleImage
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.BottomMenuBinding
import com.hits.graphic_editor.databinding.ExtraTopMenuBinding
import com.hits.graphic_editor.databinding.TopMenuBinding
import com.hits.graphic_editor.ui.filter.Filter
import com.hits.graphic_editor.ui.filter.RGBMode


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        val image = getSimpleImage(bitmap)
        val newFilter = Filter(image, binding, layoutInflater, RGBMode.RED)


        // ------------add listener to bottom menu-------------
        bottomMenu.root.addOnTabSelectedListener(object : OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab) {

                removeTopMenu(binding, topMenu)
                removeBottomMenu(binding, bottomMenu)
                addExtraTopMenu(binding, extraTopMenu)

                when (bottomMenu.root.selectedTabPosition) {
                    0 -> {

                    }

                    1 -> {
                        val rotation = Rotation(image,applicationContext,binding,layoutInflater)
                        rotation.showBottomMenu(
                            topMenu = topMenu,
                            bottomMenu = bottomMenu,
                            )
                    }

                    2 -> {

                    }

                    3 -> {
                        newFilter.showBottomMenu(
                            topMenu = topMenu,
                            bottomMenu = bottomMenu,
                            extraTopMenu = extraTopMenu
                        )
                    }

                    4 -> {

                    }

                    5 -> {

                    }

                    6 -> {

                    }

                    7 -> {

                    }

                    8 -> {

                    }

                    9 -> {

                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        supportActionBar?.hide()
    }
}