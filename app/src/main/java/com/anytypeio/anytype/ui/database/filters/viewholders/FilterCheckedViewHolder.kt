package com.anytypeio.anytype.ui.database.filters.viewholders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.presentation.databaseview.models.FilterView
import kotlinx.android.synthetic.main.item_filter.view.*

class FilterCheckedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(filter: FilterView, click: (FilterView) -> Unit) {
        itemView.setOnClickListener {
            click.invoke(filter)
        }
        itemView.name.text = filter.name
    }
}