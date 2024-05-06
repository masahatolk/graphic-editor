package com.hits.graphic_editor.custom_api

import android.graphics.Bitmap
import androidx.core.graphics.createBitmap
import com.hits.graphic_editor.getSuperSampledSimpleImage


data class SimpleImage(
    var pixels: IntArray,
    val width: Int,
    val height: Int)
{
    constructor(width: Int, height: Int) : this(
        pixels = IntArray(width * height),
        width = width,
        height = height
    )
    operator fun get(x: Int, y:Int):Int {
        return pixels[y * width + x]
    }
    operator fun set(x: Int, y: Int, value: Int) {
        pixels[y * width + x] = value
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