package com.hits.graphic_editor.affine_transform

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.Toast
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.utils.Filter
import com.hits.graphic_editor.utils.ProcessedImage
import kotlinx.coroutines.runBlocking

class AffineTransform(
    private val context: Context,
    override val binding: ActivityNewProjectBinding,
    override val layoutInflater: LayoutInflater,
    override val processedImage: ProcessedImage
):Filter {
    override fun showBottomMenu() {
        addGestures()
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun addGestures(){
        val HASNT_OCCURRED = -1

        val pointerIds = mutableListOf(HASNT_OCCURRED, HASNT_OCCURRED,HASNT_OCCURRED)
        val pointerStartXs = mutableListOf(0F, 0F, 0F)
        val pointerStartYs = mutableListOf(0F, 0F, 0F)
        val pointerEndXs = mutableListOf(0F, 0F, 0F)
        val pointerEndYs = mutableListOf(0F, 0F, 0F)

        //var isRunning = false
        binding.imageView.setOnTouchListener{ _, event ->
            //if (isRunning) return@setOnTouchListener true
            //isRunning = true
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
                                    )?.transformedImage
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
                                    )?.transformedImage
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
            //isRunning = false
            true
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun removeAllMenus() {
        binding.imageView.setOnTouchListener(null)
    }
}