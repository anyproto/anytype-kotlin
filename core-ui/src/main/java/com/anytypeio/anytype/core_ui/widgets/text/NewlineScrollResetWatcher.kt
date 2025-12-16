package com.anytypeio.anytype.core_ui.widgets.text

import android.text.Editable
import android.text.TextWatcher

/**
 * TextWatcher that detects newline insertion and triggers a scroll reset callback.
 * Used to reset horizontal scroll position when a new line is created in code blocks.
 */
class NewlineScrollResetWatcher(
    private val onNewlineInserted: () -> Unit
) : TextWatcher {

    private var newlineInserted = false

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // Only check for newlines when text is inserted (count > 0), not when deleted
        newlineInserted = if (count > 0 && s != null && start + count <= s.length) {
            val inserted = s.subSequence(start, start + count)
            inserted.contains('\n')
        } else {
            false
        }
    }

    override fun afterTextChanged(s: Editable?) {
        if (newlineInserted) {
            newlineInserted = false
            onNewlineInserted()
        }
    }
}
