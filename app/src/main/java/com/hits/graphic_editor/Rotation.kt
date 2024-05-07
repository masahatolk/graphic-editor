package com.hits.graphic_editor

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import com.hits.graphic_editor.custom_api.SimpleImage
import com.hits.graphic_editor.custom_api.getBitMap
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.BottomMenuBinding
import com.hits.graphic_editor.databinding.ExtraTopMenuBinding
import com.hits.graphic_editor.databinding.FilterRecyclerViewBinding
import com.hits.graphic_editor.databinding.TopMenuBinding

class Rotation(
    private val simpleImage: SimpleImage,
    private val context: Context,
    private val binding: ActivityNewProjectBinding,
    private val layoutInflater: LayoutInflater
) {

    private var lastRotatedBitmap: Bitmap? = null
    private var totalRotationAngle: Int = 0

    fun showRotateButton() {
        val rotateButton = layoutInflater.inflate(R.layout.rotation_bottom_menu, null)
        rotateButton.findViewById<Button>(R.id.rotateButton).setOnClickListener {
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

        binding.root.addView(
            rotateButton,
            ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomToBottom = binding.imageView.id
                startToStart = binding.imageView.id
                endToEnd = binding.imageView.id
            }
        )
    }

    fun showBottomMenu(topMenu: TopMenuBinding, bottomMenu: BottomMenuBinding) {
        val extraTopMenu: ExtraTopMenuBinding by lazy {
            ExtraTopMenuBinding.inflate(layoutInflater)
        }
        val filterBottomMenu: FilterRecyclerViewBinding by lazy {
            FilterRecyclerViewBinding.inflate(layoutInflater)
        }

        binding.root.addView(
            extraTopMenu.root,
            ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topToTop = binding.root.id
            }
        )
        binding.root.addView(
            filterBottomMenu.root.rootView,
            ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomToBottom = binding.root.id
            }
        )

        extraTopMenu.close.setOnClickListener {

        }

        extraTopMenu.save.setOnClickListener {

        }

        showRotateButton()
    }




    fun rotateBitmap90Degrees(originalBitmap: Bitmap): Bitmap {
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

