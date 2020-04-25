package com.agileburo.anytype.core_ui.extensions

import com.agileburo.anytype.core_ui.widgets.text.TextInputWidget

fun TextInputWidget.preserveSelection(block: () -> Unit) = synchronized(this) {
    val selection = selectionStart..selectionEnd
    block()
    setSelection(selection.first, selection.last)
}