package com.hits.graphic_editor.unsharp_mask

import android.graphics.Bitmap
import android.view.LayoutInflater
import com.hits.graphic_editor.Filter
import com.hits.graphic_editor.custom_api.SimpleImage
import com.hits.graphic_editor.custom_api.getBitMap
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.UnsharpmaskBottomMenuBinding
import com.hits.graphic_editor.unsharp_mask.addUnsharpMaskingControls

class UnsharpMask(
    override val binding: ActivityNewProjectBinding,
    override val layoutInflater: LayoutInflater
) : Filter {

    lateinit var simpleImage: SimpleImage
    private var lastProcessedBitmap: Bitmap? = null
    var blurRadius: Float = 10f
    var amount: Float = 1.0f
    var threshold: Int = 0

    val unsharpMaskBinding: UnsharpmaskBottomMenuBinding by lazy {
        UnsharpmaskBottomMenuBinding.inflate(layoutInflater)
    }

    override fun showBottomMenu() {
        addUnsharpMaskingControls(binding, this, unsharpMaskBinding)
    }

    fun applyUnsharpMasking() {
        val inputBitmap = lastProcessedBitmap ?: getBitMap(simpleImage)
        val resultBitmap = unsharpMask(inputBitmap, blurRadius, amount, threshold)
        lastProcessedBitmap = resultBitmap
        binding.imageView.setImageBitmap(resultBitmap)
    }

    fun applyChanges() {
        lastProcessedBitmap = getBitMap(simpleImage)
        applyUnsharpMasking()
    }

    private fun unsharpMask(inputBitmap: Bitmap, radius: Float, amount: Float, threshold: Int): Bitmap {

        return inputBitmap
    }

    private fun applyGaussianBlurToImage(){

    }
}
