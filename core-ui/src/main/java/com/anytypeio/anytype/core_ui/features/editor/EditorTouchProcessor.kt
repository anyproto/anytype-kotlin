package com.anytypeio.anytype.core_ui.features.editor

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import timber.log.Timber
import kotlin.math.abs

/**
 * @property [fallback] fallback method for processing touch event.
 * @property [onLongClick] long click event (replacement for [View.OnLongClickListener])
 * @property [onDragAndDropTrigger] drag-and-drop triggering event
 */
class EditorTouchProcessor(
    val fallback: (event: MotionEvent?) -> Boolean,
    var onLongClick: () -> Unit = {},
    var onDragAndDropTrigger: (event: MotionEvent?) -> Unit = { }
) {

    val moves = mutableListOf<Float>()

    private val actionHandler = Handler(Looper.getMainLooper())

    private val dragAndDropTimeoutRunnable = Runnable {
        Timber.d("Runnable triggered")
        if (moves.size > 1) {
            val first = moves.first()
            val last = moves.last()
            val delta = abs(first - last)
            if (delta == 0f) {
                Timber.d("Runnable dispatched 1")
                onDragAndDropTrigger(lastEvent)
            } else {
                Timber.d("Runnable ignored 1")
            }
        } else {
            Timber.d("Runnable dispatched 2")
            onDragAndDropTrigger(lastEvent)
        }
        moves.clear()
    }

    private var actionUpStartInMillis: Long = 0

    private var lastEvent: MotionEvent? = null

    fun process(v: View, event: MotionEvent?): Boolean {
        if (event != null) {
            lastEvent = event
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    Timber.d("ACTION DOWN")
                    actionUpStartInMillis = SystemClock.elapsedRealtime()
                    actionHandler.postDelayed(
                        dragAndDropTimeoutRunnable,
                        DND_TIMEOUT
                    )
                    moves.clear()
                }
                MotionEvent.ACTION_MOVE -> {
                    Timber.d("ACTION MOVE: $event")
                    moves.add(event.getY(0))
                    if (moves.size > 1) {
                        val first = moves.first()
                        val last = moves.last()
                        Timber.d("ACTION MOVE DELTA: ${abs(last - first)}")
                    }
                }
                MotionEvent.ACTION_CANCEL -> {
                    if (moves.isEmpty() && event.elapsed() > DND_TIMEOUT) {
                        v.emulateHapticFeedback()
                    }
                    Timber.d("ACTION CANCEL")
                    actionHandler.removeCallbacksAndMessages(null)
                    moves.clear()
                }
                MotionEvent.ACTION_UP -> {
                    moves.clear()
                    Timber.d("ACTION UP")
                    actionHandler.removeCallbacksAndMessages(null)
                    return when (actionUpStartInMillis.untilNow()) {
                        in LONG_PRESS_TIMEOUT..DND_TIMEOUT -> {
                            onLongClick()
                            v.performLongClickWithHaptic()
                            true
                        }
                        else -> {
                            return fallback(event)
                        }
                    }
                }
                else -> {
                    Timber.d("Ignored motion event: $event")
                }
            }
        }
        return fallback(event)
    }

    companion object {
        val LONG_PRESS_TIMEOUT: Long = ViewConfiguration.getLongPressTimeout().toLong()
        val DND_TIMEOUT: Long = 2 * LONG_PRESS_TIMEOUT
    }

    private fun View.emulateHapticFeedback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (this !is TextInputWidget) {
                performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
        }
    }
}

private fun View.performLongClickWithHaptic() {
    if (this !is TextInputWidget && !performLongClick()) {
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }
}

private fun Long.untilNow() = SystemClock.elapsedRealtime() - this

private fun MotionEvent.elapsed() = eventTime - downTime