package com.anytypeio.anytype.core_ui.features.editor

import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration

/**
 * @property [fallback] fallback method for processing touch event.
 * @property [onLongClick] long click event (replacement for [View.OnLongClickListener])
 * @property [onDragAndDropTrigger] drag-and-drop triggering event
 */
class EditorTouchProcessor(
    val fallback: (event: MotionEvent?) -> Boolean,
    var onLongClick: () -> Unit = {},
    var onDragAndDropTrigger: () -> Unit = {}
) {

    private val actionHandler = Handler()
    private val dragAndDropTimeoutRunnable = Runnable { onDragAndDropTrigger() }

    private var actionUpStartInMillis: Long = 0

    fun process(v: View, event: MotionEvent?): Boolean {
        if (event != null) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    actionUpStartInMillis = System.currentTimeMillis()
                    actionHandler.postDelayed(
                        dragAndDropTimeoutRunnable,
                        DND_TIMEOUT
                    )
                }
                MotionEvent.ACTION_UP -> {
                    actionHandler.removeCallbacksAndMessages(null)
                    return when (System.currentTimeMillis() - actionUpStartInMillis) {
                        in LONG_PRESS_TIMEOUT..DND_TIMEOUT -> {
                            onLongClick()
                            v.performLongClick()
                            true
                        }
                        else -> {
                            return fallback(event)
                        }
                    }
                }
            }
        }
        return fallback(event)
    }

    companion object {
        const val DND_TIMEOUT : Long = 800
        val LONG_PRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout()
    }
}