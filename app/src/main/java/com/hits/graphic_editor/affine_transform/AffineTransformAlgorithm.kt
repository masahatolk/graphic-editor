package com.hits.graphic_editor.affine_transform

import com.hits.graphic_editor.custom_api.MipMapsContainer
import com.hits.graphic_editor.custom_api.SimpleImage
import com.hits.graphic_editor.custom_api.divideIntColorByInt
import com.hits.graphic_editor.utils.FVec2
import com.hits.graphic_editor.utils.Vec2
import com.hits.graphic_editor.utils.getBilinearFilteredPixelInt
import com.hits.graphic_editor.utils.getTrilinearFilterBlendCoeff
import com.hits.graphic_editor.utils.getTrilinearFilteredPixelInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.math.sqrt


data class PointTransfer(
    var fromX: Float,
    var fromY: Float,
    var toX: Float,
    var toY: Float,
)
data class EdgePoints(
    val leftY: Int,
    val topX: Int,
    val rightY: Int,
    val bottomX: Int
)
data class AffineTransformedResult(
    val transformedImage: SimpleImage,
    val cropOffset: Vec2?
)
{
    fun getSimpleImage(): SimpleImage{
        return transformedImage
    }
    fun getCropPreviewSimpleImage(): SimpleImage {
        cropOffset ?: return transformedImage
        val cropPreviewImg = transformedImage.copy(pixels = transformedImage.pixels.clone())
        val jobs: MutableList<Job> = mutableListOf()

        for (y in 0 until transformedImage.height) {
            jobs.add(CoroutineScope(Dispatchers.Default).launch {
                for (x in 0 until cropOffset.x) {
                    cropPreviewImg[x, y] =
                        cropPreviewImg[x, y].divideIntColorByInt(2)
                }
                for (x in transformedImage.width - cropOffset.x until transformedImage.width) {
                    cropPreviewImg[x, y] =
                        cropPreviewImg[x, y].divideIntColorByInt(2)
                }
            })
        }
        for (x in 0 until cropOffset.x) {
            jobs.add(CoroutineScope(Dispatchers.Default).launch {
                for (y in 0 until cropOffset.y) {
                    cropPreviewImg[x, y] =
                        cropPreviewImg[x, y].divideIntColorByInt(2)
                }
                for (y in transformedImage.height - cropOffset.y until transformedImage.height) {
                    cropPreviewImg[x, y] =
                        cropPreviewImg[x, y].divideIntColorByInt(2)
                }
            })
        }
        runBlocking{ jobs.forEach { it.join() }}
        return cropPreviewImg
    }
    fun getCroppedSimpleImage(): SimpleImage {
        cropOffset ?: return transformedImage
        val resultImg = SimpleImage(
            transformedImage.width - cropOffset.x * 2,
            transformedImage.height - cropOffset.y * 2
        )
        for (x in 0 until resultImg.width)
            for (y in 0 until resultImg.height)
                resultImg[x, y] = transformedImage[x + cropOffset.x, y + cropOffset.y]
        return resultImg
    }
}
private fun getCropOffset(
    img: SimpleImage, edgePoints: EdgePoints, ratioXY: Float): Vec2
{
    val leftTopLine = arrayOf(
        edgePoints.leftY,
        edgePoints.topX,
        -edgePoints.leftY * edgePoints.topX
    )
    val topRightLine = arrayOf(
        edgePoints.rightY,
        -edgePoints.topX,
        -edgePoints.topX * edgePoints.rightY
    )

    var leftWidth = 0
    var rightWidth = img.width - 1
    var ltPoint = Vec2(0,0)
    while (leftWidth <= rightWidth) {

        val midWidth = leftWidth + (rightWidth - leftWidth) / 2
        val midHeight = (midWidth / ratioXY).roundToInt()
        ltPoint = Vec2((img.width - 1 - midWidth) / 2, (img.height - 1 - midHeight) / 2)
        val trPoint = Vec2((img.width - 1 + midWidth) / 2, (img.height - 1 - midHeight) / 2)

        val signLT = leftTopLine[0] * ltPoint.x +
                leftTopLine[1] * ltPoint.y + leftTopLine[2]
        val signTR = topRightLine[0] * trPoint.x +
                topRightLine[1] * trPoint.y + topRightLine[2]

        if (signLT == 0 && signTR ==0)
            return ltPoint

        if (signLT > 0 && signTR > 0)
            leftWidth = midWidth + 1
        else rightWidth = midWidth - 1
    }
    return ltPoint
}
private fun getAffineTransformationMatrix(
    transf1: PointTransfer, transf2: PointTransfer, transf3: PointTransfer
): Array<Array<Float>>?
{
    // start X coordinates
    val a = arrayOf(transf1.fromX, transf2.fromX, transf3.fromX)
    // start Y coordinates
    val b = arrayOf(transf1.fromY, transf2.fromY, transf3.fromY)
    // new X coordinates
    val c1 = arrayOf(transf1.toX, transf2.toX, transf3.toX)
    // new Y coordinates
    val c2 = arrayOf(transf1.toY, transf2.toY, transf3.toY)

    fun getTransformCoeffs(c: Array<Float>): Array<Float>?
    {
        val x2Div = (a[1]-a[0])*(b[2]-b[0])-(a[2]-a[0])*(b[1]-b[0])
        val x1Div = a[2]-a[0]

        if (x2Div == 0F || x1Div == 0F)
            return null

        val x2 = ((a[1]-a[0])*(c[2]-c[0])-(a[2]-a[0])*(c[1]-c[0])) / x2Div
        val x1 = ((c[2]-c[0])-(b[2]-b[0])*x2) / x1Div
        val x3 = c[0]-a[0]*x1-b[0]*x2

        return arrayOf(x1, x2, x3)
    }

    val row1 = getTransformCoeffs(c1) ?: return null
    val row2 = getTransformCoeffs(c2) ?: return null

    return arrayOf(row1, row2)
}
private fun getReversedTransformationMatrix(matrix: Array<Array<Float>>): Array<Array<Float>>
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
private suspend fun getAffineTransformedSimpleImage(
    mipMapsContainer: MipMapsContainer, matrix: Array<Array<Float>>): SimpleImage
{
    val img = mipMapsContainer.img
    val oldHeight = img.height
    val oldWidth = img.width

    // function ignores coordinates offset and centers new image
    fun getTransformedFVec2(vec: FVec2, matrix: Array<Array<Float>>): FVec2
            = FVec2(
        matrix[0][0] * vec.x + matrix[0][1] * vec.y,
        matrix[1][0] * vec.x + matrix[1][1] * vec.y)

    val reversedMatrix = getReversedTransformationMatrix(matrix)

    val cornerCoordinates = arrayOf(
        FVec2(0F, 0F),
        getTransformedFVec2(FVec2(img.width.toFloat(), 0F), matrix),
        getTransformedFVec2(FVec2(0F, img.height.toFloat()), matrix),
        getTransformedFVec2(FVec2(img.width.toFloat(), img.height.toFloat()), matrix))

    val translatedLeft = ceil(cornerCoordinates.minBy{ it.x }.x).toInt()
    val translatedTop = ceil(cornerCoordinates.minBy{ it.y }.y).toInt()
    val translatedRight = cornerCoordinates.maxBy{ it.x }.x.toInt()
    val translatedBottom = cornerCoordinates.maxBy{ it.y }.y.toInt()

    val newWidth = translatedRight - translatedLeft
    val newHeight = translatedBottom - translatedTop
    val newImg = SimpleImage(newWidth, newHeight)

    val currCoeff = sqrt(newWidth * newHeight / (img.width * img.height).toFloat())

    val jobs: MutableList<Job> = mutableListOf()

    if (currCoeff >= MipMapsContainer.constSizeCoeffs.last() ||
        currCoeff <= MipMapsContainer.constSizeCoeffs.first())
    {
        for (translatedX in translatedLeft until translatedRight) {
            jobs.add(CoroutineScope(Dispatchers.Default).launch {
                    for (translatedY in translatedTop until translatedBottom) {
                        val oldVec = getTransformedFVec2(
                            FVec2(translatedX.toFloat(), translatedY.toFloat()),
                            reversedMatrix
                        )

                        val newX = translatedX - translatedLeft
                        val newY = translatedY - translatedTop

                        if (0 <= oldVec.x && oldVec.x < oldWidth &&
                            0 <= oldVec.y && oldVec.y < oldHeight
                        )
                            newImg[newX, newY] = getBilinearFilteredPixelInt(
                                img, oldVec.x / oldWidth, oldVec.y / oldHeight
                            )
                    }
                })
        }
    }
    else{
        val biggerImgIndex = MipMapsContainer.constSizeCoeffs.indexOfFirst { it >= currCoeff }

        //waits for mipmaps to generate
        mipMapsContainer.jobs[biggerImgIndex - 1].join()
        mipMapsContainer.jobs[biggerImgIndex].join()

        val biggerImg = mipMapsContainer.mipMaps[biggerImgIndex]
        val smallerImg = mipMapsContainer.mipMaps[biggerImgIndex - 1]
        val blendCoeff = getTrilinearFilterBlendCoeff(
            MipMapsContainer.constSizeCoeffs[biggerImgIndex - 1],
            MipMapsContainer.constSizeCoeffs[biggerImgIndex],
            currCoeff)

        for (translatedX in translatedLeft until translatedRight) {
            jobs.add(CoroutineScope(Dispatchers.Default).launch {
                for (translatedY in translatedTop until translatedBottom) {
                    val oldVec = getTransformedFVec2(
                        FVec2(translatedX.toFloat(), translatedY.toFloat()),
                        reversedMatrix
                    )

                    val newX = translatedX - translatedLeft
                    val newY = translatedY - translatedTop

                    if (0 <= oldVec.x && oldVec.x < oldWidth &&
                        0 <= oldVec.y && oldVec.y < oldHeight
                    )
                        newImg[newX, newY] = getTrilinearFilteredPixelInt(
                            smallerImg, biggerImg, blendCoeff,
                            oldVec.x / oldWidth, oldVec.y / oldHeight
                        )
                }
            })
        }
    }
    jobs.forEach{it.join()}
    return newImg
}
private suspend fun getAffineTransformedSimpleImage(
    mipMapsContainer: MipMapsContainer,
    transf1: PointTransfer,
    transf2: PointTransfer,
    transf3: PointTransfer):SimpleImage?
{
    val matrix = getAffineTransformationMatrix(transf1, transf2, transf3) ?: return null
    return getAffineTransformedSimpleImage(mipMapsContainer, matrix)
}




