package com.hits.graphic_editor.cube_3d

import android.annotation.SuppressLint
import android.app.Activity
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.MotionEvent.AXIS_HSCROLL
import android.view.MotionEvent.AXIS_VSCROLL
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.slider.Slider
import com.hits.graphic_editor.custom_api.SimpleImage
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.Cube3dBottomMenuBinding
import com.hits.graphic_editor.utils.FVec3
import com.hits.graphic_editor.utils.Filter
import com.hits.graphic_editor.utils.ProcessedImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
class Cube3D(
    override val binding: ActivityNewProjectBinding,
    override val layoutInflater: LayoutInflater,
    override val processedImage: ProcessedImage,
    private val mainActivity: Activity
):Filter {
    override fun onStart() {
        initScene()
        addBottomMenu()
        addBindings()
        initGestures()
    }
    override fun onClose() {
        renderHighResFrame()
        processedImage.addToLocalStackAndSetImageToView(scene.canvas)
        removeScalingBottomMenu()
        deleteGestures()
        destroyScene()
    }

    private val bottomMenu: Cube3dBottomMenuBinding by lazy {
        Cube3dBottomMenuBinding.inflate(layoutInflater)
    }
    private val fovSlider = bottomMenu.FovSlider
    private val scene: Scene = Scene(
        processedImage.getSimpleImage(),
        Cube(processedImage.getSimpleImageBeforeFiltering()),
        Camera()
    )
    private var isRunning: Boolean = true
    private lateinit var frameJob: Job
    private val sceneLoopJob = CoroutineScope(Dispatchers.Default).launch(start = CoroutineStart.LAZY) {
        while (isRunning) {
            frameJob = CoroutineScope(Dispatchers.Default).launch {
                scene.renderFrame()
            }
            frameJob.join()
            val img = scene.canvas.copy(pixels = scene.canvas.pixels.clone())
            mainActivity.runOnUiThread{
                processedImage.addToLocalStackAndSetImageToView(img)
            }
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
    private fun removeScalingBottomMenu() {
        binding.root.removeView(bottomMenu.root)
    }
    private fun addBindings() {
        fovSlider.addOnChangeListener() { _: Slider, fl: Float, _: Boolean ->
            runBlocking { frameJob.join()}
            scene.setCameraFOV(fl.toInt())
        }
    }
    private fun initScene(){
        scene.setCameraFOV(fovSlider.value.toInt())
        scene.setResolution(binding.imageView.measuredWidth)
        sceneLoopJob.start()
    }
    private fun destroyScene(){
        isRunning = false
        runBlocking{sceneLoopJob.cancelAndJoin()}
    }
    private fun renderHighResFrame(){
        if (processedImage.getSimpleImageBeforeFiltering().width > binding.imageView.measuredWidth)
            scene.setResolution(processedImage.getSimpleImageBeforeFiltering().width)
        scene.renderFrame()
    }


    @SuppressLint("ClickableViewAccessibility")
    fun initGestures() {
         val scaleDetector = ScaleGestureDetector(
             mainActivity,
             object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    scene.changeObjectDistance((detector.previousSpan - detector.currentSpan)/100)
                    return true
                }
            })

        var oldX = 0F
        var oldY = 0F
        binding.imageView.setOnTouchListener { _, event ->
            runBlocking { frameJob.join()} // возможно тут много ивентов стакается и ждет поэтому подлагивает
            scaleDetector.onTouchEvent(event)

            if (event.pointerCount > 1)
                return@setOnTouchListener true

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    oldX = event.x
                    oldY = event.y
                }
                MotionEvent.ACTION_MOVE -> {
                    scene.rotateObject(FVec3(
                        (event.y - oldY)/500,
                        -(event.x - oldX)/500,
                        0F
                    ))

                    oldX = event.x
                    oldY = event.y
                }
            }
            true
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    fun deleteGestures(){
        binding.imageView.setOnTouchListener(null)
    }
}