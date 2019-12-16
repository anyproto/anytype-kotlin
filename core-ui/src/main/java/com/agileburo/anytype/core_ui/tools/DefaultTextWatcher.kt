package com.agileburo.anytype.core_ui.tools

import android.text.Editable
import android.text.TextWatcher

class DefaultTextWatcher(val onTextChanged: (Editable) -> Unit) : TextWatcher {
    override fun afterTextChanged(s: Editable) {
        onTextChanged(s)
    }
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
}