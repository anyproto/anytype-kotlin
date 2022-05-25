package com.anytypeio.anytype.core_ui.features.sets

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.databinding.ItemObjectTypeBinding
import com.anytypeio.anytype.presentation.sets.CreateObjectTypeView

class CreateObjectTypeAdapter(
    private val onObjectTypeClick: (Int) -> Unit
) : RecyclerView.Adapter<CreateObjectTypeAdapter.ViewHolder>() {

    var types: List<CreateObjectTypeView> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(
            binding = ItemObjectTypeBinding.inflate(
                inflater, parent, false
            )
        )
    }

    override fun getItemCount(): Int = types.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(types[position], onObjectTypeClick)
    }

    class ViewHolder(val binding: ItemObjectTypeBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            type: CreateObjectTypeView,
            onObjectTypeClick: (Int) -> Unit
        ) {
            itemView.setOnClickListener {
                onObjectTypeClick(type.layout)
            }
            binding.title.text = type.name
            itemView.isSelected = type.isSelected
        }
    }
}