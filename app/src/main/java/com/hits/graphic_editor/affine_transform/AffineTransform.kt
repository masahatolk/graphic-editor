package com.hits.graphic_editor.affine_transform

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.slider.Slider
import com.hits.graphic_editor.custom_api.argbToInt
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.AffineBottomMenuBinding
import com.hits.graphic_editor.utils.FVec2
import com.hits.graphic_editor.utils.Filter
import com.hits.graphic_editor.utils.ProcessedImage
import kotlinx.coroutines.runBlocking
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

class AffineTransform(
    private val context: Context,
    override val binding: ActivityNewProjectBinding,
    override val layoutInflater: LayoutInflater,
    override val processedImage: ProcessedImage
):Filter {
    override fun onStart() {
        addBottomMenu()
        setPaint()
        setListeners()
        addGestures()
    }
    override fun onClose(onSave: Boolean) {
        removeListeners()
        clearExtraImageView()
        if (onSave) addHighResImageToStack()
        removeBottomMenu()
    }
    private fun setPaint(){
        paint.strokeWidth = 9F.toViewPixels()
        paint.color = argbToInt(255, 200,200,200)
    }
    private fun clearExtraImageView(){
        binding.extraImageView.setImageResource(0)
    }
    private fun addHighResImageToStack() {
        if (pointTransfers.size == 3)
            runBlocking {
                imageResult = getAffineTransformedResult(
                    processedImage.getMipMapsContainer(),
                    pointTransfers[0],
                    pointTransfers[1],
                    pointTransfers[2],
                    ratioSlider.value
                )
                if (imageResult != null)
                    processedImage.addToLocalStack(
                        if (cropSwitch.isChecked) imageResult!!.getCroppedSimpleImage(
                            ratioSlider.value
                        )
                        else imageResult!!.getSimpleImage()
                    )
            }
    }
    private fun setListeners(){
        ratioSlider.value = processedImage.getSimpleImageBeforeFiltering().width /
                processedImage.getSimpleImageBeforeFiltering().height.toFloat()

        ratioSlider.addOnChangeListener() { _: Slider, value: Float, _: Boolean ->
            if (!cropSwitch.isChecked) return@addOnChangeListener

            if (imageResult != null) {
                processedImage.addToLocalStackAndSetImageToView(
                    imageResult!!.getCropPreviewSimpleImage(value)
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
    @SuppressLint("ClickableViewAccessibility")
    private fun removeListeners(){
        binding.imageView.setOnTouchListener(null)
    }


    private val bottomMenu: AffineBottomMenuBinding by lazy {
        AffineBottomMenuBinding.inflate(layoutInflater)
    }
    private val cropSwitch = bottomMenu.cropSwitch
    private val ratioSlider = bottomMenu.aspectRatioSlider


    private var pointTransfers: MutableList<PointTransfer> = mutableListOf()
    private var imageResult: AffineTransformedResult? = null
    private val canvasBitmap = Bitmap.createBitmap(
        processedImage.getSimpleImageBeforeFiltering().width,
        processedImage.getSimpleImageBeforeFiltering().height,
        Bitmap.Config.ARGB_8888)
    private val arrowsCanvas = Canvas(canvasBitmap)
    private val paint = Paint()
    private val maxPreviewResolution: Int =
        binding.imageView.measuredWidth * binding.imageView.measuredHeight / 4

    private fun Float.toViewPixels() =
        this * canvasBitmap.width / binding.imageView.measuredWidth
    private fun Canvas.drawArrow(tr: PointTransfer){
        this.drawLine(tr.fromX, tr.fromY, tr.toX, tr.toY, paint)
        this.drawCircle(tr.fromX, tr.fromY, 15F.toViewPixels(), paint)

        // arrow end
        var firstVec = FVec2(tr.fromX - tr.toX, tr.fromY - tr.toY)
        var secondVec = FVec2(tr.fromX - tr.toX, tr.fromY - tr.toY)
        val ang = PI/8

        firstVec.x = firstVec.x * cos(ang).toFloat() - firstVec.y * sin(ang).toFloat()
        firstVec.y = firstVec.x * sin(ang).toFloat() + firstVec.y * cos(ang).toFloat()
        firstVec.resize(50F.toViewPixels())

        secondVec.x = secondVec.x * cos(-ang).toFloat() - secondVec.y * sin(-ang).toFloat()
        secondVec.y = secondVec.x * sin(-ang).toFloat() + secondVec.y * cos(-ang).toFloat()
        secondVec.resize(50F.toViewPixels())

        firstVec += FVec2(tr.toX, tr.toY)
        secondVec += FVec2(tr.toX, tr.toY)

        this.drawLine(firstVec.x, firstVec.y, tr.toX, tr.toY, paint)
        this.drawLine(secondVec.x, secondVec.y, tr.toX, tr.toY, paint)
    }
    private fun Canvas.drawArrows(transfers: MutableList<PointTransfer>){
        transfers.forEach { this.drawArrow(it) }
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun addGestures(){
        var capturedPointIndex = 0
        var startPointCaptured = false

        binding.imageView.setOnTouchListener{ _, event ->
            val imageX = event.x * canvasBitmap.width / binding.imageView.measuredWidth
            val imageY = event.y * canvasBitmap.height / binding.imageView.measuredHeight

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {

                    for (i in 0 until pointTransfers.size){
                        if (hypot(pointTransfers[i].fromX - imageX,
                                pointTransfers[i].fromY - imageY) < 60F.toViewPixels()) {
                            capturedPointIndex = i
                            startPointCaptured = true
                            return@setOnTouchListener true
                        }
                        if (hypot(pointTransfers[i].toX - imageX,
                                pointTransfers[i].toY - imageY) < 60F.toViewPixels()) {
                            capturedPointIndex = i
                            startPointCaptured = false
                            return@setOnTouchListener true
                        }
                    }

                    if (pointTransfers.size > 2)
                        pointTransfers.removeFirst()

                    pointTransfers.add(PointTransfer(imageX, imageY, imageX, imageY))
                    capturedPointIndex = pointTransfers.size - 1
                    startPointCaptured = false
                }
                MotionEvent.ACTION_MOVE ->{

                    if (startPointCaptured) {
                        pointTransfers[capturedPointIndex].fromX = imageX
                        pointTransfers[capturedPointIndex].fromY = imageY
                    }
                    else{
                        pointTransfers[capturedPointIndex].toX = imageX
                        pointTransfers[capturedPointIndex].toY = imageY
                    }

                    arrowsCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                    arrowsCanvas.drawArrows(pointTransfers)
                    binding.extraImageView.setImageBitmap(canvasBitmap)
                }
                MotionEvent.ACTION_UP ->{

                    if (pointTransfers.size != 3) return@setOnTouchListener true

                    runBlocking {
                        imageResult = getAffineTransformedResult(
                            processedImage.getMipMapsContainer(),
                            pointTransfers[0],
                            pointTransfers[1],
                            pointTransfers[2],
                            ratioSlider.value,
                            maxPreviewResolution
                        )
                        if (imageResult != null) {
                            processedImage.addToLocalStackAndSetImageToView(
                                if (cropSwitch.isChecked) imageResult!!.getCropPreviewSimpleImage(
                                    ratioSlider.value
                                )
                                else imageResult!!.getSimpleImage()
                            )
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