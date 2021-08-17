package com.anytypeio.anytype.core_ui.features.editor.holders.text

import android.view.View
import android.widget.TextView
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import kotlinx.android.synthetic.main.item_block_description.view.*
import timber.log.Timber

class Description(view: View) : BlockViewHolder(view) {

    private val content = itemView.tvBlockDescription

    fun bind(view: BlockView.Description) {
        if (view.mode == BlockView.Mode.READ) enableReadMode() else enableEditMode()
        if (!content.hasFocus()) {
            content.pauseTextWatchers {
                content.setText(view.description, TextView.BufferType.EDITABLE)
            }
        } else {
            Timber.d("Skipping binding for block in focus")
        }
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