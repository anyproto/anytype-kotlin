package com.anytypeio.anytype.core_ui.widgets.dv.board

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.core_ui.extensions.light
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.core_ui.views.animations.DotsLoadingIndicator
import com.anytypeio.anytype.core_ui.views.animations.FadeAnimationSpecs
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.core_ui.widgets.dv.scroll.DataviewScrollCoordinator
import com.anytypeio.anytype.core_ui.widgets.dv.scroll.MotionOrigin
import com.anytypeio.anytype.core_ui.widgets.dv.scroll.dataviewColumnTouchObserver
import com.anytypeio.anytype.core_ui.widgets.dv.scroll.rememberDataviewColumnScrollConnection

/** How close to the end of a column (in items) triggers the next page request. */
private const val BOARD_LOAD_MORE_THRESHOLD = 3

/**
 * A single board column: a header with the group label, a color dot and the card
 * count, followed by a vertically scrolling list of draggable cards. Highlights
 * itself while a card from another column hovers over it.
 */
@Composable
internal fun BoardColumnContent(
    column: Viewer.Board.Column,
    dragState: BoardDragState,
    targetColumnId: String?,
    boardCoordsProvider: () -> LayoutCoordinates?,
    onCardClick: (Id) -> Unit,
    onColumnLoadMore: (columnId: String) -> Unit,
    canCreateObject: Boolean = false,
    onCreateInColumn: (columnId: String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewerId: String = "preview",
    coordinator: DataviewScrollCoordinator? = null,
    scrollStore: BoardScrollStore? = null
) {
    val isDropTarget = dragState.isDragging &&
        dragState.sourceColumnId != column.id && targetColumnId == column.id
    val background = when {
        isDropTarget -> colorResource(id = R.color.shape_secondary)
        column.backgroundColor != null -> light(column.backgroundColor!!)
        else -> colorResource(id = R.color.shape_tertiary)
    }
    val saved = remember(viewerId, column.id, scrollStore) { scrollStore?.anchor(viewerId, column.id) }
    val restoreGeneration = remember(viewerId, column.id, scrollStore) {
        coordinator?.inputGeneration ?: scrollStore?.inputGeneration
    }
    val initialIds = remember(viewerId, column.id) { column.cards.map { it.objectId } }
    val initialResolved = remember(viewerId, column.id) {
        boardAnchorResolved(saved, column)
    }
    val listState = remember(viewerId, column.id) {
        val index = if (saved?.id == null || initialIds.isEmpty()) 0 else resolveBoardAnchor(saved, initialIds) + 1
        LazyListState(index, saved?.offset ?: 0)
    }
    val connection = if (coordinator != null) {
        rememberDataviewColumnScrollConnection(coordinator, viewerId, column.id, listState)
    } else null
    val currentColumn by rememberUpdatedState(column)
    val currentLoadMore by rememberUpdatedState(onColumnLoadMore)
    var restored by remember(viewerId, column.id) { mutableStateOf(initialResolved) }
    val restoreIds = remember(column.cards) { column.cards.map { it.objectId } }
    val bottomClearance = with(LocalDensity.current) { 88.dp.toPx() }
    var listBounds by remember { mutableStateOf<Rect?>(null) }
    var labelBottom by remember { mutableStateOf<Float?>(null) }

    fun updateViewport() {
        val bounds = listBounds ?: return
        val top = (labelBottom ?: bounds.top).coerceIn(bounds.top, bounds.bottom)
        dragState.cardViewports[column.id] = Rect(
            bounds.left, top, bounds.right, (bounds.bottom - bottomClearance).coerceAtLeast(top)
        )
    }

    DisposableEffect(viewerId, column.id, listState, connection) {
        dragState.columnListStates[column.id] = listState
        if (connection != null) dragState.columnScrollConnections[column.id] = connection
        scrollStore?.register(viewerId, column.id) {
            if (!restored && saved != null &&
                restoreGeneration == (coordinator?.inputGeneration ?: scrollStore?.inputGeneration)
            ) saved else columnScrollAnchor(listState, currentColumn.cards.map { it.objectId })
        }
        onDispose {
            scrollStore?.unregister(viewerId, column.id)
            dragState.columnListStates.remove(column.id)
            dragState.columnScrollConnections.remove(column.id)
            dragState.cardViewports.remove(column.id)
            dragState.endInsertionBounds.remove(column.id)
        }
    }
    LaunchedEffect(viewerId, column.id, restoreIds, column.count, column.hasLoadedRecords, saved) {
        if (!restored && saved != null &&
            restoreGeneration == (coordinator?.inputGeneration ?: scrollStore?.inputGeneration) &&
            !listState.isScrollInProgress
        ) {
            val ids = column.cards.map { it.objectId }
            if (saved.id != null && ids.isEmpty() &&
                (!column.hasLoadedRecords || column.count > 0)
            ) return@LaunchedEffect
            val index = if (saved.id == null || ids.isEmpty()) 0 else resolveBoardAnchor(saved, ids) + 1
            val restore: suspend () -> Unit = { listState.scrollToItem(index, saved.offset) }
            if (connection != null) connection.withOrigin(MotionOrigin.Restoration, restore) else restore()
            restored = boardAnchorResolved(saved, column)
        }
    }

    // Loaded-count is the request generation. It re-arms after every page even if the
    // near-end predicate stays true, and layout changes can make it true without a drag.
    val canPaginate = rememberUpdatedState(column.hasLoadedRecords && column.cards.size < column.count)
    val shouldPage by remember {
        derivedStateOf {
            shouldLoadMore(
                lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1,
                totalItemsCount = listState.layoutInfo.totalItemsCount,
                canPaginate = canPaginate.value,
                threshold = BOARD_LOAD_MORE_THRESHOLD
            )
        }
    }
    var requestedCount by remember(viewerId, column.id) { mutableStateOf<Int?>(null) }
    LaunchedEffect(shouldPage, column.cards.size, column.count) {
        if (shouldPage && requestedCount != column.cards.size) {
            requestedCount = column.cards.size
            currentLoadMore(column.id)
        }
    }

    val input = if (connection != null) {
        Modifier.dataviewColumnTouchObserver(connection).nestedScroll(connection)
    } else Modifier
    LazyColumn(
        state = listState,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .then(input)
            .fillMaxSize()
            .onGloballyPositioned { coords ->
                val board = boardCoordsProvider()
                if (board != null && coords.isAttached) {
                    listBounds = board.localBoundingBoxOf(coords)
                    updateViewport()
                }
            },
        userScrollEnabled = !dragState.isDragging,
        contentPadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        stickyHeader(key = "board-label:${column.id}", contentType = "label") { _ ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(background)
                    .onGloballyPositioned { coords ->
                        val board = boardCoordsProvider()
                        if (board != null && coords.isAttached) {
                            labelBottom = board.localBoundingBoxOf(coords).bottom
                            updateViewport()
                        }
                    }
                    // Keep the same inset while pinned. A top contentPadding would scroll
                    // away; placing it inside the sticky item leaves the full column touchable.
                    .padding(top = 8.dp)
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                column.color?.let { color ->
                    Box(Modifier.size(8.dp).clip(CircleShape).background(dark(color)))
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    text = column.label,
                    style = Title2,
                    color = colorResource(id = R.color.text_primary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                if (column.hasLoadedRecords) {
                    Text("${column.count}", style = Caption1Regular, color = colorResource(id = R.color.text_secondary))
                }
            }
        }
        items(column.cards, key = { "$BOARD_CARD_KEY_PREFIX${it.objectId}" }, contentType = { "card" }) { card ->
            BoardCard(card, column.id, dragState, boardCoordsProvider, onCardClick)
        }
        if (column.hasLoadedRecords && isColumnFullyLoaded(column.cards.size, column.count)) {
            item(key = "board-end:${column.id}", contentType = "insertion") {
                Spacer(Modifier.fillMaxWidth().height(24.dp).onGloballyPositioned { coords ->
                    val board = boardCoordsProvider()
                    if (board != null && coords.isAttached) {
                        dragState.endInsertionBounds[column.id] = board.localBoundingBoxOf(coords)
                    }
                })
                DisposableEffect(column.id) { onDispose { dragState.endInsertionBounds.remove(column.id) } }
            }
        }
        if (canCreateObject) {
            item(key = "board-add:${column.id}", contentType = "add") {
                BoardAddCardButton(onClick = { onCreateInColumn(column.id) })
            }
        }
        if (!column.hasLoadedRecords || canPaginate.value) {
            item(key = "board-load-more:${column.id}", contentType = "loading") {
                Box(Modifier.fillMaxWidth().height(48.dp), contentAlignment = Alignment.Center) {
                    DotsLoadingIndicator(
                        animating = true,
                        animationSpecs = FadeAnimationSpecs(itemCount = 3),
                        color = colorResource(id = R.color.glyph_active),
                        size = ButtonSize.Small
                    )
                }
            }
        }
    }
}

@Composable
private fun BoardCard(
    card: Viewer.Board.Card,
    columnId: String,
    dragState: BoardDragState,
    boardCoordsProvider: () -> LayoutCoordinates?,
    onCardClick: (Id) -> Unit
) {
    val isBeingDragged = dragState.draggedCard?.objectId == card.objectId

    // Keep the board's hit-test map current; the drag gesture itself lives on the board
    // container (see BoardScreen), so this item can be disposed without killing a drag.
    DisposableEffect(card.objectId) {
        dragState.cardColumns[card.objectId] = columnId
        onDispose {
            dragState.cardBounds.remove(card.objectId)
            dragState.cardColumns.remove(card.objectId)
        }
    }

    BoardCardItem(
        card = card,
        onClick = { onCardClick(card.objectId) },
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coords ->
                val board = boardCoordsProvider()
                if (board != null && coords.isAttached) {
                    dragState.cardBounds[card.objectId] = board.localBoundingBoxOf(coords, clipBounds = false)
                }
            }
            .alpha(if (isBeingDragged) 0.4f else 1f)
    )
}

/**
 * A card-styled "＋ New" row at the bottom of a column. Tapping it asks the host to create a
 * new object whose group value matches this column (wired in [BoardColumnContent]).
 */
@Composable
private fun BoardAddCardButton(onClick: () -> Unit) {
    // Matches Figma "Gallery Card" (node 10521:24257): a card-shaped tile with a centered
    // 24dp plus, white fill, subtle transparent-secondary border, 16dp corners.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colorResource(id = R.color.background_primary))
            .border(
                width = 1.dp,
                color = colorResource(id = R.color.shape_transparent_secondary),
                shape = RoundedCornerShape(16.dp)
            )
            .noRippleThrottledClickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_default_plus),
            contentDescription = stringResource(id = R.string.dataview_board_new_object),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Preview(name = "Short column + add button", widthDp = 280, backgroundColor = 0xFFEFEFEF, showBackground = true)
@Composable
private fun BoardColumnShortPreview() {
    BoardColumnContent(
        column = Viewer.Board.Column(
            id = "todo",
            label = "To Do",
            cards = listOf(
                Viewer.Board.Card(
                    objectId = "1",
                    name = "Buy milk",
                    icon = ObjectIcon.None,
                    relations = emptyList(),
                    hideIcon = true
                ),
                Viewer.Board.Card(
                    objectId = "2",
                    name = "Walk the dog",
                    icon = ObjectIcon.None,
                    relations = emptyList(),
                    hideIcon = true
                )
            ),
            count = 2
        ),
        dragState = remember { BoardDragState() },
        targetColumnId = null,
        boardCoordsProvider = { null },
        onCardClick = {},
        onColumnLoadMore = {},
        canCreateObject = true,
        onCreateInColumn = {},
        modifier = Modifier.width(280.dp).height(600.dp)
    )
}

@Preview(name = "Empty column + add button", widthDp = 280, backgroundColor = 0xFFEFEFEF, showBackground = true)
@Composable
private fun BoardColumnEmptyPreview() {
    BoardColumnContent(
        column = Viewer.Board.Column(
            id = "done",
            label = "Done",
            cards = emptyList(),
            count = 0
        ),
        dragState = remember { BoardDragState() },
        targetColumnId = null,
        boardCoordsProvider = { null },
        onCardClick = {},
        onColumnLoadMore = {},
        canCreateObject = true,
        onCreateInColumn = {},
        modifier = Modifier.width(280.dp).height(600.dp)
    )
}
