package com.hits.graphic_editor.ui.filter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.slider.Slider
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.hits.graphic_editor.addBottomMenu
import com.hits.graphic_editor.addTopMenu
import com.hits.graphic_editor.custom_api.SimpleImage
import com.hits.graphic_editor.custom_api.alpha
import com.hits.graphic_editor.custom_api.argbToInt
import com.hits.graphic_editor.custom_api.blue
import com.hits.graphic_editor.custom_api.getBitMap
import com.hits.graphic_editor.custom_api.getSimpleImage
import com.hits.graphic_editor.custom_api.green
import com.hits.graphic_editor.custom_api.red
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.BottomMenuBinding
import com.hits.graphic_editor.databinding.ContrastSliderBinding
import com.hits.graphic_editor.databinding.ExtraTopMenuBinding
import com.hits.graphic_editor.databinding.FilterRecyclerViewBinding
import com.hits.graphic_editor.databinding.RgbMenuBinding
import com.hits.graphic_editor.databinding.TopMenuBinding
import com.hits.graphic_editor.getSuperSampledSimpleImage
import com.hits.graphic_editor.removeExtraTopMenu
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.min

class Filter(
    private var simpleImage: SimpleImage,
    private val binding: ActivityNewProjectBinding,
    private val layoutInflater: LayoutInflater
) {

    // -----------------create necessary fields-----------------
    private val simpleImageCopy: SimpleImage = simpleImage.copy(pixels = simpleImage.pixels.clone())
    private var smallSimpleImage: SimpleImage = getSuperSampledSimpleImage(simpleImage, 0.5F)

    private var adapter: FilterRecyclerViewAdapter =
        FilterRecyclerViewAdapter(getListOfSamples(), object : OnClickFilterListener {
            override fun onClick(filterName: String) {
                updateFilterMode(filterName)
            }
        })

    private val filterBottomMenu: FilterRecyclerViewBinding by lazy {
        FilterRecyclerViewBinding.inflate(layoutInflater)
    }
    private val contrastSlider: ContrastSliderBinding by lazy {
        ContrastSliderBinding.inflate(layoutInflater)
    }
    private val rgbMenu: RgbMenuBinding by lazy {
        RgbMenuBinding.inflate(layoutInflater)
    }

    // -----------------create necessary fields-----------------
    fun showBottomMenu(
        topMenu: TopMenuBinding,
        bottomMenu: BottomMenuBinding,
        extraTopMenu: ExtraTopMenuBinding
    ) {
        //TODO change it
        filterBottomMenu.filterRecyclerView.adapter = adapter

        addFilterBottomMenu(binding, filterBottomMenu)

        //TODO
        // ------------set listeners to extraTopMenu------------
        extraTopMenu.close.setOnClickListener {

            binding.imageView.setImageBitmap(getBitMap(simpleImage))

            // TODO put it in other place
            removeExtraTopMenu(binding, extraTopMenu)
            removeFilterBottomMenu(binding, filterBottomMenu)
            removeContrastSlider(binding, contrastSlider)
            removeRgbMenu(binding, rgbMenu)

            addTopMenu(binding, topMenu)
            addBottomMenu(binding, bottomMenu)
        }

        extraTopMenu.save.setOnClickListener {

            simpleImage = simpleImageCopy.copy(pixels = simpleImage.pixels.clone())

            removeExtraTopMenu(binding, extraTopMenu)
            removeFilterBottomMenu(binding, filterBottomMenu)
            removeContrastSlider(binding, contrastSlider)
            removeRgbMenu(binding, rgbMenu)

            addTopMenu(binding, topMenu)
            addBottomMenu(binding, bottomMenu)
        }
    }

    //TODO enum
    fun updateFilterMode(filterName: String) {

        when (filterName) {
            "Inversion" -> {
                binding.imageView.setImageBitmap(getBitMap(inverse(simpleImageCopy)))
            }

            "Grayscale" -> {
                binding.imageView.setImageBitmap(getBitMap(grayscale(simpleImageCopy)))
            }

            "Black and white" -> {
                binding.imageView.setImageBitmap(getBitMap(blackAndWhite(simpleImageCopy)))
            }

            "Sepia" -> {
                binding.imageView.setImageBitmap(getBitMap(sepia(simpleImageCopy)))
            }

            "Contrast" -> {

                val flow = MutableStateFlow(0f)

                addContrastSlider(binding, contrastSlider)

                //TODO
                val slider: Slider = contrastSlider.contrastSlider
                slider.addOnChangeListener(Slider.OnChangeListener { p0, p1, p2 ->
                    flow.update { p1 }
                })

                val scope = CoroutineScope(Dispatchers.Default)
                scope.launch {
                    flow.collect {
                        binding.imageView.setImageBitmap(
                            getBitMap(contrast(simpleImageCopy, it.toInt()))
                        )
                    }
                }
            }

            "RGB" -> {
                addRgbMenu(binding, rgbMenu)

                binding.imageView.setImageBitmap(getBitMap(rgb(simpleImageCopy, "red")))

                rgbMenu.root.addOnTabSelectedListener(object : OnTabSelectedListener {

                    override fun onTabSelected(p0: TabLayout.Tab?) {
                        when (rgbMenu.root.selectedTabPosition) {
                            0 -> {
                                binding.imageView.setImageBitmap(getBitMap(rgb(simpleImage, "red")))
                            }

                            1 -> {
                                binding.imageView.setImageBitmap(
                                    getBitMap(
                                        rgb(
                                            simpleImage,
                                            "green"
                                        )
                                    )
                                )
                            }

                            2 -> {
                                binding.imageView.setImageBitmap(
                                    getBitMap(
                                        rgb(
                                            simpleImage,
                                            "blue"
                                        )
                                    )
                                )
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

        if (filterName != "Contrast") binding.root.removeView(contrastSlider.root)
        if (filterName != "RGB") binding.root.removeView(rgbMenu.root)
    }

    //TODO do I need to create new file for all these algorithms?

    private fun inverse(simpleImage: SimpleImage): SimpleImage {

        val img = simpleImage.copy(pixels = simpleImage.pixels.clone())

        for (i in 0 until img.height) {
            for (j in 0 until img.width) {
                img[j, i] = argbToInt(
                    img[j, i].alpha(),
                    255 - img[j, i].red(),
                    255 - img[j, i].green(),
                    255 - img[j, i].blue()
                )
            }
        }

        return img
    }

    private fun grayscale(simpleImage: SimpleImage): SimpleImage {

        val img = simpleImage.copy(pixels = simpleImage.pixels.clone())

        for (i in 0 until img.height) {
            for (j in 0 until img.width) {

                val averageValue =
                    (img[j, i].red() + img[j, i].green() + img[j, i].blue()) / 3

                img[j, i] = argbToInt(
                    img[j, i].alpha(),
                    averageValue,
                    averageValue,
                    averageValue
                )
            }
        }

        return img
    }

    private fun blackAndWhite(simpleImage: SimpleImage): SimpleImage {

        val img = simpleImage.copy(pixels = simpleImage.pixels.clone())

        for (i in 0 until img.height) {
            for (j in 0 until img.width) {

                val averageValue =
                    (img[j, i].red() + img[j, i].green() + img[j, i].blue()) / 3

                val value: Int = if (averageValue > 128) 255
                else 0

                img[j, i] = argbToInt(
                    img[j, i].alpha(),
                    value,
                    value,
                    value
                )
            }
        }

        return img
    }

    private fun sepia(simpleImage: SimpleImage): SimpleImage {

        val img = simpleImage.copy(pixels = simpleImage.pixels.clone())

        for (i in 0 until img.height) {
            for (j in 0 until img.width) {

                val red =
                    min(
                        255, (img[j, i].red() * 0.393 +
                                img[j, i].green() * 0.769 +
                                img[j, i].blue() * 0.189).toInt()
                    )

                val green =
                    min(
                        255, (img[j, i].red() * 0.349 +
                                img[j, i].green() * 0.686 +
                                img[j, i].blue() * 0.168).toInt()
                    )

                val blue =
                    min(
                        255, (img[j, i].red() * 0.272 +
                                img[j, i].green() * 0.534 +
                                img[j, i].blue() * 0.131).toInt()
                    )

                img[j, i] = argbToInt(
                    img[j, i].alpha(),
                    red,
                    green,
                    blue
                )
            }
        }

        return img
    }

    private fun contrast(simpleImage: SimpleImage, contrastCoefficient: Int): SimpleImage {

        val img = simpleImage.copy(pixels = simpleImage.pixels.clone())
        val contrastCorrectionFactor: Float =
            259 * (contrastCoefficient.toFloat() + 255) / 255 / (259 - contrastCoefficient.toFloat())

        for (i in 0 until img.height) {
            for (j in 0 until img.width) {

                val red =
                    truncate((img[j, i].red() - 128).toFloat() * contrastCorrectionFactor + 128)
                val green =
                    truncate((img[j, i].green() - 128).toFloat() * contrastCorrectionFactor + 128)
                val blue =
                    truncate((img[j, i].blue() - 128).toFloat() * contrastCorrectionFactor + 128)

                img.pixels[i * img.width + j] = argbToInt(
                    img[j, i].alpha(),
                    red,
                    green,
                    blue
                )
            }
        }

        return img
    }

    //TODO make it normal pls using enum
    private fun rgb(simpleImage: SimpleImage, color: String): SimpleImage {
        val img = simpleImage.copy(pixels = simpleImage.pixels.clone())

        if (color == "red") {
            for (i in 0 until img.height) {
                for (j in 0 until img.width) {
                    img[j, i] = argbToInt(
                        img[j, i].alpha(),
                        img[j, i].red(),
                        0,
                        0
                    )
                }
            }
        } else if (color == "green") {
            for (i in 0 until img.height) {
                for (j in 0 until img.width) {
                    img[j, i] = argbToInt(
                        img[j, i].alpha(),
                        0,
                        img[j, i].green(),
                        0
                    )
                }
            }
        } else {
            for (i in 0 until img.height) {
                for (j in 0 until img.width) {
                    img[j, i] = argbToInt(
                        img[j, i].alpha(),
                        0,
                        0,
                        img[j, i].blue()
                    )
                }
            }
        }

        return img
    }

    private fun getListOfSamples(): MutableList<ItemFilter> {
        val items = mutableListOf<ItemFilter>()
        items.add(ItemFilter(getBitMap(inverse(smallSimpleImage)), "Inversion"))
        items.add(ItemFilter(getBitMap(grayscale(smallSimpleImage)), "Grayscale"))
        items.add(ItemFilter(getBitMap(blackAndWhite(smallSimpleImage)), "Black and white"))
        items.add(ItemFilter(getBitMap(sepia(smallSimpleImage)), "Sepia"))
        items.add(ItemFilter(getBitMap(contrast(smallSimpleImage, 100)), "Contrast"))
        items.add(ItemFilter(getBitMap(rgb(smallSimpleImage, "red")), "RGB"))
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