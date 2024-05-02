package com.hits.graphic_editor

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import java.io.InputStream

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


    fun getBitmapFromUri(uri: Uri?): Bitmap? {
    uri?.let{
         val inputStream = contentResolver.openInputStream(uri) ?: return null
         val bitmap = BitmapFactory.decodeStream(inputStream)
         inputStream.close()
         return  bitmap
        }
    return null
    }

}