package com.hits.graphic_editor

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hits.graphic_editor.custom_api.MipMapsContainer
import com.hits.graphic_editor.custom_api.getBitMap
import com.hits.graphic_editor.custom_api.getSimpleImage
import com.hits.graphic_editor.databinding.ActivityMainBinding
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val drawable = binding.image1.drawable as BitmapDrawable
        var bitmap = drawable.bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val mipMaps = MipMapsContainer(getSimpleImage(bitmap))
        binding.btn.setOnClickListener {
            lifecycleScope.launch {
                //mipMaps.suspendInit()
                bitmap = getBitMap(getScaledSimpleImage(mipMaps, 2.6F, true))
            }.invokeOnCompletion {
                binding.image2.setImageBitmap(bitmap)
            }
        }

    }
}