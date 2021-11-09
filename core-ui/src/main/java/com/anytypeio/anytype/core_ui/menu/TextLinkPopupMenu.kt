package com.anytypeio.anytype.core_ui.menu

import android.content.Context
import android.view.View
import android.widget.PopupMenu
import com.anytypeio.anytype.core_ui.R

class TextLinkPopupMenu(
    context: Context,
    view: View,
    onCopyLinkClicked: () -> Unit,
    onEditLinkClicked: () -> Unit,
    onUnlinkClicked: () -> Unit
) : PopupMenu(context, view) {

    init {
        menuInflater.inflate(R.menu.menu_text_link, menu)
        setOnMenuItemClickListener { item ->
            when(item.itemId) {
                R.id.copy_link -> onCopyLinkClicked()
                R.id.edit_link -> onEditLinkClicked()
                R.id.unlink -> onUnlinkClicked()
                else -> throw IllegalStateException("Unexpected menu item: $item")
            }
            true
        }
    }
}