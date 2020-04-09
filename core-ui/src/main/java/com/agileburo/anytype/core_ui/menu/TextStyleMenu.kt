package com.agileburo.anytype.core_ui.menu

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.common.Markup

class TextStyleMenu(
    private val onMenuItemClicked: (Markup.Type) -> Unit
) : ActionMode.Callback2() {

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        mode.menuInflater.inflate(R.menu.menu_text_style, menu)
        return true
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.itemBold -> {
                onMenuItemClicked(Markup.Type.BOLD)
                mode?.finish()
                true
            }
            R.id.itemItalic -> {
                onMenuItemClicked(Markup.Type.ITALIC)
                mode?.finish()
                true
            }
            R.id.itemStrike -> {
                onMenuItemClicked(Markup.Type.STRIKETHROUGH)
                mode?.finish()
                true
            }
            R.id.itemCode -> {
                onMenuItemClicked(Markup.Type.KEYBOARD)
                mode?.finish()
                true
            }
//Todo Turn on after new color and background toolbars
//            R.id.itemColor -> {
//                onMenuItemClicked(Markup.Type.TEXT_COLOR)
//                mode?.finish()
//                true
//            }
            R.id.itemLink -> {
                onMenuItemClicked(Markup.Type.LINK)
                mode?.finish()
                true
            }
//            R.id.itemBackground -> {
//                onMenuItemClicked(Markup.Type.BACKGROUND_COLOR)
//                mode?.finish()
//                true
//            }
            else -> false
        }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode?) {}
}