package com.anytypeio.anytype.core_ui.features.dataview.modals

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_ui.extensions.relationIcon
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.sets.model.ColumnView

abstract class FilterViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    abstract val textTitle: TextView
    abstract val textCondition: TextView
    abstract val iconFormat: ImageView
    abstract val iconArrow: ImageView
    abstract val iconRemove: ImageView

    fun setup(isEditMode: Boolean, title: String, condition: String, format: ColumnView.Format) {
        if (isEditMode) {
            iconRemove.visible()
            iconArrow.gone()
        } else {
            iconRemove.gone()
            iconArrow.visible()
        }
        textTitle.text = title
        textCondition.text = condition
        iconFormat.setBackgroundResource(format.relationIcon(isMedium = true))
    }
}