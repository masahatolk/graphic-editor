package com.hits.graphic_editor

import android.R.color
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Color.valueOf
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.createBitmap
import com.hits.graphic_editor.databinding.ActivityMainBinding
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.round

typealias IntColor = Int
data class SimpleImage(
    val pixels: IntArray,
    val width: Int,
    val height: Int)
{
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SimpleImage

        if (!pixels.contentEquals(other.pixels)) return false
        if (height != other.height) return false
        if (width != other.width) return false

        return true
    }
    override fun hashCode(): Int {
        var result = pixels.contentHashCode()
        result = 31 * result + height
        result = 31 * result + width
        return result
    }
    fun toBitMap(): Bitmap {
        val output = createBitmap(width, height)
        output.setPixels(pixels, 0, width, 0, 0, width, height)
        return output
    }
}
fun IntColor.alpha(): IntColor = this shr 24
fun IntColor.red(): IntColor = this shr 16 and 0xff
fun IntColor.green(): IntColor = this shr 8 and 0xff
fun IntColor.blue(): IntColor = this and 0xff
fun getBilinearFilteredPixelInt(img: SimpleImage, u: Float, v: Float): Int
{
    val floatX = u * img.width
    val floatY = v * img.height

    val x = floatX.toInt()
    val y = floatY.toInt()

    val uRatio = floatX - x
    val vRatio = floatY - y
    val uOpposite = 1 - uRatio
    val vOpposite = 1 - vRatio

    val alpha = ((img.pixels[x * img.width + y].alpha() * uOpposite + img.pixels[(x + 1) * img.width + y].alpha() * uRatio) * vOpposite +
            (img.pixels[x * img.width + y + 1].alpha() * uOpposite + img.pixels[(x + 1) * img.width + y + 1].alpha() * uRatio) * vRatio).toInt()

    val red = ((img.pixels[x * img.width + y].red() * uOpposite + img.pixels[(x + 1) * img.width + y].red() * uRatio) * vOpposite +
                (img.pixels[x * img.width + y + 1].red() * uOpposite + img.pixels[(x + 1) * img.width + y + 1].red() * uRatio) * vRatio).toInt()

    val green = ((img.pixels[x * img.width + y].green() * uOpposite + img.pixels[(x + 1) * img.width + y].green() * uRatio) * vOpposite +
            (img.pixels[x * img.width + y + 1].green() * uOpposite + img.pixels[(x + 1) * img.width + y + 1].green() * uRatio) * vRatio).toInt()

    val blue = ((img.pixels[x * img.width + y].blue() * uOpposite + img.pixels[(x + 1) * img.width + y].blue() * uRatio) * vOpposite +
            (img.pixels[x * img.width + y + 1].blue() * uOpposite + img.pixels[(x + 1) * img.width + y + 1].blue() * uRatio) * vRatio).toInt()

    return (alpha shl 24) or (red shl 16) or (green shl 8) or blue
}
fun argbToInt(alpha: Int, red: Int, green: Int, blue: Int) =
    (alpha shl 24) or (red shl 16) or (green shl 8) or blue
fun getSuperSampledPixelInt(img: SimpleImage, u: Float, v: Float): Int
{
    val floatX = u * img.width
    val floatY = v * img.height

    val x = floatX.toInt()
    val y = floatY.toInt()

    val uRatio = floatX - x
    val vRatio = floatY - y
    val uOpposite = 1 - uRatio
    val vOpposite = 1 - vRatio

    val alpha = ((img.pixels[x * img.width + y].alpha() * uOpposite + img.pixels[(x + 1) * img.width + y].alpha() * uRatio) * vOpposite +
            (img.pixels[x * img.width + y + 1].alpha() * uOpposite + img.pixels[(x + 1) * img.width + y + 1].alpha() * uRatio) * vRatio).toInt()

    val red = ((img.pixels[x * img.width + y].red() * uOpposite + img.pixels[(x + 1) * img.width + y].red() * uRatio) * vOpposite +
            (img.pixels[x * img.width + y + 1].red() * uOpposite + img.pixels[(x + 1) * img.width + y + 1].red() * uRatio) * vRatio).toInt()

    val green = ((img.pixels[x * img.width + y].green() * uOpposite + img.pixels[(x + 1) * img.width + y].green() * uRatio) * vOpposite +
            (img.pixels[x * img.width + y + 1].green() * uOpposite + img.pixels[(x + 1) * img.width + y + 1].green() * uRatio) * vRatio).toInt()

    val blue = ((img.pixels[x * img.width + y].blue() * uOpposite + img.pixels[(x + 1) * img.width + y].blue() * uRatio) * vOpposite +
            (img.pixels[x * img.width + y + 1].blue() * uOpposite + img.pixels[(x + 1) * img.width + y + 1].blue() * uRatio) * vRatio).toInt()

    return argbToInt(alpha, red, green, blue)
}
fun getSimpleImage(input: Bitmap): SimpleImage
{
    val height = input.height
    val width = input.width

    val bitmapPixels = IntArray(width * height)
    input.getPixels(bitmapPixels, 0, width, 0, 0, width, height)

    return SimpleImage(bitmapPixels, width, height)
}
fun getBilinearFilteredSimpleImage(input: SimpleImage, coeff: Float): SimpleImage
{
    val oldHeight = input.height
    val oldWidth = input.width

    val newHeight = (oldHeight * coeff).toInt()
    val newWidth = (oldWidth * coeff).toInt()

    val newPixels = IntArray(newWidth * newHeight)

    for (y in 0 until newHeight - coeff.toInt())
    {
        for (x in 0 until newWidth - coeff.toInt())
        {
            newPixels[x * newWidth + y] =
                getBilinearFilteredPixelInt(input, x / newWidth.toFloat(), y / newHeight.toFloat())
        }
    }

    return SimpleImage(newPixels, newWidth, newHeight)
}
const val FLOAT_PRECISION = 1e6
fun isWhole(num: Float) =
    abs(num - round(num)) <= FLOAT_PRECISION
