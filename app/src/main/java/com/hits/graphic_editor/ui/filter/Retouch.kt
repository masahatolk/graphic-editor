package com.hits.graphic_editor.ui.filter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.hits.graphic_editor.SimpleImage
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.hits.graphic_editor.R
import com.hits.graphic_editor.argbToInt
import com.hits.graphic_editor.blue
import com.hits.graphic_editor.custom_api.IntColor
import com.hits.graphic_editor.custom_api.alpha
import com.hits.graphic_editor.custom_api.blendedIntColor
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.BottomMenuBinding
import com.hits.graphic_editor.databinding.ExtraTopMenuBinding
import com.hits.graphic_editor.databinding.TopMenuBinding
import com.hits.graphic_editor.getBitMap
import com.hits.graphic_editor.green
import com.hits.graphic_editor.red
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Retouch(
    private var simpleImage: SimpleImage,
    private val context: Context,
    private val binding: ActivityNewProjectBinding,
    private val layoutInflater: LayoutInflater
) {
    private var brushSize: Int = 10
    private var retouchCoefficient: Float = 0.5f
    private var lastX: Int = 0
    private var lastY: Int = 0

    private fun updateBrushSize(progress: Int) {
        brushSize = progress
    }

    private fun updateRetouchCoefficient(progress: Int) {
        retouchCoefficient = progress / 10f
    }

    private fun applyRetouchToImage(image: SimpleImage, touchX: Int, touchY: Int): SimpleImage {
        val retouchedImage = image.copy()
        val pixelColors = mutableListOf<IntColor>()
        val radiusSquared = brushSize * brushSize

        val centerX = touchX
        val centerY = touchY
        val startX = maxOf(0, centerX - brushSize)
        val startY = maxOf(0, centerY - brushSize)
        val endX = minOf(image.width - 1, centerX + brushSize)
        val endY = minOf(image.height - 1, centerY + brushSize)

        for (nx in startX..endX) {
            for (ny in startY..endY) {
                val dx = nx - centerX
                val dy = ny - centerY
                if (dx * dx + dy * dy <= radiusSquared) {
                    if (nx in 0 until image.width && ny in 0 until image.height) {
                        pixelColors.add(image[nx, ny])
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
                    if (nx in 0 until image.width && ny in 0 until image.height) {
                        retouchedImage[nx, ny] = blendColors(image[nx, ny], averageColor, retouchCoefficient)
                    }
                }
            }
        }

        return retouchedImage
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

    private fun isInRetouchArea(centerX: Int, centerY: Int, x: Int, y: Int, radius: Int): Boolean {
        val dx = x - centerX
        val dy = y - centerY
        return dx * dx + dy * dy <= radius * radius
    }

    private fun blendColors(color1: IntColor, color2: IntColor, coef: Float): IntColor {
        val alpha = (color1.alpha() * coef + color2.alpha() * (1 - coef)).toInt()
        val red = (color1.red() * coef + color2.red() * (1 - coef)).toInt()
        val green = (color1.green() * coef + color2.green() * (1 - coef)).toInt()
        val blue = (color1.blue() * coef + color2.blue() * (1 - coef)).toInt()
        return argbToInt(alpha, red, green, blue)
    }

    fun showSeekBarLayout() {
        val seekBarLayout = layoutInflater.inflate(R.layout.retouch_bottom_menu, null)
        val retouchCoefSeekBar = seekBarLayout.findViewById<SeekBar>(R.id.retouchCoef)
        val brushSizeSeekBar = seekBarLayout.findViewById<SeekBar>(R.id.brushSize)

        val brushSizeExplanation = seekBarLayout.findViewById<TextView>(R.id.brushSizeExplanation)
        val retouchCoefExplanation = seekBarLayout.findViewById<TextView>(R.id.retouchCoefExplanation)

        retouchCoefSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateRetouchCoefficient(progress)
                retouchCoefExplanation.text = "Коэффициент ретуширования: ${progress / 10f}"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        brushSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateBrushSize(progress)
                brushSizeExplanation.text = "Размер кисти: $progress"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val rootViewGroup = binding.root as ViewGroup

        rootViewGroup.addView(
            seekBarLayout,
            ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            }
        )
    }



    fun showBottomMenu(topMenu: TopMenuBinding, bottomMenu: BottomMenuBinding) {
        showSeekBarLayout()

        binding.imageView.setOnTouchListener { v, event ->
            val imageViewWidth = binding.imageView.width
            val imageViewHeight = binding.imageView.height
            val imageWidth = simpleImage.width
            val imageHeight = simpleImage.height


            val scaleX = imageViewWidth.toFloat() / imageWidth
            val scaleY = imageViewHeight.toFloat() / imageHeight
            val scale = scaleX.coerceAtMost(scaleY)

            val offsetX = (imageViewWidth - imageWidth * scale) / 2
            val offsetY = (imageViewHeight - imageHeight * scale) / 2

            val touchX = ((event.x - offsetX) / scale).toInt()
            val touchY = ((event.y - offsetY) / scale).toInt()

            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    if (touchX >= 0 && touchX < imageWidth && touchY >= 0 && touchY < imageHeight) {
                        simpleImage = applyRetouchToImage(simpleImage, touchX, touchY)
                        binding.imageView.setImageBitmap(getBitMap(simpleImage))
                    } else {

                        Log.e("Retouch", "Touch coordinates are out of bounds: ($touchX, $touchY)")
                    }
                }
            }
            true
        }


        val extraTopMenu: ExtraTopMenuBinding by lazy {
            ExtraTopMenuBinding.inflate(layoutInflater)
        }
        extraTopMenu.close.setOnClickListener {

        }

        extraTopMenu.save.setOnClickListener {

        }

        binding.root.addView(
            extraTopMenu.root,
            ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topToTop = binding.root.id
            }
        )
    }
}
