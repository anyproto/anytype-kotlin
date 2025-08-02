package com.anytypeio.anytype.core_ui.features.dataview.modals

import android.widget.ImageView
import android.widget.TextView
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemDvViewerFilterTagBinding
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.core_ui.extensions.light
import com.anytypeio.anytype.core_ui.widgets.RelationFormatIconWidget
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.setDrawableColor
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.extension.hasValue
import com.anytypeio.anytype.presentation.sets.model.FilterView

class FilterTagViewHolder(val binding: ItemDvViewerFilterTagBinding) :
    FilterViewHolder(binding.root) {

    override val textTitle: TextView get() = binding.tvTitle
    override val textCondition: TextView get() = binding.tvCondition
    override val iconFormat: RelationFormatIconWidget get() = binding.iconFormat
    override val iconArrow: ImageView get() = binding.iconArrow
    override val iconRemove: ImageView get() = binding.iconRemoveTag

    private val tagView = binding.tag
    private val numberView: TextView get() = binding.number

    fun bind(
        item: FilterView.Expression.Tag
    ) {
        setup(
            relationKey = item.relation,
            isEditMode = item.isInEditMode,
            title = item.title,
            condition = item.condition.title,
            format = item.format
        )
        if (item.condition.hasValue()) {
            val valuesSize = item.filterValue.value.size
            if (item.filterValue.value.isNotEmpty()) {
                val tag = item.filterValue.value.first()
                val color = ThemeColor.entries.find { it.code == tag.color }
                setupTag(tagView, color, tag.tag)
            } else {
                tagView.gone()
            }
            if (valuesSize > MAX_ITEMS) {
                numberView.visible()
                numberView.text = "+${valuesSize - MAX_ITEMS}"
            } else {
                numberView.gone()
            }
        } else {
            tagView.gone()
            numberView.gone()
        }
    }

    private fun setupTag(
        textView: TextView,
        color: ThemeColor?,
        txt: String
    ) {
        textView.apply {
            visible()
            val textColorPrimary = resources.getColor(R.color.text_secondary, null)
            val defaultBackground = resources.getColor(R.color.shape_primary, null)
            setTextColor(color?.let { resources.dark(it, textColorPrimary) } ?: textColorPrimary)
            setBackgroundResource(R.drawable.rect_dv_cell_tag_item)
            background.setDrawableColor(color?.let { resources.light(it, defaultBackground) }
                ?: defaultBackground)
            text = txt
        }
    }

    companion object {
        private const val MAX_ITEMS = 1
    }
}