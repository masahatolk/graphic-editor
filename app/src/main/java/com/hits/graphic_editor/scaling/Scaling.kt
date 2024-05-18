package com.hits.graphic_editor.scaling

import android.view.LayoutInflater
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
    val scalingBottomMenu: ScalingBottomMenuBinding by lazy {
        ScalingBottomMenuBinding.inflate(layoutInflater)
    }

    override fun showBottomMenu () {
        addScalingBottomMenu(binding, scalingBottomMenu)
        setListeners()
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
}