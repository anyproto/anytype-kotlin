package com.anytypeio.anytype.core_ui.common

import android.graphics.Color
import android.text.style.BackgroundColorSpan

class GhostEditorSelectionSpan(color: Int = COLOR) : BackgroundColorSpan(color) {
    companion object {
        val COLOR = Color.parseColor("#99FFB522")
    }
}