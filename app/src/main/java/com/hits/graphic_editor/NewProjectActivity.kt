package com.hits.graphic_editor

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.net.toUri
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.BottomMenuBinding
import com.hits.graphic_editor.databinding.TopMenuBinding
import com.hits.graphic_editor.ui.filter.Filter
import com.hits.graphic_editor.ui.filter.Retouch


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val photo = intent?.getStringExtra("photo")

        pickedPhoto = photo?.toUri()
        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, pickedPhoto);

        binding.imageView.setImageURI(pickedPhoto)

        binding.root.addView(
            topMenu.root,
            ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topToTop = binding.root.id
            }
        )
        binding.root.addView(
            bottomMenu.root.rootView,
            ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomToBottom = binding.root.id
            }
        )

        bottomMenu.root.addOnTabSelectedListener(object : OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab) {

                binding.root.removeView(topMenu.root)
                binding.root.removeView(bottomMenu.root)

                /*GlobalScope.launch {
                    //delay(1000L)
                    withContext(Dispatchers.Main) {
                        binding.root.addView(
                            topMenu.root,
                            ConstraintLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            ).apply {
                                topToTop = binding.root.id
                            }
                        )

                        binding.root.addView(
                            bottomMenu.root,
                            ConstraintLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            ).apply {
                                bottomToBottom = binding.root.id
                            }
                        )
                    }
                }*/

                val image = getSimpleImage(bitmap)

                when (bottomMenu.root.selectedTabPosition) {
                    0 -> {

                    }

                    1 -> {

                    }

                    2 -> {

                    }

                    3 -> {
                        val newFilter = Filter(image, applicationContext, binding, layoutInflater)
                        newFilter.showBottomMenu(
                            topMenu = topMenu,
                            bottomMenu = bottomMenu,
                        )
                    }

                    4 -> {
                        val retouch = Retouch(image, applicationContext, binding, layoutInflater)
                        retouch.showBottomMenu(
                            topMenu = topMenu,
                            bottomMenu = bottomMenu
                        )

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