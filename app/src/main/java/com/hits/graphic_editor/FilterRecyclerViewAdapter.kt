package com.hits.graphic_editor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hits.graphic_editor.databinding.ItemFilterBinding

class FilterRecyclerViewAdapter(_items : MutableList<ItemFilter>) : RecyclerView.Adapter<FilterRecyclerViewAdapter.FilterViewHolder>() {

    private var items: MutableList<ItemFilter> = _items

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ItemFilterBinding by lazy {
            ItemFilterBinding.inflate(inflater)
        }
        return FilterViewHolder(binding)
    }

    //TODO
    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
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