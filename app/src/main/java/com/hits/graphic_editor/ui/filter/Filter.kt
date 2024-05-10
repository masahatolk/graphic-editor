package com.hits.graphic_editor.ui.filter

import android.view.LayoutInflater
import com.google.android.material.slider.Slider
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.hits.graphic_editor.custom_api.SimpleImage
import com.hits.graphic_editor.custom_api.getBitMap
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.ChannelShiftSliderBinding
import com.hits.graphic_editor.databinding.ContrastSliderBinding
import com.hits.graphic_editor.databinding.FilterRecyclerViewBinding
import com.hits.graphic_editor.databinding.GrainSliderBinding
import com.hits.graphic_editor.databinding.MosaicSliderBinding
import com.hits.graphic_editor.databinding.RgbMenuBinding
import com.hits.graphic_editor.getSuperSampledSimpleImage

class Filter(
    private val binding: ActivityNewProjectBinding,
    private val layoutInflater: LayoutInflater,
    private var rgbMode: RGBMode
) {

    // -----------------create necessary fields-----------------
    var simpleImage : SimpleImage? = null
    private var smallSimpleImage: SimpleImage? = null
    private var verticalShift: Int = 0
    private var horizontalShift: Int = 0
    private var grainNumber: Int = 5
    private var squareSide: Int = 5
    private var contrastCoefficient: Int = 0


    val filterBottomMenu: FilterRecyclerViewBinding by lazy {
        FilterRecyclerViewBinding.inflate(layoutInflater)
    }
    val contrastSlider: ContrastSliderBinding by lazy {
        ContrastSliderBinding.inflate(layoutInflater)
    }
    val rgbMenu: RgbMenuBinding by lazy {
        RgbMenuBinding.inflate(layoutInflater)
    }
    val mosaicSlider: MosaicSliderBinding by lazy {
        MosaicSliderBinding.inflate(layoutInflater)
    }
    val channelShiftSlider: ChannelShiftSliderBinding by lazy {
        ChannelShiftSliderBinding.inflate(layoutInflater)
    }
    val grainSlider: GrainSliderBinding by lazy {
        GrainSliderBinding.inflate(layoutInflater)
    }
    private var adapter: FilterRecyclerViewAdapter =
        FilterRecyclerViewAdapter(getListOfSamples(), object : OnClickFilterListener {
            override fun onClick(filterMode: FilterMode) {
                updateFilterMode(filterMode)
            }
        })


    fun showBottomMenu() {
        smallSimpleImage = getSuperSampledSimpleImage(this.simpleImage!!, 0.5F)
        adapter.items = getListOfSamples()
        filterBottomMenu.filterRecyclerView.adapter = adapter

        addFilterBottomMenu(binding, filterBottomMenu)
    }

    fun updateFilterMode(filterMode: FilterMode) {

        when (filterMode) {
            FilterMode.INVERSION -> {
                binding.imageView.setImageBitmap(getBitMap(inverse(this.simpleImage!!)))
            }

            FilterMode.GRAYSCALE -> {
                binding.imageView.setImageBitmap(getBitMap(grayscale(this.simpleImage!!)))
            }

            FilterMode.BLACK_AND_WHITE -> {
                binding.imageView.setImageBitmap(getBitMap(blackAndWhite(this.simpleImage!!)))
            }

            FilterMode.SEPIA -> {
                binding.imageView.setImageBitmap(getBitMap(sepia(this.simpleImage!!)))
            }

            FilterMode.CONTRAST -> {

                addContrastSlider(binding, contrastSlider)

                binding.imageView.setImageBitmap(
                    getBitMap(contrast(this.simpleImage!!, contrastCoefficient))
                )

                val slider: Slider = contrastSlider.contrastSlider
                slider.addOnChangeListener(Slider.OnChangeListener { p0, p1, p2 ->
                    contrastCoefficient = p1.toInt()
                    binding.imageView.setImageBitmap(
                        getBitMap(contrast(this.simpleImage!!, contrastCoefficient))
                    )
                })
            }

            FilterMode.RGB -> {
                addRgbMenu(binding, rgbMenu)

                binding.imageView.setImageBitmap(getBitMap(rgb(this.simpleImage!!, rgbMode)))

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
                        binding.imageView.setImageBitmap(getBitMap(rgb(this@Filter.simpleImage!!, rgbMode)))
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab) {}
                    override fun onTabReselected(tab: TabLayout.Tab) {}
                })
            }

            FilterMode.MOSAIC -> {

                addMosaicSlider(binding, mosaicSlider)

                binding.imageView.setImageBitmap(
                    getBitMap(mosaic(this.simpleImage!!, squareSide))
                )

                val slider: Slider = mosaicSlider.mosaicSlider
                slider.addOnChangeListener(Slider.OnChangeListener { p0, p1, p2 ->
                    squareSide = p1.toInt()
                    binding.imageView.setImageBitmap(
                        getBitMap(mosaic(this.simpleImage!!, squareSide))
                    )
                })
            }

            FilterMode.GRAIN -> {

                addGrainSlider(binding, grainSlider)

                binding.imageView.setImageBitmap(
                    getBitMap(grain(this.simpleImage!!, grainNumber))
                )

                val slider: Slider = grainSlider.grainSlider
                slider.addOnChangeListener(Slider.OnChangeListener { p0, p1, p2 ->
                    grainNumber = p1.toInt()
                    binding.imageView.setImageBitmap(
                        getBitMap(grain(this.simpleImage!!, grainNumber))
                    )
                })
            }

            FilterMode.CHANNEL_SHIFT -> {
                addChannelShiftSlider(binding, channelShiftSlider)

                binding.imageView.setImageBitmap(
                    getBitMap(channelShift(this.simpleImage!!, horizontalShift, verticalShift))
                )

                val verticalSlider: Slider = channelShiftSlider.verticalSlider
                val horizontalSlider: Slider = channelShiftSlider.horizontalSlider

                verticalSlider.addOnChangeListener(Slider.OnChangeListener { p0, p1, p2 ->
                    verticalShift = p1.toInt()
                    binding.imageView.setImageBitmap(
                        getBitMap(channelShift(this.simpleImage!!, horizontalShift, verticalShift))
                    )
                })
                horizontalSlider.addOnChangeListener(Slider.OnChangeListener { p0, p1, p2 ->
                    horizontalShift = p1.toInt()
                    binding.imageView.setImageBitmap(
                        getBitMap(channelShift(this.simpleImage!!, horizontalShift, verticalShift))
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
        if(smallSimpleImage != null){
            items.add(ItemFilter(getBitMap(inverse(smallSimpleImage!!)), FilterMode.INVERSION))
            items.add(ItemFilter(getBitMap(grayscale(smallSimpleImage!!)), FilterMode.GRAYSCALE))
            items.add(
                ItemFilter(
                    getBitMap(blackAndWhite(smallSimpleImage!!)),
                    FilterMode.BLACK_AND_WHITE
                )
            )
            items.add(ItemFilter(getBitMap(sepia(smallSimpleImage!!)), FilterMode.SEPIA))
            items.add(ItemFilter(getBitMap(contrast(smallSimpleImage!!, 100)), FilterMode.CONTRAST))
            items.add(ItemFilter(getBitMap(rgb(smallSimpleImage!!, rgbMode)), FilterMode.RGB))
            items.add(ItemFilter(getBitMap(mosaic(smallSimpleImage!!, 10)), FilterMode.MOSAIC))
            items.add(ItemFilter(getBitMap(grain(smallSimpleImage!!, 50)), FilterMode.GRAIN))
            items.add(
                ItemFilter(
                    getBitMap(channelShift(smallSimpleImage!!, 100, 100)),
                    FilterMode.CHANNEL_SHIFT
                )
            )
        }
        return items
    }
}