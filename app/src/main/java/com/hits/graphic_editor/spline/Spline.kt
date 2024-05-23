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
import android.widget.RadioGroup
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.PolygonSplineChipBinding
import com.hits.graphic_editor.databinding.SplineBottomMenuBinding
import com.hits.graphic_editor.utils.ProcessedImage

enum class SplineMode {
    SPLINE,
    POLYGON,
    DELETE
}

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

    private var currPointIndex: Int = -1

    private var splineMode: SplineMode = SplineMode.SPLINE
    private var prevMode: SplineMode = SplineMode.SPLINE

    private var movingMode = false
    private var antialiasingMode = false
    private var polygonMode = false

    val splineBottomMenu: SplineBottomMenuBinding by lazy {
        SplineBottomMenuBinding.inflate(layoutInflater)
    }
    val polygonSplineChip: PolygonSplineChipBinding by lazy {
        PolygonSplineChipBinding.inflate(layoutInflater)
    }

    fun showBottomMenu() {
        addSplineBottomMenu(binding, splineBottomMenu)

        canvas = Canvas(bitmap)
        resultCanvas = Canvas(resultBitmap)

        setListeners()
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setListeners() {
        splineBottomMenu.SplineGroup.setOnCheckedChangeListener(OnCheckListener())

        splineBottomMenu.newSpline.setOnClickListener {
            if (prevMode == SplineMode.POLYGON) removePolygonChip(binding, polygonSplineChip)
            defaultBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

            paths.add(mutableListOf())
            extraPointsList.add(mutableListOf())
            middles.clear()
        }

        splineBottomMenu.antialiasingChip.setOnClickListener {
            antialiasingMode = !antialiasingMode
            if (paths.size != 0 && extraPointsList.size != 0) {
                draw(
                    canvas,
                    resultCanvas,
                    paths.last(),
                    extraPointsList.last(),
                    paint,
                    splineMode,
                    antialiasingMode,
                    polygonMode
                )
                drawByDefault(canvas, defaultBitmap, paint)

                binding.extraImageView.setImageBitmap(bitmap)
            }
        }

        splineBottomMenu.colorPickerButton.setOnClickListener { colorPicker.show() }

        colorPicker.setColorListener { color, colorHex ->
            paint.color = color
        }

        binding.extraImageView.setOnTouchListener(CustomTouchListener())
    }

    inner class CustomTouchListener : OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            if (!antialiasingMode) {
                when (event?.action) {

                    MotionEvent.ACTION_DOWN -> {

                        if (splineMode == SplineMode.SPLINE || splineMode == SplineMode.POLYGON) {
                            if (paths.size != 0 && !checkForMatchPoint(event.x, event.y)) {

                                movingMode = false
                                currPointIndex = -1

                                val path = paths.last()
                                val extraPoints = extraPointsList.last()

                                path.add(Point(event.x.toInt(), event.y.toInt()))
                                if (path.size > 1) {

                                    middles.add(calculateMiddlePoint(path.last(), path[path.lastIndex - 1]))

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

                                draw(
                                    canvas,
                                    resultCanvas,
                                    path,
                                    extraPoints,
                                    paint,
                                    splineMode,
                                    antialiasingMode,
                                    polygonMode
                                )
                                drawByDefault(canvas, defaultBitmap, paint)

                                binding.extraImageView.setImageBitmap(bitmap)


                            } else movingMode = true

                        } else if (splineMode == SplineMode.POLYGON) {


                        } else if (splineMode == SplineMode.DELETE) {

                            if (paths.size != 0 && checkForMatchPoint(event.x, event.y)) {
                                if (currPointIndex != 0 && currPointIndex != paths.last().lastIndex) {
                                    extraPointsList.last().removeAt((currPointIndex - 1) * 2)
                                    extraPointsList.last().removeAt((currPointIndex - 1) * 2)
                                }
                                if (currPointIndex == 0 && paths.last().size > 2) {
                                    extraPointsList.last().removeFirst()
                                    extraPointsList.last().removeFirst()
                                }
                                if (currPointIndex == paths.last().lastIndex && paths.last().size > 2) {
                                    extraPointsList.last().removeLast()
                                    extraPointsList.last().removeLast()
                                }
                                paths.last().removeAt(currPointIndex)
                            } else currPointIndex = -1

                            if (paths.size != 0) {
                                draw(
                                    canvas,
                                    resultCanvas,
                                    paths.last(),
                                    extraPointsList.last(),
                                    paint,
                                    splineMode,
                                    antialiasingMode,
                                    polygonMode
                                )
                                drawByDefault(canvas, defaultBitmap, paint)

                                binding.extraImageView.setImageBitmap(bitmap)
                            }
                        }

                    }

                    MotionEvent.ACTION_MOVE -> {
                        if (splineMode == SplineMode.SPLINE && movingMode && paths.size != 0) {
                            paths.last()[currPointIndex] =
                                Point(event.x.toInt(), event.y.toInt())
                            calculateFieldsForMovingPoint(
                                paths.last(),
                                extraPointsList.last(),
                                currPointIndex
                            )

                            draw(
                                canvas,
                                resultCanvas,
                                paths.last(),
                                extraPointsList.last(),
                                paint,
                                splineMode,
                                antialiasingMode,
                                polygonMode
                            )
                            drawByDefault(canvas, defaultBitmap, paint)

                            binding.extraImageView.setImageBitmap(bitmap)
                        }
                    }
                }
            }

            return true
        }
    }

    private fun checkForMatchPoint(x: Float, y: Float): Boolean {
        for (j in 0 until paths.last().size) {
            if (paths.last()[j].x + pointRadius * 2 >= x && x >= paths.last()[j].x - pointRadius * 2 &&
                paths.last()[j].y + pointRadius * 2 >= y && y >= paths.last()[j].y - pointRadius * 2
            ) {
                currPointIndex = j
                return true
            }
        }
        return false
    }

    inner class OnCheckListener : RadioGroup.OnCheckedChangeListener {
        override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
            val id =
                splineBottomMenu.SplineGroup.checkedRadioButtonId
            val radioButton = splineBottomMenu.SplineGroup.findViewById<View>(id)
            val index = splineBottomMenu.SplineGroup.indexOfChild(radioButton)
            prevMode = SplineMode.entries.toTypedArray()[splineMode.ordinal]
            splineMode = SplineMode.entries.toTypedArray()[index]

            removePolygonChip(binding, polygonSplineChip)

            if (splineMode == SplineMode.SPLINE) {

            }

            if (splineMode == SplineMode.POLYGON) {
                addPolygonChip(binding, polygonSplineChip)

                polygonSplineChip.polygonChip.setOnClickListener {
                    polygonMode = !polygonMode
                    if (polygonMode && paths.size != 0) {

                        val path = paths.last()

                        if (path.size > 2) {
                            val middle = calculateMiddlePoint(path.first(), path.last())
                            middles.add(middle)

                            addExtraPoints(path.lastIndex - 1, path.lastIndex, 0)
                            addExtraPoints(path.lastIndex, 0, 1)
                        }

                        draw(
                            canvas,
                            resultCanvas,
                            paths.last(),
                            extraPointsList.last(),
                            paint,
                            SplineMode.SPLINE,
                            antialiasingMode,
                            polygonMode
                        )
                        drawByDefault(canvas, defaultBitmap, paint)

                        binding.extraImageView.setImageBitmap(bitmap)
                    }

                }

                if (prevMode == SplineMode.SPLINE && splineMode == SplineMode.DELETE) {
                    //if (prevMode == SplineMode.POLYGON) removePolygonChip(binding, polygonSplineChip)
                }
            }
        }
    }

    fun addExtraPoints(prevIndex: Int, currIndex: Int, nextIndex: Int) {

        val prev = paths.last()[prevIndex]
        val curr = paths.last()[currIndex]
        val next = paths.last()[nextIndex]

        val leftLength: Float = calculateLength(prev, curr)
        val rightLength: Float = calculateLength(curr, next)

        val diffPoint = calculateExtraPoints(
            paths.last()[currIndex],
            middles[getIndex(currIndex)],
            middles[currIndex],
            leftLength / rightLength
        )
        extraPointsList.last().add(
            Point(
                middles[getIndex(currIndex)].x + diffPoint.x,
                middles[getIndex(currIndex)].y + diffPoint.y
            )
        )
        extraPointsList.last().add(
            Point(
                middles[currIndex].x + diffPoint.x,
                middles[currIndex].y + diffPoint.y
            )
        )
    }

    fun getIndex (index: Int) : Int {
        return if(index == 0) middles.lastIndex
        else index - 1
    }
}