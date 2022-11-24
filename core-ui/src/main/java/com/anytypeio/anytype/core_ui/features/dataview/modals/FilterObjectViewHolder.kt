package com.anytypeio.anytype.core_ui.features.dataview.modals

import android.widget.ImageView
import android.widget.TextView
import com.anytypeio.anytype.core_ui.databinding.ItemDvViewerFilterObjectBinding
import com.anytypeio.anytype.core_ui.widgets.ObjectIconTextWidget
import com.anytypeio.anytype.core_ui.widgets.RelationFormatIconWidget
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.extension.hasValue
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.sets.model.FilterView
import com.anytypeio.anytype.presentation.sets.model.ObjectView

class FilterObjectViewHolder(val binding: ItemDvViewerFilterObjectBinding) : FilterViewHolder(binding.root) {

    override val textTitle: TextView get() = binding.tvTitle
    override val textCondition: TextView get() = binding.tvCondition
    override val iconFormat: RelationFormatIconWidget get() = binding.iconFormat
    override val iconArrow: ImageView get() = binding.iconArrow
    override val iconRemove: ImageView get() = binding.iconRemoveObject

    fun bind(item: FilterView.Expression.Object) = with(itemView) {
        setup(
            isEditMode = item.isInEditMode,
            title = item.title,
            condition = item.condition.title,
            format = item.format
        )

        for (i in 0..MAX_VISIBLE_OBJECTS_INDEX) getViewByIndex(i)?.gone()
        if (item.condition.hasValue()) {
            item.filterValue.value.forEachIndexed { index, objectView ->
                if (objectView is ObjectView.Default) {
                    when (index) {
                        in 0..MAX_VISIBLE_OBJECTS_INDEX -> {
                            getViewByIndex(index)?.let { view ->
                                view.visible()
                                view.setup(
                                    name = objectView.name,
                                    icon = objectView.icon
                                )
                            }
                        }
                    }
                }
            }
        } else {
            for (i in 0..MAX_VISIBLE_OBJECTS_INDEX) getViewByIndex(i)?.apply {
                this.setup(name = null, icon = ObjectIcon.None)
            }
        }
    }

    private fun getViewByIndex(index: Int): ObjectIconTextWidget? = when (index) {
        0 -> binding.object0
        1 -> binding.object1
        2 -> binding.object2
        else -> null
    }

    companion object {
        const val MAX_VISIBLE_OBJECTS_INDEX = 2
    }
}