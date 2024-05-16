package com.hits.graphic_editor

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hits.graphic_editor.custom_api.SimpleImage
import com.hits.graphic_editor.custom_api.getBitMap
import com.hits.graphic_editor.custom_api.getSimpleImage
import com.hits.graphic_editor.databinding.ActivityMainBinding
import com.hits.graphic_editor.utils.FVec3

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

            val drawable = binding.image1.drawable as BitmapDrawable
            val bitmap = drawable.bitmap.copy(Bitmap.Config.ARGB_8888, true)
            val img = getSimpleImage(bitmap)
            //val mipmaps = MipMapsContainer(img)

            /* ---------------------------------- */
            val scene = Scene(
                SimpleImage(600, 600),
                Cube(img),
                Camera()
            )

        binding.btn.setOnClickListener {
                scene.renderFrame()
                binding.image.setImageBitmap(getBitMap(scene.canvas))
                scene.changeObjectDistance(-0.5F)
                scene.rotateObject(FVec3(0.1F, 0.1F, 0.1F))
            /* ---------------------------------- */
        }
    }
}