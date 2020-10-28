package com.anytypeio.anytype.library_syntax_highlighter

import android.text.Editable
import android.text.TextWatcher

class SyntaxTextWatcher(private val onAfterTextChanged: () -> Unit) : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    override fun afterTextChanged(s: Editable?) {
        onAfterTextChanged()
    }
}