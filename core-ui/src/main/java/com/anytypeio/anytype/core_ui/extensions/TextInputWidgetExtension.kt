package com.anytypeio.anytype.core_ui.extensions

import com.anytypeio.anytype.core_ui.common.isLinksOrMentionsPresent
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.presentation.page.editor.Markup

fun TextInputWidget.preserveSelection(block: () -> Unit) = synchronized(this) {
    val selection = selectionStart..selectionEnd
    block()
    setSelection(selection.first, selection.last)
}

fun TextInputWidget.applyMovementMethod(item: Markup) {
    if (item.marks.isLinksOrMentionsPresent()) {
        setLinksClickable()
    } else {
        setDefaultMovementMethod()
    }
}