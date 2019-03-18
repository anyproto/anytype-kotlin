package com.agileburo.anytype.feature_editor.ui

import android.graphics.Color
import android.graphics.Typeface
import androidx.annotation.ColorInt
import android.text.TextPaint
import com.agileburo.anytype.feature_editor.ui.FontSpan

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