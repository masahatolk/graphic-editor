package com.hits.graphic_editor.affine_transform

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.slider.Slider
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.AffineBottomMenuBinding
import com.hits.graphic_editor.utils.Filter
import com.hits.graphic_editor.utils.ProcessedImage
import kotlinx.coroutines.runBlocking

class AffineTransform(
    private val context: Context,
    override val binding: ActivityNewProjectBinding,
    override val layoutInflater: LayoutInflater,
    override val processedImage: ProcessedImage
):Filter {
    companion object{
        const val HASNT_OCCURRED= -1
    }
    override fun onStart() {
        addBottomMenu()
        setListeners()
        addGestures()
    }
    @SuppressLint("ClickableViewAccessibility")
    override fun onClose() {
        binding.imageView.setOnTouchListener(null)
        removeBottomMenu()
    }
    private val bottomMenu: AffineBottomMenuBinding by lazy {
        AffineBottomMenuBinding.inflate(layoutInflater)
    }
    private val transformBtn = bottomMenu.transformButton
    private val cropSwitch = bottomMenu.cropSwitch
    private val ratioSlider = bottomMenu.aspectRatioSlider

    private lateinit var pointTransfers: MutableList<PointTransfer>

    private var imageResult: AffineTransformedResult? = null
    private fun setListeners(){
        ratioSlider.value = processedImage.getSimpleImage().width /
                processedImage.getSimpleImage().height.toFloat()

        transformBtn.setOnClickListener {
            runBlocking {
                imageResult = getAffineTransformedResult(
                    processedImage.getMipMapsContainer(),
                    pointTransfers[0],
                    pointTransfers[1],
                    pointTransfers[2],
                    ratioSlider.value
                )
                if (imageResult != null) {
                    processedImage.addToLocalStackAndSetImageToView(
                        if (cropSwitch.isChecked) imageResult!!.getCropPreviewSimpleImage(ratioSlider.value)
                        else imageResult!!.getSimpleImage()
                    )
                }
            }
        }
        ratioSlider.addOnChangeListener() { _: Slider, value: Float, _: Boolean ->
            if (!cropSwitch.isChecked) return@addOnChangeListener

            if (imageResult != null) {
                processedImage.addToLocalStackAndSetImageToView(
                    imageResult!!.getCropPreviewSimpleImage(ratioSlider.value)
                )
            }
        }
        cropSwitch.setOnClickListener {
            if (imageResult != null) {
                processedImage.addToLocalStackAndSetImageToView(
                    if (cropSwitch.isChecked) imageResult!!.getCropPreviewSimpleImage(ratioSlider.value)
                    else imageResult!!.getSimpleImage()
                )
            }
        }
    }




    val pointerIds = mutableListOf(HASNT_OCCURRED, HASNT_OCCURRED,HASNT_OCCURRED)
    val pointerStartXs = mutableListOf(0F, 0F, 0F)
    val pointerStartYs = mutableListOf(0F, 0F, 0F)
    val pointerEndXs = mutableListOf(0F, 0F, 0F)
    val pointerEndYs = mutableListOf(0F, 0F, 0F)
    @SuppressLint("ClickableViewAccessibility")
    private fun addGestures(){
        binding.root.setOnTouchListener{ _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (pointerIds[0] == HASNT_OCCURRED) {
                        pointerStartXs[0] = event.x
                        pointerStartYs[0] = event.y
                        pointerIds[0] = event.getPointerId(event.actionIndex)
                    }
                }
                MotionEvent.ACTION_POINTER_DOWN -> {
                    for (i in 1 until 3) {
                        if (pointerIds[i] == HASNT_OCCURRED) {
                            pointerStartXs[i] = event.x
                            pointerStartYs[i] = event.y
                            pointerIds[i] = event.getPointerId(event.actionIndex)
                            break
                        }
                    }
                }
                MotionEvent.ACTION_POINTER_UP -> {
                    for (i in 0 until 3) {
                        if (pointerIds[i] == event.getPointerId(event.actionIndex)) {
                            pointerEndXs[i] = event.x
                            pointerEndYs[i] = event.y

                            if (pointerIds.all { it != HASNT_OCCURRED }) {
                                pointerIds.replaceAll { _ -> HASNT_OCCURRED }
                                val transformedImage = runBlocking {
                                    getAffineTransformedResult(
                                        processedImage.getMipMapsContainer(),
                                        PointTransfer(
                                            pointerStartXs[0],
                                            pointerEndXs[0],
                                            pointerStartYs[0],
                                            pointerEndYs[0]
                                        ),
                                        PointTransfer(
                                            pointerStartXs[1],
                                            pointerEndXs[1],
                                            pointerStartYs[1],
                                            pointerEndYs[1]
                                        ),
                                        PointTransfer(
                                            pointerStartXs[2],
                                            pointerEndXs[2],
                                            pointerStartYs[2],
                                            pointerEndYs[2]
                                        )
                                    )?.getSimpleImage()
                                }
                                if (transformedImage != null) {
                                    processedImage.addToLocalStackAndSetImageToView(transformedImage)
                                }
                                else{
                                    Toast.makeText(context, "No valid transform!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
                MotionEvent.ACTION_UP -> {
                    for (i in 0 until 3) {
                        if (pointerIds[i] == event.getPointerId(event.actionIndex)) {
                            pointerEndXs[i] = event.x
                            pointerEndYs[i] = event.y

                            if (pointerIds.all { it != HASNT_OCCURRED }) {
                                pointerIds.replaceAll { _ -> HASNT_OCCURRED }
                                val transformedImage = runBlocking {
                                    getAffineTransformedResult(
                                        processedImage.getMipMapsContainer(),
                                        PointTransfer(
                                            pointerStartXs[0],
                                            pointerEndXs[0],
                                            pointerStartYs[0],
                                            pointerEndYs[0]
                                        ),
                                        PointTransfer(
                                            pointerStartXs[1],
                                            pointerEndXs[1],
                                            pointerStartYs[1],
                                            pointerEndYs[1]
                                        ),
                                        PointTransfer(
                                            pointerStartXs[2],
                                            pointerEndXs[2],
                                            pointerStartYs[2],
                                            pointerEndYs[2]
                                        )
                                    )?.getSimpleImage()
                                }
                                if (transformedImage != null) {
                                    processedImage.addToLocalStackAndSetImageToView(transformedImage)
                                }
                                else{
                                    Toast.makeText(context, "No valid transform!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
            }
            true
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