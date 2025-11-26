package com.anytypeio.anytype.core_ui.menu

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.PopupMenu
import com.anytypeio.anytype.core_ui.R

class ObjectHeaderContextMenu(
    context: Context,
    view: View,
    onOpenAsObjectClicked: () -> Unit
) : PopupMenu(context, view, Gravity.BOTTOM, 0, R.style.DefaultPopupMenuStyle) {
    init {
        menuInflater.inflate(R.menu.menu_object_header_context, menu)
        setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.open_as_object -> onOpenAsObjectClicked()
                else -> throw IllegalStateException("Unexpected menu item: $item")
            }
            true
        }
    }
}
