package com.anytypeio.anytype.ui.profile
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.style.LineBackgroundSpan
import kotlin.math.abs

class RoundedBackgroundColorSpan(
    backgroundColor: Int,
    private val padding: Float,
    private val radius: Float,
    private val lineHeight: Float
) : LineBackgroundSpan {

    companion object {
        private const val NO_INIT = -1f
    }

    private val rect = RectF()
    private val paint = Paint().apply {
        color = backgroundColor
        isAntiAlias = true
    }

    private var prevWidth = NO_INIT
    private var prevRight = NO_INIT

    override fun drawBackground(
        c: Canvas,
        p: Paint,
        left: Int,
        right: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence,
        start: Int,
        end: Int,
        lineNumber: Int
    ) {

        val actualWidth = p.measureText(text, start, end) + 2f * padding
        val widthDiff = abs(prevWidth - actualWidth)
        val diffIsShort = widthDiff < 2f * radius

        val width = if (lineNumber == 0) {
            actualWidth
        } else if ((actualWidth < prevWidth) && diffIsShort) {
            prevWidth
        } else if ((actualWidth > prevWidth) && diffIsShort) {
            actualWidth + (2f * radius - widthDiff)
        } else {
            actualWidth
        }

        val shiftLeft = 0f - padding
        val shiftRight = width + shiftLeft

        val shiftGap = abs((bottom.toFloat() - top.toFloat() - lineHeight) /2f)

        rect.set(shiftLeft, top.toFloat() + shiftGap, shiftRight, bottom.toFloat() - shiftGap)

        c.drawRoundRect(rect, radius, radius, paint)

        prevWidth = width
        prevRight = rect.right
    }
}