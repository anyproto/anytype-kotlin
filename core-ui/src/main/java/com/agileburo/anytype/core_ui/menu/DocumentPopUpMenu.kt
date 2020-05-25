package com.agileburo.anytype.core_ui.menu

import android.content.Context
import android.view.View
import android.widget.PopupMenu
import com.agileburo.anytype.core_ui.R

class DocumentPopUpMenu(
    context: Context,
    view: View,
    onArchiveClicked: () -> Unit,
    onRedoClicked: () -> Unit,
    onUndoClicked: () -> Unit
) : PopupMenu(context, view) {

    init {
        menuInflater.inflate(R.menu.menu_page, menu)
        setOnMenuItemClickListener { item ->
            when(item.itemId) {
                R.id.archive -> onArchiveClicked()
                R.id.undo -> onUndoClicked()
                R.id.redo -> onRedoClicked()
                else -> throw IllegalStateException("Unexpected menu item: $item")
            }
            true
        }
    }
}