package com.anytypeio.anytype.core_ui.menu

import android.content.Context
import android.view.View
import android.widget.PopupMenu
import com.anytypeio.anytype.core_ui.R

class ObjectTypePopupMenu(
    context: Context,
    view: View,
    onChangeTypeClicked: () -> Unit,
    onOpenSetClicked: () -> Unit
) : PopupMenu(context, view) {
    init {
        menuInflater.inflate(R.menu.menu_object_type, menu)
        setOnMenuItemClickListener { item ->
            when(item.itemId) {
                R.id.change_type -> onChangeTypeClicked()
                R.id.open_set -> onOpenSetClicked()
                else -> throw IllegalStateException("Unexpected menu item: $item")
            }
            true
        }
    }
}