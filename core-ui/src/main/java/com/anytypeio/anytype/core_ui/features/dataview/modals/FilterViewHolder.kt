package com.anytypeio.anytype.core_ui.features.dataview.modals

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_ui.widgets.RelationFormatIconWidget
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible

abstract class FilterViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    abstract val textTitle: TextView
    abstract val textCondition: TextView
    abstract val iconFormat: RelationFormatIconWidget
    abstract val iconArrow: ImageView
    abstract val iconRemove: ImageView

    fun setup(
        relationKey: Key,
        isEditMode: Boolean,
        title: String,
        condition: String,
        format: RelationFormat
    ) {
        if (isEditMode) {
            iconRemove.visible()
            iconArrow.gone()
        } else {
            iconRemove.gone()
            iconArrow.visible()
        }
        textTitle.text = title
        textCondition.text = condition
        iconFormat.bind(isName = relationKey == Relations.NAME, format = format)
    }
}