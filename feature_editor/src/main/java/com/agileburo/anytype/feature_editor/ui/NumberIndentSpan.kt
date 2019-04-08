package com.agileburo.anytype.feature_editor.ui

import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.style.LeadingMarginSpan

/**
 * Created by Konstantin Ivanov
 * email :  ki@agileburo.com
 * on 30.03.2019.
 */
class NumberIndentSpan(private val leadWidth: Int, private val gapWidth: Int, private val index: Int) :
    LeadingMarginSpan {

    override fun getLeadingMargin(first: Boolean): Int {
        return leadWidth + gapWidth
    }

    override fun drawLeadingMargin(
        c: Canvas,
        p: Paint,
        x: Int,
        dir: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence,
        start: Int,
        end: Int,
        first: Boolean,
        l: Layout
    ) {
        if (first) {
            val orgStyle = p.style
            p.style = Paint.Style.FILL
            val width = p.measureText("4.")
            c.drawText("$index.", (leadWidth + x - width / 2) * dir, bottom - p.descent(), p)
            p.style = orgStyle
        }
    }
}
