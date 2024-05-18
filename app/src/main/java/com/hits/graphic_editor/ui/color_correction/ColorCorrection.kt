package com.hits.graphic_editor.ui.color_correction

import android.view.LayoutInflater
import com.google.android.material.slider.Slider
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.hits.graphic_editor.custom_api.SimpleImage
import com.hits.graphic_editor.custom_api.getBitMap
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.ChannelShiftSliderBinding
import com.hits.graphic_editor.databinding.ColorCorrectionRecyclerViewBinding
import com.hits.graphic_editor.databinding.ContrastSliderBinding
import com.hits.graphic_editor.databinding.GrainSliderBinding
import com.hits.graphic_editor.databinding.MosaicSliderBinding
import com.hits.graphic_editor.databinding.RgbMenuBinding
import com.hits.graphic_editor.face_detection.FaceDetection
import com.hits.graphic_editor.face_detection.removeFaceDetectionBottomMenu
import com.hits.graphic_editor.scaling.getSuperSampledSimpleImage
import kotlinx.coroutines.runBlocking
import org.opencv.core.Mat
import org.opencv.core.MatOfRect


class ColorCorrection(
    val binding: ActivityNewProjectBinding,
    private val layoutInflater: LayoutInflater,
    private val faceDetection: FaceDetection
) : ColorCorrectionAlgorithms() {

    // -----------------create necessary fields-----------------
    lateinit var simpleImage: SimpleImage
    private lateinit var smallSimpleImage: SimpleImage


    private lateinit var matrix: Mat
    private lateinit var faces: MatOfRect


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
        ColorCorrectionRecyclerViewAdapter(getListOfSamples(), object : OnClickListener {
            override fun onClick(mode: ColorCorrectionMode) {
                updateFilterMode(mode)
            }
        })
    }


    fun showBottomMenu() {
        runBlocking { smallSimpleImage = getSuperSampledSimpleImage(simpleImage, 0.15F) }
        adapter.items = getListOfSamples()
        colorCorrectionBottomMenu.colorCorrectionRecyclerView.adapter = adapter
        matrix = faceDetection.getMatrix(simpleImage)
        faces = faceDetection.detectFaces(matrix)

        addFilterBottomMenu(binding, colorCorrectionBottomMenu)
    }

    fun updateFilterMode(filterMode: ColorCorrectionMode) {

        when (filterMode) {
            ColorCorrectionMode.FACE_DETECTION -> {
                faceDetection.showBottomMenu(simpleImage)
            }

            ColorCorrectionMode.INVERSION -> {
                if (faceDetection.isDetectionApplied) binding.imageView.setImageBitmap(
                    getBitMap(
                        processFaces(simpleImage, ::inverse)
                    )
                )
                else binding.imageView.setImageBitmap(getBitMap(inverse(simpleImage)))
            }

            ColorCorrectionMode.GRAYSCALE -> {
                if (faceDetection.isDetectionApplied) binding.imageView.setImageBitmap(
                    getBitMap(
                        processFaces(simpleImage, ::grayscale)
                    )
                )
                else binding.imageView.setImageBitmap(getBitMap(grayscale(simpleImage)))
            }

            ColorCorrectionMode.BLACK_AND_WHITE -> {
                if (faceDetection.isDetectionApplied) binding.imageView.setImageBitmap(
                    getBitMap(
                        processFaces(simpleImage, ::blackAndWhite)
                    )
                )
                else binding.imageView.setImageBitmap(getBitMap(blackAndWhite(simpleImage)))
            }

            ColorCorrectionMode.SEPIA -> {
                if (faceDetection.isDetectionApplied) binding.imageView.setImageBitmap(
                    getBitMap(
                        processFaces(simpleImage, ::sepia)
                    )
                )
                else binding.imageView.setImageBitmap(getBitMap(sepia(simpleImage)))
            }

            ColorCorrectionMode.CONTRAST -> {

                removeContrastSlider(binding, contrastSlider)
                addContrastSlider(binding, contrastSlider)

                if (faceDetection.isDetectionApplied) binding.imageView.setImageBitmap(
                    getBitMap(
                        processFaces(simpleImage, ::contrast)
                    )
                )
                else binding.imageView.setImageBitmap(
                    getBitMap(contrast(simpleImage))
                )

                val slider: Slider = contrastSlider.contrastSlider
                slider.addOnChangeListener(Slider.OnChangeListener { p0, p1, p2 ->
                    contrastCoefficient = p1.toInt()
                    if (faceDetection.isDetectionApplied) binding.imageView.setImageBitmap(
                        getBitMap(
                            processFaces(simpleImage, ::contrast)
                        )
                    )
                    else binding.imageView.setImageBitmap(
                        getBitMap(contrast(simpleImage))
                    )
                })
            }

            ColorCorrectionMode.RGB -> {

                removeRgbMenu(binding, rgbMenu)
                addRgbMenu(binding, rgbMenu)

                if (faceDetection.isDetectionApplied) binding.imageView.setImageBitmap(
                    getBitMap(
                        processFaces(simpleImage, ::rgb)
                    )
                )
                else binding.imageView.setImageBitmap(getBitMap(rgb(simpleImage)))

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
                        if (faceDetection.isDetectionApplied) binding.imageView.setImageBitmap(
                            getBitMap(
                                processFaces(simpleImage, ::rgb)
                            )
                        )
                        else binding.imageView.setImageBitmap(getBitMap(rgb(this@ColorCorrection.simpleImage)))
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab) {}
                    override fun onTabReselected(tab: TabLayout.Tab) {}
                })
            }

            ColorCorrectionMode.MOSAIC -> {

                removeMosaicSlider(binding, mosaicSlider)
                addMosaicSlider(binding, mosaicSlider)

                if (faceDetection.isDetectionApplied) binding.imageView.setImageBitmap(
                    getBitMap(
                        processFaces(simpleImage, ::mosaic)
                    )
                )
                else binding.imageView.setImageBitmap(
                    getBitMap(mosaic(simpleImage))
                )

                val slider: Slider = mosaicSlider.mosaicSlider
                slider.addOnChangeListener(Slider.OnChangeListener { p0, p1, p2 ->
                    squareSide = p1.toInt()
                    if (faceDetection.isDetectionApplied) binding.imageView.setImageBitmap(
                        getBitMap(
                            processFaces(simpleImage, ::mosaic)
                        )
                    )
                    else binding.imageView.setImageBitmap(
                        getBitMap(mosaic(simpleImage))
                    )
                })
            }

            ColorCorrectionMode.GRAIN -> {

                removeGrainSlider(binding, grainSlider)
                addGrainSlider(binding, grainSlider)

                if (faceDetection.isDetectionApplied) binding.imageView.setImageBitmap(
                    getBitMap(
                        processFaces(simpleImage, ::grain)
                    )
                )
                else binding.imageView.setImageBitmap(
                    getBitMap(grain(simpleImage))
                )

                val slider: Slider = grainSlider.grainSlider
                slider.addOnChangeListener(Slider.OnChangeListener { p0, p1, p2 ->
                    grainNumber = p1.toInt()
                    if (faceDetection.isDetectionApplied) binding.imageView.setImageBitmap(
                        getBitMap(
                            processFaces(simpleImage, ::grain)
                        )
                    )
                    else binding.imageView.setImageBitmap(
                        getBitMap(grain(simpleImage))
                    )
                })
            }

            ColorCorrectionMode.CHANNEL_SHIFT -> {

                removeChannelShiftSlider(binding, channelShiftSlider)
                addChannelShiftSlider(binding, channelShiftSlider)

                if (faceDetection.isDetectionApplied) binding.imageView.setImageBitmap(
                    getBitMap(
                        processFaces(simpleImage, ::channelShift)
                    )
                )
                else binding.imageView.setImageBitmap(
                    getBitMap(channelShift(simpleImage))
                )

                val verticalSlider: Slider = channelShiftSlider.verticalSlider
                val horizontalSlider: Slider = channelShiftSlider.horizontalSlider

                verticalSlider.addOnChangeListener(Slider.OnChangeListener { p0, p1, p2 ->
                    verticalShift = p1.toInt()
                    if (faceDetection.isDetectionApplied) binding.imageView.setImageBitmap(
                        getBitMap(
                            processFaces(simpleImage, ::channelShift)
                        )
                    )
                    else binding.imageView.setImageBitmap(
                        getBitMap(channelShift(simpleImage))
                    )
                })
                horizontalSlider.addOnChangeListener(Slider.OnChangeListener { p0, p1, p2 ->
                    horizontalShift = p1.toInt()
                    if (faceDetection.isDetectionApplied) binding.imageView.setImageBitmap(
                        getBitMap(
                            processFaces(simpleImage, ::channelShift)
                        )
                    )
                    else binding.imageView.setImageBitmap(
                        getBitMap(channelShift(simpleImage))
                    )
                })
            }
        }

        if (filterMode != ColorCorrectionMode.CONTRAST) removeContrastSlider(
            binding,
            contrastSlider
        )
        if (filterMode != ColorCorrectionMode.RGB) removeRgbMenu(binding, rgbMenu)
        if (filterMode != ColorCorrectionMode.MOSAIC) removeMosaicSlider(binding, mosaicSlider)
        if (filterMode != ColorCorrectionMode.GRAIN) removeGrainSlider(binding, grainSlider)
        if (filterMode != ColorCorrectionMode.CHANNEL_SHIFT) removeChannelShiftSlider(
            binding,
            channelShiftSlider
        )
        if (filterMode != ColorCorrectionMode.FACE_DETECTION) removeFaceDetectionBottomMenu(
            binding,
            faceDetection.faceDetectionBottomMenu
        )
    }

    private fun getListOfSamples(): MutableList<ItemColorCorrection> {
        val items = mutableListOf<ItemColorCorrection>()

        items.add(
            ItemColorCorrection(
                faceDetection.getDetection(smallSimpleImage),
                ColorCorrectionMode.FACE_DETECTION
            )
        )
        items.add(
            ItemColorCorrection(
                getBitMap(inverse(smallSimpleImage)),
                ColorCorrectionMode.INVERSION
            )
        )
        items.add(
            ItemColorCorrection(
                getBitMap(grayscale(smallSimpleImage)),
                ColorCorrectionMode.GRAYSCALE
            )
        )
        items.add(
            ItemColorCorrection(
                getBitMap(blackAndWhite(smallSimpleImage)),
                ColorCorrectionMode.BLACK_AND_WHITE
            )
        )
        items.add(
            ItemColorCorrection(
                getBitMap(sepia(smallSimpleImage)),
                ColorCorrectionMode.SEPIA
            )
        )
        items.add(
            ItemColorCorrection(
                getBitMap(contrast(smallSimpleImage)),
                ColorCorrectionMode.CONTRAST
            )
        )
        items.add(
            ItemColorCorrection(
                getBitMap(rgb(smallSimpleImage)),
                ColorCorrectionMode.RGB
            )
        )
        items.add(
            ItemColorCorrection(
                getBitMap(mosaic(smallSimpleImage)),
                ColorCorrectionMode.MOSAIC
            )
        )
        items.add(
            ItemColorCorrection(
                getBitMap(grain(smallSimpleImage)),
                ColorCorrectionMode.GRAIN
            )
        )
        items.add(
            ItemColorCorrection(
                getBitMap(channelShift(smallSimpleImage)),
                ColorCorrectionMode.CHANNEL_SHIFT
            )
        )

        return items
    }

    private fun processFaces(
        simpleImage: SimpleImage,
        function: (img: SimpleImage) -> SimpleImage
    ): SimpleImage {
        val img = simpleImage.copy(pixels = simpleImage.pixels.clone())
        for (face in faces.toArray()) {
            val pixels = img.getPixels(face.x, face.y, face.width, face.height)
            var faceSimpleImage = SimpleImage(
                pixels,
                face.width,
                face.height
            )
            faceSimpleImage = function(faceSimpleImage)
            img.setPixels(face.x, face.y, faceSimpleImage.pixels, face.width)
        }
        return img
    }
}