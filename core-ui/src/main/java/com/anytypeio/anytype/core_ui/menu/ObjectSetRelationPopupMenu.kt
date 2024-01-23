package com.anytypeio.anytype.core_ui.menu

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.PopupMenu
import com.anytypeio.anytype.core_ui.R

class ObjectSetRelationPopupMenu(
    context: Context,
    view: View,
    onConvertToCollection: () -> Unit
) : PopupMenu(context, view, Gravity.BOTTOM, 0, R.style.DefaultPopupMenuStyle) {
    init {
        menuInflater.inflate(R.menu.menu_set_relation, menu)
        setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.turn_into -> onConvertToCollection()
                else -> throw IllegalStateException("Unexpected menu item: $item")
            }
            true
        }
    }
}