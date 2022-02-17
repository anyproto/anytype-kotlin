package com.anytypeio.anytype.core_ui.features.sets

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.databinding.ItemListBaseBinding
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.presentation.sets.model.Viewer

class PickSortingTypeAdapter(
    private val items: List<Viewer.SortType>,
    private val keySelected: String,
    private val typeSelected: Int,
    private val click: (String, Viewer.SortType) -> Unit
) : RecyclerView.Adapter<PickSortingTypeAdapter.SortHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SortHolder {
        return SortHolder(
            binding = ItemListBaseBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SortHolder, position: Int) {
        holder.bind(
            key = keySelected,
            item = items[position],
            type = typeSelected,
            click = click
        )
    }

    override fun getItemCount(): Int = items.size

    class SortHolder(val binding: ItemListBaseBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            key: String,
            type: Int,
            item: Viewer.SortType,
            click: (String, Viewer.SortType) -> Unit
        ) {
            with(binding) {
                icon.gone()
                text.text = item.name
                itemView.isSelected = item.ordinal == type
                itemView.setOnClickListener {
                    click(key, item)
                }
            }
        }
    }
}