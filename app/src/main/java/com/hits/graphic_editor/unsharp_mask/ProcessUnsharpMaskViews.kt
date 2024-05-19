package com.hits.graphic_editor.unsharp_mask

import android.view.ViewGroup
import android.widget.SeekBar
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.UnsharpmaskBottomMenuBinding

fun addUnsharpMaskingControls(binding: ActivityNewProjectBinding, unsharpMask: UnsharpMask, unsharpMaskBinding: UnsharpmaskBottomMenuBinding) {
    binding.root.addView(
        unsharpMaskBinding.root,
        ConstraintLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomToBottom = binding.root.id
            startToStart = binding.root.id
            endToEnd = binding.root.id
        }
    )

    setupIconListeners(unsharpMask, unsharpMaskBinding)
    setupSeekBarListeners(unsharpMask, unsharpMaskBinding)
    setupApplyButtonListener(unsharpMask, unsharpMaskBinding)
}

fun removeUnsharpMaskingControls(binding: ActivityNewProjectBinding, unsharpMaskBinding: UnsharpmaskBottomMenuBinding) {
    binding.root.removeView(unsharpMaskBinding.root)
}

private fun setupIconListeners(unsharpMask: UnsharpMask, unsharpMaskBinding: UnsharpmaskBottomMenuBinding) {
    unsharpMaskBinding.radiusIcon.setOnClickListener {
        unsharpMaskBinding.radiusSeekBar.visibility = View.VISIBLE
        unsharpMaskBinding.amountSeekBar.visibility = View.GONE
        unsharpMaskBinding.thresholdSeekBar.visibility = View.GONE
        unsharpMaskBinding.radiusValue.visibility = View.VISIBLE
        unsharpMaskBinding.amountValue.visibility = View.GONE
        unsharpMaskBinding.thresholdValue.visibility = View.GONE
    }

    unsharpMaskBinding.amountIcon.setOnClickListener {
        unsharpMaskBinding.radiusSeekBar.visibility = View.GONE
        unsharpMaskBinding.amountSeekBar.visibility = View.VISIBLE
        unsharpMaskBinding.thresholdSeekBar.visibility = View.GONE
        unsharpMaskBinding.radiusValue.visibility = View.GONE
        unsharpMaskBinding.amountValue.visibility = View.VISIBLE
        unsharpMaskBinding.thresholdValue.visibility = View.GONE
    }

    unsharpMaskBinding.thresholdIcon.setOnClickListener {
        unsharpMaskBinding.radiusSeekBar.visibility = View.GONE
        unsharpMaskBinding.amountSeekBar.visibility = View.GONE
        unsharpMaskBinding.thresholdSeekBar.visibility = View.VISIBLE
        unsharpMaskBinding.radiusValue.visibility = View.GONE
        unsharpMaskBinding.amountValue.visibility = View.GONE
        unsharpMaskBinding.thresholdValue.visibility = View.VISIBLE
    }
}

private fun setupSeekBarListeners(unsharpMask: UnsharpMask, unsharpMaskBinding: UnsharpmaskBottomMenuBinding) {
    unsharpMaskBinding.radiusSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            unsharpMask.blurRadius = progress.toFloat()
            unsharpMaskBinding.radiusValue.text = "Радиус размытия: $progress"
            unsharpMask.applyUnsharpMasking()
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    })

    unsharpMaskBinding.amountSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            unsharpMask.amount = progress / 100f
            unsharpMaskBinding.amountValue.text = "Коэффициент усиления: ${unsharpMask.amount}"
            unsharpMask.applyUnsharpMasking()
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    })

    unsharpMaskBinding.thresholdSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            unsharpMask.threshold = progress
            unsharpMaskBinding.thresholdValue.text = "Порог: $progress"
            unsharpMask.applyUnsharpMasking()
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    })
}

private fun setupApplyButtonListener(unsharpMask: UnsharpMask, unsharpMaskBinding: UnsharpmaskBottomMenuBinding) {
    unsharpMaskBinding.applyButton.setOnClickListener {
        unsharpMask.applyChanges()
    }
}
