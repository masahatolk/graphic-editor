package com.hits.graphic_editor

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.scale
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.BottomMenuBinding
import com.hits.graphic_editor.databinding.ExtraTopMenuBinding
import com.hits.graphic_editor.databinding.FilterRecyclerViewBinding
import com.hits.graphic_editor.databinding.TopMenuBinding
import kotlin.math.min

class Filter(private val simpleImage: SimpleImage, context: Context, private val binding: ActivityNewProjectBinding) {

    //private var smallSimpleImage : SimpleImage =
    private var adapter: FilterRecyclerViewAdapter = FilterRecyclerViewAdapter(getListOfSamples(), object: onClickFilterListener{
        override fun onClick(filterName: String) {
            setFilter(filterName)
        }
    })

    fun showBottomMenu(
        topMenu : TopMenuBinding,
        bottomMenu : BottomMenuBinding,
        layoutInflater: LayoutInflater
    ) {

        val extraTopMenu: ExtraTopMenuBinding by lazy {
            ExtraTopMenuBinding.inflate(layoutInflater)
        }
        val filterBottomMenu: FilterRecyclerViewBinding by lazy {
            FilterRecyclerViewBinding.inflate(layoutInflater)
        }
        filterBottomMenu.filterRecyclerView.adapter = adapter

        binding.root.addView(
            extraTopMenu.root,
            ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topToTop = binding.root.id
            }
        )
        binding.root.addView(
            filterBottomMenu.root.rootView,
            ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomToBottom = binding.root.id
            }
        )

        extraTopMenu.close.setOnClickListener {
            binding.root.removeView(extraTopMenu.root)
            binding.root.removeView(filterBottomMenu.root)

            binding.root.addView(
                topMenu.root,
                ConstraintLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    topToTop = binding.root.id
                }
            )

            binding.root.addView(
                bottomMenu.root,
                ConstraintLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomToBottom = binding.root.id
                }
            )
        }
    }

    fun setFilter(filterName: String) {
        when(filterName) {
            "Inversion" -> {
                binding.imageView.setImageBitmap(getBitMap(inverse()))
            }
            "Grayscale" -> {
                binding.imageView.setImageBitmap(getBitMap(grayscale()))
            }
            "Black and white" -> {
                binding.imageView.setImageBitmap(getBitMap(blackAndWhite()))
            }
            "Sepia" -> {
                binding.imageView.setImageBitmap(getBitMap(sepia()))
            }
            "Contrast" -> {

            }
            "RGB" -> {

            }
            "Mosaic" -> {

            }
            "Dots" -> {

            }
            "Channel shift" -> {

            }
        }
    }

    private fun inverse() : SimpleImage {

        val img = simpleImage.copy(pixels = simpleImage.pixels.clone())

        for (i in 0 until img.height) {
            for (j in 0 until img.width) {
                img[i, j] = argbToInt(
                    img[i, j].alpha(),
                    255 - img[i, j].red(),
                    255 - img[i, j].green(),
                    255 - img[i, j].blue()
                )
            }
        }

        return img
    }

    private fun grayscale() : SimpleImage {

        val img = simpleImage.copy(pixels = simpleImage.pixels.clone())

        for (i in 0 until img.height) {
            for (j in 0 until img.width) {

                val averageValue =
                    (img[i, j].red() + img[i, j].green() + img[i, j].blue()) / 3

                img[i, j] = argbToInt(
                    img[i, j].alpha(),
                    averageValue,
                    averageValue,
                    averageValue
                )
            }
        }

        return img
    }

    private fun blackAndWhite() : SimpleImage {

        val img = simpleImage.copy(pixels = simpleImage.pixels.clone())

        for (i in 0 until img.height) {
            for (j in 0 until img.width) {

                val averageValue =
                    (img[i, j].red() + img[i, j].green() + img[i, j].blue()) / 3

                val value: Int
                if (averageValue > 128) value = 255
                else value = 0

                img[i, j] = argbToInt(
                    img[i, j].alpha(),
                    value,
                    value,
                    value
                )
            }
        }

        return img
    }

    private fun sepia() : SimpleImage {

        val img = simpleImage.copy(pixels = simpleImage.pixels.clone())

        for (i in 0 until img.height) {
            for (j in 0 until img.width) {

                val red =
                    min(
                        255, (img[i, j].red() * 0.393 +
                                img[i, j].green() * 0.769 +
                                img[i, j].blue() * 0.189).toInt()
                    )

                val green =
                    min(
                        255, (img[i, j].red() * 0.349 +
                                img[i, j].green() * 0.686 +
                                img[i, j].blue() * 0.168).toInt()
                    )

                val blue =
                    min(
                        255, (img[i, j].red() * 0.272 +
                                img[i, j].green() * 0.534 +
                                img[i, j].blue() * 0.131).toInt()
                    )

                img[i, j] = argbToInt(
                    img[i, j].alpha(),
                    red,
                    green,
                    blue
                )
            }
        }

        return img
    }

    /*fun SimpleImage.contrast(contrastCoefficient: Float) {

        for (i in 0 until this.height) {
            for (j in 0 until this.width) {

                this.pixels[i * this.width + j] = argbToInt(
                    this.pixels[i * this.width + j].alpha(),
                    this.pixels[i * this.width + j].red() * contrastCoefficient,
                    this.pixels[i * this.width + j].green() * contrastCoefficient,
                    this.pixels[i * this.width + j].blue() * contrastCoefficient
                )
            }
        }
    }*/

    fun rgb() {

    }

    //TODO add scaling using Vasya's algorithm
    private fun getListOfSamples() : MutableList<ItemFilter> {
        val items = mutableListOf<ItemFilter>()
        items.add(ItemFilter(getBitMap(inverse()), "Inversion"))
        items.add(ItemFilter(getBitMap(grayscale()), "Grayscale"))
        items.add(ItemFilter(getBitMap(blackAndWhite()), "Black and white"))
        items.add(ItemFilter(getBitMap(sepia()), "Sepia"))
        //contrast
        //rgb
        //mosaic
        //dots
        //channel shift

        return items
    }
}