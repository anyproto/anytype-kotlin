package com.anytypeio.anytype.core_ui.widgets.toolbar

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.layout.SpacingItemDecoration
import kotlinx.android.synthetic.main.item_paginator_page.view.*
import kotlinx.android.synthetic.main.widget_data_view_pagination_toolbar.view.*

class DataViewPaginatorToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    var onNumberClickCallback :  (Pair<Int, Boolean>) -> Unit = {}

    private val paginatorAdapter = Adapter { onNumberClickCallback(it) }

    init {
        LayoutInflater.from(context).inflate(R.layout.widget_data_view_pagination_toolbar, this)
        setBackgroundColor(Color.WHITE)
        rvPaginator.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            addItemDecoration(
                SpacingItemDecoration(
                    spacingStart = resources.getDimension(R.dimen.dp_12).toInt(),
                    spacingEnd = resources.getDimension(R.dimen.dp_12).toInt()
                )
            )
            adapter = paginatorAdapter
        }
    }

    fun set(count: Int, index: Int) {
        val update = mutableListOf<Pair<Int, Boolean>>()
        repeat(count) {
            val number = it.inc()
            update.add(
                Pair(number, index == number)
            )
        }
        paginatorAdapter.submitList(update)
        if (index > 0) rvPaginator.smoothScrollToPosition(index)
    }

    class Adapter(
        private val onNumberClicked: (Pair<Int, Boolean>) -> Unit
    ): ListAdapter<Pair<Int, Boolean>, Adapter.ViewHolder>(PageNumberDiffer) {

        override fun onCreateViewHolder(
            parent: ViewGroup, viewType: Int
        ): ViewHolder = ViewHolder(parent).apply {
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onNumberClicked(getItem(pos))
                }
            }
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(getItem(position))
        }

        class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_paginator_page,
                parent,
                false
            )
        ) {
            fun bind(item: Pair<Int, Boolean>) {
                val (num, isSelected) = item
                itemView.tvNumber.text = num.toString()
                itemView.tvNumber.isSelected = isSelected
            }
        }
    }

    object PageNumberDiffer : DiffUtil.ItemCallback<Pair<Int, Boolean>>() {
        override fun areItemsTheSame(
            oldItem: Pair<Int, Boolean>,
            newItem: Pair<Int, Boolean>
        ): Boolean = newItem.first == oldItem.first
        override fun areContentsTheSame(
            oldItem: Pair<Int, Boolean>,
            newItem: Pair<Int, Boolean>
        ): Boolean = newItem.first == oldItem.first && newItem.second == oldItem.second
    }
}