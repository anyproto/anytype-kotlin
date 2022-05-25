package com.anytypeio.anytype.core_ui.features.relations.holders

import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemEditCellObjectBinding
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.relations.RelationValueView

class ObjectRelationObjectHolder(
    val binding: ItemEditCellObjectBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: RelationValueView.Object.Default) = with(binding) {
        tvTitle.text = item.name
        if (item.typeName != null) {
            tvSubtitle.text = item.typeName
        } else {
            tvSubtitle.setText(R.string.unknown_object_type)
        }
        objectSelectionIndex.visible()
        objectSelectionIndex.isSelected = item.isSelected == true
        iconWidget.setIcon(item.icon)
    }
}