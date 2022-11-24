package com.anytypeio.anytype.core_ui.features.dataview.modals

import android.widget.ImageView
import android.widget.TextView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemDvViewerFilterCheckboxBinding
import com.anytypeio.anytype.core_ui.widgets.RelationFormatIconWidget
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.extension.hasValue
import com.anytypeio.anytype.presentation.sets.model.FilterView

class FilterCheckboxViewHolder(
    val binding: ItemDvViewerFilterCheckboxBinding
) : FilterViewHolder(binding.root) {

    override val textTitle: TextView get() = binding.tvTitle
    override val textCondition: TextView get() = binding.tvCondition
    override val iconFormat: RelationFormatIconWidget get() = binding.iconFormat
    override val iconArrow: ImageView get() = binding.iconArrow
    override val iconRemove: ImageView get() = binding.iconRemoveCheckbox

    fun bind(item: FilterView.Expression.Checkbox) = with(itemView) {
        setup(
            isEditMode = item.isInEditMode,
            title = item.title,
            condition = item.condition.title,
            format = item.format
        )
        if (item.condition.hasValue()) {
            binding.tvValue.visible()
            binding.tvValue.text = if (item.filterValue.value == true) {
                itemView.context.getString(R.string.dv_filter_checkbox_checked)
            } else {
                itemView.context.getString(R.string.dv_filter_checkbox_not_checked)
            }
        } else {
            binding.tvValue.text = null
            binding.tvValue.invisible()
        }
    }
}