package com.anytypeio.anytype.core_utils.ext

import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import android.widget.TextView
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object PopupExtensions {

    private fun calculateSelectedTextBounds(anchorView: TextView): Rect {
        val result = Rect()
        val selStart = anchorView.selectionStart
        val selEnd = anchorView.selectionEnd
        val min = max(0, min(selStart, selEnd))
        val max = max(0, max(selStart, selEnd))

        // Calculate the selection bounds
        val selBounds = RectF()
        val selection = Path()
        anchorView.layout.getSelectionPath(min, max, selection)
        selection.computeBounds(selBounds, true)
        selBounds.roundOut(result)
        return result
    }

    fun calculateContentBounds(
        anchorView: TextView,
        bottomAllowance: Int
    ): Rect {
        val contentRect = Rect()
        val selRect = calculateSelectedTextBounds(anchorView)
        val anchorRect = calculateRectInWindow(anchorView)

        contentRect.top = anchorRect.top + selRect.top + anchorView.paddingTop
        contentRect.bottom =
            anchorRect.top + selRect.bottom + anchorView.paddingTop + bottomAllowance

        return contentRect
    }

    /**
     * Calculate the location of the view in the window, or null if not in window.
     */
    fun calculateRectInWindow(view: View?): Rect {
        view?.let {
            val location = IntArray(2)
            it.getLocationInWindow(location)
            return Rect(
                location[0],
                location[1],
                location[0] + it.measuredWidth,
                location[1] + it.measuredHeight
            )
        }
        return Rect()
    }

    fun lerp(a: Int, b: Int, v: Float): Int = (a + (b - a) * v).roundToInt()

}