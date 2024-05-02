package com.hits.graphic_editor

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.google.android.material.tabs.TabLayout
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding

class NewProjectActivity : AppCompatActivity() {

    private val binding: ActivityNewProjectBinding by lazy {
        ActivityNewProjectBinding.inflate(layoutInflater)
    }

    var pickedPhoto: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val photo = intent?.getStringExtra("photo")

        pickedPhoto = photo?.toUri()

        binding.imageView.setImageURI(pickedPhoto)

        supportActionBar?.hide()
    }

}