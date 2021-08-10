package com.anytypeio.anytype.core_ui.extensions

import android.graphics.Point
import com.anytypeio.anytype.core_ui.common.isLinksOrMentionsPresent
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.presentation.editor.editor.Markup

fun TextInputWidget.preserveSelection(block: () -> Unit) = synchronized(this) {
    val selection = selectionStart..selectionEnd
    block()
    setSelection(selection.first, selection.last)
}

fun TextInputWidget.applyMovementMethod(item: Markup) {
    if (item.marks.isNotEmpty() && item.marks.isLinksOrMentionsPresent()) {
        setLinksClickable()
    } else {
        setDefaultMovementMethod()
    }
}

fun TextInputWidget.getSelectionCoords(): Point {
    val pos = selectionStart
    val line = layout.getLineForOffset(pos)
    val baseline = layout.getLineBaseline(line)
    val ascent = layout.getLineAscent(line)
    val x = layout.getPrimaryHorizontal(pos).toInt()
    val y = baseline + ascent
    return Point(x, y)
}