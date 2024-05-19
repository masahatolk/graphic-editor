package com.hits.graphic_editor.rotation

import android.graphics.Bitmap
import android.view.LayoutInflater
import com.hits.graphic_editor.utils.Filter
import com.hits.graphic_editor.custom_api.SimpleImage
import com.hits.graphic_editor.custom_api.getBitMap
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.RotationBottomMenuBinding
import com.hits.graphic_editor.utils.ProcessedImage

class Rotation(
    override val binding: ActivityNewProjectBinding,
    override val layoutInflater: LayoutInflater,
    override val processedImage: ProcessedImage
) : Filter {

    private lateinit var simpleImage : SimpleImage
    private var lastRotatedBitmap: Bitmap? = null
    private var totalRotationAngle: Int = 0

    val rotateButton: RotationBottomMenuBinding by lazy {
        RotationBottomMenuBinding.inflate(layoutInflater)
    }

    private fun showRotateButton() {

        lastRotatedBitmap = null

        rotateButton.rotateButton.setOnClickListener {
            val rotatedBitmap = if (lastRotatedBitmap != null) {
                rotateBitmap90Degrees(lastRotatedBitmap!!)
            } else {
                rotateBitmap90Degrees(getBitMap(simpleImage))
            }
            lastRotatedBitmap = rotatedBitmap
            binding.imageView.setImageBitmap(rotatedBitmap)
            totalRotationAngle += 90
            totalRotationAngle %= 360
        }
    }

    override fun showBottomMenu() {
        addRotateButton(binding, rotateButton)
        showRotateButton()
    }


    private fun rotateBitmap90Degrees(originalBitmap: Bitmap): Bitmap {
        val width = originalBitmap.width
        val height = originalBitmap.height

        val pixels = IntArray(width * height)
        originalBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val rotatedPixels = IntArray(height * width)

        for (x in 0 until width) {
            for (y in 0 until height) {
                val newY = width - 1 - x
                rotatedPixels[newY * height + y] = pixels[y * width + x]
            }
        }

        return Bitmap.createBitmap(rotatedPixels, height, width, originalBitmap.config)
    }
}

