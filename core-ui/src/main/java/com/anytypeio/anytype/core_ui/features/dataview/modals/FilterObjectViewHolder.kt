package com.anytypeio.anytype.core_ui.features.dataview.modals

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.anytypeio.anytype.core_ui.widgets.ObjectIconTextWidget
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.sets.model.FilterView
import com.anytypeio.anytype.presentation.sets.model.ObjectView
import kotlinx.android.synthetic.main.item_dv_viewer_filter_object.view.*

class FilterObjectViewHolder(view: View) : FilterViewHolder(view) {

    override val textTitle: TextView get() = itemView.tvTitle
    override val textCondition: TextView get() = itemView.tvCondition
    override val iconFormat: ImageView get() = itemView.iconFormat
    override val iconArrow: ImageView get() = itemView.iconArrow
    override val iconRemove: ImageView get() = itemView.iconRemoveObject

    fun bind(item: FilterView.Expression.Object) = with(itemView) {
        setup(
            isEditMode = item.isInEditMode,
            title = item.title,
            condition = item.condition.title,
            format = item.format
        )
        for (i in 0..MAX_VISIBLE_OBJECTS_INDEX) getViewByIndex(i)?.gone()
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
    }

    private fun getViewByIndex(index: Int): ObjectIconTextWidget? = when (index) {
        0 -> itemView.object0
        1 -> itemView.object1
        2 -> itemView.object2
        else -> null
    }

    companion object {
        const val MAX_VISIBLE_OBJECTS_INDEX = 2
    }
}