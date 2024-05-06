package com.hits.graphic_editor

import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.BottomMenuBinding
import com.hits.graphic_editor.databinding.ExtraTopMenuBinding
import com.hits.graphic_editor.databinding.TopMenuBinding

fun addTopMenu (binding: ActivityNewProjectBinding, topMenu: TopMenuBinding) {
    binding.root.addView(
        topMenu.root,
        ConstraintLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            topToTop = binding.root.id
        }
    )
}
fun removeTopMenu (binding: ActivityNewProjectBinding, topMenu: TopMenuBinding) {
    binding.root.removeView(topMenu.root)
}
fun removeBottomMenu (binding: ActivityNewProjectBinding, bottomMenu: BottomMenuBinding) {
    binding.root.removeView(bottomMenu.root)
}
fun addBottomMenu (binding: ActivityNewProjectBinding, bottomMenu: BottomMenuBinding) {
    binding.root.addView(
        bottomMenu.root.rootView,
        ConstraintLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomToBottom = binding.root.id
        }
    )
}
fun addExtraTopMenu (binding: ActivityNewProjectBinding, extraTopMenu: ExtraTopMenuBinding) {
    binding.root.addView(
        extraTopMenu.root,
        ConstraintLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            topToTop = binding.root.id
        }
    )
}
fun removeExtraTopMenu (binding: ActivityNewProjectBinding, extraTopMenu: ExtraTopMenuBinding) {
    binding.root.removeView(extraTopMenu.root)
}