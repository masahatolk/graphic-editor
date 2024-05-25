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
val rect: Rect = Rect(0, 0, width, height)
var splinePaint: Paint = Paint()


fun drawLines(
    canvas: Canvas,
    path: MutableList<Point>,
    paint: Paint,
    splineMode: SplineMode,
    prevSplineMode: SplineMode
) {
    for (i in 1 until path.size) {
        canvas.drawLine(
            path[i - 1].x.toFloat(),
            path[i - 1].y.toFloat(),
            path[i].x.toFloat(),
            path[i].y.toFloat(),
            paint
        )
    }
    if ((splineMode == SplineMode.POLYGON || (prevSplineMode == SplineMode.POLYGON && splineMode == SplineMode.DELETE)) && path.size != 0)
        canvas.drawLine(
            path.last().x.toFloat(),
            path.last().y.toFloat(),
            path.first().x.toFloat(),
            path.first().y.toFloat(),
            paint
        )
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
    paint.alpha = 255
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
    splineMode: SplineMode,
    prevSplineMode: SplineMode
) {

    if (path.size >= 3) {
        if (splineMode == SplineMode.POLYGON || (prevSplineMode == SplineMode.POLYGON && splineMode == SplineMode.DELETE)) {
            drawSplinePoints(
                canvas,
                path[path.lastIndex - 1],
                extraPoints[(path.lastIndex - 1) * 2 - 1],
                extraPoints[(path.lastIndex - 1) * 2],
                path.last(),
                antialiasingMode
            )

            drawSplinePoints(
                canvas,
                path.last(),
                extraPoints[(path.lastIndex - 1) * 2 + 1],
                extraPoints[(path.lastIndex - 1) * 2 + 2],
                path.first(),
                antialiasingMode
            )

            drawSplinePoints(
                canvas,
                path.first(),
                extraPoints[(path.lastIndex - 1) * 2 + 3],
                extraPoints.first(),
                path[1],
                antialiasingMode
            )
        } else {
            calculateAuxSplinePoints(canvas, path[0], extraPoints[0], path[1], antialiasingMode)
            calculateAuxSplinePoints(
                canvas, path[path.lastIndex - 1], extraPoints.last(), path.last(), antialiasingMode
            )
        }

        if (path.size > 3) {
            for (i in 2 until path.size - 1) {

                drawSplinePoints(
                    canvas,
                    path[i - 1],
                    extraPoints[(i - 1) * 2 - 1],
                    extraPoints[(i - 1) * 2],
                    path[i],
                    antialiasingMode
                )
            }
        }
    }
}

fun drawSplinePoints(
    canvas: Canvas,
    first: Point,
    second: Point,
    third: Point,
    fourth: Point,
    antialiasingMode: Boolean
) {
    var point: Point
    var t = 0f
    while (t < 1.0f) {

        point = calculatePoint(
            first, second, third, fourth, t
        )
        if (antialiasingMode) drawAntialiasingSplinePoint(canvas, point, splinePaint)
        drawSplinePoint(canvas, point, splinePaint)
        t += 0.001f
    }
}

fun draw(
    canvas: Canvas,
    path: MutableList<Point>,
    extraPoints: MutableList<Point>,
    paint: Paint,
    antialiasingMode: Boolean,
    splineMode: SplineMode,
    prevSplineMode: SplineMode
) {
    splinePaint.strokeWidth = paint.strokeWidth
    splinePaint.color = paint.color

    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
    drawLines(canvas, path, paint, splineMode, prevSplineMode)
    drawSplines(canvas, path, extraPoints, antialiasingMode, splineMode, prevSplineMode)
    drawPoints(canvas, path, paint)

}

fun drawByDefault(canvas: Canvas, bitmap: Bitmap, paint: Paint) {
    canvas.drawBitmap(bitmap, null, rect, paint)
}