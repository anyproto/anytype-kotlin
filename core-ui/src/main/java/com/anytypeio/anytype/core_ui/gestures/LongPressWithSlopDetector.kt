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
 * Custom DragGestureDetector that uses touch slop to differentiate between:
 * - Long-press without movement (shows menu)
 * - Long-press with drag movement (starts dragging)
 *
 * Based on approach from https://github.com/Calvin-LL/Reorderable/issues/55
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
            val longPress = awaitLongPressOrCancellation(down.id) ?: run {
                onDragCancel()
                return@awaitEachGesture
            }

            var isDragging = false
            val pointerId = longPress.id
            val dragStartOffset = longPress.position

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
                        change.consume()
                    }
                }

                if (isDragging && verticalDelta != 0f) {
                    onDrag(change, Offset(0f, verticalDelta))
                    change.consume()
                }
            }
        }
    }
}