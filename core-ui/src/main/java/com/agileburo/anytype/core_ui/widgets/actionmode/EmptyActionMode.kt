package com.agileburo.anytype.core_ui.widgets.actionmode

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem

class EmptyActionMode(
    private val onCreate: () -> Unit,
    private val onDestroy: () -> Unit
) : ActionMode.Callback2() {

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        return false
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        onCreate()
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        menu?.clear()
        return true
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        onDestroy()
    }
}