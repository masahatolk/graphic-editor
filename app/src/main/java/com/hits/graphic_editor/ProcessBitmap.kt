package com.hits.graphic_editor

import android.graphics.Bitmap
import androidx.core.graphics.createBitmap

typealias IntColor = Int

data class SimpleImage(
    val pixels: IntArray,
    val width: Int,
    val height: Int
) {
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

    operator fun get(y: Int, x: Int): Int {
        return pixels[y * width + x]
    }

    operator fun set(i: Int, j: Int, value: Int) {
        pixels[i * width + j] = value
    }
}

fun getBitMap(img: SimpleImage): Bitmap {
    val output = createBitmap(img.width, img.height)
    output.setPixels(img.pixels, 0, img.width, 0, 0, img.width, img.height)
    return output
}

fun getSimpleImage(input: Bitmap): SimpleImage {
    val height = input.height
    val width = input.width

    val bitmapPixels = IntArray(width * height)
    input.getPixels(bitmapPixels, 0, width, 0, 0, width, height)

    return SimpleImage(bitmapPixels, width, height)
}

fun IntColor.alpha(): IntColor = this shr 24 and 0xff
fun IntColor.red(): IntColor = this shr 16 and 0xff
fun IntColor.green(): IntColor = this shr 8 and 0xff
fun IntColor.blue(): IntColor = this and 0xff

fun argbToInt(alpha: Int, red: Int, green: Int, blue: Int) =
    (alpha shl 24) or (red shl 16) or (green shl 8) or blue

