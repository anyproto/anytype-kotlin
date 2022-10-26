package com.anytypeio.anytype.core_ui.features.editor.slash

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemListObjectSmallBinding
import com.anytypeio.anytype.core_ui.databinding.ItemSlashWidgetStyleBinding
import com.anytypeio.anytype.core_ui.databinding.ItemSlashWidgetSubheaderBinding
import com.anytypeio.anytype.core_ui.features.editor.slash.holders.ActionMenuHolder
import com.anytypeio.anytype.core_ui.features.editor.slash.holders.ObjectTypeMenuHolder
import com.anytypeio.anytype.core_ui.features.editor.slash.holders.SubheaderMenuHolder
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashItem

class SlashObjectTypesAdapter(
    private var items: List<SlashItem>,
    private val clicks: (SlashItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun update(items: List<SlashItem>) {
        if (items.isEmpty()) {
            clear()
        } else {
            this.items = items
            notifyDataSetChanged()
        }
    }

    fun clear() {
        val size = items.size
        if (size > 0) {
            items = listOf()
            notifyItemRangeRemoved(0, size)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            R.layout.item_list_object_small -> ObjectTypeMenuHolder(
                binding = ItemListObjectSmallBinding.inflate(
                    inflater, parent, false
                )
            )
            R.layout.item_slash_widget_style -> ActionMenuHolder(
                binding = ItemSlashWidgetStyleBinding.inflate(
                    inflater, parent, false
                )
            )
            R.layout.item_slash_widget_subheader -> SubheaderMenuHolder(
                binding = ItemSlashWidgetSubheaderBinding.inflate(
                    inflater, parent, false
                )
            ).apply {
                itemView.findViewById<FrameLayout>(R.id.flBack).setOnClickListener {
                    clicks(SlashItem.Back)
                }
            }
            else -> throw IllegalArgumentException("Wrong viewtype:$viewType")
        }.apply {
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    clicks(items[pos])
                }
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ObjectTypeMenuHolder -> {
                val item = items[position] as SlashItem.ObjectType
                holder.bind(item)
            }
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

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is SlashItem.ObjectType -> R.layout.item_list_object_small
        is SlashItem.Subheader -> R.layout.item_slash_widget_subheader
        is SlashItem.Actions -> R.layout.item_slash_widget_style
        else -> throw IllegalArgumentException("Wrong item type:${items[position]} for SlashObjectTypeAdapter")
    }
}