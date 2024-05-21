package com.hits.graphic_editor.scaling

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.hits.graphic_editor.utils.Filter
import com.hits.graphic_editor.custom_api.SimpleImage
import com.hits.graphic_editor.custom_api.getBitMap
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.ScalingBottomMenuBinding
import com.hits.graphic_editor.utils.ProcessedImage
import kotlinx.coroutines.runBlocking

class Scaling (
    override val binding: ActivityNewProjectBinding,
    override val layoutInflater: LayoutInflater,
    override val processedImage: ProcessedImage
): Filter {
    override fun onStart () {
        addScalingBottomMenu()
        setListeners()
    }
    override fun onClose() {
        removeScalingBottomMenu()
    }

    private val scalingBottomMenu: ScalingBottomMenuBinding by lazy {
        ScalingBottomMenuBinding.inflate(layoutInflater)
    }

    private fun setListeners() {
        scalingBottomMenu.scalingButton.setOnClickListener() {
            val coefficient: Float = scalingBottomMenu.scalingCoefficient.text.toString().toFloat()
            runBlocking {
                processedImage.addToLocalStack(
                    getScaledSimpleImage(
                        processedImage.getMipMapsContainer(),
                        coefficient,
                        scalingBottomMenu.antialiasing.isChecked
                    )
                )
                binding.imageView.setImageBitmap(getBitMap(processedImage.getSimpleImage()))
            }
        }
    }
    private fun addScalingBottomMenu () {
        binding.root.addView(
            scalingBottomMenu.root.rootView,
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
    private fun removeScalingBottomMenu () {
        binding.root.removeView(scalingBottomMenu.root)
    }
}