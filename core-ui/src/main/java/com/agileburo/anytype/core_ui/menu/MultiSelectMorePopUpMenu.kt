package com.agileburo.anytype.core_ui.menu

import android.content.Context
import android.view.View
import android.widget.PopupMenu
import com.agileburo.anytype.core_ui.R

class MultiSelectMorePopUpMenu(
    context: Context,
    view: View,
    onTurnInto: () -> Unit,
    onMoveTo: () -> Unit
) : PopupMenu(context, view) {

    init {
        menuInflater.inflate(R.menu.menu_multi_select, menu)
        setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.turn_into -> onTurnInto()
                R.id.move_to -> onMoveTo()
                else -> throw IllegalStateException("Unexpected menu item: $item")
            }
            true
        }
    }
}