package com.hits.graphic_editor

import com.hits.graphic_editor.custom_api.MipMapsContainer
import com.hits.graphic_editor.custom_api.SimpleImage
import com.hits.graphic_editor.utils.getBilinearFilteredPixelInt
import com.hits.graphic_editor.utils.getTrilinearFilterBlendCoeff
import com.hits.graphic_editor.utils.getTrilinearFilteredPixelInt
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vec2(var x: Float, var y: Float)
data class PointTransfer(
    var fromX: Float,
    var fromY: Float,
    var toX: Float,
    var toY: Float,
)
fun getAffineTransformationMatrix(
    transf1: PointTransfer, transf2: PointTransfer, transf3: PointTransfer): Array<Array<Float>>
{
    // start X coordinates
    val a = arrayOf(transf1.fromX, transf2.fromX, transf3.fromX)
    // start Y coordinates
    val b = arrayOf(transf1.fromY, transf2.fromY, transf3.fromY)
    // new X coordinates
    val c1 = arrayOf(transf1.toX, transf2.toX, transf3.toX)
    // new Y coordinates
    val c2 = arrayOf(transf1.toY, transf2.toY, transf3.toY)

    fun getTransformCoeffs(c: Array<Float>): Array<Float>
    {
        val x2 = ((a[1]-a[0])*(c[2]-c[0])-(a[2]-a[0])*(c[1]-c[0]))/
                ((a[1]-a[0])*(b[2]-b[0])-(a[2]-a[0])*(b[1]-b[0]))
        val x1 = ((c[2]-c[0])-(b[2]-b[0])*x2)/(a[2]-a[0])
        val x3 = c[0]-a[0]*x1-b[0]*x2

        return arrayOf(x1, x2, x3)
    }

    return arrayOf(
        getTransformCoeffs(c1),
        getTransformCoeffs(c2)
    )
}
fun getReversedTransformationMatrix(matrix: Array<Array<Float>>): Array<Array<Float>>
{
    val ax = matrix[0][0]
    val bx = matrix[0][1]
    val ay = matrix[1][0]
    val by = matrix[1][1]

    val newBx = bx/(bx*ay-by*ax)
    val newAx = by/(by*ax-bx*ay)

    val newBy = ax/(ax*by-ay*bx)
    val newAy = ay/(ay*bx-ax*by)

    return arrayOf(
        arrayOf(newAx, newBx, -matrix[0][2]),
        arrayOf(newAy, newBy,-matrix[1][2])
    )
}
fun getAffineTransformedSimpleImage(input: MipMapsContainer, matrix: Array<Array<Float>>): SimpleImage
{
    val img = input.img
    val oldHeight = img.height
    val oldWidth = img.width

    // function ignores coordinates offset and centers new image
    fun getTransformedVec2(vec: Vec2, matrix: Array<Array<Float>>): Vec2
            = Vec2(
        matrix[0][0] * vec.x + matrix[0][1] * vec.y,
        matrix[1][0] * vec.x + matrix[1][1] * vec.y)

    val reversedMatrix = getReversedTransformationMatrix(matrix)

    val cornerCoordinates = arrayOf(
        Vec2(0F, 0F),
        getTransformedVec2(Vec2(img.width.toFloat(), 0F), matrix),
        getTransformedVec2(Vec2(0F, img.height.toFloat()), matrix),
        getTransformedVec2(Vec2(img.width.toFloat(), img.height.toFloat()), matrix))

    val translatedLeft = ceil(cornerCoordinates.minBy{ it.x }.x).toInt()
    val translatedTop = ceil(cornerCoordinates.minBy{ it.y }.y).toInt()
    val translatedRight = cornerCoordinates.maxBy{ it.x }.x.toInt()
    val translatedBottom = cornerCoordinates.maxBy{ it.y }.y.toInt()

    val newWidth = translatedRight - translatedLeft
    val newHeight = translatedBottom - translatedTop
    val newImg = SimpleImage(newWidth, newHeight)

    val currCoeff = sqrt(newWidth * newHeight / (img.width * img.height).toFloat())

    if (currCoeff >= MipMapsContainer.constSizeCoeffs.last() ||
        currCoeff <= MipMapsContainer.constSizeCoeffs.first())
    {
        for (translatedX in translatedLeft until translatedRight) {
            for (translatedY in translatedTop until translatedBottom) {
                val oldVec = getTransformedVec2(
                    Vec2(translatedX.toFloat(), translatedY.toFloat()),
                    reversedMatrix)

                val newX = translatedX - translatedLeft
                val newY = translatedY - translatedTop

                if (0 <= oldVec.x && oldVec.x < oldWidth &&
                    0 <= oldVec.y && oldVec.y < oldHeight)
                    newImg[newX, newY] = getBilinearFilteredPixelInt(
                        img, oldVec.x / oldWidth, oldVec.y / oldHeight)
            }
        }
    }
    else{
        val biggerImgIndex = MipMapsContainer.constSizeCoeffs.indexOfFirst { it >= currCoeff }
        val biggerImg = input.mipMaps[biggerImgIndex]
        val smallerImg = input.mipMaps[biggerImgIndex - 1]
        val blendCoeff = getTrilinearFilterBlendCoeff(
            MipMapsContainer.constSizeCoeffs[biggerImgIndex - 1],
            MipMapsContainer.constSizeCoeffs[biggerImgIndex],
            currCoeff)

        for (translatedX in translatedLeft until translatedRight) {
            for (translatedY in translatedTop until translatedBottom) {
                val oldVec = getTransformedVec2(
                    Vec2(translatedX.toFloat(), translatedY.toFloat()),
                    reversedMatrix)

                val newX = translatedX - translatedLeft
                val newY = translatedY - translatedTop

                if (0 <= oldVec.x && oldVec.x < oldWidth &&
                    0 <= oldVec.y && oldVec.y < oldHeight)
                    newImg[newX, newY] = getTrilinearFilteredPixelInt(
                        smallerImg, biggerImg, blendCoeff,
                        oldVec.x / oldWidth, oldVec.y / oldHeight)
            }
        }
    }
    return newImg
}
fun getRotatedSimpleImage(input: MipMapsContainer, angle: Float): SimpleImage
{
    return getAffineTransformedSimpleImage(input,
        arrayOf(
            arrayOf(cos(angle),-sin(angle),0F),
            arrayOf(sin(angle),cos(angle),0F)
        ))
}