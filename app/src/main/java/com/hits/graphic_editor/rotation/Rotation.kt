package com.hits.graphic_editor.rotation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.hits.graphic_editor.utils.Filter
import com.hits.graphic_editor.custom_api.getBitMap
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.RotationBottomMenuBinding
import com.hits.graphic_editor.utils.ProcessedImage
import kotlinx.coroutines.runBlocking

class Rotation(
    override val binding: ActivityNewProjectBinding,
    override val layoutInflater: LayoutInflater,
    override val processedImage: ProcessedImage
) : Filter {
    override fun showBottomMenu() {
        addRotate90Button()
        setListenerToRotate90Button()
    }
    override fun removeAllMenus () {
        removeRotate90Button()
    }

    private var totalDegreeAngle: Int = 0
    private val bottomMenuBinding: RotationBottomMenuBinding by lazy {
        RotationBottomMenuBinding.inflate(layoutInflater)
    }

    private fun setListenerToRotate90Button() {
        //rotate90Button
        bottomMenuBinding.rotateButton.setOnClickListener {
            totalDegreeAngle = (totalDegreeAngle + 90) % 360
            runBlocking {
                processedImage.addToLocalStack(
                    getRotatedSimpleImage(
                        processedImage.getMipMapsContainer(),
                        totalDegreeAngle
                    )
                )
            }
            binding.imageView.setImageBitmap(getBitMap(processedImage.getSimpleImage()))
        }
    }

    private fun addRotate90Button() {
        binding.root.addView(
            bottomMenuBinding.root.rootView,
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
    private fun removeRotate90Button() {
        binding.root.removeView(bottomMenuBinding.root)
    }
}

