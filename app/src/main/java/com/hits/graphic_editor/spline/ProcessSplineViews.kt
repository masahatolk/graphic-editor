package com.hits.graphic_editor.spline

import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.SplineMenuButtonBinding

fun addSplineMenuButton(
    binding: ActivityNewProjectBinding,
    splineMenuButton: SplineMenuButtonBinding
) {
    binding.root.addView(
        splineMenuButton.root,
        ConstraintLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomToBottom = binding.guideline.id
            leftToLeft = binding.root.id
            rightToRight = binding.root.id
        }
    )
}

fun removeSplineMenuButton(
    binding: ActivityNewProjectBinding,
    splineMenuButton: SplineMenuButtonBinding
) {
    binding.root.removeView(splineMenuButton.root)
}