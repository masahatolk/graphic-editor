package com.hits.graphic_editor

import com.hits.graphic_editor.custom_api.MipMapsContainer
import com.hits.graphic_editor.custom_api.SimpleImage

class ProcessedImage() {

    var undoStack: MutableList<SimpleImage> = mutableListOf()
    var redoStack: MutableList<SimpleImage> = mutableListOf()
    lateinit var mipMaps: MipMapsContainer

    lateinit var image: SimpleImage
}