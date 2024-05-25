package com.hits.graphic_editor.spline

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.RadioGroup
import android.widget.Toast
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.SplineBottomSheetBinding
import com.hits.graphic_editor.databinding.SplineMenuButtonBinding
import com.hits.graphic_editor.utils.Filter
import com.hits.graphic_editor.utils.ProcessedImage

enum class SplineMode {
    SPLINE,
    POLYGON,
    DELETE
}

val height = Resources.getSystem().displayMetrics.heightPixels
val width = Resources.getSystem().displayMetrics.widthPixels


class Spline(
    override val binding: ActivityNewProjectBinding,
    override val layoutInflater: LayoutInflater,
    override val processedImage: ProcessedImage,
    private val colorPicker: ColorPickerDialog.Builder
) : Filter {

    private lateinit var canvas: Canvas
    private var bitmap: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    private var defaultBitmap: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    private var paint: Paint = Paint().apply {
        strokeWidth = 2F
    }
    private var paths: MutableList<MutableList<Point>> = mutableListOf()
    private var extraPointsList: MutableList<MutableList<Point>> = mutableListOf()

    private var middles: MutableList<Point> = mutableListOf()

    private var currPointIndex: Int = -1

    private var splineMode: SplineMode = SplineMode.SPLINE
    private var prevSplineMode: SplineMode = SplineMode.SPLINE
    private var prevPrevSplineMode: SplineMode = SplineMode.SPLINE

    private var movingMode = false
    private var antialiasingMode = false

    private val splineMenuButton: SplineMenuButtonBinding by lazy {
        SplineMenuButtonBinding.inflate(layoutInflater)
    }
    val splineBottomMenu: SplineBottomSheetBinding by lazy {
        SplineBottomSheetBinding.inflate(layoutInflater)
    }
    private var dialog: BottomSheetDialog = BottomSheetDialog(binding.root.context)

    override fun onStart() {
        addSplineMenuButton(binding, splineMenuButton)

        canvas = Canvas(bitmap)

        setListeners()
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setListeners() {
        dialog.setContentView(splineBottomMenu.root)

        splineMenuButton.button.setOnClickListener {
            dialog.show()
        }

        splineBottomMenu.SplineGroup.setOnCheckedChangeListener(OnCheckListener())

        splineBottomMenu.newSpline.setOnClickListener {
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
                    paths.last(),
                    extraPointsList.last(),
                    paint,
                    antialiasingMode,
                    splineMode,
                    prevSplineMode
                )
                drawByDefault(canvas, defaultBitmap, paint)

                binding.superExtraImageView.setImageBitmap(bitmap)
            }
        }

        splineBottomMenu.colorPickerButton.setOnClickListener { colorPicker.show() }

        colorPicker.setColorListener { color, colorHex ->
            paint.color = color
        }

        binding.superExtraImageView.setOnTouchListener(CustomTouchListener())
    }

    inner class CustomTouchListener : OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            if (!antialiasingMode) {
                when (event?.action) {

                    MotionEvent.ACTION_DOWN -> {

                        if (splineMode != SplineMode.DELETE) {
                            if (paths.size != 0) {

                                if (!checkForMatchPoint(
                                        event.x,
                                        event.y
                                    ) && paths.last().size < 10
                                ) {

                                    movingMode = false
                                    currPointIndex = -1

                                    val path = paths.last()

                                    path.add(Point(event.x.toInt(), event.y.toInt()))
                                    if (path.size > 1) {

                                        middles.add(
                                            calculateMiddlePoint(
                                                path.last(),
                                                path[path.lastIndex - 1]
                                            )
                                        )

                                        if (path.size > 2) {
                                            if ((path.size == 3 && splineMode == SplineMode.POLYGON) || splineMode != SplineMode.POLYGON)
                                                addExtraPoints(
                                                    path.lastIndex - 2,
                                                    path.lastIndex - 1,
                                                    path.lastIndex
                                                )

                                            if (splineMode == SplineMode.POLYGON && path.size >= 3) {
                                                if (path.size > 3)
                                                    middles.add(
                                                        calculateMiddlePoint(
                                                            path[path.lastIndex - 1],
                                                            path.last()
                                                        )
                                                    )
                                                middles.add(
                                                    calculateMiddlePoint(
                                                        path.first(),
                                                        path.last()
                                                    )
                                                )

                                                addExtraPoints(
                                                    path.lastIndex - 1,
                                                    path.lastIndex,
                                                    0
                                                )
                                                if (path.size == 3) addExtraPoints(
                                                    path.lastIndex,
                                                    0,
                                                    1
                                                )
                                            }
                                        }
                                    }

                                    draw(
                                        canvas,
                                        path,
                                        extraPointsList.last(),
                                        paint,
                                        antialiasingMode,
                                        splineMode,
                                        prevSplineMode
                                    )
                                    drawByDefault(canvas, defaultBitmap, paint)

                                    binding.superExtraImageView.setImageBitmap(bitmap)
                                } else movingMode = true
                                if (paths.last().size >= 10 && !movingMode) Toast.makeText(
                                    binding.root.context,
                                    "you can't put more than 10 points!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {

                            if (paths.size != 0) {

                                if (checkForMatchPoint(event.x, event.y)) {
                                    if (currPointIndex != 0 && currPointIndex != paths.last().lastIndex) {
                                        removeExtraPoints((currPointIndex - 1) * 2)
                                    }
                                    if (currPointIndex == 0 && prevSplineMode != SplineMode.POLYGON && paths.last().size > 2) {
                                        removeExtraPoints(0)
                                    } else if ((currPointIndex == paths.last().lastIndex || currPointIndex == 0) && paths.last().size > 2) {

                                        if (currPointIndex == paths.last().lastIndex) {
                                            if (prevSplineMode == SplineMode.SPLINE) removeExtraPoints(
                                                (paths.last().lastIndex - 1) * 2 - 2
                                            )
                                            else removeExtraPoints((paths.last().lastIndex - 1) * 2 + 1)
                                        } else {
                                            removeExtraPoints(extraPointsList.last().lastIndex - 1)
                                        }
                                    }
                                    paths.last().removeAt(currPointIndex)
                                    if (currPointIndex == 0 && prevSplineMode == SplineMode.POLYGON && paths.last().size != 0) {
                                        val path = paths.last().toMutableList()

                                        path[0] = paths.last().last()
                                        for (i in 0 until paths.last().size - 1) {
                                            path[i + 1] = paths.last()[i]
                                        }
                                        paths[paths.lastIndex] = path
                                    }
                                    if (paths.last().size < 3) {
                                        if (paths.last().size < 2) middles.clear()
                                        else {
                                            for (i in 1 until middles.size)
                                                middles.removeAt(1)
                                        }
                                        extraPointsList.last().clear()
                                    }
                                }

                                draw(
                                    canvas,
                                    paths.last(),
                                    extraPointsList.last(),
                                    paint,
                                    antialiasingMode,
                                    splineMode,
                                    prevSplineMode
                                )
                                drawByDefault(canvas, defaultBitmap, paint)

                                binding.superExtraImageView.setImageBitmap(bitmap)
                            } else currPointIndex = -1
                        }
                    }

                    MotionEvent.ACTION_MOVE -> {
                        if (splineMode != SplineMode.DELETE && movingMode && paths.size != 0) {
                            paths.last()[currPointIndex] =
                                Point(event.x.toInt(), event.y.toInt())
                            calculateFieldsForMovingPoint(
                                paths.last(),
                                extraPointsList.last(),
                                currPointIndex,
                                splineMode
                            )

                            draw(
                                canvas,
                                paths.last(),
                                extraPointsList.last(),
                                paint,
                                antialiasingMode,
                                splineMode,
                                prevSplineMode
                            )
                            drawByDefault(canvas, defaultBitmap, paint)

                            binding.superExtraImageView.setImageBitmap(bitmap)
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
            prevPrevSplineMode = SplineMode.entries.toTypedArray()[prevSplineMode.ordinal]
            prevSplineMode = SplineMode.entries.toTypedArray()[splineMode.ordinal]
            splineMode = SplineMode.entries.toTypedArray()[index]

            if ((splineMode == SplineMode.POLYGON && prevSplineMode == SplineMode.SPLINE) ||
                splineMode == SplineMode.SPLINE && prevSplineMode == SplineMode.POLYGON ||
                (splineMode == SplineMode.SPLINE && prevPrevSplineMode == SplineMode.POLYGON) ||
                (splineMode == SplineMode.POLYGON && prevPrevSplineMode == SplineMode.SPLINE)) {

                defaultBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

                paths.add(mutableListOf())
                extraPointsList.add(mutableListOf())
                middles.clear()
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
            middles[prevIndex],
            middles[currIndex],
            leftLength / rightLength
        )

        if (splineMode == SplineMode.POLYGON && paths.last().size > 3) {

            val prevLast = Point(extraPointsList.last()[extraPointsList.last().lastIndex - 1])
            val last = Point(extraPointsList.last()[extraPointsList.last().lastIndex])

            extraPointsList.last()[extraPointsList.last().lastIndex - 1] = Point(
                middles[prevIndex].x + diffPoint.x,
                middles[prevIndex].y + diffPoint.y
            )
            extraPointsList.last()[extraPointsList.last().lastIndex] = Point(
                middles[currIndex].x + diffPoint.x,
                middles[currIndex].y + diffPoint.y
            )
            extraPointsList.last().add(prevLast)
            extraPointsList.last().add(last)

        } else {
            extraPointsList.last().add(
                Point(
                    middles[prevIndex].x + diffPoint.x,
                    middles[prevIndex].y + diffPoint.y
                )
            )
            extraPointsList.last().add(
                Point(
                    middles[currIndex].x + diffPoint.x,
                    middles[currIndex].y + diffPoint.y
                )
            )
        }

        if (paths.last().size > 3 && splineMode == SplineMode.POLYGON)
            calculateFieldsForMovingPoint(
                paths.last(),
                extraPointsList.last(),
                currIndex,
                splineMode
            )
    }

    private fun removeExtraPoints(index: Int) {
        extraPointsList.last().removeAt(index)
        extraPointsList.last().removeAt(index)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onClose(onSave: Boolean) {
        binding.superExtraImageView.setOnTouchListener(null)
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        paths.clear()
        extraPointsList.clear()
        dialog.dismiss()
        removeSplineMenuButton(binding, splineMenuButton)
    }
}