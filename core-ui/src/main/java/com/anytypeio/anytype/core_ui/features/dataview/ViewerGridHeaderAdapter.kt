package com.anytypeio.anytype.core_ui.features.dataview

import android.view.View
import android.view.ViewGroup
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.AbstractAdapter
import com.anytypeio.anytype.core_ui.common.AbstractViewHolder
import com.anytypeio.anytype.presentation.sets.model.ColumnView
import kotlinx.android.synthetic.main.viewer_cell.view.*

class ViewerGridHeaderAdapter(
    items: List<ColumnView> = emptyList(),
    val onCreateNewColumnClicked: () -> Unit,
) : AbstractAdapter<ColumnView>(items) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): AbstractViewHolder<ColumnView> = when (viewType) {
        HEADER_TYPE -> {
            DefaultHolder(
                view = inflate(parent, R.layout.item_grid_column_header)
            )
        }
        PLUS_TYPE -> {
            PlusHolder(
                view = inflate(parent, R.layout.item_grid_column_header_plus)
            ).apply {
                itemView.setOnClickListener {
                    onCreateNewColumnClicked()
                }
            }
        }
        else -> throw IllegalStateException("Unexpected view type: $viewType")
    }

    override fun onBindViewHolder(holder: AbstractViewHolder<ColumnView>, position: Int) {
        if (position < items.size) super.onBindViewHolder(holder, position)
    }

    override fun getItemViewType(position: Int) = if (position == items.size) {
        PLUS_TYPE
    } else {
        HEADER_TYPE
    }

    override fun getItemCount(): Int {
        return if (super.getItemCount() == 0)
            0
        else
            super.getItemCount() + 1
    }

    class DefaultHolder(view: View) : AbstractViewHolder<ColumnView>(view) {
        override fun bind(item: ColumnView) {
            itemView.cellText.text = item.text
        }
    }

    class PlusHolder(view: View) : AbstractViewHolder<ColumnView>(view) {
        override fun bind(item: ColumnView) {}
    }

    override fun update(update: List<ColumnView>) {
        items = update
        notifyDataSetChanged()
    }

    companion object {
        const val HEADER_TYPE = 0
        const val PLUS_TYPE = 1
    }
}