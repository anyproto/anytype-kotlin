package com.agileburo.anytype.ui.database.filters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.R
import com.agileburo.anytype.presentation.databaseview.models.FilterView
import com.agileburo.anytype.ui.database.filters.viewholders.FilterCheckedViewHolder
import com.agileburo.anytype.ui.database.filters.viewholders.FilterViewHolder
import com.agileburo.anytype.ui.database.table.adapter.toView

class FiltersAdapter(
    private val onFilterClick: (FilterView) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var data = mutableListOf<FilterView>()

    override fun getItemViewType(position: Int): Int = when (data[position].isChecked) {
        true -> TYPE_CHECKED
        false -> TYPE_UNCHECKED
    }

    fun setData(filters: List<FilterView>) {
        data.clear()
        data.addAll(filters)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            TYPE_CHECKED -> FilterCheckedViewHolder(
                LayoutInflater.from(parent.context).toView(
                    R.layout.item_filter_checked,
                    parent
                )
            )
            TYPE_UNCHECKED -> FilterViewHolder(
                LayoutInflater.from(parent.context).toView(
                    R.layout.item_filter,
                    parent
                )
            )
            else -> throw RuntimeException("Wrong filter ViewType")
        }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = when (holder) {
        is FilterCheckedViewHolder -> {
            holder.bind(data[position], onFilterClick)
        }
        is FilterViewHolder -> {
            holder.bind(data[position], onFilterClick)
        }
        else -> {
        }
    }

    companion object {
        const val TYPE_CHECKED = 1
        const val TYPE_UNCHECKED = 2
        const val TYPE_PLUS = 3
    }
}