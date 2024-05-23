package com.hits.graphic_editor.spline

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.Rect

const val pointRadius: Float = 15F
private const val splinePointRadius: Float = 2F
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

fun drawAntialiasingSplinePoint(canvas: Canvas, point: Point, paint: Paint) {
    paint.alpha = 2
    canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), splinePointRadius + 6, paint)
    paint.alpha = 4
    canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), splinePointRadius + 4, paint)
    paint.alpha = 5
    canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), splinePointRadius + 3, paint)
    paint.alpha = 7
    canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), splinePointRadius + 2, paint)
    paint.alpha = 10
    canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), splinePointRadius + 1, paint)
    paint.alpha = 70
}

fun drawPoints(canvas: Canvas, path: MutableList<Point>, paint: Paint) {
    for (i in 0 until path.size) {
        canvas.drawCircle(path[i].x.toFloat(), path[i].y.toFloat(), pointRadius, paint)
    }
}

fun drawSplines(
    canvas: Canvas,
    path: MutableList<Point>,
    extraPoints: MutableList<Point>,
    antialiasingMode: Boolean,
    polygonMode: Boolean
) {
    var point: Point
    var t = 0.0f

    if (path.size >= 3) {
        if (!polygonMode) {
            while (t < 1.0f) {

                point = calculateAuxPoint(
                    path[0],
                    extraPoints[0],
                    path[1],
                    t
                )
                if (antialiasingMode) drawAntialiasingSplinePoint(canvas, point, splinePaint)
                drawSplinePoint(canvas, point, splinePaint)
                t += 0.001f
            }
            t = 0.0f
        }

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
                    if (antialiasingMode) drawAntialiasingSplinePoint(canvas, point, splinePaint)
                    drawSplinePoint(canvas, point, splinePaint)
                    t += 0.001f
                }

                j += 2
                t = 0.0f
            }
        }

        if (!polygonMode) {
            while (t < 1f) {
                point = calculateAuxPoint(
                    path[path.lastIndex - 1],
                    extraPoints.last(),
                    path.last(),
                    t
                )
                if (antialiasingMode) drawAntialiasingSplinePoint(canvas, point, splinePaint)
                drawSplinePoint(canvas, point, splinePaint)
                t += 0.001f
            }
            t = 0.0f
        }

        if (polygonMode) {
            while (t < 1f) {

                point = calculatePoint(
                    path[path.lastIndex - 1],
                    extraPoints[(path.lastIndex - 1) * 2 - 1],
                    extraPoints[(path.lastIndex - 1) * 2],
                    path.last(),
                    t
                )
                if (antialiasingMode) drawAntialiasingSplinePoint(canvas, point, splinePaint)
                drawSplinePoint(canvas, point, splinePaint)
                t += 0.001f
            }

            t = 0f
            while (t < 1f) {

                point = calculatePoint(
                    path.last(),
                    extraPoints[(path.lastIndex - 1) * 2 + 1],
                    extraPoints[(path.lastIndex - 1) * 2 + 2],
                    path.first(),
                    t
                )
                if (antialiasingMode) drawAntialiasingSplinePoint(canvas, point, splinePaint)
                drawSplinePoint(canvas, point, splinePaint)
                t += 0.001f
            }

            t = 0f
            while (t < 1f) {

                point = calculatePoint(
                    path.first(),
                    extraPoints[(path.lastIndex - 1) * 2 + 3],
                    extraPoints[0],
                    path[1],
                    t
                )
                if (antialiasingMode) drawAntialiasingSplinePoint(canvas, point, splinePaint)
                drawSplinePoint(canvas, point, splinePaint)
                t += 0.001f
            }
        }
    }
}

fun draw(
    canvas: Canvas,
    resultCanvas: Canvas,
    path: MutableList<Point>,
    extraPoints: MutableList<Point>,
    paint: Paint,
    splineMode: SplineMode,
    antialiasingMode: Boolean,
    polygonMode: Boolean
) {
    splinePaint.strokeWidth = paint.strokeWidth
    splinePaint.color = paint.color

    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
    drawPoints(canvas, path, paint)
    if (splineMode != SplineMode.POLYGON) {
        drawLines(canvas, path, paint)
        drawSplines(canvas, path, extraPoints, antialiasingMode, polygonMode)
        drawSplines(resultCanvas, path, extraPoints, antialiasingMode, polygonMode)
    }
}

fun drawByDefault(canvas: Canvas, bitmap: Bitmap, paint: Paint) {
    canvas.drawBitmap(bitmap, null, rect, paint)
}