package com.hits.graphic_editor.ui.filter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import com.hits.graphic_editor.SimpleImage
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.SeekBar
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
        return applyRetouchToPixel(image, touchX, touchY, brushSize, retouchCoefficient)
    }

    private fun applyRetouchToPixel(image: SimpleImage, x: Int, y: Int, brushSize: Int, retouchCoef: Float): SimpleImage {
        val retouchedImage = simpleImage.copy(pixels = simpleImage.pixels.clone())
        val backgroundColor = calculateBackgroundColor(image, x, y, brushSize)

        for (dx in -brushSize..brushSize) {
            for (dy in -brushSize..brushSize) {
                val nx = x + dx
                val ny = y + dy

                if (nx in 0 until image.width && ny in 0 until image.height) {
                    val pixelColor = image[nx, ny]

                    val retouchedColor = if (isInRetouchArea(x, y, nx, ny, brushSize)) {
                        blendColors(pixelColor, backgroundColor, retouchCoef)
                    } else {
                        pixelColor
                    }

                    retouchedImage[nx, ny] = retouchedColor
                }
            }
        }

        return retouchedImage
    }

    private fun calculateBackgroundColor(image: SimpleImage, centerX: Int, centerY: Int, radius: Int): IntColor {
        return image[centerX, centerY]
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

        retouchCoefSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateRetouchCoefficient(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        brushSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateBrushSize(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.root.addView(
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

            val scaleX = imageWidth.toFloat() / imageViewWidth
            val scaleY = imageHeight.toFloat() / imageViewHeight

            val touchX = (event.x * scaleX).toInt()
            val touchY = (event.y * scaleY).toInt()
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastX = touchX
                    lastY = touchY
                    simpleImage = applyRetouchToImage(simpleImage,lastX,lastY)
                    binding.imageView.setImageBitmap(getBitMap(simpleImage))
                }
                MotionEvent.ACTION_MOVE -> {
                    lastX = event.x.toInt()
                    lastY = event.y.toInt()
                    simpleImage = applyRetouchToImage(simpleImage,lastX,lastY)
                    binding.imageView.setImageBitmap(getBitMap(simpleImage))
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
