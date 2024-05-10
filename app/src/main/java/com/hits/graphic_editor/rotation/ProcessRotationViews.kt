package com.hits.graphic_editor.rotation

import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.RotationBottomMenuBinding


fun addRotateButton(binding: ActivityNewProjectBinding, rotateButton: RotationBottomMenuBinding) {
    binding.root.addView(
        rotateButton.root.rootView,
        ConstraintLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomToBottom = binding.root.id
            leftToLeft = binding.root.id
            rightToRight = binding.root.id
            verticalBias = 0.3F
        }
    )
}

fun removeButton(binding: ActivityNewProjectBinding, rotateButton: RotationBottomMenuBinding) {
    binding.root.removeView(rotateButton.root)
}

fun removeAllRotateMenus (binding: ActivityNewProjectBinding, rotation: Rotation) {
    removeButton(binding, rotation.rotateButton)
}