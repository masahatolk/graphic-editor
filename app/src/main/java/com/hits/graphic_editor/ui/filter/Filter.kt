package com.hits.graphic_editor.ui.filter

import android.view.LayoutInflater
import com.google.android.material.slider.Slider
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.hits.graphic_editor.addBottomMenu
import com.hits.graphic_editor.addTopMenu
import com.hits.graphic_editor.custom_api.SimpleImage
import com.hits.graphic_editor.custom_api.getBitMap
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.BottomMenuBinding
import com.hits.graphic_editor.databinding.ChannelShiftSliderBinding
import com.hits.graphic_editor.databinding.ContrastSliderBinding
import com.hits.graphic_editor.databinding.ExtraTopMenuBinding
import com.hits.graphic_editor.databinding.FilterRecyclerViewBinding
import com.hits.graphic_editor.databinding.GrainSliderBinding
import com.hits.graphic_editor.databinding.MosaicSliderBinding
import com.hits.graphic_editor.databinding.RgbMenuBinding
import com.hits.graphic_editor.databinding.TopMenuBinding
import com.hits.graphic_editor.getSuperSampledSimpleImage
import com.hits.graphic_editor.removeExtraTopMenu

class Filter(
    private var simpleImage: SimpleImage,
    private val binding: ActivityNewProjectBinding,
    private val layoutInflater: LayoutInflater,
    private var rgbMode: RGBMode
) {

    // -----------------create necessary fields-----------------
    private var simpleImageCopy: SimpleImage = simpleImage.copy(pixels = simpleImage.pixels.clone())
    private var smallSimpleImage: SimpleImage = getSuperSampledSimpleImage(simpleImage, 0.5F)
    private var verticalShift: Int = 0
    private var horizontalShift: Int = 0
    private var grainNumber: Int = 5
    private var squareSide: Int = 5
    private var contrastCoefficient: Int = 0

    private var adapter: FilterRecyclerViewAdapter =
        FilterRecyclerViewAdapter(getListOfSamples(), object : OnClickFilterListener {
            override fun onClick(filterMode: FilterMode) {
                updateFilterMode(filterMode)
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
    private val mosaicSlider: MosaicSliderBinding by lazy {
        MosaicSliderBinding.inflate(layoutInflater)
    }
    private val channelShiftSlider: ChannelShiftSliderBinding by lazy {
        ChannelShiftSliderBinding.inflate(layoutInflater)
    }
    private val grainSlider: GrainSliderBinding by lazy {
        GrainSliderBinding.inflate(layoutInflater)
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
            removeMosaicSlider(binding, mosaicSlider)
            removeChannelShiftSlider(binding, channelShiftSlider)
            removeGrainSlider(binding, grainSlider)

            addTopMenu(binding, topMenu)
            addBottomMenu(binding, bottomMenu)
        }

        extraTopMenu.save.setOnClickListener {

            simpleImage = simpleImageCopy.copy(pixels = simpleImage.pixels.clone())

            removeExtraTopMenu(binding, extraTopMenu)
            removeFilterBottomMenu(binding, filterBottomMenu)
            removeContrastSlider(binding, contrastSlider)
            removeRgbMenu(binding, rgbMenu)
            removeMosaicSlider(binding, mosaicSlider)
            removeChannelShiftSlider(binding, channelShiftSlider)
            removeGrainSlider(binding, grainSlider)

            addTopMenu(binding, topMenu)
            addBottomMenu(binding, bottomMenu)
        }
    }

    fun updateFilterMode(filterMode: FilterMode) {

        when (filterMode) {
            FilterMode.INVERSION -> {
                binding.imageView.setImageBitmap(getBitMap(inverse(simpleImageCopy)))
            }

            FilterMode.GRAYSCALE -> {
                binding.imageView.setImageBitmap(getBitMap(grayscale(simpleImageCopy)))
            }

            FilterMode.BLACK_AND_WHITE -> {
                binding.imageView.setImageBitmap(getBitMap(blackAndWhite(simpleImageCopy)))
            }

            FilterMode.SEPIA -> {
                binding.imageView.setImageBitmap(getBitMap(sepia(simpleImageCopy)))
            }

            FilterMode.CONTRAST -> {

                addContrastSlider(binding, contrastSlider)

                binding.imageView.setImageBitmap(
                    getBitMap(contrast(simpleImageCopy, contrastCoefficient))
                )

                val slider: Slider = contrastSlider.contrastSlider
                slider.addOnChangeListener(Slider.OnChangeListener { p0, p1, p2 ->
                    contrastCoefficient = p1.toInt()
                    binding.imageView.setImageBitmap(
                        getBitMap(contrast(simpleImageCopy, contrastCoefficient))
                    )
                })
            }

            FilterMode.RGB -> {
                addRgbMenu(binding, rgbMenu)

                binding.imageView.setImageBitmap(getBitMap(rgb(simpleImageCopy, rgbMode)))

                rgbMenu.root.addOnTabSelectedListener(object : OnTabSelectedListener {

                    override fun onTabSelected(p0: TabLayout.Tab?) {
                        when (rgbMenu.root.selectedTabPosition) {
                            0 -> {
                                rgbMode = RGBMode.RED
                            }

                            1 -> {
                                rgbMode = RGBMode.GREEN
                            }

                            2 -> {
                                rgbMode = RGBMode.BLUE
                            }
                        }
                        binding.imageView.setImageBitmap(getBitMap(rgb(simpleImage, rgbMode)))
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab) {}
                    override fun onTabReselected(tab: TabLayout.Tab) {}
                })
            }

            FilterMode.MOSAIC -> {

                addMosaicSlider(binding, mosaicSlider)

                binding.imageView.setImageBitmap(
                    getBitMap(mosaic(simpleImageCopy, squareSide))
                )

                val slider: Slider = mosaicSlider.mosaicSlider
                slider.addOnChangeListener(Slider.OnChangeListener { p0, p1, p2 ->
                    squareSide = p1.toInt()
                    binding.imageView.setImageBitmap(
                        getBitMap(mosaic(simpleImageCopy, squareSide))
                    )
                })
            }

            FilterMode.GRAIN -> {

                addGrainSlider(binding, grainSlider)

                binding.imageView.setImageBitmap(
                    getBitMap(grain(simpleImageCopy, grainNumber))
                )

                val slider: Slider = grainSlider.grainSlider
                slider.addOnChangeListener(Slider.OnChangeListener { p0, p1, p2 ->
                    grainNumber = p1.toInt()
                    binding.imageView.setImageBitmap(
                        getBitMap(grain(simpleImageCopy, grainNumber))
                    )
                })
            }

            FilterMode.CHANNEL_SHIFT -> {
                addChannelShiftSlider(binding, channelShiftSlider)

                binding.imageView.setImageBitmap(
                    getBitMap(channelShift(simpleImageCopy, horizontalShift, verticalShift))
                )

                val verticalSlider: Slider = channelShiftSlider.verticalSlider
                val horizontalSlider: Slider = channelShiftSlider.horizontalSlider

                verticalSlider.addOnChangeListener(Slider.OnChangeListener { p0, p1, p2 ->
                    verticalShift = p1.toInt()
                    binding.imageView.setImageBitmap(
                        getBitMap(channelShift(simpleImageCopy, horizontalShift, verticalShift))
                    )
                })
                horizontalSlider.addOnChangeListener(Slider.OnChangeListener { p0, p1, p2 ->
                    horizontalShift = p1.toInt()
                    binding.imageView.setImageBitmap(
                        getBitMap(channelShift(simpleImageCopy, horizontalShift, verticalShift))
                    )
                })
            }
        }

        if (filterMode != FilterMode.CONTRAST) removeContrastSlider(binding, contrastSlider)
        if (filterMode != FilterMode.RGB) removeRgbMenu(binding, rgbMenu)
        if (filterMode != FilterMode.MOSAIC) removeMosaicSlider(binding, mosaicSlider)
        if (filterMode != FilterMode.GRAIN) removeGrainSlider(binding, grainSlider)
        if (filterMode != FilterMode.CHANNEL_SHIFT) removeChannelShiftSlider(
            binding,
            channelShiftSlider
        )
    }

    private fun getListOfSamples(): MutableList<ItemFilter> {
        val items = mutableListOf<ItemFilter>()
        items.add(ItemFilter(getBitMap(inverse(smallSimpleImage)), FilterMode.INVERSION))
        items.add(ItemFilter(getBitMap(grayscale(smallSimpleImage)), FilterMode.GRAYSCALE))
        items.add(
            ItemFilter(
                getBitMap(blackAndWhite(smallSimpleImage)),
                FilterMode.BLACK_AND_WHITE
            )
        )
        items.add(ItemFilter(getBitMap(sepia(smallSimpleImage)), FilterMode.SEPIA))
        items.add(ItemFilter(getBitMap(contrast(smallSimpleImage, 100)), FilterMode.CONTRAST))
        items.add(ItemFilter(getBitMap(rgb(smallSimpleImage, rgbMode)), FilterMode.RGB))
        items.add(ItemFilter(getBitMap(mosaic(smallSimpleImage, 10)), FilterMode.MOSAIC))
        items.add(ItemFilter(getBitMap(grain(smallSimpleImage, 50)), FilterMode.GRAIN))
        items.add(
            ItemFilter(
                getBitMap(channelShift(smallSimpleImage, 100, 100)),
                FilterMode.CHANNEL_SHIFT
            )
        )

        return items
    }
}