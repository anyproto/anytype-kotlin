package com.agileburo.anytype.core_ui.tools

import android.text.Editable
import android.text.TextWatcher
import timber.log.Timber

class DefaultTextWatcher(val onTextChanged: (Editable) -> Unit) : TextWatcher {

    private var locked: Boolean = false

    override fun afterTextChanged(s: Editable) {
        Timber.d("OnTextChanged: $this")
        if (!locked)
            onTextChanged(s)
        else
            Timber.d("Locked text watcher. Skipping text update...")
    }
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    fun lock() {
        locked = true
    }

    fun unlock() {
        locked = false
    }
}