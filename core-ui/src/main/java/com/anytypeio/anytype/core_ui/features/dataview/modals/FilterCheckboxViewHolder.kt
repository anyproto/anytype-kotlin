package com.anytypeio.anytype.core_ui.features.dataview.modals

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.presentation.sets.model.FilterView
import kotlinx.android.synthetic.main.item_dv_viewer_filter_checkbox.view.*

class FilterCheckboxViewHolder(view: View) : FilterViewHolder(view) {

    override val textTitle: TextView get() = itemView.tvTitle
    override val textCondition: TextView get() = itemView.tvCondition
    override val iconFormat: ImageView get() = itemView.iconFormat
    override val iconArrow: ImageView get() = itemView.iconArrow
    override val iconRemove: ImageView get() = itemView.iconRemoveCheckbox

    fun bind(item: FilterView.Expression.Checkbox) = with(itemView) {
        setup(
            isEditMode = item.isInEditMode,
            title = item.title,
            condition = item.condition.title,
            format = item.format
        )
        tvValue.text = if (item.filterValue.value == true) {
            itemView.context.getString(R.string.dv_filter_checkbox_checked)
        } else {
            itemView.context.getString(R.string.dv_filter_checkbox_not_checked)
        }
    }
}