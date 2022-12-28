package com.anytypeio.anytype.core_ui.tools

import android.text.TextWatcher

interface TextInputTextWatcher: TextWatcher {
    fun lock()
    fun unlock()
}