package com.hits.graphic_editor.spline

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.hits.graphic_editor.custom_api.getBitMap
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.SplineBottomMenuBinding
import com.hits.graphic_editor.utils.ProcessedImage

class Spline(
    val binding: ActivityNewProjectBinding,
    private val layoutInflater: LayoutInflater,
    private val bitmap: Bitmap,
    private val colorPicker: ColorPickerDialog.Builder
) {

    private lateinit var canvas: Canvas
    private lateinit var paint: Paint
    private var x: Float = 0F
    private var y: Float = 0F

    val splineBottomMenu: SplineBottomMenuBinding by lazy {
        SplineBottomMenuBinding.inflate(layoutInflater)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun showBottomMenu() {

        addSplineBottomMenu(binding, splineBottomMenu)

        splineBottomMenu.colorPickerButton.setOnClickListener {colorPicker.show()}

        paint = Paint()
        paint.strokeWidth = 10F

        colorPicker.setColorListener { color, colorHex ->
            paint.color = color
        }

        canvas = Canvas(bitmap)


        binding.imageView.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    x = event.x
                    y = event.y
                    canvas.drawCircle(x, y, 15F, paint)
                    binding.imageView.setImageBitmap(bitmap)
                }
            }
            v?.onTouchEvent(event) ?: true
        }
    }
}