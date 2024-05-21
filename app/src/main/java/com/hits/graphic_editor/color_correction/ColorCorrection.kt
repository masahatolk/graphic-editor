package com.hits.graphic_editor.color_correction

import android.graphics.Bitmap
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
import com.hits.graphic_editor.utils.Filter
import com.hits.graphic_editor.utils.ProcessedImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.opencv.core.Mat
import org.opencv.core.MatOfRect
import org.opencv.core.Rect

class ColorCorrection(
    override val binding: ActivityNewProjectBinding,
    override val layoutInflater: LayoutInflater,
    override val processedImage: ProcessedImage,
    private val faceDetection: FaceDetection
) : ColorCorrectionAlgorithms(), Filter {

    // -----------------create necessary fields-----------------
    private lateinit var smallSimpleImage: SimpleImage

    private lateinit var faces: Array<Rect>

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
    private val openClInitJob = CoroutineScope(Dispatchers.Default).launch {
        faces = faceDetection.detectFaces(faceDetection.getMatrix(processedImage.getSimpleImage())).toArray()
    }
    private lateinit var cachedDetectionBm: Bitmap
    private val cachedDetectionJob = CoroutineScope(Dispatchers.IO).launch {
        cachedDetectionBm = faceDetection.getDetection(processedImage.getSimpleImageBeforeFiltering())
    }

    override fun onStart() {
        runBlocking { smallSimpleImage = getSuperSampledSimpleImage(processedImage.getSimpleImage(), 0.09F) }
        adapter.items = getListOfSamples()
        colorCorrectionBottomMenu.colorCorrectionRecyclerView.adapter = adapter
        addFilterBottomMenu(binding, colorCorrectionBottomMenu)
    }

    override fun onClose() {
        removeAllFilterMenus(binding, this)
    }

    fun updateFilterMode(filterMode: ColorCorrectionMode) {

        when (filterMode) {
            ColorCorrectionMode.FACE_DETECTION -> {
                runBlocking {cachedDetectionJob.join()}
                faceDetection.showBottomMenu(cachedDetectionBm)
            }

            ColorCorrectionMode.INVERSION -> {
                applyFilter(::inverse)
            }

            ColorCorrectionMode.GRAYSCALE -> {
                applyFilter(::grayscale)
            }

            ColorCorrectionMode.BLACK_AND_WHITE -> {
                applyFilter(::blackAndWhite)
            }

            ColorCorrectionMode.SEPIA -> {
                applyFilter(::sepia)
            }

            ColorCorrectionMode.CONTRAST -> {

                removeContrastSlider(binding, contrastSlider)
                addContrastSlider(binding, contrastSlider)

                applyFilter(::contrast)

                val slider: Slider = contrastSlider.contrastSlider
                slider.addOnChangeListener(Slider.OnChangeListener { p0, p1, p2 ->
                    contrastCoefficient = p1.toInt()
                    applyFilter(::contrast)
                })
            }

            ColorCorrectionMode.RGB -> {

                removeRgbMenu(binding, rgbMenu)
                addRgbMenu(binding, rgbMenu)

                applyFilter(::rgb)

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
                        applyFilter(::rgb)
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab) {}
                    override fun onTabReselected(tab: TabLayout.Tab) {}
                })
            }

            ColorCorrectionMode.MOSAIC -> {

                removeMosaicSlider(binding, mosaicSlider)
                addMosaicSlider(binding, mosaicSlider)

                applyFilter(::mosaic)

                val slider: Slider = mosaicSlider.mosaicSlider
                slider.addOnChangeListener(Slider.OnChangeListener { p0, p1, p2 ->
                    squareSide = p1.toInt()
                    applyFilter(::mosaic)
                })
            }

            ColorCorrectionMode.GRAIN -> {

                removeGrainSlider(binding, grainSlider)
                addGrainSlider(binding, grainSlider)

                applyFilter(::grain)

                val slider: Slider = grainSlider.grainSlider
                slider.addOnChangeListener(Slider.OnChangeListener { p0, p1, p2 ->
                    grainNumber = p1.toInt()
                    applyFilter(::grain)
                })
            }

            ColorCorrectionMode.CHANNEL_SHIFT -> {

                removeChannelShiftSlider(binding, channelShiftSlider)
                addChannelShiftSlider(binding, channelShiftSlider)

                applyFilter(::channelShift)

                val verticalSlider: Slider = channelShiftSlider.verticalSlider
                val horizontalSlider: Slider = channelShiftSlider.horizontalSlider

                verticalSlider.addOnChangeListener(Slider.OnChangeListener { p0, p1, p2 ->
                    verticalShift = p1.toInt()
                    applyFilter(::channelShift)
                })
                horizontalSlider.addOnChangeListener(Slider.OnChangeListener { p0, p1, p2 ->
                    horizontalShift = p1.toInt()
                    applyFilter(::channelShift)
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

    private fun applyFilter(function: (img: SimpleImage) -> SimpleImage) {
        if (faceDetection.isDetectionApplied) {
            processedImage.addToLocalStackAndSetImageToView(
                processFaces(processedImage.getSimpleImageBeforeFiltering(), function))
        }
        else{
            processedImage.addToLocalStackAndSetImageToView(
                function(processedImage.getSimpleImageBeforeFiltering()))
        }
    }

    private fun processFaces(
        simpleImage: SimpleImage,
        function: (img: SimpleImage) -> SimpleImage
    ): SimpleImage {
        val img = simpleImage.copy(pixels = simpleImage.pixels.clone())
        runBlocking { openClInitJob.join()}
        for (face in faces) {
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