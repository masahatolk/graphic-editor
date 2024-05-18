package com.hits.graphic_editor.color_correction

import android.graphics.Bitmap
import com.hits.graphic_editor.color_correction.ColorCorrectionMode

data class ItemColorCorrection(
    val appliedColorCorrectionSample: Bitmap,
    val colorCorrectionMode: ColorCorrectionMode
)