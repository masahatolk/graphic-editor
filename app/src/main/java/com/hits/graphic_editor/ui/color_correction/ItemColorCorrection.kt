package com.hits.graphic_editor.ui.color_correction

import android.graphics.Bitmap

data class ItemColorCorrection(
    val appliedColorCorrectionSample: Bitmap,
    val colorCorrectionMode: ColorCorrectionMode
)