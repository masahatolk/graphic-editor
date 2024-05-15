package com.hits.graphic_editor.scaling

import com.hits.graphic_editor.custom_api.MipMapsContainer
import com.hits.graphic_editor.custom_api.SimpleImage
import com.hits.graphic_editor.custom_api.alpha
import com.hits.graphic_editor.custom_api.argbToInt
import com.hits.graphic_editor.custom_api.blue
import com.hits.graphic_editor.custom_api.getTruncatedChannel
import com.hits.graphic_editor.custom_api.green
import com.hits.graphic_editor.custom_api.red
import com.hits.graphic_editor.utils.getBilinearFilteredPixelInt
import com.hits.graphic_editor.utils.getTrilinearFilterBlendCoeff
import com.hits.graphic_editor.utils.getTrilinearFilteredPixelInt
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

const val FLOAT_PRECISION = 1e-8
fun getBilinearFilteredSimpleImage(img: SimpleImage, coeff: Float): SimpleImage
{
    val newHeight = (img.height * coeff).toInt()
    val newWidth = (img.width * coeff).toInt()

    val newImage = SimpleImage(newWidth, newHeight)

    for (y in 0 until newHeight)
        for (x in 0 until newWidth)
            newImage[x, y] = getBilinearFilteredPixelInt(
                img, x / newWidth.toFloat(), y / newHeight.toFloat())

    return newImage
}
fun getSuperSampledSimpleImage(img: SimpleImage, coeff: Float): SimpleImage
{
    val oldHeight = img.height
    val oldWidth = img.width

    val newHeight = (oldHeight * coeff).toInt()
    val newWidth = (oldWidth * coeff).toInt()

    val newPixels = IntArray(newWidth * newHeight)
    val newChannels = FloatArray(newWidth * newHeight * 4)

    val newOldRatioY = newHeight / oldHeight.toFloat()
    val newOldRatioX = newWidth / oldWidth.toFloat()

    for (newY in 0 until newHeight)
    {
        val oldY = oldHeight * newY / newHeight
        val yStep = (ceil((newY + 1) * oldHeight / newHeight.toFloat()) -
                floor(newY * oldHeight / newHeight.toFloat())).roundToInt()
        val topOffset = newY - oldY * newOldRatioY
        val bottomOffset = (ceil((newY + 1) * oldHeight / newHeight.toFloat()) -
                (newY + 1) * oldHeight / newHeight.toFloat()) * newOldRatioY

        for (newX in 0 until newWidth)
        {
            val oldX = oldWidth * newX / newWidth
            val xStep = (ceil((newX + 1) * oldWidth / newWidth.toFloat()) -
                    floor(newX * oldWidth / newWidth.toFloat())).roundToInt()
            val leftOffset = newX - oldX * newOldRatioX
            val rightOffset = (ceil((newX + 1) * oldWidth / newWidth.toFloat()) -
                    (newX + 1) * oldWidth / newWidth.toFloat()) * newOldRatioX

            fun addToChannels(pixelsX: Int, pixelsY: Int, pixelArea: Float)
            {
                newChannels[4 * (newY * newWidth + newX)] +=
                    img[pixelsX, pixelsY].alpha() * pixelArea
                newChannels[4 * (newY * newWidth + newX) + 1] +=
                    img[pixelsX, pixelsY].red() * pixelArea
                newChannels[4 * (newY * newWidth + newX) + 2] +=
                    img[pixelsX, pixelsY].green() * pixelArea
                newChannels[4 * (newY * newWidth + newX) + 3] +=
                    img[pixelsX, pixelsY].blue() * pixelArea
            }

            // inner pixels
            for (innerY in 1 until yStep - 1)
            {
                for (innerX in 1 until xStep - 1)
                {
                    addToChannels(
                        oldX + innerX,
                        oldY + innerY,
                        newOldRatioX * newOldRatioY)
                }
            }
            //left pixels
            for (innerY in 1 until yStep - 1)
            {
                addToChannels(
                    oldX,
                    oldY + innerY,
                    newOldRatioY * (newOldRatioX - leftOffset))
            }
            //right pixels
            for (innerY in 1 until yStep - 1)
            {
                addToChannels(
                    oldX + xStep - 1,
                    oldY + innerY,
                    newOldRatioY * (newOldRatioX - rightOffset))
            }
            //top pixels
            for (innerX in 1 until xStep - 1)
            {
                addToChannels(
                    oldX + innerX,
                    oldY,
                    newOldRatioX * (newOldRatioY - topOffset))
            }
            //bottom pixels
            for (innerX in 1 until xStep - 1)
            {
                addToChannels(
                    oldX + innerX,
                    oldY + yStep - 1,
                    newOldRatioX * (newOldRatioY - bottomOffset))
            }
            //corner pixels
            //left top
            addToChannels(
                oldX,
                oldY,
                (newOldRatioX - leftOffset) * (newOldRatioY - topOffset))
            //right top
            addToChannels(
                oldX + xStep - 1,
                oldY,
                (newOldRatioX - rightOffset) * (newOldRatioY - topOffset))
            //left bottom
            addToChannels(
                oldX,
                oldY + yStep - 1,
                (newOldRatioX - leftOffset) * (newOldRatioY - bottomOffset))
            //right bottom
            addToChannels(
                oldX + xStep - 1,
                oldY + yStep - 1,
                (newOldRatioX - rightOffset) * (newOldRatioY - bottomOffset))
        }
    }

    for (i in 0 until newWidth * newHeight)
    {
        newPixels[i] = argbToInt(
            newChannels[4 * i],
            newChannels[4 * i + 1],
            newChannels[4 * i + 2],
            newChannels[4 * i + 3])
    }
    return SimpleImage(newPixels, newWidth, newHeight)
}
fun getTrilinearFilteredSimpleImage(input: MipMapsContainer, coeff: Float): SimpleImage
{
    if (coeff <= MipMapsContainer.constSizeCoeffs.first() ||
        coeff >= MipMapsContainer.constSizeCoeffs.last())
        return getBilinearFilteredSimpleImage(input.img, coeff)

    val newWidth = (input.img.width * coeff).toInt()
    val newHeight = (input.img.height * coeff).toInt()
    val newImage = SimpleImage(IntArray(newWidth * newHeight), newWidth, newHeight)

    for (i in 1 until MipMapsContainer.constSizeCoeffs.size)
    {
        if (coeff <= MipMapsContainer.constSizeCoeffs[i])
        {
            val blendCoeff = getTrilinearFilterBlendCoeff(
                MipMapsContainer.constSizeCoeffs[i - 1], MipMapsContainer.constSizeCoeffs[i], coeff)
            for (y in 0 until newHeight)
                for (x in 0 until newWidth)
                    newImage[x, y] = getTrilinearFilteredPixelInt(input.mipMaps[i - 1], input.mipMaps[i],
                            blendCoeff, x/newWidth.toFloat(), y/newHeight.toFloat())
            break
        }
    }
    return newImage
}
fun getConvolutionedSimpleImage(img: SimpleImage, coeff: Float, radius:Int = 3): SimpleImage
{
    fun lanczos(x: Float):Float {
        if (abs(x) > radius)
            return 0F
        if (abs(x) < FLOAT_PRECISION)
            return 1F

        val normX = x * Math.PI
        return (radius * sin(normX) * sin(normX / radius) /
                (normX * normX)).toFloat()
    }
    val newHeight = (img.height * coeff).toInt()
    val newWidth = (img.width * coeff).toInt()

    //along rows
    val tempImg = SimpleImage(IntArray(newWidth * img.height), newWidth, img.height)
    var kernels = Array(newWidth){FloatArray(radius * 2)}
    for (newX in 0 until newWidth) {
        var kernelSum = 0F
        val u = newX/newWidth.toFloat()
        val floatX = u * (img.width - 1)
        val x = floatX.toInt()
        for (i in kernels[newX].indices) {
            if (x - radius + i in 0..<img.width) {
                kernels[newX][i] = lanczos(x - floatX + 1F - radius + i)
                kernelSum += kernels[newX][i]
            }
        }
        for (i in kernels[newX].indices)
            kernels[newX][i] /= kernelSum
    }
    for (y in 0 until img.height)
    {
        for (newX in 0 until newWidth)
        {
            val u = newX/newWidth.toFloat()
            val floatX = u * (img.width - 1)
            val x = floatX.toInt()

            var alpha = 0F; var red = 0F
            var green = 0F; var blue = 0F

            val kernel = kernels[newX]
            val leftBound = max(0, radius - x)
            val rightBound = min(radius * 2, img.width + radius - x)
            for (i in leftBound until rightBound)
            {
                alpha += img[x - radius + i, y].alpha() * kernel[i]
                red += img[x - radius + i, y].red() * kernel[i]
                green += img[x - radius + i, y].green() * kernel[i]
                blue += img[x - radius + i, y].blue() * kernel[i]
            }
            tempImg[newX, y] = argbToInt(
                getTruncatedChannel(alpha),
                getTruncatedChannel(red),
                getTruncatedChannel(green),
                getTruncatedChannel(blue)
            )
        }
    }

    //along colums
    val newImg = SimpleImage(IntArray(newWidth * newHeight), newWidth, newHeight)
    kernels = Array(newHeight){FloatArray(radius * 2)}
    for (newY in 0 until newHeight) {
        var kernelSum = 0F
        val v = newY/newHeight.toFloat()
        val floatY = v * (tempImg.height - 1)
        val y = floatY.toInt()
        for (i in kernels[newY].indices) {
            if (y - radius + i in 0..<tempImg.height) {
                kernels[newY][i] = lanczos(y - floatY + 1F - radius + i)
                kernelSum += kernels[newY][i]
            }
        }
        for (i in kernels[newY].indices)
            kernels[newY][i] /= kernelSum
    }
    for (x in 0 until newWidth)
    {
        for (newY in 0 until newHeight)
        {
            val v = newY/newHeight.toFloat()
            val floatY = v * (tempImg.height - 1)
            val y = floatY.toInt()

            var alpha = 0F; var red = 0F
            var green = 0F; var blue = 0F

            val kernel = kernels[newY]
            val topBound = max(0, radius - y)
            val bottomBound = min(radius * 2, tempImg.height + radius - y)
            for (i in topBound until bottomBound)
            {
                alpha += tempImg[x, y + i - radius].alpha() * kernel[i]
                red += tempImg[x, y + i - radius].red() * kernel[i]
                green += tempImg[x, y + i - radius].green() * kernel[i]
                blue += tempImg[x, y + i - radius].blue() * kernel[i]
            }
            newImg[x, newY] = argbToInt(
                getTruncatedChannel(alpha),
                getTruncatedChannel(red),
                getTruncatedChannel(green),
                getTruncatedChannel(blue)
            )
        }
    }
    return newImg
}
fun getScaledSimpleImage(mipMaps: MipMapsContainer, scaleCoeff: Float, antiAliasing: Boolean = false): SimpleImage
{
    if (scaleCoeff == 1F)
        return mipMaps.img

    if (scaleCoeff < 1F) {
        return if (antiAliasing) getSuperSampledSimpleImage(mipMaps.img, scaleCoeff)
        else getTrilinearFilteredSimpleImage(mipMaps, scaleCoeff)
    }

    return if(antiAliasing) getConvolutionedSimpleImage(mipMaps.img, scaleCoeff)
    else getBilinearFilteredSimpleImage(mipMaps.img, scaleCoeff)
}