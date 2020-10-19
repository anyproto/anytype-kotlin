package com.anytypeio.anytype.core_ui.common

import android.graphics.Color
import android.text.style.BackgroundColorSpan

class SearchTargetHighlightSpan(color: Int = COLOR) : BackgroundColorSpan(color) {
    companion object {
        const val COLOR = Color.YELLOW
    }
}