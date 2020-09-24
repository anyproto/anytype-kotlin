package com.anytypeio.anytype.core_ui.menu

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.Markup

class TextBlockContextMenu(
    private val menuType: ContextMenuType,
    private val onTextColorClicked: (ActionMode) -> Boolean,
    private val onBackgroundColorClicked: (ActionMode) -> Boolean,
    private val onMenuItemClicked: (Markup.Type) -> Unit
) : ActionMode.Callback2() {

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        mode.menuInflater.inflate(getMenu(menuType), menu)
        return true
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.itemBold -> {
                onMenuItemClicked(Markup.Type.BOLD)
                mode.finish()
                true
            }
            R.id.itemItalic -> {
                onMenuItemClicked(Markup.Type.ITALIC)
                mode.finish()
                true
            }
            R.id.itemStrike -> {
                onMenuItemClicked(Markup.Type.STRIKETHROUGH)
                mode.finish()
                true
            }
            R.id.itemCode -> {
                onMenuItemClicked(Markup.Type.KEYBOARD)
                mode.finish()
                true
            }
            R.id.itemColor -> {
                onTextColorClicked(mode)
            }
            R.id.itemLink -> {
                onMenuItemClicked(Markup.Type.LINK)
                mode.finish()
                true
            }
            R.id.itemBackground -> {
                onBackgroundColorClicked(mode)
            }
            else -> false
        }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode?) {}

    private fun getMenu(type: ContextMenuType) = when (type) {
        ContextMenuType.TEXT -> R.menu.menu_text_style
        ContextMenuType.HEADER -> R.menu.menu_header_style
        ContextMenuType.HIGHLIGHT -> R.menu.menu_highlight_style
    }
}

enum class ContextMenuType { TEXT, HEADER, HIGHLIGHT }
