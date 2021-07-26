package com.anytypeio.anytype.core_ui.menu

import android.content.Context
import android.view.View
import android.widget.PopupMenu
import com.anytypeio.anytype.core_ui.R

class DataViewEditViewPopupMenu(
    context: Context,
    view: View,
    isDeletionAllowed: Boolean
) : PopupMenu(context, view) {
    init {
        if (isDeletionAllowed)
            menuInflater.inflate(R.menu.menu_edit_data_view_viewer, menu)
        else
            menuInflater.inflate(R.menu.menu_edit_data_view_viewer_deletion_disabled, menu)
    }
}