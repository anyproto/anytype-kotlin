package com.anytypeio.anytype.core_ui.features.editor.slash

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.features.editor.slash.holders.MediaMenuHolder
import com.anytypeio.anytype.core_ui.features.editor.slash.holders.SubheaderMenuHolder
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashItem

class SlashMediaAdapter(
    items: List<SlashItem>,
    clicks: (SlashItem) -> Unit
) : SlashBaseAdapter(items, clicks) {

    override fun createHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder = MediaMenuHolder(inflater.inflate(viewType, parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is MediaMenuHolder -> {
                val item = items[position] as SlashItem.Media
                holder.bind(item)
            }
            is SubheaderMenuHolder -> {
                val item = items[position] as SlashItem.Subheader
                holder.bind(item)
            }
        }
    }

    override fun getItemViewType(position: Int): Int = when (val item = items[position]) {
        is SlashItem.Media -> R.layout.item_slash_widget_style
        is SlashItem.Subheader -> R.layout.item_slash_widget_subheader
        else -> throw IllegalArgumentException("Wrong item type:$item")
    }
}