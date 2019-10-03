package com.agileburo.anytype.feature_login.ui.login.presentation.ui.utils

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