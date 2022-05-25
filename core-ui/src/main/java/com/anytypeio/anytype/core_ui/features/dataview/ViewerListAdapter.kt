package com.anytypeio.anytype.core_ui.features.dataview

import android.view.LayoutInflater
import android.view.ViewGroup
import com.anytypeio.anytype.core_ui.common.AbstractAdapter
import com.anytypeio.anytype.core_ui.common.AbstractViewHolder
import com.anytypeio.anytype.core_ui.databinding.ItemViewerGridBinding

class ViewerListAdapter(items: List<String> = emptyList()) : AbstractAdapter<String>(items) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AbstractViewHolder<String> = Holder(
        ItemViewerGridBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    class Holder(val binding: ItemViewerGridBinding) : AbstractViewHolder<String>(binding.root) {
        override fun bind(item: String) {
            binding.tvTitle.text = item
        }
    }
}