package com.anytypeio.anytype.core_ui.features.dataview.modals

import android.widget.ImageView
import android.widget.TextView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemDvViewerFilterStatusBinding
import com.anytypeio.anytype.core_ui.extensions.color
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor
import com.anytypeio.anytype.presentation.extension.hasValue
import com.anytypeio.anytype.presentation.sets.model.FilterView
import timber.log.Timber

class FilterStatusViewHolder(val binding: ItemDvViewerFilterStatusBinding) :
    FilterViewHolder(binding.root) {

    override val textTitle: TextView get() = binding.tvTitle
    override val textCondition: TextView get() = binding.tvCondition
    override val iconFormat: ImageView get() = binding.iconFormat
    override val iconArrow: ImageView get() = binding.iconArrow
    override val iconRemove: ImageView get() = binding.iconRemoveStatus

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
            binding.tvValue.visible()
            val status = item.filterValue.value
            if (status != null) {
                binding.tvValue.text = status.status
                setTextColor(binding.tvValue, status.color)
            }
        } else {
            binding.tvValue.text = null
            binding.tvValue.invisible()
        }
    }

    private fun setTextColor(view: TextView, color: String) {
        val defaultColor = view.context.color(R.color.default_filter_tag_text_color)
        val value = ThemeColor.values().find { value -> value.code == color }
        if (value == null) {
            Timber.w("Could not find value for text color: $color")
        }
        view.setTextColor(view.resources.dark(value ?: ThemeColor.DEFAULT, defaultColor))
    }
}