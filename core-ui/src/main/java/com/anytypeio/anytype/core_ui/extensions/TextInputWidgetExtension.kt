package com.anytypeio.anytype.core_ui.extensions

import com.anytypeio.anytype.core_ui.common.isLinksOrMentionsPresent
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.presentation.editor.editor.Markup

fun TextInputWidget.applyMovementMethod(item: Markup) {
    if (item.marks.isNotEmpty() && item.marks.isLinksOrMentionsPresent()) {
        setLinksClickable()
    } else {
        setDefaultMovementMethod()
    }
}

fun TextInputWidget.disable() {
    isEnabled = false
    pauseTextWatchers {
        enableReadMode()
    }
}