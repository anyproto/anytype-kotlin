package com.anytypeio.anytype.core_ui.widgets.dv.board

import androidx.compose.foundation.background
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.gestures.stopScroll
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.zIndex
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.presentation.sets.model.Viewer
import kotlin.math.roundToInt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import com.anytypeio.anytype.core_ui.widgets.dv.scroll.DataviewScrollCoordinator
import com.anytypeio.anytype.core_ui.widgets.dv.scroll.MotionOrigin
import com.anytypeio.anytype.core_ui.widgets.dv.scroll.dataviewBoardTouchObserver
import com.anytypeio.anytype.core_ui.widgets.dv.scroll.dataviewHeaderScrollEmitter

private val COLUMN_WIDTH = 280.dp

/**
 * Kanban board with drag-and-drop. Columns are laid out in a horizontally
 * scrolling [LazyRow]; long-pressing a card lifts it. Dropping it on another
 * column moves it there ([onCardMoved]); dropping it at a new position within
 * its own column reorders it ([onCardReordered]).
 */
@Composable
internal fun BoardScreen(
    board: Viewer.Board,
    onCardClick: (Id) -> Unit,
    onCardMoved: (cardId: Id, sourceColumnId: String, targetColumnId: String, targetOrderedIds: List<Id>?) -> Unit,
    onCardReordered: (columnId: String, orderedCardIds: List<Id>) -> Unit,
    onColumnLoadMore: (columnId: String) -> Unit,
    canCreateObject: Boolean = false,
    onCreateInColumn: (columnId: String) -> Unit = {},
    modifier: Modifier = Modifier,
    scrollCoordinator: DataviewScrollCoordinator? = null,
    scrollStore: BoardScrollStore = remember { BoardScrollStore() },
    registerDragCancellation: ((() -> Unit) -> Unit) = {},
    registerDragValidation: (((Viewer.Board) -> Unit) -> Unit) = {}
) {
    val scrollKey = board.scrollKey()
    val dragState = remember(scrollKey) { BoardDragState() }
    val savedRow = remember(scrollKey, scrollStore) { scrollStore.anchor(scrollKey, null) }
    val restoreGeneration = remember(scrollKey, scrollStore) {
        scrollCoordinator?.inputGeneration ?: scrollStore.inputGeneration
    }
    val lazyRowState = remember(scrollKey) {
        LazyListState(
            if (savedRow == null) 0 else resolveBoardAnchor(savedRow, board.columns.map { it.id }),
            savedRow?.offset ?: 0
        )
    }
    var rowRestored by remember(scrollKey) { mutableStateOf(savedRow == null || board.columns.isNotEmpty()) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    var boardCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var boardWidth by remember { mutableIntStateOf(0) }
    val currentBoard by rememberUpdatedState(board)
    val currentOnCardMoved by rememberUpdatedState(onCardMoved)
    val currentOnCardReordered by rememberUpdatedState(onCardReordered)
    val currentCoordinator by rememberUpdatedState(scrollCoordinator)
    val stopDrag: () -> Unit = {
        dragState.stop()
        currentCoordinator?.setBlocked(MotionOrigin.CardDrag, false)
    }
    val currentStopDrag by rememberUpdatedState(stopDrag)
    DisposableEffect(scrollKey, scrollStore) {
        registerDragCancellation { currentStopDrag() }
        registerDragValidation { next ->
            val dragged = dragState.draggedCard
            if (dragged != null && next.columns.none { column ->
                    column.id == dragState.sourceColumnId &&
                        column.cards.any { it.objectId == dragged.objectId }
                }) currentStopDrag()
        }
        scrollStore.register(scrollKey, null) {
            if (!rowRestored && savedRow != null &&
                restoreGeneration == (scrollCoordinator?.inputGeneration ?: scrollStore.inputGeneration)
            ) savedRow else rowScrollAnchor(lazyRowState, currentBoard.columns.map { it.id })
        }
        onDispose {
            currentStopDrag()
            registerDragCancellation {}
            registerDragValidation {}
            scrollStore.unregister(scrollKey, null)
        }
    }
    LaunchedEffect(board.columns, dragState.draggedCard?.objectId) {
        val dragged = dragState.draggedCard
        if (dragged != null && board.columns.none { column ->
                column.id == dragState.sourceColumnId && column.cards.any { it.objectId == dragged.objectId }
            }) currentStopDrag()
    }
    LaunchedEffect(scrollKey, board.columns.size, savedRow) {
        if (!rowRestored && savedRow != null && board.columns.isNotEmpty() &&
            restoreGeneration == (scrollCoordinator?.inputGeneration ?: scrollStore.inputGeneration) &&
            !lazyRowState.isScrollInProgress
        ) {
            lazyRowState.scrollToItem(resolveBoardAnchor(savedRow, board.columns.map { it.id }), savedRow.offset)
            rowRestored = true
        }
    }
    val targetColumnId by remember {
        derivedStateOf { if (dragState.isDragging) dragState.targetColumnId() else null }
    }
    val onDrop: () -> Unit = {
        try {
            val card = dragState.draggedCard
            val source = dragState.sourceColumnId
            val target = dragState.targetColumnId()
            val targetColumn = currentBoard.columns.find { it.id == target }
            if (card != null && source != null && target != null && targetColumn != null) {
                val visible = dragState.visibleCardBounds()
                val insertion = boardInsertionIndex(
                    targetColumn, card.objectId, dragState.pointer, visible,
                    dragState.cardViewports[target], dragState.endInsertionBounds[target]
                )
                val full = targetColumn.hasLoadedRecords && isColumnFullyLoaded(targetColumn.cards.size, targetColumn.count)
                if (target == source) {
                    if (full && insertion != null) {
                        val ids = targetColumn.cards.map { it.objectId }.filter { it != card.objectId }.toMutableList()
                        ids.add(insertion.coerceIn(0, ids.size), card.objectId)
                        if (ids != targetColumn.cards.map { it.objectId }) currentOnCardReordered(target, ids)
                    }
                } else {
                    val order = if (full && insertion != null) {
                        targetColumn.cards.map { it.objectId }.toMutableList().apply {
                            add(insertion.coerceIn(0, size), card.objectId)
                        }
                    } else null
                    currentOnCardMoved(card.objectId, source, target, order)
                }
            }
        } finally { currentStopDrag() }
    }
    val currentOnDrop by rememberUpdatedState(onDrop)
    val stopRow: () -> Unit = {
        scrollStore.onUserInput()
        scope.launch(start = CoroutineStart.UNDISPATCHED) { lazyRowState.stopScroll(MutatePriority.PreventUserInput) }
        if (scrollCoordinator == null) {
            dragState.columnListStates.values.forEach { state ->
                scope.launch(start = CoroutineStart.UNDISPATCHED) { state.stopScroll(MutatePriority.PreventUserInput) }
            }
        }
    }
    val currentStopRow by rememberUpdatedState(stopRow)
    val observeInput = remember(scrollCoordinator, scrollStore) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput) {
                    scrollStore.onUserInput()
                    if (available.x != 0f) scrollCoordinator?.cancel()
                }
                return Offset.Zero
            }
        }
    }
    // Null coordinator is the embedded screen: only its existing outer header owns scrolling.
    val route = if (scrollCoordinator == null) {
        Modifier.nestedScroll(rememberNestedScrollInteropConnection())
    } else Modifier.dataviewBoardTouchObserver(scrollCoordinator) { currentStopRow() }

    Box(
        modifier = modifier.fillMaxSize().then(route).nestedScroll(observeInput)
            .onGloballyPositioned {
                boardCoords = it
                if (boardWidth != it.size.width) boardWidth = it.size.width
            }
            .pointerInput(Unit) {
                val edge = 56.dp.toPx()
                awaitEachGesture {
                    try {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        if (currentCoordinator == null) currentStopRow()
                        // Reject labels/gutters/occluded cards BEFORE long-press recognition
                        // commits automatic consumption. A hold there can still become scroll.
                        if (findCardAt(down.position, currentBoard.columns, dragState.visibleCardBounds()) == null) {
                            return@awaitEachGesture
                        }
                        val held = awaitLongPressOrCancellation(down.id) ?: return@awaitEachGesture
                        val hit = findCardAt(held.position, currentBoard.columns, dragState.visibleCardBounds())
                            ?: return@awaitEachGesture
                        val rect = dragState.cardBounds[hit.card.objectId] ?: return@awaitEachGesture
                        currentCoordinator?.setBlocked(MotionOrigin.CardDrag, true)
                        dragState.start(hit.card, hit.columnId, rect.topLeft, held.position,
                            IntSize(rect.width.roundToInt(), rect.height.roundToInt()))
                        if (drag(held.id) { change ->
                                if (dragState.isDragging) {
                                    dragState.drag(change.positionChange(), boardCoords?.size?.width ?: 0,
                                        boardCoords?.size?.height ?: 0, edge)
                                    change.consume()
                                }
                            }) {
                            currentEvent.changes.forEach { if (it.changedToUp()) it.consume() }
                            if (dragState.isDragging) currentOnDrop()
                        } else currentStopDrag()
                    } catch (cancelled: CancellationException) {
                        currentStopDrag()
                        throw cancelled
                    }
                }
            }
    ) {
        Column(Modifier.fillMaxSize()) {
            Spacer(Modifier.fillMaxWidth().height(12.dp).backgroundInput(scrollCoordinator, scrollKey, !dragState.isDragging))
            if (board.columns.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth().backgroundInput(scrollCoordinator, scrollKey, !dragState.isDragging),
                    contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.dataview_board_no_objects), style = BodyCalloutRegular,
                        color = colorResource(R.color.text_tertiary), textAlign = TextAlign.Center)
                }
            } else {
                val widthDp = with(density) { boardWidth.toDp() }
                val trailingSpace = maxOf(16.dp, widthDp - 16.dp - COLUMN_WIDTH * board.columns.size - 12.dp * (board.columns.size - 1))
                LazyRow(
                    state = lazyRowState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    userScrollEnabled = !dragState.isDragging
                ) {
                    itemsIndexed(board.columns, key = { _, column -> column.id }) { index, column ->
                        DisposableEffect(column.id) { onDispose { dragState.columnBounds.remove(column.id) } }
                        Row(Modifier.fillMaxHeight()) {
                            if (index == 0) Spacer(Modifier.width(16.dp).fillMaxHeight()
                                .backgroundInput(scrollCoordinator, scrollKey, !dragState.isDragging))
                            BoardColumnContent(
                                column = column,
                                dragState = dragState,
                                targetColumnId = targetColumnId,
                                boardCoordsProvider = { boardCoords },
                                onCardClick = onCardClick,
                                onColumnLoadMore = onColumnLoadMore,
                                canCreateObject = canCreateObject,
                                onCreateInColumn = onCreateInColumn,
                                viewerId = scrollKey,
                                coordinator = scrollCoordinator,
                                scrollStore = scrollStore,
                                modifier = Modifier.width(COLUMN_WIDTH).fillMaxHeight().onGloballyPositioned { coords ->
                                    val boardCoordinates = boardCoords
                                    if (boardCoordinates != null && coords.isAttached) {
                                        dragState.columnBounds[column.id] = boardCoordinates.localBoundingBoxOf(coords)
                                    }
                                }
                            )
                            Spacer(Modifier.width(if (index == board.columns.lastIndex) trailingSpace else 12.dp)
                                .fillMaxHeight().backgroundInput(scrollCoordinator, scrollKey, !dragState.isDragging))
                        }
                    }
                }
            }
            Spacer(Modifier.fillMaxWidth().height(12.dp).backgroundInput(scrollCoordinator, scrollKey, !dragState.isDragging))
        }
        if (dragState.isDragging) {
            val target = targetColumnId
            val draggedId = dragState.draggedCard?.objectId
            val column = board.columns.find { it.id == target }
            val colRect = target?.let(dragState.columnBounds::get)
            if (column != null && colRect != null && draggedId != null && column.hasLoadedRecords && isColumnFullyLoaded(column.cards.size, column.count)) {
                val pad = with(density) { 8.dp.toPx() }
                Box(Modifier.zIndex(2f).offset {
                    val visible = dragState.visibleCardBounds()
                    val end = target?.let(dragState.endInsertionBounds::get)
                    val insertion = boardInsertionIndex(column, draggedId, dragState.pointer, visible,
                        target?.let(dragState.cardViewports::get), end)
                    val y = when {
                        insertion == null -> null
                        end?.contains(dragState.pointer) == true -> end.top
                        else -> insertionY(column, draggedId, visible, dragState.pointer)
                    }
                    if (y != null) IntOffset((colRect.left + pad).roundToInt(), (y - 1f).roundToInt()) else IntOffset(0, -10_000)
                }.width(with(density) { (colRect.width - 2 * pad).toDp() }).height(2.dp)
                    .background(colorResource(R.color.text_primary)))
            }
        }
        val dragged = dragState.draggedCard
        if (dragged != null && dragState.cardSize.width > 0) {
            Box(Modifier.zIndex(3f).offset { dragState.cardTopLeft.round() }
                .size(with(density) { dragState.cardSize.width.toDp() }, with(density) { dragState.cardSize.height.toDp() })
                .shadow(8.dp, RoundedCornerShape(8.dp))) {
                BoardCardItem(dragged, onClick = {}, modifier = Modifier.fillMaxSize().alpha(0.97f))
            }
        }
    }
    LaunchedEffect(dragState.autoScroll) {
        val direction = dragState.autoScroll
        if (direction != 0) {
            lazyRowState.scroll {
                var previous = withFrameNanos { it }
                while (dragState.isDragging && ((direction > 0 && lazyRowState.canScrollForward) ||
                            (direction < 0 && lazyRowState.canScrollBackward))) {
                    val frame = withFrameNanos { it }
                    scrollBy(boardAutoScrollDistance(direction, density.density, frame - previous))
                    previous = frame
                }
            }
        }
    }
    LaunchedEffect(dragState.verticalAutoScroll, targetColumnId) {
        val direction = dragState.verticalAutoScroll
        val state = targetColumnId?.let(dragState.columnListStates::get)
        if (direction != 0 && state != null) {
            val autoScroll: suspend () -> Unit = {
                state.scroll {
                    var previous = withFrameNanos { it }
                    while (dragState.isDragging && ((direction > 0 && state.canScrollForward) ||
                                (direction < 0 && state.canScrollBackward))) {
                        val frame = withFrameNanos { it }
                        scrollBy(boardAutoScrollDistance(direction, density.density, frame - previous))
                        previous = frame
                    }
                }
            }
            val connection = targetColumnId?.let(dragState.columnScrollConnections::get)
            if (connection != null) connection.withOrigin(MotionOrigin.DragAutoScroll, autoScroll) else autoScroll()
        }
    }
}

