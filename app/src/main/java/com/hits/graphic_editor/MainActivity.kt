package com.hits.graphic_editor

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.blue
import androidx.core.graphics.createBitmap
import com.hits.graphic_editor.databinding.ActivityMainBinding
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

// COLORS
typealias IntColor = Int
fun IntColor.alpha(): IntColor = this shr 24
fun IntColor.red(): IntColor = this shr 16 and 0xff
fun IntColor.green(): IntColor = this shr 8 and 0xff
fun IntColor.blue(): IntColor = this and 0xff
fun IntColor.multiplyIntColorByInt(c: Float): IntColor =
    argbToInt(this.alpha() * c, this.red() * c,this.green() * c,this.blue() * c)
fun IntColor.divideIntColorByInt(d: Float): IntColor =
    argbToInt(this.alpha() / d, this.red() / d,this.green() / d,this.blue() / d)
fun IntColor.addIntColorToIntColor(other: Int): IntColor =
    argbToInt(this.alpha() + other.alpha(), this.red() + other.red(),
        this.green() + other.green(),this.blue() + other.blue)
fun getTruncatedChannel(channel: Int):Int =
    max(0, min(255, channel))
fun argbToInt(alpha: Int, red: Int, green: Int, blue: Int) =
    (alpha shl 24) or (red shl 16) or (green shl 8) or blue
fun argbToInt(alpha: Float, red: Float, green: Float, blue: Float) =
    (alpha.roundToInt() shl 24) or (red.roundToInt() shl 16) or (green.roundToInt() shl 8) or blue.roundToInt()
fun blendedIntColor(first: IntColor, second: IntColor, coeff: Float): IntColor
{
    return argbToInt(
        first.alpha() * coeff + second.alpha() * (1 - coeff),
        first.red() * coeff + second.red() * (1 - coeff),
        first.green()* coeff + second.green() * (1 - coeff),
        first.blue()* coeff + second.blue() * (1 - coeff)
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
            newChannels[4 * i],
            newChannels[4 * i + 1],
            newChannels[4 * i + 2],
            newChannels[4 * i + 3])
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
const val FLOAT_PRECISION = 1e-8
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

    val newPixels = IntArray(newWidth * newHeight)

    //rows
    var kernels = Array(newWidth){FloatArray(radius * 2)}
    for (newX in 0 until newWidth) {
        var kernelSum = 0F
        val u = newX/newWidth.toFloat()
        val floatX = u * (img.width - 1)
        val x = floatX.toInt()
        for (i in kernels[newX].indices) {
            if (x - radius + i in 0..<img.width) {
                kernels[newX][i] = lanczos(floatX - x - radius + 0.5F + i)
                kernelSum += kernels[newX][i]
            }
        }
        for (i in kernels[newX].indices)
            kernels[newX][i] /= kernelSum
    }
    for (newY in 0 until newHeight)
    {
        val v = newY/newHeight.toFloat()
        val floatY = v * (img.height - 1)
        val y = floatY.toInt()

        val topBound = max(0, radius - y)
        val bottomBound = min(6, img.height + radius - y)
        val yDiff = bottomBound - topBound;

        for (newX in 0 until newWidth)
        {
            val newIndex = newY * newWidth + newX
            val u = newX/newWidth.toFloat()
            val floatX = u * (img.width - 1)
            val x = floatX.toInt()

            var alpha = 0F; var red = 0F
            var green = 0F; var blue = 0F

            val kernel = kernels[newX]
            val leftBound = max(0, radius - x)
            val rightBound = min(6, img.width + radius - x)
            for (i in leftBound until rightBound)
            {
                for (j in topBound until bottomBound)
                {
                    alpha += img[x - radius + i, y + j - radius].alpha() * kernel[i] / yDiff
                    red += img[x - radius + i, y + j - radius].red() * kernel[i] / yDiff
                    green += img[x - radius + i, y + j - radius].green() * kernel[i] / yDiff
                    blue += img[x - radius + i, y + j - radius].blue() * kernel[i] / yDiff
                }
            }
            newPixels[newIndex] = argbToInt(
                alpha.roundToInt(),
                (red.roundToInt()),
                (green.roundToInt()),
                (blue.roundToInt()))
        }
    }

    //colums
    kernels = Array(newHeight){FloatArray(radius * 2)}
    for (newY in 0 until newHeight) {
        var kernelSum = 0F
        val v = newY/newHeight.toFloat()
        val floatY = v * (img.height - 1)
        val y = floatY.toInt()
        for (i in kernels[newY].indices) {
            if (y - radius + i in 0..<img.height) {
                kernels[newY][i] = lanczos(floatY - y - radius + 0.5F + i)
                kernelSum += kernels[newY][i]
            }
        }
        for (i in kernels[newY].indices)
            kernels[newY][i] /= kernelSum
    }
    for (newX in 0 until newWidth)
    {
        val u = newX/newWidth.toFloat()
        val floatX = u * (img.width - 1)
        val x = floatX.toInt()

        val leftBound = max(0, radius - x)
        val rightBound = min(6, img.width + radius - x)
        val xDiff = rightBound - leftBound

        for (newY in 0 until newHeight)
        {
            val newIndex = newY * newWidth + newX
            val v = newY/newHeight.toFloat()
            val floatY = v * (img.height - 1)
            val y = floatY.toInt()

            var alpha = 0F; var red = 0F
            var green = 0F; var blue = 0F

            val kernel = kernels[newY]
            val topBound = max(0, radius - y)
            val bottomBound = min(6, img.height + radius - y)
            for (i in topBound until bottomBound)
            {
                for (j in leftBound until rightBound)
                {
                    alpha += img[x - radius + j, y + i - radius].alpha() * kernel[i] / xDiff
                    red += img[x - radius + j, y + i - radius].red() * kernel[i] / xDiff
                    green += img[x - radius + j, y + i - radius].green() * kernel[i] / xDiff
                    blue += img[x - radius + j, y + i - radius].blue() * kernel[i] / xDiff
                }
            }
            newPixels[newIndex] = argbToInt(
                255,//alpha.roundToInt(),
                getTruncatedChannel(red.roundToInt()),
                getTruncatedChannel(green.roundToInt()),
                getTruncatedChannel(blue.roundToInt()))
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
            bitmap = getBitMap(getScaledSimpleImage(mipMaps, 1.7F, true))
            //
            binding.image2.setImageBitmap(bitmap)
        }

        /**/
    }
}