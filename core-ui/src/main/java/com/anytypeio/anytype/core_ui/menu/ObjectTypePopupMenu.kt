package com.anytypeio.anytype.core_ui.menu

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.PopupMenu
import com.anytypeio.anytype.core_ui.R

class ObjectTypePopupMenu(
    context: Context,
    view: View,
    onChangeTypeClicked: () -> Unit,
    onOpenSetClicked: () -> Unit,
    allowChangingObjectType: Boolean = false,
    allowOnlyChangingType: Boolean = false
) : PopupMenu(context, view, Gravity.BOTTOM, 0, R.style.DefaultPopupMenuStyle) {
    init {
        if (allowOnlyChangingType) {
            menuInflater.inflate(R.menu.menu_object_type_only, menu)
        } else {
            if (allowChangingObjectType) {
                menuInflater.inflate(R.menu.menu_object_type, menu)
            } else {
                menuInflater.inflate(R.menu.menu_object_type_change_disabled, menu)
            }
        }
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