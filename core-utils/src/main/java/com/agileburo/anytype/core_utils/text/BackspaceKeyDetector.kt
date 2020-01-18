package com.agileburo.anytype.core_utils.text

import android.view.KeyEvent
import android.view.KeyEvent.ACTION_DOWN
import android.view.KeyEvent.KEYCODE_DEL
import android.view.View

class BackspaceKeyDetector(
    private val onBackspaceClicked: () -> Unit
) : View.OnKeyListener {
    override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
        if (event.action == ACTION_DOWN && keyCode == KEYCODE_DEL)
            onBackspaceClicked()
        return false
    }
}