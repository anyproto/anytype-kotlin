package com.agileburo.anytype.ui.filters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.R
import com.agileburo.anytype.presentation.filters.model.FilterView
import com.agileburo.anytype.ui.filters.viewholders.FilterCheckedViewHolder
import com.agileburo.anytype.ui.filters.viewholders.FilterViewHolder
import com.agileburo.anytype.ui.table.toView
import java.lang.RuntimeException

class FiltersAdapter(
    private val click: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var data = mutableListOf<FilterView>()

    override fun getItemViewType(position: Int): Int = when (data[position].isChecked) {
        true -> TYPE_CHECKED
        false -> TYPE_UNCHECKED
    }

    fun updateData(filters: List<FilterView>) {
        val size = data.size
        data.addAll(filters)
        notifyItemRangeInserted(size, filters.size)
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
            holder.bind(data[position], click)
        }
        is FilterViewHolder -> {
            holder.bind(data[position], click)
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