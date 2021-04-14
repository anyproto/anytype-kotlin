package com.anytypeio.anytype.core_ui.features.editor.holders.text

import android.view.View
import android.widget.TextView
import com.anytypeio.anytype.core_ui.features.page.BlockViewHolder
import com.anytypeio.anytype.presentation.page.editor.model.BlockView
import kotlinx.android.synthetic.main.item_block_description.view.*

class Description(view: View) : BlockViewHolder(view) {
    fun bind(view: BlockView.Description) {
        if (view.mode == BlockView.Mode.READ) enableReadMode() else enableEditMode()
        itemView.tvBlockDescription.setText(view.description, TextView.BufferType.EDITABLE)
    }
    fun enableReadMode() {
        itemView.tvBlockDescription.enableReadMode()
        itemView.tvBlockDescription.selectionWatcher = null
        itemView.tvBlockDescription.clearTextWatchers()
    }

    fun enableEditMode() {
        itemView.tvBlockDescription.enableEditMode()
    }
}