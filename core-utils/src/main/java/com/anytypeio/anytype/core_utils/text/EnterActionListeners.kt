package com.anytypeio.anytype.core_utils.text

import android.view.KeyEvent
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.view.inputmethod.EditorInfo.IME_ACTION_GO
import android.view.inputmethod.EditorInfo.IME_ACTION_UNSPECIFIED
import android.widget.TextView

abstract class EnterActionListener(
    val condition: (actionId: Int, keyEvent: KeyEvent?) -> Boolean,
    open val onEnter: (tv: TextView) -> Unit
) :
    TextView.OnEditorActionListener {
    override fun onEditorAction(tv: TextView, actionId: Int, keyEvent: KeyEvent?): Boolean {
        return if (condition(actionId, keyEvent)) {
            onEnter(tv)
            true
        } else {
            false
        }
    }
}

class OnNewLineActionListener(override val onEnter: (tv: TextView) -> Unit) :
    EnterActionListener(
        onEnter = onEnter,
        condition = { actionId, keyEvent ->
            actionId == IME_ACTION_GO ||
                    actionId == IME_ACTION_UNSPECIFIED && keyEvent.isEnterPressed()
        }
    )

class OnEnterActionListener(override val onEnter: (tv: TextView) -> Unit) :
    EnterActionListener(
        onEnter = onEnter,
        condition = { actionId, keyEvent ->
            actionId == IME_ACTION_GO
                    || actionId == IME_ACTION_DONE
                    || actionId == IME_ACTION_UNSPECIFIED && keyEvent.isEnterPressed()
        }
    )