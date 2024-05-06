package com.hits.graphic_editor.ui.filter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.slider.Slider
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.hits.graphic_editor.SimpleImage
import com.hits.graphic_editor.alpha
import com.hits.graphic_editor.argbToInt
import com.hits.graphic_editor.blue
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.BottomMenuBinding
import com.hits.graphic_editor.databinding.ContrastSliderBinding
import com.hits.graphic_editor.databinding.ExtraTopMenuBinding
import com.hits.graphic_editor.databinding.FilterRecyclerViewBinding
import com.hits.graphic_editor.databinding.RgbMenuBinding
import com.hits.graphic_editor.databinding.TopMenuBinding
import com.hits.graphic_editor.getBitMap
import com.hits.graphic_editor.green
import com.hits.graphic_editor.red
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.math.min

class Filter(
    private val simpleImage: SimpleImage,
    private val context: Context,
    private val binding: ActivityNewProjectBinding,
    private val layoutInflater: LayoutInflater
) {

    //private var smallSimpleImage : SimpleImage =
    private var adapter: FilterRecyclerViewAdapter =
        FilterRecyclerViewAdapter(getListOfSamples(), object : OnClickFilterListener {
            override fun onClick(filterName: String) {
                updateFilterMode(filterName)
            }
        })

    private val contrastSlider: ContrastSliderBinding by lazy {
        ContrastSliderBinding.inflate(layoutInflater)
    }
    private val rgbMenu : RgbMenuBinding by lazy {
        RgbMenuBinding.inflate(layoutInflater)
    }

    fun showBottomMenu(
        topMenu: TopMenuBinding,
        bottomMenu: BottomMenuBinding
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

            binding.imageView.setImageBitmap(getBitMap(simpleImage))

            binding.root.removeView(extraTopMenu.root)
            binding.root.removeView(filterBottomMenu.root)
            binding.root.removeView(contrastSlider.root)
            binding.root.removeView(rgbMenu.root)

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

        extraTopMenu.save.setOnClickListener {

            binding.root.removeView(extraTopMenu.root)
            binding.root.removeView(filterBottomMenu.root)
            binding.root.removeView(contrastSlider.root)
            binding.root.removeView(rgbMenu.root)

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

    fun updateFilterMode(filterName: String) {

        when (filterName) {
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

                val flow = MutableStateFlow(0f)

                binding.root.addView(
                    contrastSlider.root.rootView,
                    ConstraintLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        bottomToBottom = binding.guideline.id
                        leftToLeft = binding.root.id
                        rightToRight = binding.root.id
                    }
                )
                //TODO
                val slider: Slider = contrastSlider.contrastSlider
                slider.addOnChangeListener(Slider.OnChangeListener { p0, p1, p2 ->
                    flow.update { p1 }
                })

                val scope = CoroutineScope(Dispatchers.Default)
                scope.launch {
                    flow.collect{
                        binding.imageView.setImageBitmap(
                            getBitMap(contrast(it.toInt()))
                        )
                    }
                }
            }

            "RGB" -> {
                binding.root.addView(
                    rgbMenu.root.rootView,
                    ConstraintLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        bottomToBottom = binding.guideline.id
                        leftToLeft = binding.root.id
                        rightToRight = binding.root.id
                    }
                )

                binding.imageView.setImageBitmap(getBitMap(rgb("red")))

                rgbMenu.root.addOnTabSelectedListener(object : OnTabSelectedListener {

                    override fun onTabSelected(p0: TabLayout.Tab?) {
                        when(rgbMenu.root.selectedTabPosition) {
                            0 -> {
                                binding.imageView.setImageBitmap(getBitMap(rgb("red")))
                            }
                            1 -> {
                                binding.imageView.setImageBitmap(getBitMap(rgb("green")))
                            }
                            2 -> {
                                binding.imageView.setImageBitmap(getBitMap(rgb("blue")))
                            }
                        }
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab) {}
                    override fun onTabReselected(tab: TabLayout.Tab) {}
                })
            }

            "Mosaic" -> {

            }

            "Dots" -> {

            }

            "Channel shift" -> {

            }
        }

        if(filterName != "Contrast") binding.root.removeView(contrastSlider.root)
        if(filterName != "RGB") binding.root.removeView(rgbMenu.root)
    }

    private fun inverse(): SimpleImage {

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

    private fun grayscale(): SimpleImage {

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

    private fun blackAndWhite(): SimpleImage {

        val img = simpleImage.copy(pixels = simpleImage.pixels.clone())

        for (i in 0 until img.height) {
            for (j in 0 until img.width) {

                val averageValue =
                    (img[i, j].red() + img[i, j].green() + img[i, j].blue()) / 3

                val value: Int = if (averageValue > 128) 255
                else 0

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

    private fun sepia(): SimpleImage {

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

    private fun contrast(contrastCoefficient: Int): SimpleImage {

        val img = simpleImage.copy(pixels = simpleImage.pixels.clone())
        val contrastCorrectionFactor: Float =
            259 * (contrastCoefficient.toFloat() + 255) / 255 / (259 - contrastCoefficient.toFloat())

        for (i in 0 until img.height) {
            for (j in 0 until img.width) {

                val red =
                    truncate((img[i, j].red() - 128).toFloat() * contrastCorrectionFactor + 128)
                val green =
                    truncate((img[i, j].green() - 128).toFloat() * contrastCorrectionFactor + 128)
                val blue =
                    truncate((img[i, j].blue() - 128).toFloat() * contrastCorrectionFactor + 128)

                img.pixels[i * img.width + j] = argbToInt(
                    img[i, j].alpha(),
                    red,
                    green,
                    blue
                )
            }
        }

        return img
    }

    //TODO make it normal pls
    private fun rgb(color: String) : SimpleImage {
        val img = simpleImage.copy(pixels = simpleImage.pixels.clone())

        if(color == "red") {
            for (i in 0 until img.height) {
                for (j in 0 until img.width) {
                    img[i, j] = argbToInt(
                        img[i, j].alpha(),
                        img[i, j].red(),
                        0,
                        0
                    )
                }
            }
        }
        else if(color == "green") {
            for (i in 0 until img.height) {
                for (j in 0 until img.width) {
                    img[i, j] = argbToInt(
                        img[i, j].alpha(),
                        0,
                        img[i, j].green(),
                        0
                    )
                }
            }
        }
        else {
            for (i in 0 until img.height) {
                for (j in 0 until img.width) {
                    img[i, j] = argbToInt(
                        img[i, j].alpha(),
                        0,
                        0,
                        img[i, j].blue()
                    )
                }
            }
        }

        return img
    }

    //TODO add scaling using Vasya's algorithm
    private fun getListOfSamples(): MutableList<ItemFilter> {
        val items = mutableListOf<ItemFilter>()
        items.add(ItemFilter(getBitMap(inverse()), "Inversion"))
        items.add(ItemFilter(getBitMap(grayscale()), "Grayscale"))
        items.add(ItemFilter(getBitMap(blackAndWhite()), "Black and white"))
        items.add(ItemFilter(getBitMap(sepia()), "Sepia"))
        items.add(ItemFilter(getBitMap(contrast(100)), "Contrast"))
        items.add(ItemFilter(getBitMap(rgb("red")), "RGB"))
        //mosaic
        //dots
        //channel shift

        return items
    }

    private fun truncate(x: Float): Int {
        if (x < 0) return 0
        else if (x > 255) return 255
        return x.toInt()
    }
}