package com.agileburo.anytype.core_utils.text

import android.view.KeyEvent
import android.view.KeyEvent.ACTION_DOWN
import android.view.KeyEvent.KEYCODE_DEL
import android.view.View
import android.widget.TextView

class BackspaceKeyDetector(
    private val onStartLineBackspaceClicked: () -> Unit
) : View.OnKeyListener {
    override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
        if (event.action == ACTION_DOWN && keyCode == KEYCODE_DEL) {
            (v as? TextView)?.let {
                if (it.selectionStart == 0 && it.selectionEnd == 0) {
                    onStartLineBackspaceClicked()
                }
            }
        }
        return false
    }
}