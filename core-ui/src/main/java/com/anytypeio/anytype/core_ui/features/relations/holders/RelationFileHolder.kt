package com.anytypeio.anytype.core_ui.features.relations.holders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.extensions.getMimeIcon
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.sets.RelationValueBaseViewModel.RelationValueView
import kotlinx.android.synthetic.main.item_edit_cell_file.view.*

class RelationFileHolder(view: View) : RecyclerView.ViewHolder(view) {

    fun bind(item: RelationValueView.File) = with(itemView) {
        tvTitle.text = "${item.name}.${item.ext}"
        val mimeIcon = item.mime.getMimeIcon(item.name)
        iconMime.setImageResource(mimeIcon)
        fileSelectionIndex.visible()
        if (item.isSelected == true) {
            fileSelectionIndex.isSelected = true
            fileSelectionIndex.text = item.selectedNumber
        } else {
            fileSelectionIndex.isSelected = false
            fileSelectionIndex.text = null
        }
    }
}