package com.anytypeio.anytype.core_ui.common

import android.os.Build
import android.text.TextPaint
import android.text.style.UnderlineSpan

class Underline(private val underlineHeight: Float) : UnderlineSpan(), Span {

    override fun updateDrawState(ds: TextPaint) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ds.underlineColor = ds.color
            ds.underlineThickness = underlineHeight
        } else {
            super.updateDrawState(ds)
        }
    }
}