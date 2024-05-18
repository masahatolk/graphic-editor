package com.hits.graphic_editor.ui.color_correction

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hits.graphic_editor.databinding.ItemColorCorrectionBinding

interface OnClickColorCorrectionListener {
    fun onClick(colorCorrectionMode: ColorCorrectionMode)
}

class ColorCorrectionRecyclerViewAdapter(
    var items: MutableList<ItemColorCorrection>,
    private val actionListener: OnClickColorCorrectionListener
) : RecyclerView.Adapter<ColorCorrectionRecyclerViewAdapter.ColorCorrectionViewHolder>(), View.OnClickListener {

    override fun onClick(v: View) {
        val item = v.tag as ItemColorCorrection
        actionListener.onClick(item.colorCorrectionMode)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorCorrectionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ItemColorCorrectionBinding = ItemColorCorrectionBinding.inflate(inflater)

        binding.root.setOnClickListener(this)

        return ColorCorrectionViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ColorCorrectionViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            holder.itemView.tag = item

            appliedFilterName.text = item.colorCorrectionMode.toString()
            //TODO
            appliedFilterSample.setImageBitmap(item.appliedColorCorrectionSample)
            /*if(item.appliedFilterSample != null) {

            } else {

            }*/
        }
    }

    class ColorCorrectionViewHolder(
        val binding: ItemColorCorrectionBinding
    ) : RecyclerView.ViewHolder(binding.root)
}