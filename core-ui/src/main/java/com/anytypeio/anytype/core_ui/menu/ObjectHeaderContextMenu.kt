package com.anytypeio.anytype.core_ui.menu

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.PopupMenu
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.SupportedLayouts
import com.anytypeio.anytype.core_ui.R

class ObjectHeaderContextMenu(
    context: Context,
    view: View,
    showMoveToBin: Boolean,
    showRemoveFromCollection: Boolean,
    layout: ObjectType.Layout? = null,
    onOpenAsObjectClicked: () -> Unit,
    onCopyLinkClicked: () -> Unit,
    onMoveToBinClicked: () -> Unit,
    onRemoveFromCollectionClicked: () -> Unit,
    onOpenInBrowserClicked: () -> Unit = {},
    onOpenFileClicked: () -> Unit = {}
) : PopupMenu(context, view, Gravity.BOTTOM, 0, R.style.DefaultPopupMenuStyle) {
    init {
        menuInflater.inflate(R.menu.menu_object_header_context, menu)
        menu.findItem(R.id.move_to_bin)?.isVisible = showMoveToBin
        menu.findItem(R.id.remove_from_collection)?.isVisible = showRemoveFromCollection
        menu.findItem(R.id.open_in_browser)?.isVisible = layout == ObjectType.Layout.BOOKMARK
        menu.findItem(R.id.open_file)?.isVisible = SupportedLayouts.isFileLayout(layout)
        setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.open_in_browser -> onOpenInBrowserClicked()
                R.id.open_file -> onOpenFileClicked()
                R.id.open_as_object -> onOpenAsObjectClicked()
                R.id.copy_link -> onCopyLinkClicked()
                R.id.move_to_bin -> onMoveToBinClicked()
                R.id.remove_from_collection -> onRemoveFromCollectionClicked()
                else -> throw IllegalStateException("Unexpected menu item: $item")
            }
            true
        }
    }
}
