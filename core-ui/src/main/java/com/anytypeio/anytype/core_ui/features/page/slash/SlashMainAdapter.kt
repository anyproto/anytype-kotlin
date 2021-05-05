package com.anytypeio.anytype.core_ui.features.page.slash

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.features.page.slash.holders.MainMenuHolder
import com.anytypeio.anytype.presentation.page.editor.slash.SlashItem

class SlashMainAdapter(
    private var items: List<SlashItem>,
    private val clicks: (SlashItem) -> Unit
) : RecyclerView.Adapter<MainMenuHolder>() {

    fun update(items: List<SlashItem>) {
        this.items = items
        notifyDataSetChanged()
    }

    fun clear() {
        val size = items.size
        if (size > 0) {
            items = listOf()
            notifyItemRangeRemoved(0, size)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainMenuHolder {
        val inflater = LayoutInflater.from(parent.context)
        return MainMenuHolder(
            view = inflater.inflate(R.layout.item_slash_widget_main, parent, false)
        ).apply {
            itemView.setOnClickListener {
                clicks(items[bindingAdapterPosition])
            }
        }
    }

    override fun onBindViewHolder(holder: MainMenuHolder, position: Int) {
        val item = items[position] as SlashItem.Main
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size
}