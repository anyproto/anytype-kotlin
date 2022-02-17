package com.anytypeio.anytype.core_ui.features.dataview.modals

import android.widget.ImageView
import android.widget.TextView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemDvViewerFilterNumberBinding
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.extension.hasValue
import com.anytypeio.anytype.presentation.sets.model.FilterView

class FilterNumberViewHolder(val binding: ItemDvViewerFilterNumberBinding) : FilterViewHolder(binding.root) {

    override val textTitle: TextView get() = binding.tvTitle
    override val textCondition: TextView get() = binding.tvCondition
    override val iconFormat: ImageView get() = binding.iconFormat
    override val iconArrow: ImageView get() = binding.iconArrow
    override val iconRemove: ImageView get() = binding.iconRemoveNumber

    fun bind(
        item: FilterView.Expression.Number
    ) {
        setup(
            isEditMode = item.isInEditMode,
            title = item.title,
            condition = item.condition.title,
            format = item.format
        )
        if (item.condition.hasValue()) {
            binding.tvValue.visible()
            binding.tvValue.text = itemView.context.getString(R.string.value_quotes, item.filterValue.value.toString())
        } else {
            binding.tvValue.text = null
            binding.tvValue.invisible()
        }
    }
}