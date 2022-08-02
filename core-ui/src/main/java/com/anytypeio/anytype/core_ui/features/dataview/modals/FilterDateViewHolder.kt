package com.anytypeio.anytype.core_ui.features.dataview.modals

import android.widget.ImageView
import android.widget.TextView
import com.anytypeio.anytype.core_models.DVFilterQuickOption
import com.anytypeio.anytype.core_ui.databinding.ItemDvViewerFilterDateBinding
import com.anytypeio.anytype.core_utils.ext.formatTimestamp
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.extension.hasValue
import com.anytypeio.anytype.presentation.relations.toName
import com.anytypeio.anytype.presentation.sets.model.FilterView

class FilterDateViewHolder(val binding: ItemDvViewerFilterDateBinding) :
    FilterViewHolder(binding.root) {

    override val textTitle: TextView get() = binding.tvTitle
    override val textCondition: TextView get() = binding.tvCondition
    override val iconFormat: ImageView get() = binding.iconFormat
    override val iconArrow: ImageView get() = binding.iconArrow
    override val iconRemove: ImageView get() = binding.iconRemoveDate

    fun bind(
        item: FilterView.Expression.Date
    ) = with(itemView) {
        setup(
            isEditMode = item.isInEditMode,
            title = item.title,
            condition = item.condition.title,
            format = item.format
        )
        if (item.condition.hasValue()) {
            binding.tvValue.visible()

            binding.tvValue.text = when (item.quickOption) {
                DVFilterQuickOption.DAYS_AGO -> "${item.filterValue.value?.toString()} days ago"
                DVFilterQuickOption.DAYS_AHEAD -> "${item.filterValue.value?.toString()} days from now"
                DVFilterQuickOption.EXACT_DATE -> item.filterValue.value?.formatTimestamp(isMillis = false)
                else -> item.quickOption.toName()
            }
        } else {
            binding.tvValue.text = null
            binding.tvValue.invisible()
        }
    }
}