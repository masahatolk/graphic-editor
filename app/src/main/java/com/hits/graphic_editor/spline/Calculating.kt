package com.hits.graphic_editor.spline

import android.graphics.Point
import kotlin.math.pow
import kotlin.math.sqrt

fun update() {

}

fun calculatePoint(first: Point, second: Point, third: Point, fourth: Point, t: Float): Point {
    val point = Point()

    point.x = calculateCubicCoordinate(
        t,
        first.x.toFloat(),
        second.x.toFloat(),
        third.x.toFloat(),
        fourth.x.toFloat()
    ).toInt()
    point.y = calculateCubicCoordinate(
        t,
        first.y.toFloat(),
        second.y.toFloat(),
        third.y.toFloat(),
        fourth.y.toFloat()
    ).toInt()

    return point
}

fun calculateAuxPoint(first: Point, second: Point, third: Point, t: Float): Point {
    val point = Point()

    point.x = calculateCoordinate(
        t,
        first.x.toFloat(),
        second.x.toFloat(),
        third.x.toFloat()
    ).toInt()
    point.y = calculateCoordinate(
        t,
        first.y.toFloat(),
        second.y.toFloat(),
        third.y.toFloat()
    ).toInt()

    return point
}

fun calculateCoordinate(t: Float, c1: Float, c2: Float, c3: Float): Float {
    return (1 - t).pow(2) * c1 + 2 * (1 - t) * t * c2 + t.pow(2) * c3
}

fun calculateCubicCoordinate(t: Float, c1: Float, c2: Float, c3: Float, c4: Float): Float {
    return (1 - t).pow(3) * c1 + 3 * (1 - t).pow(2) * t * c2 + 3 * (1 - t) * t.pow(2) * c3 + t.pow(3) * c4
}

fun calculateExtraPoints(
    extraPoints: MutableList<Point>,
    controlPoint: Point,
    left: Point,
    right: Point,
    coefficient: Float
): Point {
    val interPoint = Point()
    interPoint.x = ((left.x + coefficient * right.x) / (1 + coefficient)).toInt()
    interPoint.y = ((left.y + coefficient * right.y) / (1 + coefficient)).toInt()

    val diffX = controlPoint.x - interPoint.x
    val diffY = controlPoint.y - interPoint.y

    return Point(diffX, diffY)
}

fun calculateLength(first: Point, second: Point): Float {
    return sqrt((first.x - second.x).toFloat().pow(2) + (first.y - second.y).toFloat().pow(2))
}

fun calculateMiddle(first: Int, second: Int): Int {
    return (first + second) / 2
}

fun calculateFieldsForMovingPoint(
    path: MutableList<Point>,
    extraPoints: MutableList<Point>,
    pointIndex: Int
) {
    var leftMiddle: Point? = null
    var rightMiddle: Point? = null
    var leftLength: Float = 0f
    var rightLength: Float = 0f

    if (pointIndex < path.lastIndex) {
        rightMiddle = Point(
            calculateMiddle(path[pointIndex].x, path[pointIndex + 1].x),
            calculateMiddle(path[pointIndex].y, path[pointIndex + 1].y)
        )
        rightLength = calculateLength(path[pointIndex], path[pointIndex + 1])
    }
    if (pointIndex > 0) {
        leftMiddle = Point(
            calculateMiddle(path[pointIndex - 1].x, path[pointIndex].x),
            calculateMiddle(path[pointIndex - 1].y, path[pointIndex].y)
        )
        leftLength = calculateLength(path[pointIndex - 1], path[pointIndex])
    }

    val diffPoint: Point
    if (leftMiddle != null && rightMiddle != null) {
        diffPoint = calculateExtraPoints(
            extraPoints,
            path[pointIndex],
            leftMiddle,
            rightMiddle,
            leftLength / rightLength
        )
        leftMiddle.x += diffPoint.x
        leftMiddle.y += diffPoint.y
        rightMiddle.x += diffPoint.x
        rightMiddle.y += diffPoint.y
        extraPoints[pointIndex - 1] = leftMiddle
        extraPoints[pointIndex] = rightMiddle
    }

    /*if(pointIndex < path.lastIndex) {
        calculateRightFields(rightMiddle, rightLength)
    }
    if (pointIndex > 0) {
        calculateLeftFields(leftMiddle, leftLength)
    }*/
}

fun calculateRightFields(
    path: MutableList<Point>,
    extraPoints: MutableList<Point>,
    pointIndex: Int,
    leftMiddle: Point,
    rightMiddle: Point
) {


    val leftLength = calculateLength(path[0], path[1])
    val rightLength = calculateLength(path[1], path[2])

    //calculateExtraPoints(extraPoints, , , , leftLength / rightLength)
}

fun calculateLeftFields() {

}