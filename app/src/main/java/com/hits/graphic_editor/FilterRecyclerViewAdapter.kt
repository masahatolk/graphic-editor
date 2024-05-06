package com.hits.graphic_editor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hits.graphic_editor.databinding.ItemFilterBinding

interface onClickFilterListener {
    fun onClick(filterName: String)
}

class FilterRecyclerViewAdapter(
    _items: MutableList<ItemFilter>,
    private val actionListener: onClickFilterListener
) : RecyclerView.Adapter<FilterRecyclerViewAdapter.FilterViewHolder>(), View.OnClickListener {

    override fun onClick(v: View) {
        val item = v.tag as ItemFilter
        actionListener.onClick(item.filterName)
    }

    private var items: MutableList<ItemFilter> = _items

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

            appliedFilterName.text = item.filterName
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