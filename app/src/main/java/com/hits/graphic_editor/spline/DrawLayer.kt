package com.hits.graphic_editor.spline

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View

class DrawLayer (context: Context) : View(context) {

    lateinit var paint: Paint

    override fun onDraw(canvas: Canvas) {

    }

    override fun setOnClickListener(l: OnClickListener?) {
        super.setOnClickListener(l)
    }
}