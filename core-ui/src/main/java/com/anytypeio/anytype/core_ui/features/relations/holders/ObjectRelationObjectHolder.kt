package com.anytypeio.anytype.core_ui.features.relations.holders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.sets.RelationValueBaseViewModel.RelationValueView
import kotlinx.android.synthetic.main.item_edit_cell_object.view.*

class ObjectRelationObjectHolder(view: View) : RecyclerView.ViewHolder(view) {

    fun bind(item: RelationValueView.Object) = with(itemView) {
        tvTitle.text = item.name
        if (item.type != null) {
            tvSubtitle.text = item.type
        } else {
            tvSubtitle.setText(R.string.unknown_object_type)
        }
        objectSelectionIndex.visible()
        if (item.isSelected == true) {
            objectSelectionIndex.isSelected = true
            objectSelectionIndex.text = item.selectedNumber
        } else {
            objectSelectionIndex.isSelected = false
            objectSelectionIndex.text = null
        }
        iconWidget.setIcon(
            emoji = item.emoji,
            image = item.image,
            name = item.name
        )
    }
}