@Composable
private fun Modifier.backgroundInput(coordinator: DataviewScrollCoordinator?, viewerId: String, enabled: Boolean): Modifier {
    return if (coordinator != null) dataviewHeaderScrollEmitter(coordinator, viewerId, enabled)
    else scrollable(rememberScrollableState { 0f }, Orientation.Vertical, enabled = enabled)
}

/** Background/header entry is not an implicit append; end insertion has its own visible item. */
internal fun boardInsertionIndex(
    column: Viewer.Board.Column,
    draggedId: String,
    pointer: Offset,
    visibleBounds: Map<String, Rect>,
    viewport: Rect?,
    endBounds: Rect?
): Int? {
    if (viewport?.contains(pointer) != true) return null
    val remaining = column.cards.map { it.objectId }.filter { it != draggedId }
    if (column.hasLoadedRecords && isColumnFullyLoaded(column.cards.size, column.count) && endBounds?.contains(pointer) == true) {
        return remaining.size
    }
    val visible = remaining.mapNotNull(visibleBounds::get)
    val first = visible.firstOrNull() ?: return null
    val last = visible.last()
    if (pointer.y < first.top || pointer.y > last.bottom) return null
    val index = reorderInsertIndex(remaining, pointer.y) { id -> visibleBounds[id]?.center?.y }
    val lastVisibleIndex = remaining.indexOfLast { it in visibleBounds }
    // Crossing the midpoint of the last measured card means after that card, not after
    // every unmeasured card below the viewport. Only the explicit end item can append.
    return index.coerceAtMost(lastVisibleIndex + 1)
}

