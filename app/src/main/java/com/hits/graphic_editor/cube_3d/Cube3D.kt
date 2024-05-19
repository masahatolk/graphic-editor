package com.hits.graphic_editor.cube_3d

import android.view.LayoutInflater
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding
import com.hits.graphic_editor.utils.Filter
import com.hits.graphic_editor.utils.ProcessedImage

class Cube3D(
    override val binding: ActivityNewProjectBinding,
    override val layoutInflater: LayoutInflater,
    override val processedImage: ProcessedImage
):Filter {
    override fun showBottomMenu() {
    }

    override fun removeAllMenus() {
    }
}