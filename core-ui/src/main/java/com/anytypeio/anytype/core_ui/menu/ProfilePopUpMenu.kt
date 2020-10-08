package com.anytypeio.anytype.core_ui.menu

import android.content.Context
import android.view.View
import android.widget.PopupMenu
import com.anytypeio.anytype.core_ui.R

class ProfilePopUpMenu(
    context: Context,
    view: View,
    onRedoClicked: () -> Unit,
    onUndoClicked: () -> Unit,
    onEnterMultiSelect: () -> Unit
) : PopupMenu(context, view) {

    init {
        menuInflater.inflate(R.menu.menu_profile, menu)
        setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.undo -> onUndoClicked()
                R.id.redo -> onRedoClicked()
                R.id.select -> onEnterMultiSelect()
                else -> throw IllegalStateException("Unexpected menu item: $item")
            }
            true
        }
    }
}