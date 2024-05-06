package com.hits.graphic_editor.utils

import com.hits.graphic_editor.custom_api.SimpleImage
import com.hits.graphic_editor.custom_api.alpha
import com.hits.graphic_editor.custom_api.argbToInt
import com.hits.graphic_editor.custom_api.blendedIntColor
import com.hits.graphic_editor.custom_api.blue
import com.hits.graphic_editor.custom_api.green
import com.hits.graphic_editor.custom_api.red
import kotlin.math.abs
fun getBilinearFilteredPixelInt(img: SimpleImage, u: Float, v: Float): Int
{
    val floatX = u * (img.width - 1)
    val floatY = v * (img.height - 1)

    val x = floatX.toInt()
    val y = floatY.toInt()

    val uRatio = abs(floatX - x)
    val vRatio = abs(floatY - y)
    val uOpposite = 1 - uRatio
    val vOpposite = 1 - vRatio

    val alpha = ((img[x, y].alpha() * uOpposite + img[x + 1, y].alpha() * uRatio) * vOpposite +
            (img[x, y + 1].alpha() * uOpposite + img[x + 1, y + 1].alpha() * uRatio) * vRatio).toInt()

    val red = ((img[x, y].red() * uOpposite + img[x + 1, y].red() * uRatio) * vOpposite +
            (img[x, y + 1].red() * uOpposite + img[x + 1, y + 1].red() * uRatio) * vRatio).toInt()

    val green = ((img[x, y].green() * uOpposite + img[x + 1, y].green() * uRatio) * vOpposite +
            (img[x, y + 1].green() * uOpposite + img[x + 1, y + 1].green() * uRatio) * vRatio).toInt()

    val blue = ((img[x, y].blue() * uOpposite + img[x + 1, y].blue() * uRatio) * vOpposite +
            (img[x, y + 1].blue() * uOpposite + img[x + 1, y + 1].blue() * uRatio) * vRatio).toInt()

    return argbToInt(alpha, red, green, blue)
}
fun getTrilinearFilterBlendCoeff(firstImg: SimpleImage, secondImg: SimpleImage, newWidth:Int): Float
{
    val smallerImgCoeffPowed = firstImg.width * firstImg.width
    val biggerImgCoeffPowed = secondImg.width * secondImg.width
    return (newWidth * newWidth - smallerImgCoeffPowed).toFloat() /
            (biggerImgCoeffPowed - smallerImgCoeffPowed)
}
fun getTrilinearFilterBlendCoeff(firstImgSizeCoeff: Float, secondImgSizeCoeff: Float, newImgSizeCoeff:Float): Float
{
    val smallerImgCoeffPowed = firstImgSizeCoeff * firstImgSizeCoeff
    val biggerImgCoeffPowed = secondImgSizeCoeff * secondImgSizeCoeff
    return (newImgSizeCoeff * newImgSizeCoeff - smallerImgCoeffPowed) /
            (biggerImgCoeffPowed - smallerImgCoeffPowed)
}
fun getTrilinearFilteredPixelInt(firstImg: SimpleImage, secondImg: SimpleImage,
                                 blendCoeff:Float, u: Float, v: Float): Int
{
    val smallerImgPixel =
        getBilinearFilteredPixelInt(firstImg, u, v)
    val biggerImgPixel =
        getBilinearFilteredPixelInt(secondImg, u, v)

    return  blendedIntColor(smallerImgPixel, biggerImgPixel, blendCoeff)
}