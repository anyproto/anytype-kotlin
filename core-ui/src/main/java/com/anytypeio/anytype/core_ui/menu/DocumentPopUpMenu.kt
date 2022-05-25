package com.anytypeio.anytype.core_ui.menu

import android.content.Context
import android.view.View
import android.widget.PopupMenu
import com.anytypeio.anytype.core_ui.R

class DocumentPopUpMenu(
    context: Context,
    view: View,
    onArchiveClicked: () -> Unit,
    onRedoClicked: () -> Unit,
    onUndoClicked: () -> Unit,
    onEnterMultiSelect: () -> Unit,
    onSearchClicked: () -> Unit,
    onRelationsClicked: () -> Unit
) : PopupMenu(context, view) {

    init {
        menuInflater.inflate(R.menu.menu_page, menu)
        setOnMenuItemClickListener { item ->
            when(item.itemId) {
                R.id.archive -> onArchiveClicked()
                R.id.undo -> onUndoClicked()
                R.id.redo -> onRedoClicked()
                R.id.select -> onEnterMultiSelect()
                R.id.search -> onSearchClicked()
                R.id.relations -> onRelationsClicked()
                else -> throw IllegalStateException("Unexpected menu item: $item")
            }
            true
        }
    }
}