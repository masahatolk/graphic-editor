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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
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
        var currAlgorithmJob: Job = Job()
        currAlgorithmJob.cancel()

        binding.btn.setOnClickListener {
            lifecycleScope.launch {
                if (!currAlgorithmJob.isActive) {
                    currAlgorithmJob = CoroutineScope(Dispatchers.Default).launch {
                        bitmap = getBitMap(getScaledSimpleImage(mipMaps, 3.6F, false))
                    }
                    currAlgorithmJob.join()
                    binding.image2.setImageBitmap(bitmap)
                }
            }
        }
    }
}