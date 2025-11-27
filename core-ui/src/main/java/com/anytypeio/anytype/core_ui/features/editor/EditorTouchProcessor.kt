package com.anytypeio.anytype.core_ui.features.editor

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.text.Layout
import android.text.style.ClickableSpan
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import com.anytypeio.anytype.core_ui.extensions.disable
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import timber.log.Timber
import kotlin.math.abs

/**
 * Handles touch gestures for editor interactions.
 *
 * @property touchSlop threshold for detecting drag motion.
 * @property fallback fallback method for processing touch event.
 * @property onLongClick long click event (replacement for [View.OnLongClickListener])
 * @property onDragAndDropTrigger drag-and-drop triggering event
 */
class EditorTouchProcessor(
    private val touchSlop: Int,
    val fallback: (event: MotionEvent?) -> Boolean,
    var onLongClick: () -> Unit = {},
    var onDragAndDropTrigger: (event: MotionEvent?) -> Unit = { }
) {

    private val moves = mutableListOf<Float>()
    private val actionHandler = Handler(Looper.getMainLooper())
    private var actionUpStartInMillis: Long = 0
    private var lastEvent: MotionEvent? = null
    private var isDragging: Boolean = false

    private val dragAndDropTimeoutRunnable = Runnable {
        val delta = if (moves.size > 1) abs(moves.last() - moves.first()) else 0f

        if (!isDragging && delta <= touchSlop) {
            Timber.d("Triggering drag due to long press without movement")
            onDragAndDropTrigger(lastEvent)
        } else if (isDragging) {
            Timber.d("Triggering drag due to long press with movement")
            onDragAndDropTrigger(lastEvent)
        } else {
            Timber.d("Skipping drag trigger")
        }

        moves.clear()
    }

    fun process(v: View, event: MotionEvent?): Boolean {
        event ?: return fallback(null)
        lastEvent = event

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                Timber.d("ACTION DOWN")
                actionUpStartInMillis = SystemClock.elapsedRealtime()
                isDragging = false
                moves.clear()
                actionHandler.postDelayed(dragAndDropTimeoutRunnable, DND_TIMEOUT)
            }

            MotionEvent.ACTION_MOVE -> {
                Timber.d("ACTION MOVE: $event")
                val y = event.getY(0)
                moves.add(y)
                if (moves.size > 1) {
                    val delta = abs(moves.last() - moves.first())
                    Timber.d("ACTION MOVE DELTA: $delta")
                    if (delta > touchSlop) {
                        isDragging = true
                    }
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                Timber.d("ACTION CANCEL")
                if (!isDragging && event.elapsed() > DND_TIMEOUT) {
                    v.emulateHapticFeedback()
                }
                actionHandler.removeCallbacksAndMessages(null)
                moves.clear()
            }

            MotionEvent.ACTION_UP -> {
                Timber.d("ACTION UP")
                actionHandler.removeCallbacksAndMessages(null)
                moves.clear()

                if (v is TextInputWidget) {
                    val x = (event.x - v.totalPaddingLeft + v.scrollX).toInt()
                    val y = (event.y - v.totalPaddingTop + v.scrollY).toInt()
                    val layout: Layout = v.layout
                    val line = layout.getLineForVertical(y)
                    if (x <= layout.getLineMax(line)) {
                        val offset = layout.getOffsetForHorizontal(line, x.toFloat())
                        val link = v.editableText.getSpans(offset, offset, ClickableSpan::class.java)
                        if (link.isNotEmpty()) {
                            v.clearFocus()
                            link[0].onClick(v)
                            return true
                        }
                    }
                }

                return when {
                    !isDragging && actionUpStartInMillis.untilNow() >= LONG_PRESS_TIMEOUT -> {
                        onLongClick()
                        v.performLongClickWithHaptic()
                        true
                    }
                    else -> fallback(event)
                }
            }

            else -> {
                Timber.d("Ignored motion event: $event")
            }
        }

        return fallback(event)
    }

    companion object {
        val LONG_PRESS_TIMEOUT: Long = android.view.ViewConfiguration.getLongPressTimeout().toLong()
        val DND_TIMEOUT: Long = 2 * LONG_PRESS_TIMEOUT
    }

    private fun View.emulateHapticFeedback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && this !is TextInputWidget) {
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
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
