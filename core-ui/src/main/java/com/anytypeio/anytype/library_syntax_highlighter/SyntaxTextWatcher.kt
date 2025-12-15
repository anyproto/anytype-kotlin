package com.anytypeio.anytype.library_syntax_highlighter

import android.text.Editable
import android.text.TextWatcher

class SyntaxTextWatcher(private val onAfterTextChanged: () -> Unit) : TextWatcher {
    private var locked = false

    override fun afterTextChanged(s: Editable?) {
        if (!locked) onAfterTextChanged()
    }

    override fun lock() { locked = true }
    override fun unlock() { locked = false }
}