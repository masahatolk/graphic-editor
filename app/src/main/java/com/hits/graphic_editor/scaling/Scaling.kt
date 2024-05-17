package com.hits.graphic_editor.scaling

import android.view.LayoutInflater
import com.hits.graphic_editor.Filter
import com.hits.graphic_editor.custom_api.SimpleImage
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.ScalingBottomMenuBinding

class Scaling (
    override val binding: ActivityNewProjectBinding,
    override val layoutInflater: LayoutInflater
): Filter {

    lateinit var simpleImage : SimpleImage
    val scalingBottomMenu: ScalingBottomMenuBinding by lazy {
        ScalingBottomMenuBinding.inflate(layoutInflater)
    }

    override fun showBottomMenu () {

        addScalingBottomMenu(binding, scalingBottomMenu)

        //setListeners()
    }

    /*private fun setListeners() {
        scalingBottomMenu.scalingButton.setOnClickListener() {
            val coefficient: Int = Integer.parseInt(scalingBottomMenu.scalingCoefficient.text.toString())
            simpleImage = getScaledSimpleImage(
                simpleImage,
                coefficient.toFloat(),
                scalingBottomMenu.antialiasing.isChecked)
            binding.imageView.setImageBitmap(getBitMap(simpleImage))
        }
    }*/
}