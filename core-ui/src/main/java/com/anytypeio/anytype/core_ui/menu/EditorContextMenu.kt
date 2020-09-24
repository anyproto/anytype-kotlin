package com.anytypeio.anytype.core_ui.menu

import android.os.Build
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import com.anytypeio.anytype.core_ui.R

class EditorContextMenu(
    private val onStyleClick: () -> Unit
) : ActionMode.Callback2(){

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            menu.apply {
                add(0, android.R.id.textAssist, 0, TITLE_STYLE)
            }
        } else {
            menu.apply {
                add(0, R.id.menuStyle, 3, TITLE_STYLE)
            }
        }
        return true
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean =
        when (item.itemId) {
            android.R.id.textAssist -> {
                when (item.title) {
                    TITLE_STYLE -> {
                        onStyleClick()
                        //mode.finish()
                        true
                    }
                    else -> false
                }
            }
            R.id.menuStyle -> {
                onStyleClick()
                //mode.finish()
                true
            }
            else -> false
        }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode?) {}

    companion object {
        const val TITLE_STYLE = "Style"
    }
}