package com.hits.graphic_editor.unsharp_mask

import android.graphics.Bitmap
import android.view.LayoutInflater
import com.hits.graphic_editor.Filter
import com.hits.graphic_editor.custom_api.SimpleImage
import com.hits.graphic_editor.custom_api.alpha
import com.hits.graphic_editor.custom_api.argbToInt
import com.hits.graphic_editor.custom_api.blue
import com.hits.graphic_editor.custom_api.getBitMap
import com.hits.graphic_editor.custom_api.getTruncatedChannel
import com.hits.graphic_editor.custom_api.green
import com.hits.graphic_editor.custom_api.red
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.UnsharpmaskBottomMenuBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class UnsharpMask(
    override val binding: ActivityNewProjectBinding,
    override val layoutInflater: LayoutInflater
) : Filter {

    lateinit var simpleImage: SimpleImage
    private var lastProcessedBitmap: Bitmap? = null
    var blurRadius: Float = 10f
    var amount: Float = 1.0f
    var threshold: Int = 0

    val unsharpMaskBinding: UnsharpmaskBottomMenuBinding by lazy {
        UnsharpmaskBottomMenuBinding.inflate(layoutInflater)
    }

    override fun showBottomMenu() {
        addUnsharpMaskingControls(binding, this, unsharpMaskBinding)
    }

    fun applyUnsharpMasking() {
        val inputBitmap = lastProcessedBitmap ?: getBitMap(simpleImage)
        val resultBitmap = unsharpMask(inputBitmap, blurRadius, amount, threshold)
        lastProcessedBitmap = resultBitmap
        binding.imageView.setImageBitmap(resultBitmap)
    }

    private fun unsharpMask(inputBitmap: Bitmap, radius: Float, amount: Float, threshold: Int): Bitmap {
        val mask = createMask(inputBitmap, radius, amount, threshold)
        val sharpenedMask = applyEdgeEnhancement(mask, amount)
        val thresholdedMask = applyThreshold(sharpenedMask, threshold)
        return applyMask(inputBitmap, thresholdedMask)
    }

    private fun applyGaussianBlurToImage(inputBitmap: Bitmap, radius: Float): Bitmap {
        val width = inputBitmap.width
        val height = inputBitmap.height

        val horizontalBlurBitmap = Bitmap.createBitmap(width, height, inputBitmap.config)
        val verticalBlurBitmap = Bitmap.createBitmap(width, height, inputBitmap.config)

        runBlocking {
            val horizontalBlurJob = async { applyGaussianBlurHorizontally(inputBitmap, horizontalBlurBitmap, radius) }
            horizontalBlurJob.await()
            val verticalBlurJob = async { applyGaussianBlurVertically(horizontalBlurBitmap, verticalBlurBitmap, radius) }
            verticalBlurJob.await()
        }

        return verticalBlurBitmap
    }

    private suspend fun applyGaussianBlurHorizontally(inputBitmap: Bitmap, outputBitmap: Bitmap, radius: Float) = withContext(Dispatchers.Default) {
        val width = inputBitmap.width
        val height = inputBitmap.height
        val sigma = radius / 3f
        val kernelSize = (sigma * 6).toInt()
        val kernel = FloatArray(kernelSize) { i ->
            val x = i - kernelSize / 2
            ((1 / (sigma * sqrt(2 * Math.PI))) * exp(-(x * x).toDouble() / (2 * sigma * sigma).toDouble())).toFloat()
        }

        val pixels = IntArray(width)
        val resultPixels = IntArray(width)
        for (y in 0 until height) {
            inputBitmap.getPixels(pixels, 0, width, 0, y, width, 1)
            for (x in 0 until width) {
                var r = 0f
                var g = 0f
                var b = 0f
                for (i in kernel.indices) {
                    val pixel = pixels[min(max(x + i - kernelSize / 2, 0), width - 1)]
                    r += pixel.red() * kernel[i]
                    g += pixel.green() * kernel[i]
                    b += pixel.blue() * kernel[i]
                }
                resultPixels[x] = argbToInt(pixels[x].alpha(), getTruncatedChannel(r), getTruncatedChannel(g), getTruncatedChannel(b))
            }
            outputBitmap.setPixels(resultPixels, 0, width, 0, y, width, 1)
        }
    }

    private suspend fun applyGaussianBlurVertically(inputBitmap: Bitmap, outputBitmap: Bitmap, radius: Float) = withContext(Dispatchers.Default) {
        val width = inputBitmap.width
        val height = inputBitmap.height
        val sigma = radius / 3f
        val kernelSize = (sigma * 6).toInt()
        val kernel = FloatArray(kernelSize) { i ->
            val x = i - kernelSize / 2
            ((1 / (sigma * sqrt(2 * Math.PI))) * exp(-(x * x).toDouble() / (2 * sigma * sigma).toDouble())).toFloat()
        }

        val pixels = IntArray(height)
        val resultPixels = IntArray(height)
        for (x in 0 until width) {
            inputBitmap.getPixels(pixels, 0, 1, x, 0, 1, height)
            for (y in 0 until height) {
                var r = 0f
                var g = 0f
                var b = 0f
                for (i in kernel.indices) {
                    val pixel = pixels[min(max(y + i - kernelSize / 2, 0), height - 1)]
                    r += pixel.red() * kernel[i]
                    g += pixel.green() * kernel[i]
                    b += pixel.blue() * kernel[i]
                }
                resultPixels[y] = argbToInt(pixels[y].alpha(), getTruncatedChannel(r), getTruncatedChannel(g), getTruncatedChannel(b))
            }
            outputBitmap.setPixels(resultPixels, 0, 1, x, 0, 1, height)
        }
    }

    private fun createMask(inputBitmap: Bitmap, radius: Float, amount: Float, threshold: Int): Bitmap {
        val blurredBitmap = applyGaussianBlurToImage(inputBitmap, radius)
        val maskBitmap = inputBitmap.copy(inputBitmap.config, true)
        val width = maskBitmap.width
        val height = maskBitmap.height

        val originalPixels = IntArray(width * height)
        val blurredPixels = IntArray(width * height)
        val resultPixels = IntArray(width * height)

        inputBitmap.getPixels(originalPixels, 0, width, 0, 0, width, height)
        blurredBitmap.getPixels(blurredPixels, 0, width, 0, 0, width, height)

        for (i in 0 until width * height) {
            val originalPixel = originalPixels[i]
            val blurredPixel = blurredPixels[i]

            val diffR = originalPixel.red() - blurredPixel.red()
            val diffG = originalPixel.green() - blurredPixel.green()
            val diffB = originalPixel.blue() - blurredPixel.blue()

            val maskR = if (abs(diffR) >= threshold) diffR else 0
            val maskG = if (abs(diffG) >= threshold) diffG else 0
            val maskB = if (abs(diffB) >= threshold) diffB else 0

            val newR = getTruncatedChannel(originalPixel.red() + (amount * maskR).toInt())
            val newG = getTruncatedChannel(originalPixel.green() + (amount * maskG).toInt())
            val newB = getTruncatedChannel(originalPixel.blue() + (amount * maskB).toInt())

            resultPixels[i] = argbToInt(originalPixel.alpha(), newR, newG, newB)
        }

        maskBitmap.setPixels(resultPixels, 0, width, 0, 0, width, height)
        return maskBitmap
    }

    private fun applyEdgeEnhancement(mask: Bitmap, amount: Float): Bitmap {
        val width = mask.width
        val height = mask.height
        val resultPixels = IntArray(width * height)
        val maskPixels = IntArray(width * height)

        mask.getPixels(maskPixels, 0, width, 0, 0, width, height)

        for (i in 0 until width * height) {
            val pixel = maskPixels[i]
            val r = pixel.red()
            val g = pixel.green()
            val b = pixel.blue()

            val enhancedR = getTruncatedChannel(r * amount)
            val enhancedG = getTruncatedChannel(g * amount)
            val enhancedB = getTruncatedChannel(b * amount)

            resultPixels[i] = argbToInt(pixel.alpha(), enhancedR, enhancedG, enhancedB)
        }

        val enhancedBitmap = Bitmap.createBitmap(width, height, mask.config)
        enhancedBitmap.setPixels(resultPixels, 0, width, 0, 0, width, height)
        return enhancedBitmap
    }

    private fun applyThreshold(mask: Bitmap, threshold: Int): Bitmap {
        val width = mask.width
        val height = mask.height
        val resultPixels = IntArray(width * height)
        val maskPixels = IntArray(width * height)

        mask.getPixels(maskPixels, 0, width, 0, 0, width, height)

        for (i in 0 until width * height) {
            val pixel = maskPixels[i]
            val r = pixel.red()
            val g = pixel.green()
            val b = pixel.blue()

            val newR = if (abs(r) >= threshold) r else 0
            val newG = if (abs(g) >= threshold) g else 0
            val newB = if (abs(b) >= threshold) b else 0

            resultPixels[i] = argbToInt(pixel.alpha(), newR, newG, newB)
        }

        val thresholdedBitmap = Bitmap.createBitmap(width, height, mask.config)
        thresholdedBitmap.setPixels(resultPixels, 0, width, 0, 0, width, height)
        return thresholdedBitmap
    }

    private fun applyMask(inputBitmap: Bitmap, mask: Bitmap): Bitmap {
        val width = inputBitmap.width
        val height = inputBitmap.height
        val resultBitmap = Bitmap.createBitmap(width, height, inputBitmap.config)

        runBlocking {
            val originalPixels = IntArray(width * height)
            val maskPixels = IntArray(width * height)
            val resultPixels = IntArray(width * height)

            inputBitmap.getPixels(originalPixels, 0, width, 0, 0, width, height)
            mask.getPixels(maskPixels, 0, width, 0, 0, width, height)

            val jobs = (0 until height).map { y ->
                async {
                    for (x in 0 until width) {
                        val i = y * width + x
                        val originalPixel = originalPixels[i]
                        val maskPixel = maskPixels[i]

                        val newR = getTruncatedChannel(originalPixel.red() + maskPixel.red())
                        val newG = getTruncatedChannel(originalPixel.green() + maskPixel.green())
                        val newB = getTruncatedChannel(originalPixel.blue() + maskPixel.blue())

                        resultPixels[i] = argbToInt(originalPixel.alpha(), newR, newG, newB)
                    }
                }
            }
            jobs.awaitAll()

            resultBitmap.setPixels(resultPixels, 0, width, 0, 0, width, height)
        }

        return resultBitmap
    }
}

