package com.hits.graphic_editor.retouch

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.PointF
import android.view.LayoutInflater
import android.widget.ImageView
import com.hits.graphic_editor.custom_api.IntColor
import com.hits.graphic_editor.custom_api.SimpleImage
import com.hits.graphic_editor.custom_api.alpha
import com.hits.graphic_editor.custom_api.argbToInt
import com.hits.graphic_editor.custom_api.blue
import com.hits.graphic_editor.custom_api.getBitMap
import com.hits.graphic_editor.custom_api.green
import com.hits.graphic_editor.custom_api.red
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.utils.Filter
import com.hits.graphic_editor.utils.ProcessedImage

import kotlin.math.max
import kotlin.math.min

class Retouch(
    override val binding: ActivityNewProjectBinding,
    override val layoutInflater: LayoutInflater,
    override val processedImage: ProcessedImage
) : Filter {
    private lateinit var simpleImage: SimpleImage
    private var brushSize: Int = 10
    private var retouchCoefficient: Float = 0.5f
    var imageBitmap = getBitMap(simpleImage)

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
        val startX = max(0, centerX - brushSize)
        val startY = max(0, centerY - brushSize)
        val endX = min(bitmap.width - 1, centerX + brushSize)
        val endY = min(bitmap.height - 1, centerY + brushSize)

        for (nx in startX..endX) {
            for (ny in startY..endY) {
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

        for (nx in startX..endX) {
            for (ny in startY..endY) {
                val dx = nx - centerX
                val dy = ny - centerY
                if (dx * dx + dy * dy <= radiusSquared) {
                    if (nx in 0 until bitmap.width && ny in 0 until bitmap.height) {
                        val blendedColor = blendColors(bitmap.getPixel(nx, ny), averageColor, retouchCoefficient)
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

    private fun blendColors(color1: IntColor, color2: IntColor, coef: Float): IntColor {
        val alpha = (color1.alpha() * coef + color2.alpha() * (1 - coef)).toInt()
        val red = (color1.red() * coef + color2.red() * (1 - coef)).toInt()
        val green = (color1.green() * coef + color2.green() * (1 - coef)).toInt()
        val blue = (color1.blue() * coef + color2.blue() * (1 - coef)).toInt()
        return argbToInt(alpha, red, green, blue)
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

    override fun onClose() {
        TODO("Not yet implemented")
    }

    override fun onStart() {
        TODO("Not yet implemented")
    }
}


