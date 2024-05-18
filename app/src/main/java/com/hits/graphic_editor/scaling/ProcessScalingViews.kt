package com.hits.graphic_editor.scaling

import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.ScalingBottomMenuBinding

fun addScalingBottomMenu (binding: ActivityNewProjectBinding, scalingBottomMenuBinding: ScalingBottomMenuBinding) {
    binding.root.addView(
        scalingBottomMenuBinding.root.rootView,
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

fun removeScalingBottomMenu (binding: ActivityNewProjectBinding, scalingBottomMenuBinding: ScalingBottomMenuBinding) {
    binding.root.removeView(scalingBottomMenuBinding.root)
}

fun removeAllScalingMenus (binding: ActivityNewProjectBinding, scaling: Scaling) {
    removeScalingBottomMenu(binding, scaling.scalingBottomMenu)
}