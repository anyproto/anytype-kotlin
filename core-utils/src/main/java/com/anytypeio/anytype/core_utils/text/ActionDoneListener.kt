package com.anytypeio.anytype.core_utils.text

import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.TextView

class ActionDoneListener(
    private val onActionDone: (String) -> Unit
): TextView.OnEditorActionListener {

    override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) onActionDone(v.text.toString())
        v.clearFocus()
        return true
    }
}