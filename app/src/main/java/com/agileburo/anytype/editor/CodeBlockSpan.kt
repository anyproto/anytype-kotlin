package com.agileburo.anytype.editor

import android.graphics.Color
import android.graphics.Typeface
import android.support.annotation.ColorInt
import android.text.TextPaint

class CodeBlockSpan(
    font: Typeface?,
    @param:ColorInt private val backgroundColor: Int = Color.LTGRAY
) : FontSpan(font) {

    // Since we're only changing the background color, it will not affect the measure state, so
    // just override the update draw state.
    override fun updateDrawState(textPaint: TextPaint) {
        super.updateDrawState(textPaint)
        textPaint.bgColor = backgroundColor
    }
}