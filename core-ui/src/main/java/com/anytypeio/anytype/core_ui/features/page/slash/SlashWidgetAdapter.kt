package com.anytypeio.anytype.core_ui.features.page.slash

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.features.page.slash.holders.MainMenuHolder
import com.anytypeio.anytype.core_ui.features.page.slash.holders.StyleMenuHolder
import com.anytypeio.anytype.presentation.page.editor.slash.SlashItem

class SlashWidgetAdapter(
    private var items: List<SlashItem>,
    private val clicks: (SlashItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun update(items: List<SlashItem>) {
        this.items = items
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            R.layout.item_slash_widget_main -> {
                MainMenuHolder(
                    view = inflater.inflate(viewType, parent, false)
                ).apply {
                    itemView.setOnClickListener {
                        clicks(items[bindingAdapterPosition])
                    }
                }
            }
            R.layout.item_slash_widget_style -> {
                StyleMenuHolder(
                    view = inflater.inflate(viewType, parent, false)
                ).apply {
                    itemView.setOnClickListener {
                        clicks(items[bindingAdapterPosition])
                    }
                }
            }
            else -> throw IllegalArgumentException("Wrong viewtype:$viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is MainMenuHolder -> {
                val item = items[position] as SlashItem.Main
                holder.bind(item)
            }
            is StyleMenuHolder -> {
                val item = items[position] as SlashItem.Style
                holder.bind(item)
            }
        }
    }

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is SlashItem.Main -> R.layout.item_slash_widget_main
        is SlashItem.Style -> R.layout.item_slash_widget_style
        is SlashItem.Actions -> R.layout.item_slash_widget_actions
        is SlashItem.Alignment -> R.layout.item_slash_widget_alignment
        is SlashItem.Media -> R.layout.item_slash_widget_media
        is SlashItem.Other -> R.layout.item_slash_widget_other
    }

    override fun getItemCount(): Int = items.size
}