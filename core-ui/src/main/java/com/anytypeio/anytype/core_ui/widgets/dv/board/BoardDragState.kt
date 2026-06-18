package com.anytypeio.anytype.core_ui.widgets.dv.board

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize
import com.anytypeio.anytype.presentation.sets.model.Viewer

/**
 * Shared mutable state for cross-column card dragging on the Kanban board.
 * All offsets and bounds are expressed in the board container's coordinate space.
 */
class BoardDragState {

    var draggedCard by mutableStateOf<Viewer.Board.Card?>(null)
        private set
    var sourceColumnId by mutableStateOf<String?>(null)
        private set

    /** Current touch point, in board coordinates. */
    var pointer by mutableStateOf(Offset.Zero)
        private set

    /** Top-left of the floating card overlay, in board coordinates. */
    var cardTopLeft by mutableStateOf(Offset.Zero)
        private set

    /** Pixel size of the dragged card, used to size the overlay. */
    var cardSize by mutableStateOf(IntSize.Zero)
        private set

    /** -1 = auto-scroll left, +1 = auto-scroll right, 0 = none. */
    var autoScroll by mutableStateOf(0)
        private set

    /** Column bounds in board coordinates, keyed by column id. */
    val columnBounds = mutableStateMapOf<String, Rect>()

    val isDragging: Boolean get() = draggedCard != null

    fun start(card: Viewer.Board.Card, columnId: String, topLeft: Offset, pointer: Offset, size: IntSize) {
        draggedCard = card
        sourceColumnId = columnId
        cardTopLeft = topLeft
        this.pointer = pointer
        cardSize = size
        autoScroll = 0
    }

    fun drag(delta: Offset, boardWidth: Int, edge: Float) {
        pointer += delta
        cardTopLeft += delta
        autoScroll = when {
            boardWidth <= 0 -> 0
            pointer.x > boardWidth - edge -> 1
            pointer.x < edge -> -1
            else -> 0
        }
    }

    /** The column currently under the pointer, or null if none. */
    fun targetColumnId(): String? =
        columnBounds.entries.firstOrNull { it.value.contains(pointer) }?.key

    fun stop() {
        draggedCard = null
        sourceColumnId = null
        cardSize = IntSize.Zero
        autoScroll = 0
    }
}
