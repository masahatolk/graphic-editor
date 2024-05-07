package com.hits.graphic_editor.ui.filter

import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.ChannelShiftSliderBinding
import com.hits.graphic_editor.databinding.ContrastSliderBinding
import com.hits.graphic_editor.databinding.FilterRecyclerViewBinding
import com.hits.graphic_editor.databinding.GrainSliderBinding
import com.hits.graphic_editor.databinding.MosaicSliderBinding
import com.hits.graphic_editor.databinding.RgbMenuBinding

fun addFilterBottomMenu (binding: ActivityNewProjectBinding, filterBottomMenu: FilterRecyclerViewBinding) {
    binding.root.addView(
        filterBottomMenu.root.rootView,
        ConstraintLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomToBottom = binding.root.id
        }
    )
}

fun removeFilterBottomMenu (binding: ActivityNewProjectBinding, filterBottomMenu: FilterRecyclerViewBinding) {
    binding.root.removeView(filterBottomMenu.root)
}
fun addContrastSlider (binding: ActivityNewProjectBinding, contrastSlider: ContrastSliderBinding) {
    binding.root.addView(
        contrastSlider.root.rootView,
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
fun removeContrastSlider (binding: ActivityNewProjectBinding, contrastSlider: ContrastSliderBinding) {
    binding.root.removeView(contrastSlider.root)
}

fun addMosaicSlider (binding: ActivityNewProjectBinding, mosaicSlider: MosaicSliderBinding) {
    binding.root.addView(
        mosaicSlider.root.rootView,
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
fun removeMosaicSlider (binding: ActivityNewProjectBinding, mosaicSlider: MosaicSliderBinding) {
    binding.root.removeView(mosaicSlider.root)
}

fun addChannelShiftSlider (binding: ActivityNewProjectBinding, channelShiftSlider: ChannelShiftSliderBinding) {
    binding.root.addView(
        channelShiftSlider.root.rootView,
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

fun removeChannelShiftSlider (binding: ActivityNewProjectBinding, channelShiftSlider: ChannelShiftSliderBinding) {
    binding.root.removeView(channelShiftSlider.root)
}

fun addGrainSlider (binding: ActivityNewProjectBinding, grainSlider: GrainSliderBinding) {
    binding.root.addView(
        grainSlider.root.rootView,
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

fun removeGrainSlider (binding: ActivityNewProjectBinding, grainSlider: GrainSliderBinding) {
    binding.root.removeView(grainSlider.root)
}

fun addRgbMenu (binding: ActivityNewProjectBinding, rgbMenu: RgbMenuBinding) {
    binding.root.addView(
        rgbMenu.root.rootView,
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
fun removeRgbMenu (binding: ActivityNewProjectBinding, rgbMenu: RgbMenuBinding) {
    binding.root.removeView(rgbMenu.root)
}
