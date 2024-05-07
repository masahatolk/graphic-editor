package com.hits.graphic_editor.ui.filter

import android.graphics.Bitmap

data class ItemFilter(
    val appliedFilterSample: Bitmap,
    val filterMode: FilterMode
)