package com.anytypeio.anytype.core_ui.tools

import android.view.View
import timber.log.Timber

class LockableFocusChangeListener(
    private val onFocusChanged: (Boolean) -> Unit
): View.OnFocusChangeListener {

    private var locked: Boolean = false

    fun lock() {
        locked = true
    }

    fun unlock() {
        locked = false
    }

    override fun onFocusChange(v: View, hasFocus: Boolean) {
        if (!locked) {
            onFocusChanged(hasFocus)
        }
        else {
            Timber.d("Locked focus-change listener is locked")
        }
    }
}