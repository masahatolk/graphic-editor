package com.hits.graphic_editor.custom_api

import android.graphics.Bitmap
import androidx.core.graphics.createBitmap
import com.hits.graphic_editor.scaling.getSuperSampledSimpleImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class SimpleImage(
    var pixels: IntArray,
    val width: Int,
    val height: Int
) {
    constructor(width: Int, height: Int) : this(
        pixels = IntArray(width * height),
        width = width,
        height = height
    )

    operator fun get(x: Int, y: Int): Int {
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

    fun getPixels(x: Int, y: Int, width: Int, height: Int): IntArray {
        var pixels = IntArray(height * width)

        for (i in 0 until height) {
            for (j in 0 until width) {
                pixels[i * width + j] = this.pixels[(y + i) * this.width + (x + j)]
            }
        }
        return pixels
    }

    fun setPixels(x: Int, y: Int, settedPixels: IntArray, width: Int) {

        for (i in settedPixels.indices) {
            pixels[(y + i / width) * this.width + (i % width + x)] = settedPixels[i]
        }
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

fun Bitmap.setPixels(img: SimpleImage) =
    this.setPixels(img.pixels, 0, img.width, 0, 0, img.width, img.height)

data class MipMapsContainer(
    var img: SimpleImage,
    var mipMaps: MutableList<SimpleImage> = mutableListOf(),
    var jobs: MutableList<Job> = mutableListOf()
) {
    companion object {
        val constSizeCoeffs = arrayOf(0.15F, 0.30F, 0.5F, 0.65F, 0.8F, 0.92F)
    }

    constructor(simpleImg: SimpleImage) : this(img = simpleImg)

    init {
        for (coeff in constSizeCoeffs) {
            jobs.add(CoroutineScope(Dispatchers.Default).launch(start = CoroutineStart.LAZY) {
                mipMaps.add(getSuperSampledSimpleImage(img, coeff))
            })
        }
        CoroutineScope(Dispatchers.Default).launch {
            jobs.forEach { it.join() }
        }
    }

    fun cancelJobs() {
        jobs.forEach { it.cancel() }
    }
}