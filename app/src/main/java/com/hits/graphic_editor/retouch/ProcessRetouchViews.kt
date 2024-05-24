package com.hits.graphic_editor.retouch

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.hits.graphic_editor.R
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.ExtraTopMenuBinding
import com.hits.graphic_editor.databinding.RetouchBottomMenuBinding
import com.hits.graphic_editor.databinding.ScalingBottomMenuBinding


@SuppressLint("ClickableViewAccessibility")
fun showBottomMenu(binding: ActivityNewProjectBinding, retouch: Retouch, bottomMenu: RetouchBottomMenuBinding) {
    showSeekBarLayout(binding, retouch, bottomMenu)

    binding.imageView.setOnTouchListener { v, event ->
        if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
            val imageView = binding.imageView

            val touchX = event.x
            val touchY = event.y
            val imageCoords = retouch.getImageCoordinates(imageView, touchX, touchY)
            val imageX = imageCoords.x.toInt()
            val imageY = imageCoords.y.toInt()

            if (imageX in 0 until retouch.imageBitmap.width && imageY in 0 until retouch.imageBitmap.height) {
                retouch.imageBitmap = retouch.applyRetouchToBitmap(retouch.imageBitmap, imageX, imageY)
                binding.imageView.setImageBitmap(retouch.imageBitmap)
            }
        }
        true
    }
}

fun showSeekBarLayout(binding: ActivityNewProjectBinding, retouch: Retouch, bottomMenu: RetouchBottomMenuBinding) {
    val retouchCoefSeekBar = bottomMenu.retouchCoef
    val brushSizeSeekBar = bottomMenu.brushSize

    val brushSizeExplanation = bottomMenu.brushSizeExplanation
    val retouchCoefExplanation = bottomMenu.retouchCoefExplanation

    retouchCoefSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            retouch.updateRetouchCoefficient(progress)
            retouchCoefExplanation.text = "Коэффициент ретуширования: ${progress / 10f}"
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}

        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    })

    brushSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            retouch.updateBrushSize(progress)
            brushSizeExplanation.text = "Размер кисти: $progress"
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}

        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    })

    binding.root.addView(
        bottomMenu.root.rootView,
        ConstraintLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomToBottom = binding.root.id
            leftToLeft = binding.root.id
            rightToRight = binding.root.id
        }
    )
}