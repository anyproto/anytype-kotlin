package com.anytypeio.anytype.core_ui.features.editor.slash

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.features.editor.slash.holders.ColorMenuHolder
import com.anytypeio.anytype.core_ui.features.editor.slash.holders.SubheaderMenuHolder
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashItem
import kotlinx.android.synthetic.main.item_slash_widget_subheader.view.*

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
            R.layout.item_slash_widget_color -> {
                ColorMenuHolder(
                    view = inflater.inflate(viewType, parent, false)
                ).apply {
                    itemView.setOnClickListener {
                        clicks(items[bindingAdapterPosition])
                    }
                }
            }
            R.layout.item_slash_widget_subheader -> {
                SubheaderMenuHolder(
                    view = inflater.inflate(viewType, parent, false)
                ).apply {
                    itemView.flBack.setOnClickListener {
                        clicks.invoke(SlashItem.Back)
                    }
                }
            }
            else -> throw IllegalArgumentException("Wrong viewtype:$viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ColorMenuHolder -> {
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
        is SlashItem.Color -> R.layout.item_slash_widget_color
        is SlashItem.Subheader -> R.layout.item_slash_widget_subheader
        else -> throw IllegalArgumentException("Wrong item type:$item")
    }

    override fun getItemCount(): Int = items.size
}