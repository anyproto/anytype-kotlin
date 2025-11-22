package com.anytypeio.anytype.core_ui.gestures

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.positionChange
import kotlin.math.abs
import sh.calvin.reorderable.DragGestureDetector

/**
 * A custom drag gesture detector that distinguishes between:
 * - Static long-press (triggers menu/action)
 * - Long-press with drag movement (triggers drag-and-drop)
 *
 * This detector solves the UX challenge of supporting both context menus and drag-and-drop
 * on the same UI element by using a movement threshold (touchSlop).
 *
 * @param touchSlop The minimum vertical movement (in pixels) required to initiate a drag.
 *                  Movement less than this threshold will trigger the menu instead.
 * @param onMenuTrigger Callback invoked when user long-presses without exceeding touchSlop.
 * @param haptic HapticFeedback instance for providing haptic feedback on menu trigger.
 * @param onDragStarted Optional callback invoked when drag operation begins.
 * @param onDragStopped Optional callback invoked when drag operation ends.
 */
class LongPressWithSlopDetector(
    private val touchSlop: Float,
    private val onMenuTrigger: () -> Unit,
    private val haptic: HapticFeedback,
    private val onDragStarted: () -> Unit = {},
    private val onDragStopped: () -> Unit = {}
) : DragGestureDetector {
    override suspend fun PointerInputScope.detect(
        onDragStart: (Offset) -> Unit,
        onDragEnd: () -> Unit,
        onDragCancel: () -> Unit,
        onDrag: (PointerInputChange, Offset) -> Unit
    ) {
        awaitEachGesture {
            val down = awaitFirstDown()

            // Wait for a long-press. If it gets cancelled (pointer up or moved too far),
            // we stop handling this gesture.
            val longPress = awaitLongPressOrCancellation(down.id) ?: return@awaitEachGesture

            var isDragging = false
            val pointerId = longPress.id
            var dragStartOffset = longPress.position

            // After long-press is recognized, watch for movement.
            while (true) {
                val event = awaitPointerEvent()
                val change = event.changes.firstOrNull { it.id == pointerId } ?: continue

                if (!change.pressed) {
                    // Pointer was released; decide whether to trigger menu or end drag.
                    if (isDragging) {
                        onDragEnd()
                        onDragStopped()
                    } else {
                        onMenuTrigger()
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    break
                }

                val dragDelta = change.positionChange()
                val verticalDelta = dragDelta.y

                if (!isDragging) {
                    // Check if we've moved far enough from the long-press position to start a drag.
                    val verticalOffset = change.position.y - dragStartOffset.y
                    if (abs(verticalOffset) > touchSlop) {
                        isDragging = true
                        onDragStarted()
                        onDragStart(dragStartOffset)
                    }
                }

                if (isDragging && verticalDelta != 0f) {
                    onDrag(change, Offset(0f, verticalDelta))
                }
            }
        }
    }
}
