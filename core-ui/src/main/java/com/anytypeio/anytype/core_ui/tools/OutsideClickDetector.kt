package com.anytypeio.anytype.core_ui.tools

import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.sqrt

class OutsideClickDetector(
    private val onClick: () -> Unit
) : RecyclerView.OnItemTouchListener {

    private var startClickTime = 0L
    private var startClickX = 0f
    private var startClickY = 0f

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        if (e.action == MotionEvent.ACTION_DOWN) {
            startClickTime = System.currentTimeMillis()
            startClickX = e.x
            startClickY = e.y
        } else if (e.action == MotionEvent.ACTION_UP) {
            val duration = System.currentTimeMillis() - startClickTime
            val distance = distance(x1 = e.x, y1 = e.y, x2 = startClickX, y2 = startClickY)
            if (duration < MAX_CLICK_DURATION && distance < MAX_CLICK_DISTANCE) {
                val child = rv.findChildViewUnder(e.x, e.y)
                if (child == null) onClick()
            }
        }
        return false
    }

    /**
     * Calculates distance between two points in pixels
     */
    private fun distance(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float
    ): Float {
        val dx = x1 - x2
        val dy = y1 - y2
        return sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }

    companion object {
        const val MAX_CLICK_DURATION = 1000L
        const val MAX_CLICK_DISTANCE = 1.0F
    }
}