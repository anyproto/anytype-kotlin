package com.anytypeio.anytype.core_ui.common

import android.graphics.Color
import android.text.style.ForegroundColorSpan

class CheckedCheckboxColorSpan(color: Int = COLOR): ForegroundColorSpan(color) {
    companion object {
        val COLOR = Color.parseColor("#ACA996")
    }
}