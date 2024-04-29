package com.hits.graphic_editor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding

class NewProjectActivity : AppCompatActivity() {

    private val binding: ActivityNewProjectBinding by lazy {
        ActivityNewProjectBinding.inflate(layoutInflater)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val photo = intent?.getStringExtra("photo")

        binding.imageView.setImageURI(photo?.toUri())
    }
}