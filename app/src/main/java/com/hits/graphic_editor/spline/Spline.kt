package com.hits.graphic_editor.spline

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.SplineBottomMenuBinding
import com.hits.graphic_editor.utils.ProcessedImage


val height = Resources.getSystem().displayMetrics.heightPixels
val width = Resources.getSystem().displayMetrics.widthPixels


class Spline(
    val binding: ActivityNewProjectBinding,
    private val layoutInflater: LayoutInflater,
    private val processedImage: ProcessedImage,
    private val colorPicker: ColorPickerDialog.Builder
) {

    private lateinit var canvas: Canvas
    private lateinit var resultCanvas: Canvas
    private var bitmap: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    private var defaultBitmap: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    private var resultBitmap: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    private var paint: Paint = Paint().apply {
        strokeWidth = 2F
    }
    private var paths: MutableList<MutableList<Point>> = mutableListOf()
    private var extraPointsList: MutableList<MutableList<Point>> = mutableListOf()

    private var middles: MutableList<Point> = mutableListOf()

    private var movingPathIndex: Int = -1
    private var movingPointIndex: Int = -1

    private var splineMode = false
    private var moveMode = false

    val splineBottomMenu: SplineBottomMenuBinding by lazy {
        SplineBottomMenuBinding.inflate(layoutInflater)
    }

    fun showBottomMenu() {

        addSplineBottomMenu(binding, splineBottomMenu)

        canvas = Canvas(bitmap)
        resultCanvas = Canvas(resultBitmap)

        setListeners()
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setListeners() {
        splineBottomMenu.splineModeButton.setOnClickListener {
            splineMode = !splineMode
            if(splineMode){
                paths.add(mutableListOf())
                extraPointsList.add(mutableListOf())
                middles.clear()
            }
            else defaultBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        }
        splineBottomMenu.polygonModeButton.setOnClickListener {}
        splineBottomMenu.deleteButton.setOnClickListener {}
        splineBottomMenu.colorPickerButton.setOnClickListener { colorPicker.show() }

        colorPicker.setColorListener { color, colorHex ->
            paint.color = color
        }

        binding.extraImageView.setOnTouchListener(CustomTouchListener())
    }

    inner class CustomTouchListener : OnTouchListener{
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (splineMode) {

                        movingPathIndex = checkForMatchPoint(event.x, event.y)
                        if (movingPathIndex == -1) {
                            moveMode = false
                            movingPointIndex = -1

                            val path = paths.last()
                            val extraPoints = extraPointsList.last()

                            path.add(Point(event.x.toInt(), event.y.toInt()))
                            if (path.size > 1) {
                                val x = calculateMiddle(path.last().x, path[path.lastIndex - 1].x)
                                val y = calculateMiddle(path.last().y, path[path.lastIndex - 1].y)

                                middles.add(Point(x, y))

                                if (path.size > 2) {
                                    val prev = path[path.lastIndex - 2]
                                    val curr = path[path.lastIndex - 1]
                                    val next = path.last()

                                    val leftLength: Float = calculateLength(prev, curr)
                                    val rightLength: Float = calculateLength(curr, next)

                                    val diffPoint = calculateExtraPoints(
                                        path[path.lastIndex - 1],
                                        middles[middles.lastIndex - 1],
                                        middles.last(),
                                        leftLength / rightLength
                                    )
                                    extraPoints.add(
                                        Point(
                                            middles[middles.lastIndex - 1].x + diffPoint.x,
                                            middles[middles.lastIndex - 1].y + diffPoint.y
                                        )
                                    )
                                    extraPoints.add(
                                        Point(
                                            middles.last().x + diffPoint.x,
                                            middles.last().y + diffPoint.y
                                        )
                                    )
                                }
                            }

                            draw(canvas, resultCanvas, path, extraPoints, paint)
                            drawByDefault(canvas, defaultBitmap, paint)

                            binding.extraImageView.setImageBitmap(bitmap)


                        }
                        else moveMode = true
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    if (splineMode && moveMode) {
                        paths[movingPathIndex][movingPointIndex] =
                            Point(event.x.toInt(), event.y.toInt())
                        calculateFieldsForMovingPoint(
                            paths[movingPathIndex],
                            extraPointsList[movingPathIndex],
                            movingPointIndex
                        )

                        draw(canvas, resultCanvas, paths[movingPathIndex], extraPointsList[movingPathIndex], paint)
                        drawByDefault(canvas, defaultBitmap, paint)

                        binding.extraImageView.setImageBitmap(bitmap)
                    }
                }
            }
            return true
        }
    }

    private fun checkForMatchPoint(x: Float, y: Float): Int {
        for (i in 0 until paths.size) {
            for (j in 0 until paths[i].size) {
                if (paths[i][j].x + pointRadius * 2 >= x && x >= paths[i][j].x - pointRadius * 2 &&
                    paths[i][j].y + pointRadius * 2 >= y && y >= paths[i][j].y - pointRadius * 2
                ) {
                    movingPointIndex = j
                    return i
                }
            }
        }
        return -1
    }
}