fun getSuperSampledSimpleImage(img: SimpleImage, coeff: Float): SimpleImage
{
    val oldHeight = img.height
    val oldWidth = img.width

    val newHeight = (oldHeight * coeff).toInt()
    val newWidth = (oldWidth * coeff).toInt()

    val newPixels = IntArray(newWidth * newHeight)
    val newChannels = FloatArray(newWidth * newHeight * 4)

    val oldNewRatio = oldWidth.toFloat() / newWidth //bigger
    val newOldRatio = newWidth / oldWidth.toFloat() //smaller

    //val newPixelArea = oldNewRatio * oldNewRatio

    val minStep = if (isWhole(oldNewRatio))
        oldNewRatio.toInt() + 1
        else oldNewRatio.toInt()
    val maxStep = minStep + 1

    for (newY in 2 until newHeight - 2)
    {
        val v = newY / newHeight.toFloat()
        val oldY = (v * oldHeight).toInt()
        val yStep = //if (isWhole(v * oldHeight)) minStep else maxStep
            minStep
        val topOffset = oldY * newOldRatio - newY
        val bottomOffset = (newY + 1) * oldNewRatio - floor((newY + 1) * oldNewRatio)

        for (newX in 2 until newWidth - 2)
        {
            val u = newX / newWidth.toFloat()
            val oldX = (u * oldWidth).toInt()
            val xStep = //if (isWhole(u * oldWidth)) minStep else maxStep
                minStep
            val leftOffset = oldX * newOldRatio - newX
            val rightOffset = (newX + 1) * oldNewRatio - floor((newX + 1) * oldNewRatio)

            fun addToChannels(pixelsX: Int, pixelsY: Int, pixelArea: Float)
            {
                val pixelsIndex = pixelsY * img.width + pixelsX

                newChannels[4 * (newY * newWidth + newX)] +=
                    img.pixels[pixelsIndex].alpha() * pixelArea // newPixelArea
                newChannels[4 * (newY * newWidth + newX) + 1] +=
                    img.pixels[pixelsIndex].red() * pixelArea // newPixelArea
                newChannels[4 * (newY * newWidth + newX) + 2] +=
                    img.pixels[pixelsIndex].green() * pixelArea // newPixelArea
                newChannels[4 * (newY * newWidth + newX) + 3] +=
                    img.pixels[pixelsIndex].blue() * pixelArea // newPixelArea
            }

            // inner pixels
            for (innerY in (if (isWhole(v * oldWidth)) 0 else 1) until yStep - 1)
            {
                for (innerX in (if (isWhole(u * oldWidth)) 0 else 1) until xStep - 1)
                {
                    addToChannels(
                        oldX + innerX,
                        oldY + innerY,
                        newOldRatio * newOldRatio)
                }
            }

            //left pixels
            for (innerY in 1 until yStep - 1)
            {
                addToChannels(
                    oldX,
                    oldY + innerY,
                    newOldRatio * (newOldRatio - leftOffset))
            }
            //right pixels
            for (innerY in 1 until yStep - 1)
            {
                addToChannels(
                    oldX + xStep - 1,
                    oldY + innerY,
                    newOldRatio * (newOldRatio - rightOffset))
            }
            //top pixels
            for (innerX in 1 until xStep - 1)
            {
                addToChannels(
                    oldX + innerX,
                    oldY,
                    newOldRatio * (newOldRatio - topOffset))
            }
            //bottom pixels
            for (innerX in 1 until xStep - 1)
            {
                addToChannels(
                    oldX + innerX,
                    oldY + yStep - 1,
                    newOldRatio * (newOldRatio - bottomOffset))
            }
            //corner pixels
            //left top
            addToChannels(
                oldX,
                oldY,
                (newOldRatio - leftOffset) * (newOldRatio - topOffset))
            //right top
            addToChannels(
                oldX + xStep - 1,
                oldY,
                (newOldRatio - rightOffset) * (newOldRatio - topOffset))
            //left bottom
            addToChannels(
                oldX,
                oldY + yStep - 1,
                (newOldRatio - leftOffset) * (newOldRatio - bottomOffset))
            //right bottom
            addToChannels(
                oldX + xStep - 1,
                oldY + yStep - 1,
                (newOldRatio - rightOffset) * (newOldRatio - bottomOffset))
        }
    }

    for (i in 0 until newWidth * newHeight)
    {
        newPixels[i] = argbToInt(
            255,
            newChannels[4 * i + 1].toInt(),
            newChannels[4 * i + 2].toInt(),
            newChannels[4 * i + 3].toInt())
    }
    return SimpleImage(newPixels, newWidth, newHeight)
}
fun Bitmap.setPixels(img: SimpleImage) =
    this.setPixels(img.pixels, 0, img.width, 0, 0, img.width, img.height)

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /**/

        binding.btn.setOnClickListener {
            val drawable = binding.image1.drawable as BitmapDrawable
            var bitmap = drawable.bitmap.copy(Bitmap.Config.ARGB_8888, true)
            //
            bitmap = getSuperSampledSimpleImage(getSimpleImage(bitmap), 0.2F).toBitMap()
            //
            binding.image2.setImageBitmap(bitmap)
        }

        /**/
    }
}