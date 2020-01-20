package com.agileburo.anytype.core_ui.tools

import android.text.Editable
import android.text.TextWatcher
import timber.log.Timber

class DefaultTextWatcher(val onTextChanged: (Editable) -> Unit) : TextWatcher {
    override fun afterTextChanged(s: Editable) {
        Timber.d("OnTextChanged: $this")
        onTextChanged(s)
    }
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
}