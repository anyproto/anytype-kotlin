package com.anytypeio.anytype.core_ui.widgets.dv.board

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.zIndex
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.presentation.sets.model.Viewer
import kotlin.math.roundToInt

private const val AUTO_SCROLL_STEP_PX = 18f
private val COLUMN_WIDTH = 280.dp

/**
 * Kanban board with drag-and-drop. Columns are laid out in a horizontally
 * scrolling [LazyRow]; long-pressing a card lifts it. Dropping it on another
 * column moves it there ([onCardMoved]); dropping it at a new position within
 * its own column reorders it ([onCardReordered]).
 */
@Composable
fun BoardScreen(
    board: Viewer.Board,
    onCardClick: (Id) -> Unit,
    onCardMoved: (cardId: Id, sourceColumnId: String, targetColumnId: String) -> Unit,
    onCardReordered: (columnId: String, orderedCardIds: List<Id>) -> Unit,
    modifier: Modifier = Modifier
) {
    if (board.columns.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(id = R.string.dataview_board_no_objects),
                style = BodyCalloutRegular,
                color = colorResource(id = R.color.text_tertiary),
                textAlign = TextAlign.Center
            )
        }
        return
    }

    val dragState = remember { BoardDragState() }
    val lazyRowState = rememberLazyListState()
    val density = LocalDensity.current
    var boardCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    // Single derived target-column id so columns don't each rescan bounds per frame.
    val targetColumnId by remember {
        derivedStateOf { if (dragState.isDragging) dragState.targetColumnId() else null }
    }

    val onDrop: () -> Unit = {
        val card = dragState.draggedCard
        val source = dragState.sourceColumnId
        val target = dragState.targetColumnId()
        if (card != null && source != null && target != null) {
            if (target == source) {
                val column = board.columns.find { it.id == target }
                if (column != null) {
                    val newIds = reorderedIds(column, card.objectId, dragState.cardBounds, dragState.pointer)
                    if (newIds != column.cards.map { it.objectId }) {
                        onCardReordered(target, newIds)
                    }
                }
            } else {
                onCardMoved(card.objectId, source, target)
            }
        }
        dragState.stop()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { boardCoords = it }
    ) {
        LazyRow(
            state = lazyRowState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            userScrollEnabled = !dragState.isDragging
        ) {
            items(
                items = board.columns,
                key = { it.id }
            ) { column ->
                DisposableEffect(column.id) {
                    onDispose { dragState.columnBounds.remove(column.id) }
                }
                BoardColumnContent(
                    column = column,
                    dragState = dragState,
                    targetColumnId = targetColumnId,
                    boardCoordsProvider = { boardCoords },
                    onCardClick = onCardClick,
                    onDrop = onDrop,
                    modifier = Modifier
                        .width(COLUMN_WIDTH)
                        .fillMaxHeight()
                        .onGloballyPositioned { coords ->
                            val board = boardCoords
                            if (board != null && coords.isAttached) {
                                dragState.columnBounds[column.id] = board.localBoundingBoxOf(coords)
                            }
                        }
                )
            }
        }

        // Insertion indicator while reordering within a column.
        if (dragState.isDragging) {
            val source = dragState.sourceColumnId
            val target = targetColumnId
            val draggedId = dragState.draggedCard?.objectId
            if (target != null && target == source && draggedId != null) {
                val column = board.columns.find { it.id == target }
                val colRect = dragState.columnBounds[target]
                if (column != null && colRect != null) {
                    val y = insertionY(column, draggedId, dragState.cardBounds, dragState.pointer)
                    if (y != null) {
                        val pad = with(density) { 8.dp.toPx() }
                        Box(
                            modifier = Modifier
                                .zIndex(2f)
                                .offset { IntOffset((colRect.left + pad).roundToInt(), (y - 1f).roundToInt()) }
                                .width(with(density) { (colRect.width - 2 * pad).toDp() })
                                .height(2.dp)
                                .background(colorResource(id = R.color.text_primary))
                        )
                    }
                }
            }
        }

        // Floating overlay for the card being dragged.
        val dragged = dragState.draggedCard
        if (dragged != null && dragState.cardSize.width > 0) {
            val w = with(density) { dragState.cardSize.width.toDp() }
            val h = with(density) { dragState.cardSize.height.toDp() }
            Box(
                modifier = Modifier
                    .zIndex(3f)
                    .offset { dragState.cardTopLeft.round() }
                    .size(width = w, height = h)
                    .shadow(8.dp, RoundedCornerShape(8.dp))
            ) {
                BoardCardItem(
                    card = dragged,
                    onClick = {},
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.97f)
                )
            }
        }
    }

    // Auto-scroll the row while a dragged card hovers near either edge.
    LaunchedEffect(dragState.autoScroll) {
        val dir = dragState.autoScroll
        if (dir != 0) {
            lazyRowState.scroll {
                while (
                    (dir > 0 && lazyRowState.canScrollForward) ||
                    (dir < 0 && lazyRowState.canScrollBackward)
                ) {
                    withFrameNanos { }
                    scrollBy(dir * AUTO_SCROLL_STEP_PX)
                }
            }
        }
    }
}

/** Builds the new ordered ids for [column] with [draggedId] moved to the pointer position. */
private fun reorderedIds(
    column: Viewer.Board.Column,
    draggedId: Id,
    cardBounds: Map<String, Rect>,
    pointer: Offset
): List<Id> {
    val remaining = column.cards.map { it.objectId }.filter { it != draggedId }
    val insertIndex = remaining.count { id ->
        val b = cardBounds[id]
        b != null && (b.top + b.height / 2f) < pointer.y
    }
    val result = remaining.toMutableList()
    result.add(insertIndex.coerceIn(0, result.size), draggedId)
    return result
}

/** The y (board coordinates) of the insertion line for the pointer, or null if the column is empty. */
private fun insertionY(
    column: Viewer.Board.Column,
    draggedId: Id,
    cardBounds: Map<String, Rect>,
    pointer: Offset
): Float? {
    val bounds = column.cards.map { it.objectId }
        .filter { it != draggedId }
        .mapNotNull { cardBounds[it] }
    if (bounds.isEmpty()) return null
    val insertIndex = bounds.count { (it.top + it.height / 2f) < pointer.y }
    return if (insertIndex < bounds.size) bounds[insertIndex].top else bounds.last().bottom
}
