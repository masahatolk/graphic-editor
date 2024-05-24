package com.hits.graphic_editor.scaling

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.hits.graphic_editor.utils.Filter
import com.hits.graphic_editor.custom_api.getBitMap
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.ScalingBottomMenuBinding
import com.hits.graphic_editor.utils.ProcessedImage
import kotlinx.coroutines.runBlocking
import kotlin.math.roundToInt
import kotlin.math.sqrt

class Scaling (
    override val binding: ActivityNewProjectBinding,
    override val layoutInflater: LayoutInflater,
    override val processedImage: ProcessedImage
): Filter {
    private fun TextView.toFloat() =
        this.text.toString().toFloat()
    private fun TextView.toFloatOrNull() =
        this.text.toString().toFloatOrNull()
    private fun TextView.toInt() =
        this.text.toString().toInt()
    private fun TextView.toIntOrNull() =
        this.text.toString().toIntOrNull()
    private fun EditText.onTextChanged(onTextChanged: (CharSequence?) -> Unit) {
        val thisEditText = this
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (thisEditText.hasFocus()) {
                    onTextChanged.invoke(s)
                }
            }

            override fun afterTextChanged(editable: Editable?) {
            }
        })
    }
    override fun onStart () {
        addBottomMenu()
        setOldResolution()
        setListeners()
    }
    private fun setOldResolution(){
        oldWidthView.text = oldWidth.toString()
        oldHeightView.text = oldHeight.toString()
    }
    override fun onClose(onSave: Boolean) {
        removeBottomMenu()
    }

    private val bottomMenu: ScalingBottomMenuBinding by lazy {
        ScalingBottomMenuBinding.inflate(layoutInflater)
    }
    private fun addBottomMenu () {
        binding.root.addView(
            bottomMenu.root.rootView,
            ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomToBottom = binding.root.id
                leftToLeft = binding.root.id
                rightToRight = binding.root.id
            }
        )
    }

    private val oldWidth = processedImage.getSimpleImage().width
    private val oldHeight = processedImage.getSimpleImage().height

    private val oldRatio = oldWidth / oldHeight.toFloat()
    private val maxHeight = sqrt(ProcessedImage.MAX_SIZE / oldRatio).toInt()
    private val maxWidth = sqrt(ProcessedImage.MAX_SIZE * oldRatio).toInt()
    private val minHeight = sqrt(ProcessedImage.MIN_SIZE / oldRatio).roundToInt()
    private val minWidth = sqrt(ProcessedImage.MIN_SIZE * oldRatio).roundToInt()

    private var scalingCoefficientView = bottomMenu.scalingCoefficient
    private var oldWidthView = bottomMenu.oldWidth
    private var oldHeightView = bottomMenu.oldHeight
    private var newWidthView = bottomMenu.newWidth
    private var newHeightView = bottomMenu.newHeight

    private var newHeight: Int = oldHeight
    private var newWidth: Int = oldWidth
    private var scaleCoefficient: Float = 1F
    private fun setListeners() {
        bottomMenu.scalingButton.setOnClickListener() {
            scalingCoefficientView.text?: return@setOnClickListener
            runBlocking {
                processedImage.addToLocalStack(
                    getScaledSimpleImageByNewWidth(
                        processedImage.getMipMapsContainer(),
                        newWidth,
                        bottomMenu.antialiasing.isChecked
                    )
                )
                binding.imageView.setImageBitmap(getBitMap(processedImage.getSimpleImage()))
            }
        }

        newWidthView.onTextChanged{
            newWidth = newWidthView.text.toString().toIntOrNull()
                ?.coerceAtLeast(minWidth)
                ?.coerceAtMost(maxWidth)
                ?:minWidth

            if (newWidth != newWidthView.toIntOrNull())
                newWidthView.setText(newWidth.toString())
            scaleCoefficient = newWidth / oldWidth.toFloat()
            newHeight = (oldHeight * scaleCoefficient).roundToInt()

            scalingCoefficientView.setText(scaleCoefficient.toString())
            newHeightView.setText(newHeight.toString())
        }
        newHeightView.onTextChanged{
            newHeight = newHeightView.text.toString().toIntOrNull()
                ?.coerceAtLeast(minHeight)
                ?.coerceAtMost(maxHeight)
                ?:minHeight

            if (newHeight != newHeightView.toIntOrNull())
                newHeightView.setText(newHeight.toString())
            scaleCoefficient = newHeight / oldHeight.toFloat()
            newWidth = (oldWidth * scaleCoefficient).roundToInt()

            scalingCoefficientView.setText(scaleCoefficient.toString())
            newWidthView.setText(newWidth.toString())
        }
        scalingCoefficientView.onTextChanged{
            scaleCoefficient = scalingCoefficientView.toFloatOrNull() ?: 1F

            newHeight = (oldHeight * scaleCoefficient).roundToInt()
                .coerceAtLeast(minHeight).coerceAtMost(maxHeight)
            newWidth = (oldWidth * scaleCoefficient).roundToInt()
                .coerceAtLeast(minWidth).coerceAtMost(maxWidth)

            newWidthView.setText(newWidth.toString())
            newHeightView.setText(newHeight.toString())
        }
    }
    private fun removeBottomMenu () {
        binding.root.removeView(bottomMenu.root)
    }
}