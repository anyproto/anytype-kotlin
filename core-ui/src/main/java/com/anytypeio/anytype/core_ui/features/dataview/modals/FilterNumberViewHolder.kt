package com.anytypeio.anytype.core_ui.features.dataview.modals

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.presentation.sets.model.FilterView
import kotlinx.android.synthetic.main.item_dv_viewer_filter_number.view.*

class FilterNumberViewHolder(view: View) : FilterViewHolder(view) {

    override val textTitle: TextView get() = itemView.tvTitle
    override val textCondition: TextView get() = itemView.tvCondition
    override val iconFormat: ImageView get() = itemView.iconFormat
    override val iconArrow: ImageView get() = itemView.iconArrow
    override val iconRemove: ImageView get() = itemView.iconRemoveNumber

    fun bind(
        item: FilterView.Expression.Number
    ) {
        setup(
            isEditMode = item.isInEditMode,
            title = item.title,
            condition = item.condition.title,
            format = item.format
        )
        itemView.tvValue.text =
            itemView.context.getString(R.string.value_quotes, item.filterValue.value.toString())
    }
}