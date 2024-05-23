package com.hits.graphic_editor.affine_transform

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.slider.Slider
import com.hits.graphic_editor.custom_api.getBitMap
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.AffineBottomMenuBinding
import com.hits.graphic_editor.utils.Filter
import com.hits.graphic_editor.utils.ProcessedImage
import kotlinx.coroutines.runBlocking
import kotlin.math.hypot

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
        paint.strokeWidth = 10F
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

    private var pointTransfers: MutableList<PointTransfer> = mutableListOf()

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

    private val canvasBitmap = Bitmap.createBitmap(
        processedImage.getSimpleImage().width,
        processedImage.getSimpleImage().height,
        Bitmap.Config.ARGB_8888)
    private val arrowsCanvas = Canvas(canvasBitmap)
    private val paint = Paint()
    private fun Canvas.drawArrow(tr: PointTransfer){
        this.drawLine(tr.fromX, tr.fromY, tr.toX, tr.toY, paint)
    }
    private fun Canvas.drawArrows(transfers: MutableList<PointTransfer>){
        transfers.forEach { this.drawArrow(it) }
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun addGestures(){
        var capturedPointIndex = 0
        var startPointCaptured = false

        binding.extraImageView.setOnTouchListener{ _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {

                    for (i in 0 until pointTransfers.size){
                        if (hypot(pointTransfers[i].fromX - event.x,
                                pointTransfers[i].fromY - event.y) < 50) {
                            capturedPointIndex = i
                            startPointCaptured = true
                            return@setOnTouchListener true
                        }
                        if (hypot(pointTransfers[i].toX - event.x,
                                pointTransfers[i].toY - event.y) < 50) {
                            capturedPointIndex = i
                            startPointCaptured = false
                            return@setOnTouchListener true
                        }
                    }

                    if (pointTransfers.size > 2)
                        pointTransfers.removeFirst()

                    pointTransfers.add(PointTransfer(event.x, event.y, event.x, event.y))
                    capturedPointIndex = pointTransfers.size - 1
                    startPointCaptured = false
                }
                MotionEvent.ACTION_MOVE ->{
                    if (startPointCaptured) {
                        pointTransfers[capturedPointIndex].fromX = event.x
                        pointTransfers[capturedPointIndex].fromY = event.y
                    }
                    else{
                        pointTransfers[capturedPointIndex].toX = event.x
                        pointTransfers[capturedPointIndex].toY = event.y
                    }

                    arrowsCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                    arrowsCanvas.drawArrows(pointTransfers)
                    binding.extraImageView.setImageBitmap(canvasBitmap)
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