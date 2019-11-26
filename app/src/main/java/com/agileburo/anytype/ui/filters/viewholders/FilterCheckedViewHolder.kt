package com.agileburo.anytype.ui.filters.viewholders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.presentation.filters.model.FilterView
import kotlinx.android.synthetic.main.item_filter.view.*

class FilterCheckedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(filter: FilterView, click: (String) -> Unit) {
        itemView.setOnClickListener {
            click.invoke(filter.id)
        }
        itemView.name.text = filter.name
    }
}