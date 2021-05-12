package com.anytypeio.anytype.core_ui.features.page.slash

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.features.page.slash.holders.ActionMenuHolder
import com.anytypeio.anytype.core_ui.features.page.slash.holders.SubheaderMenuHolder
import com.anytypeio.anytype.presentation.page.editor.slash.SlashItem

class SlashActionsAdapter(
    items: List<SlashItem>,
    clicks: (SlashItem) -> Unit
) : SlashBaseAdapter(items, clicks) {

    override fun createHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder = ActionMenuHolder(inflater.inflate(viewType, parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ActionMenuHolder -> {
                val item = items[position] as SlashItem.Actions
                holder.bind(item)
            }
            is SubheaderMenuHolder -> {
                val item = items[position] as SlashItem.Subheader
                holder.bind(item)
            }
        }
    }

    override fun getItemViewType(position: Int): Int = when (val item = items[position]) {
        is SlashItem.Actions -> R.layout.item_slash_widget_style
        is SlashItem.Subheader -> R.layout.item_slash_widget_subheader
        else -> throw IllegalArgumentException("Wrong item type:$item")
    }
}