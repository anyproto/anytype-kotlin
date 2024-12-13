package com.anytypeio.anytype.core_ui.features.editor.slash

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemSlashWidgetStyleBinding
import com.anytypeio.anytype.core_ui.features.editor.slash.holders.AlignMenuHolder
import com.anytypeio.anytype.core_ui.features.editor.slash.holders.SubheaderMenuHolder
import com.anytypeio.anytype.core_ui.features.editor.slash.holders.SubheaderOnlyMenuHolder
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashItem

class SlashAlignmentAdapter(
    items: List<SlashItem>,
    clicks: (SlashItem) -> Unit
) : SlashBaseAdapter(items, clicks) {

    override fun createHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder = AlignMenuHolder(
        binding = ItemSlashWidgetStyleBinding.inflate(
            inflater, parent, false
        )
    )

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AlignMenuHolder -> {
                val item = items[position] as SlashItem.Alignment
                holder.bind(item)
            }
            is SubheaderMenuHolder -> {
                val item = items[position] as SlashItem.Subheader
                holder.bind(item)
            }
            is SubheaderOnlyMenuHolder -> {
                val item = items[position] as SlashItem.Subheader
                holder.bind(item)
            }
        }
    }

    override fun getItemViewType(position: Int): Int = when (val item = items[position]) {
        is SlashItem.Alignment -> R.layout.item_slash_widget_style
        is SlashItem.Subheader -> item.getViewType()
        else -> throw IllegalArgumentException("Wrong item type:$item")
    }
}