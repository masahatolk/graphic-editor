package com.hits.graphic_editor.face_detection

import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.FaceDetectionBottomMenuBinding

fun addFaceDetectionBottomMenu(
    binding: ActivityNewProjectBinding,
    faceDetectionBottomMenu: FaceDetectionBottomMenuBinding
) {
    binding.root.addView(
        faceDetectionBottomMenu.root.rootView,
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

fun removeFaceDetectionBottomMenu(
    binding: ActivityNewProjectBinding,
    faceDetectionBottomMenu: FaceDetectionBottomMenuBinding
) {
    binding.root.removeView(faceDetectionBottomMenu.root)
}

fun removeAllFaceDetectionMenus (binding: ActivityNewProjectBinding, faceDetection: FaceDetection) {
    removeFaceDetectionBottomMenu(binding, faceDetection.faceDetectionBottomMenu)
}