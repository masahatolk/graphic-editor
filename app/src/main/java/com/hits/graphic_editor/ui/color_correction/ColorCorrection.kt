package com.hits.graphic_editor.ui.color_correction

import android.view.LayoutInflater
import com.google.android.material.slider.Slider
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.hits.graphic_editor.Filter
import com.hits.graphic_editor.ProcessedImage
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
import kotlinx.coroutines.runBlocking

class ColorCorrection(
    override val binding: ActivityNewProjectBinding,
    override val layoutInflater: LayoutInflater,
    private var processedImage: ProcessedImage
    //var simpleImage: SimpleImage
) : Filter {

    // -----------------create necessary fields-----------------
    //lateinit var simpleImage: SimpleImage
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
        smallSimpleImage = runBlocking{getSuperSampledSimpleImage(processedImage.getSimpleImage(), 0.05F)}
        adapter.items = getListOfSamples()
        colorCorrectionBottomMenu.colorCorrectionRecyclerView.adapter = adapter

        addFilterBottomMenu(binding, colorCorrectionBottomMenu)
    }

    fun updateFilterMode(filterMode: ColorCorrectionMode) {

        when (filterMode) {
            ColorCorrectionMode.INVERSION -> {
                processedImage.addToLocalStack(inverse(processedImage.getSimpleImageBeforeFiltering()))
                binding.imageView.setImageBitmap(getBitMap(processedImage.getSimpleImage()))
            }

            ColorCorrectionMode.GRAYSCALE -> {
                processedImage.addToLocalStack(grayscale(processedImage.getSimpleImageBeforeFiltering()))
                binding.imageView.setImageBitmap(getBitMap(processedImage.getSimpleImage()))
            }

            ColorCorrectionMode.BLACK_AND_WHITE -> {
                processedImage.addToLocalStack(blackAndWhite(processedImage.getSimpleImageBeforeFiltering()))
                binding.imageView.setImageBitmap(getBitMap(processedImage.getSimpleImage()))
            }

            ColorCorrectionMode.SEPIA -> {
                processedImage.addToLocalStack(sepia(processedImage.getSimpleImageBeforeFiltering()))
                binding.imageView.setImageBitmap(getBitMap(processedImage.getSimpleImage()))
            }

            ColorCorrectionMode.CONTRAST -> {

                addContrastSlider(binding, contrastSlider)

                processedImage.addToLocalStack(contrast(processedImage.getSimpleImageBeforeFiltering(), contrastCoefficient))
                binding.imageView.setImageBitmap(getBitMap(processedImage.getSimpleImage()))

                val slider: Slider = contrastSlider.contrastSlider
                slider.addOnChangeListener(Slider.OnChangeListener { p0, p1, p2 ->
                    contrastCoefficient = p1.toInt()

                    processedImage.addToLocalStack(contrast(processedImage.getSimpleImageBeforeFiltering(), contrastCoefficient))
                    binding.imageView.setImageBitmap(getBitMap(processedImage.getSimpleImage()))
                })
            }

            ColorCorrectionMode.RGB -> {
                addRgbMenu(binding, rgbMenu)

                processedImage.addToLocalStack(rgb(processedImage.getSimpleImageBeforeFiltering(), rgbMode))
                binding.imageView.setImageBitmap(getBitMap(processedImage.getSimpleImage()))

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
                        processedImage.addToLocalStack(rgb(processedImage.getSimpleImageBeforeFiltering(), rgbMode))
                        binding.imageView.setImageBitmap(getBitMap(processedImage.getSimpleImage()))
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab) {}
                    override fun onTabReselected(tab: TabLayout.Tab) {}
                })
            }

            ColorCorrectionMode.MOSAIC -> {

                addMosaicSlider(binding, mosaicSlider)

                processedImage.addToLocalStack(mosaic(processedImage.getSimpleImageBeforeFiltering(), squareSide))
                binding.imageView.setImageBitmap(getBitMap(processedImage.getSimpleImage()))

                val slider: Slider = mosaicSlider.mosaicSlider
                slider.addOnChangeListener(Slider.OnChangeListener { p0, p1, p2 ->
                    squareSide = p1.toInt()

                    processedImage.addToLocalStack(mosaic(processedImage.getSimpleImageBeforeFiltering(), squareSide))
                    binding.imageView.setImageBitmap(getBitMap(processedImage.getSimpleImage()))
                })
            }

            ColorCorrectionMode.GRAIN -> {

                addGrainSlider(binding, grainSlider)

                processedImage.addToLocalStack(grain(processedImage.getSimpleImageBeforeFiltering(), grainNumber))
                binding.imageView.setImageBitmap(getBitMap(processedImage.getSimpleImage()))

                val slider: Slider = grainSlider.grainSlider
                slider.addOnChangeListener(Slider.OnChangeListener { p0, p1, p2 ->
                    grainNumber = p1.toInt()
                    processedImage.addToLocalStack(grain(processedImage.getSimpleImageBeforeFiltering(), grainNumber))
                    binding.imageView.setImageBitmap(getBitMap(processedImage.getSimpleImage()))
                })
            }

            ColorCorrectionMode.CHANNEL_SHIFT -> {
                addChannelShiftSlider(binding, channelShiftSlider)

                processedImage.addToLocalStack(channelShift(processedImage.getSimpleImageBeforeFiltering(), horizontalShift, verticalShift))
                binding.imageView.setImageBitmap(getBitMap(processedImage.getSimpleImage()))

                val verticalSlider: Slider = channelShiftSlider.verticalSlider
                val horizontalSlider: Slider = channelShiftSlider.horizontalSlider

                verticalSlider.addOnChangeListener(Slider.OnChangeListener { p0, p1, p2 ->
                    verticalShift = p1.toInt()

                    processedImage.addToLocalStack(channelShift(processedImage.getSimpleImageBeforeFiltering(), horizontalShift, verticalShift))
                    binding.imageView.setImageBitmap(getBitMap(processedImage.getSimpleImage()))
                })
                horizontalSlider.addOnChangeListener(Slider.OnChangeListener { p0, p1, p2 ->
                    horizontalShift = p1.toInt()

                    processedImage.addToLocalStack(channelShift(processedImage.getSimpleImageBeforeFiltering(), horizontalShift, verticalShift))
                    binding.imageView.setImageBitmap(getBitMap(processedImage.getSimpleImage()))
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