/**
 * Whether a column should request its next page: only when it [canPaginate] (more records exist
 * than are loaded) and the last visible item is within [threshold] of the end of the list.
 */
internal fun shouldLoadMore(
    lastVisibleIndex: Int,
    totalItemsCount: Int,
    canPaginate: Boolean,
    threshold: Int
): Boolean = canPaginate && lastVisibleIndex >= 0 && totalItemsCount > 0 &&
    lastVisibleIndex >= totalItemsCount - threshold

/**
 * Whether a column's [loadedCards] cover its full backend [count]. Only a fully-loaded column
 * can have its object order persisted from the client: writing a partially-paged column's order
 * would truncate the backend's full order. Used to gate both reorder and cross-column drops.
 */
internal fun isColumnFullyLoaded(loadedCards: Int, count: Int): Boolean = loadedCards >= count

/** A card found under a board-local point, together with the column it belongs to. */
internal data class BoardCardHit(val card: Viewer.Board.Card, val columnId: String)

/**
 * Finds the card whose laid-out bounds contain [point] (in board coordinates), or null
 * if the point is over empty space or a not-yet-measured card. Used by the board-level
 * long-press detector to decide which card to lift.
 */
internal fun findCardAt(
    point: Offset,
    columns: List<Viewer.Board.Column>,
    cardBounds: Map<String, Rect>
): BoardCardHit? {
    columns.forEach { column ->
        column.cards.forEach { card ->
            val rect = cardBounds[card.objectId]
            if (rect != null && rect.contains(point)) {
                return BoardCardHit(card = card, columnId = column.id)
            }
        }
    }
    return null
}

/**
 * The index in [remaining] (the column's card ids minus the dragged card, in model order) at
 * which to insert the dragged card for a pointer at [pointerY] (board coordinates). [midY]
 * returns a card's midpoint Y, or null for cards that aren't laid out (scrolled off-screen
 * in the column's LazyColumn).
 *
 * It returns the **full-list index** of the first visible card whose midpoint is at/below the
 * pointer (or the end if none) — so cards scrolled *above* the viewport still count toward the
 * position. The previous implementation counted only measured cards, which collapsed the
 * insert toward the top and persisted a corrupted order for columns taller than the viewport.
 */
internal fun reorderInsertIndex(
    remaining: List<Id>,
    pointerY: Float,
    midY: (Id) -> Float?
): Int {
    val anchor = remaining.firstOrNull { id -> midY(id)?.let { mid -> mid >= pointerY } == true }
    return if (anchor != null) remaining.indexOf(anchor) else remaining.size
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
