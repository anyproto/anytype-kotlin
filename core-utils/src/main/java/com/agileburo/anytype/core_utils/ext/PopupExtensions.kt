package com.agileburo.anytype.core_utils.ext

import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import android.widget.TextView
import kotlin.math.max
import kotlin.math.min

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

    fun calculateFloatToolbarPosition(
        anchorView: TextView,
        popupWindowHeight: Float,
        tooltipOffsetY: Int = 0
    ): PointF {

        val location = PointF()
        val selRect = calculateSelectedTextBounds(anchorView)
        val anchorRect = calculateRectInWindow(anchorView)

        location.x = 0f
        location.y = anchorRect.top + selRect.top - popupWindowHeight - tooltipOffsetY
        return location
    }

    /**
     * Calculate the location of the view in the window, or null if not in window.
     */
    private fun calculateRectInWindow(view: View?): RectF {
        view?.apply {
            val location = IntArray(2)
            view.getLocationInWindow(location)
            return RectF(
                location[0].toFloat(),
                location[1].toFloat(),
                (location[0] + view.measuredWidth).toFloat(),
                (location[1] + view.measuredHeight).toFloat()
            )
        }
        return RectF()
    }

    fun lerp(a: Float, b: Float, v: Float): Float = a + (b - a) * v

}