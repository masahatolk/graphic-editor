package com.hits.graphic_editor.ui.color_correction

import android.view.LayoutInflater
import com.google.android.material.slider.Slider
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.hits.graphic_editor.Filter
import com.hits.graphic_editor.custom_api.SimpleImage
import com.hits.graphic_editor.custom_api.getBitMap
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.ChannelShiftSliderBinding
import com.hits.graphic_editor.databinding.ColorCorrectionRecyclerViewBinding
import com.hits.graphic_editor.databinding.ContrastSliderBinding
import com.hits.graphic_editor.databinding.GrainSliderBinding
import com.hits.graphic_editor.databinding.MosaicSliderBinding
import com.hits.graphic_editor.databinding.RgbMenuBinding
import com.hits.graphic_editor.scaling.getSuperSampledSimpleImage

class ColorCorrection(
    override val binding: ActivityNewProjectBinding,
    override val layoutInflater: LayoutInflater
) : Filter {

    // -----------------create necessary fields-----------------
    lateinit var simpleImage: SimpleImage
    private lateinit var smallSimpleImage: SimpleImage
    private var verticalShift: Int = 0
    private var horizontalShift: Int = 0
    private var grainNumber: Int = 5
    private var squareSide: Int = 5
    private var contrastCoefficient: Int = 0
    private var rgbMode: RGBMode = RGBMode.RED


    val colorCorrectionBottomMenu: ColorCorrectionRecyclerViewBinding by lazy {
        ColorCorrectionRecyclerViewBinding.inflate(layoutInflater)
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
    private val adapter: ColorCorrectionRecyclerViewAdapter by lazy {
        ColorCorrectionRecyclerViewAdapter(getListOfSamples(), object : OnClickColorCorrectionListener {
            override fun onClick(colorCorrectionMode: ColorCorrectionMode) {
                updateFilterMode(colorCorrectionMode)
            }
        })
    }



    override fun showBottomMenu() {
        smallSimpleImage = getSuperSampledSimpleImage(this.simpleImage, 0.5F)
        adapter.items = getListOfSamples()
        colorCorrectionBottomMenu.colorCorrectionRecyclerView.adapter = adapter

        addFilterBottomMenu(binding, colorCorrectionBottomMenu)
    }

    fun updateFilterMode(filterMode: ColorCorrectionMode) {

        when (filterMode) {
            ColorCorrectionMode.INVERSION -> {
                binding.imageView.setImageBitmap(getBitMap(inverse(this.simpleImage)))
            }

            ColorCorrectionMode.GRAYSCALE -> {
                binding.imageView.setImageBitmap(getBitMap(grayscale(this.simpleImage)))
            }

            ColorCorrectionMode.BLACK_AND_WHITE -> {
                binding.imageView.setImageBitmap(getBitMap(blackAndWhite(this.simpleImage)))
            }

            ColorCorrectionMode.SEPIA -> {
                binding.imageView.setImageBitmap(getBitMap(sepia(this.simpleImage)))
            }

            ColorCorrectionMode.CONTRAST -> {

                addContrastSlider(binding, contrastSlider)

                binding.imageView.setImageBitmap(
                    getBitMap(contrast(this.simpleImage, contrastCoefficient))
                )

                val slider: Slider = contrastSlider.contrastSlider
                slider.addOnChangeListener(Slider.OnChangeListener { p0, p1, p2 ->
                    contrastCoefficient = p1.toInt()
                    binding.imageView.setImageBitmap(
                        getBitMap(contrast(this.simpleImage, contrastCoefficient))
                    )
                })
            }

            ColorCorrectionMode.RGB -> {
                addRgbMenu(binding, rgbMenu)

                binding.imageView.setImageBitmap(getBitMap(rgb(this.simpleImage, rgbMode)))

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
                        binding.imageView.setImageBitmap(
                            getBitMap(
                                rgb(
                                    this@ColorCorrection.simpleImage,
                                    rgbMode
                                )
                            )
                        )
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab) {}
                    override fun onTabReselected(tab: TabLayout.Tab) {}
                })
            }

            ColorCorrectionMode.MOSAIC -> {

                addMosaicSlider(binding, mosaicSlider)

                binding.imageView.setImageBitmap(
                    getBitMap(mosaic(this.simpleImage, squareSide))
                )

                val slider: Slider = mosaicSlider.mosaicSlider
                slider.addOnChangeListener(Slider.OnChangeListener { p0, p1, p2 ->
                    squareSide = p1.toInt()
                    binding.imageView.setImageBitmap(
                        getBitMap(mosaic(this.simpleImage, squareSide))
                    )
                })
            }

            ColorCorrectionMode.GRAIN -> {

                addGrainSlider(binding, grainSlider)

                binding.imageView.setImageBitmap(
                    getBitMap(grain(this.simpleImage, grainNumber))
                )

                val slider: Slider = grainSlider.grainSlider
                slider.addOnChangeListener(Slider.OnChangeListener { p0, p1, p2 ->
                    grainNumber = p1.toInt()
                    binding.imageView.setImageBitmap(
                        getBitMap(grain(this.simpleImage, grainNumber))
                    )
                })
            }

            ColorCorrectionMode.CHANNEL_SHIFT -> {
                addChannelShiftSlider(binding, channelShiftSlider)

                binding.imageView.setImageBitmap(
                    getBitMap(channelShift(this.simpleImage, horizontalShift, verticalShift))
                )

                val verticalSlider: Slider = channelShiftSlider.verticalSlider
                val horizontalSlider: Slider = channelShiftSlider.horizontalSlider

                verticalSlider.addOnChangeListener(Slider.OnChangeListener { p0, p1, p2 ->
                    verticalShift = p1.toInt()
                    binding.imageView.setImageBitmap(
                        getBitMap(channelShift(this.simpleImage, horizontalShift, verticalShift))
                    )
                })
                horizontalSlider.addOnChangeListener(Slider.OnChangeListener { p0, p1, p2 ->
                    horizontalShift = p1.toInt()
                    binding.imageView.setImageBitmap(
                        getBitMap(channelShift(this.simpleImage, horizontalShift, verticalShift))
                    )
                })
            }
        }

        if (filterMode != ColorCorrectionMode.CONTRAST) removeContrastSlider(binding, contrastSlider)
        if (filterMode != ColorCorrectionMode.RGB) removeRgbMenu(binding, rgbMenu)
        if (filterMode != ColorCorrectionMode.MOSAIC) removeMosaicSlider(binding, mosaicSlider)
        if (filterMode != ColorCorrectionMode.GRAIN) removeGrainSlider(binding, grainSlider)
        if (filterMode != ColorCorrectionMode.CHANNEL_SHIFT) removeChannelShiftSlider(
            binding,
            channelShiftSlider
        )
    }

    private fun getListOfSamples(): MutableList<ItemColorCorrection> {
        val items = mutableListOf<ItemColorCorrection>()

        items.add(ItemColorCorrection(getBitMap(inverse(smallSimpleImage)), ColorCorrectionMode.INVERSION))
        items.add(ItemColorCorrection(getBitMap(grayscale(smallSimpleImage)), ColorCorrectionMode.GRAYSCALE))
        items.add(
            ItemColorCorrection(
                getBitMap(blackAndWhite(smallSimpleImage)),
                ColorCorrectionMode.BLACK_AND_WHITE
            )
        )
        items.add(ItemColorCorrection(getBitMap(sepia(smallSimpleImage)), ColorCorrectionMode.SEPIA))
        items.add(ItemColorCorrection(getBitMap(contrast(smallSimpleImage, 100)), ColorCorrectionMode.CONTRAST))
        items.add(ItemColorCorrection(getBitMap(rgb(smallSimpleImage, rgbMode)), ColorCorrectionMode.RGB))
        items.add(ItemColorCorrection(getBitMap(mosaic(smallSimpleImage, 10)), ColorCorrectionMode.MOSAIC))
        items.add(ItemColorCorrection(getBitMap(grain(smallSimpleImage, 50)), ColorCorrectionMode.GRAIN))
        items.add(
            ItemColorCorrection(
                getBitMap(channelShift(smallSimpleImage, 100, 100)),
                ColorCorrectionMode.CHANNEL_SHIFT
            )
        )

        return items
    }
}