package com.agileburo.anytype.core_ui.tools

import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

class OutsideClickDetector(private val onClick: () -> Unit) : RecyclerView.OnItemTouchListener {

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        if (e.action == MotionEvent.ACTION_UP)
            return false
        val child = rv.findChildViewUnder(e.x, e.y)
        return if (child != null) {
            false
        } else {
            onClick()
            true
        }
    }
}