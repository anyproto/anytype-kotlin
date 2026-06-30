package com.anytypeio.anytype.core_ui.widgets.dv.board

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize
import com.anytypeio.anytype.presentation.sets.model.Viewer

/**
 * Auto-scroll direction for a pointer at [position] within a container of [size] (px): +1 past
 * the far edge, -1 before the near edge (within [edge] px), 0 otherwise. Shared by the
 * horizontal row and the vertical per-column auto-scroll.
 */
internal fun edgeAutoScroll(position: Float, size: Int, edge: Float): Int = when {
    size <= 0 -> 0
    position > size - edge -> 1
    position < edge -> -1
    else -> 0
}

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

    /** -1 = auto-scroll the hovered column up, +1 = down, 0 = none. */
    var verticalAutoScroll by mutableStateOf(0)
        private set

    /** Column bounds in board coordinates, keyed by column id. */
    val columnBounds = mutableStateMapOf<String, Rect>()

    /** Each column's LazyColumn state, keyed by column id — for vertical auto-scroll during drag. */
    val columnListStates = mutableMapOf<String, LazyListState>()

    /** Card bounds in board coordinates, keyed by object id. */
    val cardBounds = mutableStateMapOf<String, Rect>()

    val isDragging: Boolean get() = draggedCard != null

    fun start(card: Viewer.Board.Card, columnId: String, topLeft: Offset, pointer: Offset, size: IntSize) {
        draggedCard = card
        sourceColumnId = columnId
        cardTopLeft = topLeft
        this.pointer = pointer
        cardSize = size
        autoScroll = 0
        verticalAutoScroll = 0
    }

    fun drag(delta: Offset, boardWidth: Int, boardHeight: Int, edge: Float) {
        pointer += delta
        cardTopLeft += delta
        autoScroll = edgeAutoScroll(pointer.x, boardWidth, edge)
        verticalAutoScroll = edgeAutoScroll(pointer.y, boardHeight, edge)
    }

    /** The column currently under the pointer, or null if none. */
    fun targetColumnId(): String? =
        columnBounds.entries.firstOrNull { it.value.contains(pointer) }?.key

    fun stop() {
        draggedCard = null
        sourceColumnId = null
        cardSize = IntSize.Zero
        autoScroll = 0
        verticalAutoScroll = 0
    }
}
