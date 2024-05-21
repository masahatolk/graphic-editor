package com.hits.graphic_editor.spline

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.Rect

const val pointRadius: Float = 15F
private const val splinePointRadius: Float = 3F
private val rect: Rect = Rect(0, 0, width, height)
var splinePaint: Paint = Paint()


fun drawLines(canvas: Canvas, path: MutableList<Point>, paint: Paint) {
    for (i in 1 until path.size) {
        canvas.drawLine(
            path[i - 1].x.toFloat(),
            path[i - 1].y.toFloat(),
            path[i].x.toFloat(),
            path[i].y.toFloat(),
            paint
        )
    }
}

fun drawSplinePoint(canvas: Canvas, point: Point, paint: Paint) {
    canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), splinePointRadius, paint)
}

fun drawPoints(canvas: Canvas, path: MutableList<Point>, paint: Paint) {
    for(i in 0 until path.size) {
        canvas.drawCircle(path[i].x.toFloat(), path[i].y.toFloat(), pointRadius, paint)
    }
}

fun drawSplines(
    canvas: Canvas,
    path: MutableList<Point>,
    extraPoints: MutableList<Point>
) {
    var point: Point
    var t = 0.0f

    if (path.size >= 3) {
        while (t < 1.0f) {

            point = calculateAuxPoint(
                path[0],
                extraPoints[0],
                path[1],
                t
            )
            drawSplinePoint(canvas, point, splinePaint)
            t += 0.001f
        }
        t = 0.0f

        if (path.size > 3) {
            var j = 2
            for (i in 2 until path.size - 1) {
                while (t < 1f) {

                    point = calculatePoint(
                        path[i - 1],
                        extraPoints[j - 1],
                        extraPoints[j],
                        path[i],
                        t
                    )
                    drawSplinePoint(canvas, point, splinePaint)
                    t += 0.001f
                }

                j += 2
                t = 0.0f
            }
        }

        while (t < 1f) {
            point = calculateAuxPoint(
                path[path.lastIndex - 1],
                extraPoints.last(),
                path.last(),
                t
            )
            drawSplinePoint(canvas, point, splinePaint)
            t += 0.001f
        }
    }
}

fun draw(
    canvas: Canvas,
    resultCanvas: Canvas,
    path: MutableList<Point>,
    extraPoints: MutableList<Point>,
    paint: Paint
) {
    splinePaint.strokeWidth = paint.strokeWidth
    splinePaint.color = paint.color

    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
    drawPoints(canvas, path, paint)
    drawLines(canvas, path, paint)
    drawSplines(canvas, path, extraPoints)
    drawSplines(resultCanvas, path, extraPoints)
}

fun drawByDefault(canvas: Canvas, bitmap: Bitmap, paint: Paint) {
    canvas.drawBitmap(bitmap, null, rect, paint)
}