package com.hits.graphic_editor

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding


class NewProjectActivity : AppCompatActivity() {

    private val binding: ActivityNewProjectBinding by lazy {
        ActivityNewProjectBinding.inflate(layoutInflater)
    }

    var pickedPhoto: Uri? = null
    var tabLayout: TabLayout? = null
    var tabListener: OnTabSelectedListener? = null
    lateinit var containerView: ViewGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val photo = intent?.getStringExtra("photo")

        pickedPhoto = photo?.toUri()

        binding.imageView.setImageURI(pickedPhoto)

        tabLayout = binding.tabLayout
        containerView.addView(tabLayout, 0)


        tabLayout!!.addOnTabSelectedListener(object : OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tabLayout!!.selectedTabPosition == 0) {

                } else if (tabLayout!!.selectedTabPosition == 1) {

                } else if (tabLayout!!.selectedTabPosition == 2) {

                } else if (tabLayout!!.selectedTabPosition == 3) {

                } else if (tabLayout!!.selectedTabPosition == 4) {

                } else if (tabLayout!!.selectedTabPosition == 5) {

                } else if (tabLayout!!.selectedTabPosition == 6) {

                } else if (tabLayout!!.selectedTabPosition == 7) {

                } else if (tabLayout!!.selectedTabPosition == 8) {

                } else if (tabLayout!!.selectedTabPosition == 9) {

                }
                containerView.removeView(tabLayout)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        supportActionBar?.hide()
    }

    override fun onDestroy() {
        super.onDestroy()

        tabListener?.let { tabLayout?.removeOnTabSelectedListener(it) }
    }

}