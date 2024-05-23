package com.hits.graphic_editor.rotation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.slider.Slider
import com.hits.graphic_editor.affine_transform.AffineTransformedResult
import com.hits.graphic_editor.cube_3d.setCameraFOV
import com.hits.graphic_editor.utils.Filter
import com.hits.graphic_editor.custom_api.getBitMap
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.RotationBottomMenuBinding
import com.hits.graphic_editor.ui.addBottomMenu
import com.hits.graphic_editor.utils.ProcessedImage
import kotlinx.coroutines.runBlocking

class Rotation(
    override val binding: ActivityNewProjectBinding,
    override val layoutInflater: LayoutInflater,
    override val processedImage: ProcessedImage
) : Filter {
    override fun onStart() {
        addBottomMenu()
        setListeners()
    }
    override fun onClose() {
        processedImage.addToLocalStack(
            if (cropSwitch.isChecked) imageResult.getCroppedSimpleImage(ratioSlider.value)
            else imageResult.getSimpleImage()
        )
        removeBottomMenu()
    }

    private val bottomMenu: RotationBottomMenuBinding by lazy {
        RotationBottomMenuBinding.inflate(layoutInflater)
    }

    private val cropSwitch = bottomMenu.cropSwitch
    private val rotate90Btn = bottomMenu.rotate90Button
    private val rotationSlider = bottomMenu.rotationSlider
    private val ratioSlider = bottomMenu.aspectRatioSlider

    private fun totalDegreeAngle() = rotationSlider.value.toInt()
    private var imageResult: AffineTransformedResult = runBlocking {
        getRotatedImageResult(
            processedImage.getMipMapsContainer(),
            totalDegreeAngle(),
        )
    }
    private fun setListeners() {
        ratioSlider.value = processedImage.getSimpleImage().width /
                processedImage.getSimpleImage().height.toFloat()

        rotate90Btn.setOnClickListener {
            rotationSlider.value = (rotationSlider.value + 270) % 360 - 180
        }
        rotationSlider.addOnChangeListener() { _: Slider, value: Float, _: Boolean ->
            runBlocking {
                imageResult = getRotatedImageResult(
                    processedImage.getMipMapsContainer(),
                    value.toInt(),
                    ratioSlider.value
                )
                processedImage.addToLocalStackAndSetImageToView(
                    if (cropSwitch.isChecked) imageResult.getCropPreviewSimpleImage(ratioSlider.value)
                    else imageResult.getSimpleImage()
                )
            }
        }
        ratioSlider.addOnChangeListener() { _: Slider, value: Float, _: Boolean ->
            if (!cropSwitch.isChecked) return@addOnChangeListener

            processedImage.addToLocalStackAndSetImageToView(
                imageResult.getCropPreviewSimpleImage(ratioSlider.value)
            )
        }
        cropSwitch.setOnClickListener {
            processedImage.addToLocalStackAndSetImageToView(
                if (cropSwitch.isChecked) imageResult.getCropPreviewSimpleImage(ratioSlider.value)
                else imageResult.getSimpleImage()
            )
        }
    }
    private fun addBottomMenu() {
        binding.root.addView(
            bottomMenu.root.rootView,
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
    private fun removeBottomMenu() {
        binding.root.removeView(bottomMenu.root)
    }
}