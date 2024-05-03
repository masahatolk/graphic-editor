package com.hits.graphic_editor

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.createBitmap
import com.hits.graphic_editor.databinding.ActivityMainBinding
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

// COLORS

typealias IntColor = Int
fun IntColor.alpha(): IntColor = this shr 24
fun IntColor.red(): IntColor = this shr 16 and 0xff
fun IntColor.green(): IntColor = this shr 8 and 0xff
fun IntColor.blue(): IntColor = this and 0xff
fun argbToInt(alpha: Int, red: Int, green: Int, blue: Int) =
    (alpha shl 24) or (red shl 16) or (green shl 8) or blue
fun blendedIntColor(first: IntColor, second: IntColor, coeff: Float): IntColor
{
    return argbToInt(
        (first.alpha() * coeff + second.alpha() * (1 - coeff)).roundToInt(),
        (first.red() * coeff + second.red() * (1 - coeff)).roundToInt(),
        (first.green()* coeff + second.green() * (1 - coeff)).roundToInt(),
        (first.blue()* coeff + second.blue() * (1 - coeff)).roundToInt()
    )
}
// API

data class SimpleImage(
    val pixels: IntArray,
    val width: Int,
    val height: Int)
{
    operator fun get(x: Int, y:Int):Int {
        return pixels[y * width + x]
    }
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
}
fun getBitMap(img: SimpleImage): Bitmap {
    val output = createBitmap(img.width, img.height)
    output.setPixels(img.pixels, 0, img.width, 0, 0, img.width, img.height)
    return output
}
fun getSimpleImage(input: Bitmap): SimpleImage
{
    val height = input.height
    val width = input.width

    val bitmapPixels = IntArray(width * height)
    input.getPixels(bitmapPixels, 0, width, 0, 0, width, height)

    return SimpleImage(bitmapPixels, width, height)
}
fun Bitmap.setPixels(img: SimpleImage) =
    this.setPixels(img.pixels, 0, img.width, 0, 0, img.width, img.height)
data class MipMapsContainer(
    var img: SimpleImage,
    var mipMaps: MutableList<SimpleImage> = mutableListOf()
)
{
    companion object {
        val constSizeCoeffs = arrayOf(0.15F, 0.30F, 0.5F, 0.65F, 0.8F, 0.92F)
    }
    constructor(simpleImg: SimpleImage) : this(img = simpleImg)
    init{
        for (coeff in constSizeCoeffs)
            this.mipMaps.add(getSuperSampledSimpleImage(this.img, coeff))
    }
}

// FILTERS
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
fun getBilinearFilteredSimpleImage(img: SimpleImage, coeff: Float): SimpleImage
{
    val newHeight = (img.height * coeff).toInt()
    val newWidth = (img.width * coeff).toInt()

    val newPixels = IntArray(newWidth * newHeight)

    for (y in 0 until newHeight)
    {
        for (x in 0 until newWidth)
        {
            newPixels[y * newWidth + x] =
                getBilinearFilteredPixelInt(img, x / newWidth.toFloat(), y / newHeight.toFloat())
        }
    }

    return SimpleImage(newPixels, newWidth, newHeight)
}
fun getSuperSampledSimpleImage(img: SimpleImage, coeff: Float): SimpleImage
{
    val oldHeight = img.height
    val oldWidth = img.width

    val newHeight = (oldHeight * coeff).toInt()
    val newWidth = (oldWidth * coeff).toInt()

    val newPixels = IntArray(newWidth * newHeight)
    val newChannels = FloatArray(newWidth * newHeight * 4)

    // less than one
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
            newChannels[4 * i].roundToInt(),
            newChannels[4 * i + 1].roundToInt(),
            newChannels[4 * i + 2].roundToInt(),
            newChannels[4 * i + 3].roundToInt())
    }
    return SimpleImage(newPixels, newWidth, newHeight)
}
fun getTrilinearFilteredSimpleImage(input: MipMapsContainer, coeff: Float): SimpleImage {
    if (coeff <= MipMapsContainer.constSizeCoeffs.first() ||
        coeff >= MipMapsContainer.constSizeCoeffs.last())
        return getBilinearFilteredSimpleImage(input.img, coeff)

    val newWidth = (input.img.width * coeff).toInt()
    val newHeight = (input.img.height * coeff).toInt()
    val newPixels = IntArray(newWidth * newHeight)

    for (i in 1 until MipMapsContainer.constSizeCoeffs.size)
    {
        if (coeff <= MipMapsContainer.constSizeCoeffs[i])
        {
            val smallerImgCoeffPowed =
                MipMapsContainer.constSizeCoeffs[i - 1] * MipMapsContainer.constSizeCoeffs[i - 1]
            val biggerImgCoeffPowed =
                MipMapsContainer.constSizeCoeffs[i] * MipMapsContainer.constSizeCoeffs[i]
            val blendCoeff = (coeff * coeff - smallerImgCoeffPowed) /
                    (biggerImgCoeffPowed - smallerImgCoeffPowed)

            for (y in 0 until newHeight)
            {
                for (x in 0 until newWidth)
                {
                    val smallerImgPixel =
                        getBilinearFilteredPixelInt(input.mipMaps[i - 1],
                            x.toFloat() / newWidth, y.toFloat() / newHeight)
                    val biggerImgPixel =
                        getBilinearFilteredPixelInt(input.mipMaps[i],
                            x.toFloat() / newWidth, y.toFloat() / newHeight)

                    newPixels[y * newWidth + x] =
                        blendedIntColor(smallerImgPixel, biggerImgPixel, blendCoeff)
                }
            }
            break
        }
    }
    return SimpleImage(newPixels, newWidth, newHeight)
}
fun getConvolutionedSimpleImage(img: SimpleImage, coeff: Float): SimpleImage
{
    val newHeight = (img.height * coeff).toInt()
    val newWidth = (img.width * coeff).toInt()

    val newPixels = IntArray(newWidth * newHeight)

    for (y in 0 until newHeight - coeff.toInt())
    {
        for (x in 0 until newWidth - coeff.toInt())
        {
            //newPixels[y * newWidth + x] =
        }
    }

    return SimpleImage(newPixels, newWidth, newHeight)
}
fun getScaledSimpleImage(mipMaps: MipMapsContainer, scaleCoeff: Float, antiAliasing: Boolean = false): SimpleImage
{
    if (scaleCoeff == 1F)
        return mipMaps.img

    if (scaleCoeff < 1F)
        return if(antiAliasing) getTrilinearFilteredSimpleImage(mipMaps, scaleCoeff)
            else getSuperSampledSimpleImage(mipMaps.img, scaleCoeff)

    return if(antiAliasing) getConvolutionedSimpleImage(mipMaps.img, scaleCoeff)
        else getBilinearFilteredSimpleImage(mipMaps.img, scaleCoeff)
}


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
            val mipMaps = MipMapsContainer(getSimpleImage(bitmap))
            bitmap = getBitMap(getScaledSimpleImage(mipMaps, 10.7F))
            //
            binding.image2.setImageBitmap(bitmap)
        }

        /**/
    }
}