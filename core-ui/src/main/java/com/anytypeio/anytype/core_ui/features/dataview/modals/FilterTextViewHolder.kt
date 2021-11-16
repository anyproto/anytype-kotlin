package com.anytypeio.anytype.core_ui.features.dataview.modals

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.extension.hasValue
import com.anytypeio.anytype.presentation.sets.model.ColumnView
import com.anytypeio.anytype.presentation.sets.model.FilterView
import kotlinx.android.synthetic.main.item_dv_viewer_filter_text.view.*

class FilterTextViewHolder(view: View) : FilterViewHolder(view) {

    override val textTitle: TextView get() = itemView.tvTitle
    override val textCondition: TextView get() = itemView.tvCondition
    override val iconFormat: ImageView get() = itemView.iconFormat
    override val iconArrow: ImageView get() = itemView.iconArrow
    override val iconRemove: ImageView get() = itemView.iconRemoveText

    fun bind(
        item: FilterView.Expression
    ) {
        init(
            isInEditMode = item.isInEditMode,
            condition = item.condition.title,
            title = item.title,
            format = item.format
        )
        if (item.condition.hasValue()) {
            itemView.tvValue.visible()
            itemView.tvValue.text = when (item) {
                is FilterView.Expression.Email -> {
                    getStringWithQuotes(item.filterValue.value)
                }
                is FilterView.Expression.Phone -> {
                    getStringWithQuotes(item.filterValue.value)
                }
                is FilterView.Expression.Text -> {
                    getStringWithQuotes(item.filterValue.value)
                }
                is FilterView.Expression.TextShort -> {
                    getStringWithQuotes(item.filterValue.value)
                }
                is FilterView.Expression.Url -> {
                    getStringWithQuotes(item.filterValue.value)
                }
                else -> null
            }
        } else {
            itemView.tvValue.invisible()
            itemView.tvValue.text = null
        }
    }

    private fun getStringWithQuotes(value: String?): String =
        if (value == null) "" else itemView.context.getString(R.string.value_quotes, value)

    private fun init(
        isInEditMode: Boolean,
        title: String,
        condition: String,
        format: ColumnView.Format
    ) {
        setup(
            isEditMode = isInEditMode,
            title = title,
            condition = condition,
            format = format
        )
    }
}