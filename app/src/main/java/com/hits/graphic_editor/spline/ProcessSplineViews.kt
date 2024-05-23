package com.hits.graphic_editor.spline

import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.PolygonSplineChipBinding
import com.hits.graphic_editor.databinding.SplineBottomMenuBinding

fun addSplineBottomMenu(
    binding: ActivityNewProjectBinding,
    splineBottomMenu: SplineBottomMenuBinding
) {
    binding.root.addView(
        splineBottomMenu.root,
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

fun removeSplineBottomMenu(
    binding: ActivityNewProjectBinding,
    splineBottomMenu: SplineBottomMenuBinding
) {
    binding.root.removeView(splineBottomMenu.root)
}

fun addPolygonChip(
    binding: ActivityNewProjectBinding,
    polygonSplineChip: PolygonSplineChipBinding
) {
    binding.root.addView(
        polygonSplineChip.root,
        ConstraintLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomToTop = binding.guideline.id
            leftToLeft = binding.root.id
            rightToRight = binding.root.id
        }
    )
}

fun removePolygonChip(
    binding: ActivityNewProjectBinding,
    polygonSplineChip: PolygonSplineChipBinding
) {
    binding.root.removeView(polygonSplineChip.root)
}

fun removeAllSplineMenus(binding: ActivityNewProjectBinding, spline: Spline) {
    removeSplineBottomMenu(binding, spline.splineBottomMenu)
}