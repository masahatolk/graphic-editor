package com.hits.graphic_editor.ui.filter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hits.graphic_editor.databinding.ItemFilterBinding

interface OnClickFilterListener {
    fun onClick(filterMode: FilterMode)
}

class FilterRecyclerViewAdapter(
    var items: MutableList<ItemFilter>,
    private val actionListener: OnClickFilterListener
) : RecyclerView.Adapter<FilterRecyclerViewAdapter.FilterViewHolder>(), View.OnClickListener {

    override fun onClick(v: View) {
        val item = v.tag as ItemFilter
        actionListener.onClick(item.filterMode)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ItemFilterBinding = ItemFilterBinding.inflate(inflater)

        binding.root.setOnClickListener(this)

        return FilterViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            holder.itemView.tag = item

            appliedFilterName.text = item.filterMode.toString()
            //TODO
            appliedFilterSample.setImageBitmap(item.appliedFilterSample)
            /*if(item.appliedFilterSample != null) {

            } else {

            }*/
        }
    }

    class FilterViewHolder(
        val binding: ItemFilterBinding
    ) : RecyclerView.ViewHolder(binding.root)
}