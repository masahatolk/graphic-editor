package com.hits.graphic_editor.scaling

import android.view.LayoutInflater
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.ScalingBottomMenuBinding

class Scaling (
    private val binding: ActivityNewProjectBinding,
    private val layoutInflater: LayoutInflater
) {

    val scalingBottomMenu: ScalingBottomMenuBinding by lazy {
        ScalingBottomMenuBinding.inflate(layoutInflater)
    }

    fun showBottomMenu () {

        addScalingBottomMenu(binding, scalingBottomMenu)


    }

}