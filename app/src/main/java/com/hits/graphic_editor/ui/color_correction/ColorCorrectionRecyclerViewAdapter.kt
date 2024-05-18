package com.hits.graphic_editor.ui.color_correction

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hits.graphic_editor.databinding.ItemColorCorrectionBinding
import com.hits.graphic_editor.databinding.ItemFaceDetectionBinding

interface OnClickListener {
    fun onClick(mode: ColorCorrectionMode)
}

class ColorCorrectionRecyclerViewAdapter(
    var items: MutableList<ItemColorCorrection>,
    private val actionListener: OnClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    View.OnClickListener {

    override fun onClick(v: View) {
        val item = v.tag as ItemColorCorrection
        actionListener.onClick(item.colorCorrectionMode)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val vh: RecyclerView.ViewHolder
        val inflater = LayoutInflater.from(parent.context)

        if (viewType == 0) {
            val binding: ItemFaceDetectionBinding =
                ItemFaceDetectionBinding.inflate(inflater, parent, false)
            binding.root.setOnClickListener(this)
            vh = FaceDetectionViewHolder(binding)

        } else {
            val binding: ItemColorCorrectionBinding =
                ItemColorCorrectionBinding.inflate(inflater, parent, false)
            binding.root.setOnClickListener(this)
            vh = ColorCorrectionViewHolder(binding)
        }

        return vh
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]

        when (getItemViewType(position)) {
            0 -> {
                val faceDetectionHolder: FaceDetectionViewHolder = holder as FaceDetectionViewHolder
                with(faceDetectionHolder.binding) {
                    holder.itemView.tag = item

                    appliedFilterName.text = item.colorCorrectionMode.toString()
                    //TODO
                    appliedFilterSample.setImageBitmap(item.appliedColorCorrectionSample)
                    /*if(item.appliedFilterSample != null) {

                    } else {

                    }*/
                }
            }

            1 -> {
                val colorCorrectionHolder: ColorCorrectionViewHolder =
                    holder as ColorCorrectionViewHolder
                with(colorCorrectionHolder.binding) {
                    holder.itemView.tag = item

                    appliedFilterName.text = item.colorCorrectionMode.toString()
                    //TODO
                    appliedFilterSample.setImageBitmap(item.appliedColorCorrectionSample)
                    /*if(item.appliedFilterSample != null) {

                    } else {

                    }*/
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            0
        } else {
            1
        }
    }

    inner class ColorCorrectionViewHolder(
        val binding: ItemColorCorrectionBinding
    ) : RecyclerView.ViewHolder(binding.root)

    inner class FaceDetectionViewHolder(
        val binding: ItemFaceDetectionBinding
    ) : RecyclerView.ViewHolder(binding.root)
}