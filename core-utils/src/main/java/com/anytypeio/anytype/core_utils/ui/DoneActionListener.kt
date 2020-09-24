package com.anytypeio.anytype.core_utils.ui

import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.TextView

class DoneActionListener(val onActionDone: () -> Unit) : TextView.OnEditorActionListener {

    override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean {
        return if (actionId == EditorInfo.IME_ACTION_DONE) {
            onActionDone()
            true
        } else {
            false
        }
    }
}