suspend fun getAffineTransformedResult(
    mipMapsContainer: MipMapsContainer, matrix: Array<Array<Float>>): AffineTransformedResult {
    val img = mipMapsContainer.img
    val oldHeight = img.height
    val oldWidth = img.width

    // function ignores coordinates offset and centers new image
    fun getTransformedFVec2(vec: FVec2, matrix: Array<Array<Float>>): FVec2 = FVec2(
        matrix[0][0] * vec.x + matrix[0][1] * vec.y,
        matrix[1][0] * vec.x + matrix[1][1] * vec.y
    )

    val reversedMatrix = getReversedTransformationMatrix(matrix)

    val cornerCoordinates = arrayOf(
        FVec2(0F, 0F),
        getTransformedFVec2(FVec2(img.width.toFloat(), 0F), matrix),
        getTransformedFVec2(FVec2(0F, img.height.toFloat()), matrix),
        getTransformedFVec2(FVec2(img.width.toFloat(), img.height.toFloat()), matrix)
    )

    val leftTranslatedPoint = cornerCoordinates.minBy { it.x }
    val topTranslatedPoint = cornerCoordinates.minBy { it.y }
    val rightTranslatedPoint = cornerCoordinates.maxBy { it.x }
    val bottomTranslatedPoint = cornerCoordinates.maxBy { it.y }

    val translatedLeft = ceil(leftTranslatedPoint.x).toInt()
    val translatedTop = ceil(topTranslatedPoint.y).toInt()
    val translatedRight = rightTranslatedPoint.x.toInt()
    val translatedBottom = bottomTranslatedPoint.y.toInt()

    val newWidth = translatedRight - translatedLeft
    val newHeight = translatedBottom - translatedTop
    val newImg = SimpleImage(newWidth, newHeight)

    val currCoeff = sqrt(newWidth * newHeight / (img.width * img.height).toFloat())

    val jobs: MutableList<Job> = mutableListOf()

    if (currCoeff >= MipMapsContainer.constSizeCoeffs.last() ||
        currCoeff <= MipMapsContainer.constSizeCoeffs.first()
    ) {
        for (translatedX in translatedLeft until translatedRight) {
            jobs.add(CoroutineScope(Dispatchers.Default).launch {
                for (translatedY in translatedTop until translatedBottom) {
                    val oldVec = getTransformedFVec2(
                        FVec2(translatedX.toFloat(), translatedY.toFloat()),
                        reversedMatrix
                    )

                    val newX = translatedX - translatedLeft
                    val newY = translatedY - translatedTop

                    if (0 <= oldVec.x && oldVec.x < oldWidth &&
                        0 <= oldVec.y && oldVec.y < oldHeight
                    )
                        newImg[newX, newY] = getBilinearFilteredPixelInt(
                            img, oldVec.x / oldWidth, oldVec.y / oldHeight
                        )
                }
            })
        }
    } else {
        val biggerImgIndex = MipMapsContainer.constSizeCoeffs.indexOfFirst { it >= currCoeff }

        //waits for mipmaps to generate
        mipMapsContainer.jobs[biggerImgIndex - 1].join()
        mipMapsContainer.jobs[biggerImgIndex].join()

        val biggerImg = mipMapsContainer.mipMaps[biggerImgIndex]
        val smallerImg = mipMapsContainer.mipMaps[biggerImgIndex - 1]
        val blendCoeff = getTrilinearFilterBlendCoeff(
            MipMapsContainer.constSizeCoeffs[biggerImgIndex - 1],
            MipMapsContainer.constSizeCoeffs[biggerImgIndex],
            currCoeff
        )

        for (translatedX in translatedLeft until translatedRight) {
            jobs.add(CoroutineScope(Dispatchers.Default).launch {
                for (translatedY in translatedTop until translatedBottom) {
                    val oldVec = getTransformedFVec2(
                        FVec2(translatedX.toFloat(), translatedY.toFloat()),
                        reversedMatrix
                    )

                    val newX = translatedX - translatedLeft
                    val newY = translatedY - translatedTop

                    if (0 <= oldVec.x && oldVec.x < oldWidth &&
                        0 <= oldVec.y && oldVec.y < oldHeight
                    )
                        newImg[newX, newY] = getTrilinearFilteredPixelInt(
                            smallerImg, biggerImg, blendCoeff,
                            oldVec.x / oldWidth, oldVec.y / oldHeight
                        )
                }
            })
        }
    }
    jobs.forEach { it.join() }
    return AffineTransformedResult(
        newImg,
        getCropOffset(newImg, EdgePoints(
            leftTranslatedPoint.y.roundToInt(),
            topTranslatedPoint.x.roundToInt() - translatedLeft,
            rightTranslatedPoint.y.roundToInt(),
            bottomTranslatedPoint.x.roundToInt()- translatedLeft
        ),img.width/img.height.toFloat())
    )
}
suspend fun getAffineTransformedResult(
    mipMapsContainer: MipMapsContainer,
    transf1: PointTransfer,
    transf2: PointTransfer,
    transf3: PointTransfer):AffineTransformedResult?
{
    val matrix = getAffineTransformationMatrix(transf1, transf2, transf3) ?: return null
    return getAffineTransformedResult(mipMapsContainer, matrix)
}