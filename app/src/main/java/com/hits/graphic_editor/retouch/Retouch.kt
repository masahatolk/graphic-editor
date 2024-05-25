package com.hits.graphic_editor.retouch

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.PointF
import android.view.LayoutInflater
import android.widget.ImageView
import com.hits.graphic_editor.custom_api.IntColor
import com.hits.graphic_editor.custom_api.alpha
import com.hits.graphic_editor.custom_api.argbToInt
import com.hits.graphic_editor.custom_api.blue
import com.hits.graphic_editor.custom_api.getBitMap
import com.hits.graphic_editor.custom_api.getSimpleImage
import com.hits.graphic_editor.custom_api.green
import com.hits.graphic_editor.custom_api.red
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.RetouchBottomMenuBinding
import com.hits.graphic_editor.utils.Filter
import com.hits.graphic_editor.utils.ProcessedImage
import kotlin.math.max
import kotlin.math.min

class Retouch(
    override val binding: ActivityNewProjectBinding,
    override val layoutInflater: LayoutInflater,
    override val processedImage: ProcessedImage
) : Filter {
    private var brushSize: Int = 10
    private var retouchCoefficient: Float = 0.5f
    var imageBitmap = getBitMap(processedImage.getSimpleImage())

    private val bottomMenu: RetouchBottomMenuBinding by lazy {
        RetouchBottomMenuBinding.inflate(layoutInflater)
    }

    override fun onClose(onSave: Boolean) {
        binding.imageView.setOnClickListener(null)
        binding.root.removeView(bottomMenu.root)
        if (onSave)
            processedImage.addToLocalStack(getSimpleImage(imageBitmap))
    }

    override fun onStart() {
        showBottomMenu(binding, this, bottomMenu)
    }

    fun updateBrushSize(progress: Int) {
        brushSize = progress
    }

    fun updateRetouchCoefficient(progress: Int) {
        retouchCoefficient = progress / 10f
    }

    fun applyRetouchToBitmap(bitmap: Bitmap, touchX: Int, touchY: Int): Bitmap {
        val retouchedBitmap = bitmap.copy(bitmap.config, true)
        val pixelColors = mutableListOf<IntColor>()
        val radiusSquared = brushSize * brushSize
        val centerX = touchX
        val centerY = touchY

        fun interpolateCoefficient(distance: Float): Float {
            val maxDistance = brushSize.toFloat()
            val minCoefficient = 0.2f
            val maxCoefficient = 1.0f
            val t = distance / maxDistance
            return minCoefficient + (maxCoefficient - minCoefficient) * t
        }

        for (nx in (centerX - brushSize)..(centerX + brushSize)) {
            for (ny in (centerY - brushSize)..(centerY + brushSize)) {
                val dx = nx - centerX
                val dy = ny - centerY
                if (dx * dx + dy * dy <= radiusSquared) {
                    if (nx in 0 until bitmap.width && ny in 0 until bitmap.height) {
                        pixelColors.add(bitmap.getPixel(nx, ny))
                    }
                }
            }
        }

        val averageColor = calculateAverageColor(pixelColors)

        for (nx in (centerX - brushSize)..(centerX + brushSize)) {
            for (ny in (centerY - brushSize)..(centerY + brushSize)) {
                val dx = nx - centerX
                val dy = ny - centerY
                if (dx * dx + dy * dy <= radiusSquared) {
                    if (nx in 0 until bitmap.width && ny in 0 until bitmap.height) {
                        val distanceToCenter =
                            kotlin.math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                        val retouchCoefficient = interpolateCoefficient(distanceToCenter)
                        val blendedColor =
                            blendColors(bitmap.getPixel(nx, ny), averageColor, retouchCoefficient)
                        retouchedBitmap.setPixel(nx, ny, blendedColor)
                    }
                }
            }
        }

        return retouchedBitmap
    }

    private fun calculateAverageColor(colors: List<IntColor>): IntColor {
        val totalColors = colors.size
        var sumAlpha = 0
        var sumRed = 0
        var sumGreen = 0
        var sumBlue = 0

        for (color in colors) {
            sumAlpha += color.alpha()
            sumRed += color.red()
            sumGreen += color.green()
            sumBlue += color.blue()
        }

        val avgAlpha = (sumAlpha / totalColors).toInt()
        val avgRed = (sumRed / totalColors).toInt()
        val avgGreen = (sumGreen / totalColors).toInt()
        val avgBlue = (sumBlue / totalColors).toInt()

        return argbToInt(avgAlpha, avgRed, avgGreen, avgBlue)
    }

    private fun blendColors(centerColor: IntColor, averageColor: IntColor, coef: Float): IntColor {
        val alpha = (centerColor.alpha() * coef + averageColor.alpha() * (1 - coef)).toInt()
        val red = (centerColor.red() * coef + averageColor.red() * (1 - coef)).toInt()
        val green = (centerColor.green() * coef + averageColor.green() * (1 - coef)).toInt()
        val blue = (centerColor.blue() * coef + averageColor.blue() * (1 - coef)).toInt()

        val limitedAlpha = min(max(alpha, 0), 255)
        val limitedRed = min(max(red, 0), 255)
        val limitedGreen = min(max(green, 0), 255)
        val limitedBlue = min(max(blue, 0), 255)

        return argbToInt(limitedAlpha, limitedRed, limitedGreen, limitedBlue)
    }

    fun getImageCoordinates(imageView: ImageView, x: Float, y: Float): PointF {
        val matrix = FloatArray(9)
        imageView.imageMatrix.getValues(matrix)

        val scaleX = matrix[Matrix.MSCALE_X]
        val scaleY = matrix[Matrix.MSCALE_Y]

        val transX = matrix[Matrix.MTRANS_X]
        val transY = matrix[Matrix.MTRANS_Y]

        val originalX = (x - transX) / scaleX
        val originalY = (y - transY) / scaleY

        return PointF(originalX, originalY)
    }
}