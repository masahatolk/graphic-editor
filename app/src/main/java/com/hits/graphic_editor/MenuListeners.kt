package com.hits.graphic_editor

import android.graphics.drawable.BitmapDrawable
import com.hits.graphic_editor.custom_api.getBitMap
import com.hits.graphic_editor.custom_api.getSimpleImage
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.databinding.BottomMenuBinding
import com.hits.graphic_editor.databinding.ExtraTopMenuBinding
import com.hits.graphic_editor.databinding.TopMenuBinding
import com.hits.graphic_editor.ui.filter.Filter
import com.hits.graphic_editor.ui.filter.removeChannelShiftSlider
import com.hits.graphic_editor.ui.filter.removeContrastSlider
import com.hits.graphic_editor.ui.filter.removeFilterBottomMenu
import com.hits.graphic_editor.ui.filter.removeGrainSlider
import com.hits.graphic_editor.ui.filter.removeMosaicSlider
import com.hits.graphic_editor.ui.filter.removeRgbMenu

fun setListenersToExtraTopMenu(
    binding: ActivityNewProjectBinding,
    topMenu: TopMenuBinding,
    bottomMenu: BottomMenuBinding,
    extraTopMenu: ExtraTopMenuBinding,
    filter: Filter
) {
    extraTopMenu.close.setOnClickListener {

        binding.imageView.setImageBitmap(getBitMap(filter.simpleImage))

        // TODO fix this trash
        removeExtraTopMenu(binding, extraTopMenu)
        removeFilterBottomMenu(binding, filter.filterBottomMenu)
        removeContrastSlider(binding, filter.contrastSlider)
        removeRgbMenu(binding, filter.rgbMenu)
        removeMosaicSlider(binding, filter.mosaicSlider)
        removeChannelShiftSlider(binding, filter.channelShiftSlider)
        removeGrainSlider(binding, filter.grainSlider)

        addTopMenu(binding, topMenu)
        addBottomMenu(binding, bottomMenu)
    }

    extraTopMenu.save.setOnClickListener {

        val bitmap = (binding.imageView.getDrawable() as BitmapDrawable).bitmap
        filter.simpleImage = getSimpleImage(bitmap)

        removeExtraTopMenu(binding, extraTopMenu)
        removeFilterBottomMenu(binding, filter.filterBottomMenu)
        removeContrastSlider(binding, filter.contrastSlider)
        removeRgbMenu(binding, filter.rgbMenu)
        removeMosaicSlider(binding, filter.mosaicSlider)
        removeChannelShiftSlider(binding, filter.channelShiftSlider)
        removeGrainSlider(binding, filter.grainSlider)

        addTopMenu(binding, topMenu)
        addBottomMenu(binding, bottomMenu)
    }
}

fun setListenersToTopMenu(){}