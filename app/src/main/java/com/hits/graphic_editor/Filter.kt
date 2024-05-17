package com.hits.graphic_editor

import android.view.LayoutInflater
import com.hits.graphic_editor.databinding.ActivityNewProjectBinding

interface Filter {

    val binding: ActivityNewProjectBinding
    val layoutInflater: LayoutInflater
    fun showBottomMenu()

}