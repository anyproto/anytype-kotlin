package com.anytypeio.anytype.core_ui.features.dataview.modals

import android.widget.ImageView
import android.widget.TextView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemDvViewerFilterObjectBinding
import com.anytypeio.anytype.core_ui.widgets.ObjectIconWidget
import com.anytypeio.anytype.core_ui.widgets.RelationFormatIconWidget
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.extension.hasValue
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.presentation.sets.model.FilterView
import com.anytypeio.anytype.presentation.sets.model.ObjectView

class FilterObjectViewHolder(val binding: ItemDvViewerFilterObjectBinding) :
    FilterViewHolder(binding.root) {

    override val textTitle: TextView get() = binding.tvTitle
    override val textCondition: TextView get() = binding.tvCondition
    override val iconFormat: RelationFormatIconWidget get() = binding.iconFormat
    override val iconArrow: ImageView get() = binding.iconArrow
    override val iconRemove: ImageView get() = binding.iconRemoveObject

    private val objectIcon: ObjectIconWidget get() = binding.objectIcon
    private val number: TextView get() = binding.number
    private val objectName: TextView get() = binding.objectName

    fun bind(item: FilterView.Expression.Object) = with(itemView) {
        setup(
            relationKey = item.relation,
            isEditMode = item.isInEditMode,
            title = item.title,
            condition = item.condition.title,
            format = item.format
        )

        if (item.condition.hasValue() && item.filterValue.value.isNotEmpty()) {
            item.filterValue.value.forEachIndexed { index, objectView ->
                if (objectView is ObjectView.Default) {
                    if (index == 0) {
                        objectName.text = objectView.name.ifBlank {
                            context.resources.getString(R.string.untitled)
                        }
                        when (objectView.icon) {
                            ObjectIcon.None -> objectIcon.gone()
                            else -> {
                                objectIcon.visible()
                                objectIcon.setIcon(objectView.icon)
                            }
                        }
                    }
                }
            }
            val valuesSize = item.filterValue.value.size

            if (valuesSize > MAX_ITEMS) {
                number.visible()
                number.text = "+${valuesSize - MAX_ITEMS}"
            } else {
                number.gone()
            }
        } else {
            objectIcon.gone()
            number.gone()
            objectName.text = null
        }
    }

    companion object {
        private const val MAX_ITEMS = 1
    }
}