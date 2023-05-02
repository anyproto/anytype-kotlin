package com.anytypeio.anytype.core_ui.features.editor.slash

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemSlashWidgetBackgroundBinding
import com.anytypeio.anytype.core_ui.databinding.ItemSlashWidgetBackgroundDefaultBinding
import com.anytypeio.anytype.core_ui.databinding.ItemSlashWidgetColorBinding
import com.anytypeio.anytype.core_ui.databinding.ItemSlashWidgetSubheaderBinding
import com.anytypeio.anytype.core_ui.features.editor.slash.holders.BackgroundDefaultMenuHolder
import com.anytypeio.anytype.core_ui.features.editor.slash.holders.BackgroundMenuHolder
import com.anytypeio.anytype.core_ui.features.editor.slash.holders.ColorMenuHolder
import com.anytypeio.anytype.core_ui.features.editor.slash.holders.DefaultMenuHolder
import com.anytypeio.anytype.core_ui.features.editor.slash.holders.SubheaderMenuHolder
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashItem

class SlashColorAdapter(
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
            R.layout.item_slash_widget_background -> {
                BackgroundMenuHolder(
                    binding = ItemSlashWidgetBackgroundBinding.inflate(
                        inflater, parent, false
                    )
                ).apply {
                    itemView.setOnClickListener {
                        clicks(items[bindingAdapterPosition])
                    }
                }
            }
            R.layout.item_slash_widget_background_default -> {
                BackgroundDefaultMenuHolder(
                    binding = ItemSlashWidgetBackgroundDefaultBinding.inflate(
                        inflater, parent, false
                    )
                ).apply {
                    itemView.setOnClickListener {
                        clicks(items[bindingAdapterPosition])
                    }
                }
            }
            R.layout.item_slash_widget_color -> {
                ColorMenuHolder(
                    binding = ItemSlashWidgetColorBinding.inflate(
                        inflater, parent, false
                    )
                ).apply {
                    itemView.setOnClickListener {
                        clicks(items[bindingAdapterPosition])
                    }
                }
            }
            R.layout.item_slash_widget_subheader -> {
                SubheaderMenuHolder(
                    binding = ItemSlashWidgetSubheaderBinding.inflate(
                        inflater, parent, false
                    )
                ).apply {
                    itemView.findViewById<View>(R.id.flBack).setOnClickListener {
                        clicks.invoke(SlashItem.Back)
                    }
                }
            }
            else -> throw IllegalArgumentException("Wrong viewtype:$viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DefaultMenuHolder -> {
                val item = items[position] as SlashItem.Color
                holder.bind(item)
            }
            is SubheaderMenuHolder -> {
                val item = items[position] as SlashItem.Subheader
                holder.bind(item)
            }
        }
    }

    override fun getItemViewType(position: Int): Int = when (val item = items[position]) {
        is SlashItem.Color.Background -> {
            if (position == DEFAULT_BACKGROUND_POSITION) R.layout.item_slash_widget_background_default
            else R.layout.item_slash_widget_background
        }
        is SlashItem.Color.Text -> R.layout.item_slash_widget_color
        is SlashItem.Subheader -> R.layout.item_slash_widget_subheader
        else -> throw IllegalArgumentException("Wrong item type:$item")
    }

    override fun getItemCount(): Int = items.size

    companion object {
        const val DEFAULT_BACKGROUND_POSITION = 1
    }
}