package com.anytypeio.anytype.core_ui.extensions

import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget

fun TextInputWidget.preserveSelection(block: () -> Unit) = synchronized(this) {
    val selection = selectionStart..selectionEnd
    block()
    setSelection(selection.first, selection.last)
}