package com.anytypeio.anytype.core_ui.menu

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.PopupMenu
import com.anytypeio.anytype.core_ui.R

class ObjectHeaderContextMenu(
    context: Context,
    view: View,
    showMoveToBin: Boolean,
    onOpenAsObjectClicked: () -> Unit,
    onCopyLinkClicked: () -> Unit,
    onMoveToBinClicked: () -> Unit
) : PopupMenu(context, view, Gravity.BOTTOM, 0, R.style.DefaultPopupMenuStyle) {
    init {
        menuInflater.inflate(R.menu.menu_object_header_context, menu)
        menu.findItem(R.id.move_to_bin)?.isVisible = showMoveToBin
        setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.open_as_object -> onOpenAsObjectClicked()
                R.id.copy_link -> onCopyLinkClicked()
                R.id.move_to_bin -> onMoveToBinClicked()
                else -> throw IllegalStateException("Unexpected menu item: $item")
            }
            true
        }
    }
}
