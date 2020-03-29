package com.agileburo.anytype.core_ui.menu

import android.content.Context
import android.view.View
import android.widget.PopupMenu
import com.agileburo.anytype.core_ui.R

class DocumentPopUpMenu(
    context: Context,
    view: View,
    onArchiveClicked: () -> Unit
) : PopupMenu(context, view) {

    init {
        menuInflater.inflate(R.menu.menu_page, menu)
        setOnMenuItemClickListener {
            onArchiveClicked()
            true
        }
    }
}