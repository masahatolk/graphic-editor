package com.hits.graphic_editor.rotation

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.TextPaint
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View


/**
 * Slider View for Rotation selection.
 */
class RotationSlider(context: Context?) : View(context) {
    private var _value: Double = 0.0

    private var textPaint = TextPaint()
    private var textWidth: Float = 0f
    private var textHeight: Float = 0f
    private var gestureDetector: GestureDetector = GestureDetector(context, MyGestureListener())
    var paddingLeft: Float = 0F
    var paddingTop: Float = 0F
    var paddingRight: Float = 0F
    var paddingBottom: Float = 0F
    val scrollX: Float = 0F
    val scrollY: Float = 0F

    /**
     * Rotation value
     */
    var value: Double
        get() = _value
        set(value) {
            _value = value
            invalidateTextPaintAndMeasurements()
        }

    private fun invalidateTextPaintAndMeasurements() {
        textPaint.let {
            it.textSize = 40f
            it.color = Color.GRAY
            textWidth = it.measureText(_value.toString())
            textHeight = it.fontMetrics.bottom
        }
    }

    private val paint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.YELLOW
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        paddingLeft = paddingLeft
        paddingTop = paddingTop
        paddingRight = paddingRight
        paddingBottom = paddingBottom

        val contentWidth = width - paddingLeft - paddingRight
        val contentHeight = height - paddingTop - paddingBottom

        /*canvas.drawRect(
            paddingLeft.toFloat(),
            paddingTop.toFloat(),
            width - paddingRight.toFloat(),
            height - paddingBottom.toFloat(),
            paint
        )*/
        draw(canvas, paddingLeft, paddingRight)

        /*canvas.drawText(
            value.toString(),
            paddingLeft + (contentWidth - textWidth) / 2,
            paddingTop + (contentHeight + textHeight) / 2,
            textPaint
        )*/
    }

    fun draw(canvas: Canvas, x: Float, y: Float) {

        for (i in 0..50) {
            canvas.drawRoundRect(
                paddingLeft + x + i * 20,
                paddingTop,
                paddingLeft + x + i * 20 + 3,
                height - (paddingBottom + x),
                10F,
                10F,
                paint
            )
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return if (event?.let { gestureDetector.onTouchEvent(it) } == true) true else true
    }


    private class MyGestureListener() : SimpleOnGestureListener() {

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            return super.onScroll(e1, e2, distanceX, distanceY)
        }
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = 1000
        val height = 100
        setMeasuredDimension(width, height)
    }
}