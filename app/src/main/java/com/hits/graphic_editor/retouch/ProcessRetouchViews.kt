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

    val seekBarLayout = bottomMenu.root//retouch.layoutInflater.inflate(R.layout.retouch_bottom_menu, null)

    val retouchCoefSeekBar = seekBarLayout.findViewById<SeekBar>(R.id.retouchCoef)
    val brushSizeSeekBar = seekBarLayout.findViewById<SeekBar>(R.id.brushSize)

    val brushSizeExplanation = seekBarLayout.findViewById<TextView>(R.id.brushSizeExplanation)
    val retouchCoefExplanation = seekBarLayout.findViewById<TextView>(R.id.retouchCoefExplanation)

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

    val rootViewGroup = binding.root as ViewGroup

    rootViewGroup.addView(
        seekBarLayout,
        ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
        }
    )
}