package com.anytypeio.anytype.core_ui.common

import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.databinding.ItemDefaultListSectionBinding

class DefaultSectionViewHolder(
    val binding: ItemDefaultListSectionBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(name: String) {
        binding.tvSectionName.text = name
    }
}