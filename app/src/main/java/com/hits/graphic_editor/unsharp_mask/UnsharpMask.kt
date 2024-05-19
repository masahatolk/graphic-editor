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

        val blurredBitmap = applyGaussianBlurToImage(inputBitmap, radius)

        val mask = createMask(inputBitmap, radius, amount, threshold)

        val sharpenedMask = applyEdgeEnhancement(mask, amount)

        val thresholdedMask = applyThreshold(sharpenedMask, threshold)

        val resultBitmap = applyMask(inputBitmap, thresholdedMask)

        return resultBitmap
    }

    private fun applyGaussianBlurToImage(inputBitmap: Bitmap, radius: Float): Bitmap {
        val horizontalBlurBitmap = applyGaussianBlurHorizontally(inputBitmap, radius)
        return applyGaussianBlurVertically(horizontalBlurBitmap, radius)
    }

    private fun applyGaussianBlurHorizontally(inputBitmap: Bitmap, radius: Float): Bitmap {
        val outputBitmap = inputBitmap.copy(inputBitmap.config, true)
        val width = outputBitmap.width
        val height = outputBitmap.height
        val pixels = IntArray(width)

        val sigma = radius / 3f
        val kernelSize = (sigma * 6).toInt()
        val kernel = FloatArray(kernelSize)

        for (i in 0 until kernelSize) {
            val x = i - kernelSize / 2
            kernel[i] = ((1 / (sigma * sqrt(2 * Math.PI))) * exp(-(x * x).toDouble() / (2 * sigma * sigma).toDouble()).toFloat()).toFloat()
        }

        for (y in 0 until height) {
            inputBitmap.getPixels(pixels, 0, width, 0, y, width, 1)
            for (x in 0 until width) {
                var r = 0f
                var g = 0f
                var b = 0f
                for (i in 0 until kernelSize) {
                    val pixel = pixels[min(max(x + i - kernelSize / 2, 0), width - 1)]
                    r += pixel.red() * kernel[i]
                    g += pixel.green() * kernel[i]
                    b += pixel.blue() * kernel[i]
                }
                pixels[x] = argbToInt(pixels[x].alpha(), getTruncatedChannel(r), getTruncatedChannel(g), getTruncatedChannel(b))
            }
            outputBitmap.setPixels(pixels, 0, width, 0, y, width, 1)
        }

        return outputBitmap
    }

    private fun applyGaussianBlurVertically(inputBitmap: Bitmap, radius: Float): Bitmap {
        val outputBitmap = inputBitmap.copy(inputBitmap.config, true)
        val width = outputBitmap.width
        val height = outputBitmap.height
        val pixels = IntArray(height)

        val sigma = radius / 3f
        val kernelSize = (sigma * 6).toInt()
        val kernel = FloatArray(kernelSize)

        for (i in 0 until kernelSize) {
            val x = i - kernelSize / 2
            kernel[i] = ((1 / (sigma * sqrt(2 * Math.PI))) * exp(-(x * x).toDouble() / (2 * sigma * sigma).toDouble()).toFloat()).toFloat()
        }

        for (x in 0 until width) {
            inputBitmap.getPixels(pixels, 0, 1, x, 0, 1, height)
            for (y in 0 until height) {
                var r = 0f
                var g = 0f
                var b = 0f
                for (i in 0 until kernelSize) {
                    val pixel = pixels[min(max(y + i - kernelSize / 2, 0), height - 1)]
                    r += pixel.red() * kernel[i]
                    g += pixel.green() * kernel[i]
                    b += pixel.blue() * kernel[i]
                }
                pixels[y] = argbToInt(pixels[y].alpha(), getTruncatedChannel(r), getTruncatedChannel(g), getTruncatedChannel(b))
            }
            outputBitmap.setPixels(pixels, 0, 1, x, 0, 1, height)
        }

        return outputBitmap
    }

    private fun createMask(inputBitmap: Bitmap, radius: Float, amount: Float, threshold: Int): Bitmap {
        val blurredBitmap = applyGaussianBlurToImage(inputBitmap, radius)

        val maskBitmap = inputBitmap.copy(inputBitmap.config, true)
        val width = maskBitmap.width
        val height = maskBitmap.height

        for (y in 0 until height) {
            for (x in 0 until width) {
                val originalPixel = inputBitmap.getPixel(x, y)
                val blurredPixel = blurredBitmap.getPixel(x, y)

                val diffR = originalPixel.red() - blurredPixel.red()
                val diffG = originalPixel.green() - blurredPixel.green()
                val diffB = originalPixel.blue() - blurredPixel.blue()

                val maskR = if (abs(diffR) >= threshold) diffR else 0
                val maskG = if (abs(diffG) >= threshold) diffG else 0
                val maskB = if (abs(diffB) >= threshold) diffB else 0

                val newR = getTruncatedChannel(originalPixel.red() + (amount * maskR).toInt())
                val newG = getTruncatedChannel(originalPixel.green() + (amount * maskG).toInt())
                val newB = getTruncatedChannel(originalPixel.blue() + (amount * maskB).toInt())

                val newPixel = argbToInt(originalPixel.alpha(), newR, newG, newB)
                maskBitmap.setPixel(x, y, newPixel)
            }
        }

        return maskBitmap
    }


    private fun applyEdgeEnhancement(mask: Bitmap, amount: Float): Bitmap {
        val width = mask.width
        val height = mask.height
        val enhancedBitmap = Bitmap.createBitmap(width, height, mask.config)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = mask.getPixel(x, y)
                val r = pixel.red()
                val g = pixel.green()
                val b = pixel.blue()

                val enhancedR = getTruncatedChannel(r * amount)
                val enhancedG = getTruncatedChannel(g * amount)
                val enhancedB = getTruncatedChannel(b * amount)

                val newPixel = argbToInt(pixel.alpha(), enhancedR, enhancedG, enhancedB)
                enhancedBitmap.setPixel(x, y, newPixel)
            }
        }

        return enhancedBitmap
    }


    private fun applyThreshold(mask: Bitmap, threshold: Int): Bitmap {
        val width = mask.width
        val height = mask.height
        val thresholdedBitmap = Bitmap.createBitmap(width, height, mask.config)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = mask.getPixel(x, y)
                val r = pixel.red()
                val g = pixel.green()
                val b = pixel.blue()

                val newR = if (abs(r) >= threshold) r else 0
                val newG = if (abs(g) >= threshold) g else 0
                val newB = if (abs(b) >= threshold) b else 0

                val newPixel = argbToInt(pixel.alpha(), newR, newG, newB)
                thresholdedBitmap.setPixel(x, y, newPixel)
            }
        }

        return thresholdedBitmap
    }

    private fun applyMask(inputBitmap: Bitmap, mask: Bitmap): Bitmap {
        val width = inputBitmap.width
        val height = inputBitmap.height
        val resultBitmap = Bitmap.createBitmap(width, height, inputBitmap.config)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val originalPixel = inputBitmap.getPixel(x, y)
                val maskPixel = mask.getPixel(x, y)

                val newR = getTruncatedChannel(originalPixel.red() + maskPixel.red())
                val newG = getTruncatedChannel(originalPixel.green() + maskPixel.green())
                val newB = getTruncatedChannel(originalPixel.blue() + maskPixel.blue())

                val newPixel = argbToInt(originalPixel.alpha(), newR, newG, newB)
                resultBitmap.setPixel(x, y, newPixel)
            }
        }

        return resultBitmap
    }
}
