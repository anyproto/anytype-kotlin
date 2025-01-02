package com.anytypeio.anytype.core_ui.common

import android.text.Layout
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.method.MovementMethod
import android.text.method.Touch
import android.text.style.ClickableSpan
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.TextView
import me.saket.bettermovementmethod.BetterLinkMovementMethod

class CustomMovementMethod : BetterLinkMovementMethod() {

    override fun onTouchEvent(widget: TextView, buffer: Spannable, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val x = (event.x - widget.totalPaddingLeft + widget.scrollX).toInt()
            val y = (event.y - widget.totalPaddingTop + widget.scrollY).toInt()

            val layout: Layout = widget.layout
            val line = layout.getLineForVertical(y)
            val offset = layout.getOffsetForHorizontal(line, x.toFloat())

            val link = buffer.getSpans(offset, offset, ClickableSpan::class.java)
            if (link.isNotEmpty()) {
                link[0].onClick(widget)
                return true
            }
        }
        // Pass through for default behavior
        return Touch.onTouchEvent(widget, buffer, event)
    }

    override fun initialize(widget: TextView, text: Spannable) {}

    override fun onTakeFocus(view: TextView, text: Spannable, dir: Int) {}

    override fun canSelectArbitrarily(): Boolean = false

    override fun onKeyDown(
        widget: TextView?,
        text: Spannable?,
        keyCode: Int,
        event: KeyEvent?
    ): Boolean {
        return false
    }

    override fun onKeyUp(
        widget: TextView?,
        text: Spannable?,
        keyCode: Int,
        event: KeyEvent?
    ): Boolean {
        return false
    }

    override fun onKeyOther(
        view: TextView?,
        text: Spannable?,
        event: KeyEvent?
    ): Boolean {
        return false
    }

    override fun onTrackballEvent(
        widget: TextView?,
        text: Spannable?,
        event: MotionEvent?
    ): Boolean {
        return false
    }

    override fun onGenericMotionEvent(
        widget: TextView?,
        text: Spannable?,
        event: MotionEvent?
    ): Boolean {
        return false
    }
}