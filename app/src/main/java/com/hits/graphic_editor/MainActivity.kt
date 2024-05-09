package com.hits.graphic_editor

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hits.graphic_editor.custom_api.MipMapsContainer
import com.hits.graphic_editor.custom_api.getBitMap
import com.hits.graphic_editor.custom_api.getSimpleImage
import com.hits.graphic_editor.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btn.setOnClickListener {
            val drawable = binding.image1.drawable as BitmapDrawable
            var bitmap = drawable.bitmap.copy(Bitmap.Config.ARGB_8888, true)
            /* ---------------------------------- */

            val img = getSimpleImage(bitmap)
            val mipmaps = MipMapsContainer(img)
            bitmap = getBitMap(getAffineTransformedSimpleImage(mipmaps,
                getAffineTransformationMatrix(
                    PointTransfer(30F,30F,30F,70F),
                    PointTransfer(55F,50F,155F,90F),
                    PointTransfer(75F,70F,175F,110F)
                )
                /*arrayOf(
                    arrayOf(0.15F, 0F, 0F),
                    arrayOf(0F, 0.15F, 0F)
                )*/
            ))
            //bitmap = getBitMap(getRotatedSimpleImage(mipmaps, (PI/6).toFloat()))

            /* ---------------------------------- */
            binding.image2.setImageBitmap(bitmap)
        }
    }
}