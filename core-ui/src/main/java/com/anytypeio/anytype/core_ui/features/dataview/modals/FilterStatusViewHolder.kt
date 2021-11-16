package com.anytypeio.anytype.core_ui.features.dataview.modals

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.color
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor
import com.anytypeio.anytype.presentation.extension.hasValue
import com.anytypeio.anytype.presentation.sets.model.FilterView
import kotlinx.android.synthetic.main.item_dv_viewer_filter_status.view.*
import timber.log.Timber

class FilterStatusViewHolder(view: View) : FilterViewHolder(view) {

    override val textTitle: TextView get() = itemView.tvTitle
    override val textCondition: TextView get() = itemView.tvCondition
    override val iconFormat: ImageView get() = itemView.iconFormat
    override val iconArrow: ImageView get() = itemView.iconArrow
    override val iconRemove: ImageView get() = itemView.iconRemoveStatus

    fun bind(
        item: FilterView.Expression.Status
    ) {
        setup(
            isEditMode = item.isInEditMode,
            title = item.title,
            condition = item.condition.title,
            format = item.format
        )
        if (item.condition.hasValue()) {
            itemView.tvValue.visible()
            val status = item.filterValue.value
            if (status != null) {
                itemView.tvValue.text = status.status
                setTextColor(itemView.tvValue, status.color)
            }
        } else {
            itemView.tvValue.text = null
            itemView.tvValue.invisible()
        }
    }

    private fun setTextColor(view: TextView, color: String) {
        if (color.isNotBlank()) {
            val value = ThemeColor.values().find { value -> value.title == color }
            if (value != null)
                view.setTextColor(value.text)
            else{
                Timber.e("Could not find value for text color: $color")
                view.setTextColor(view.context.color(R.color.black))
            }
        } else {
            view.setTextColor(view.context.color(R.color.black))
        }
    }
}