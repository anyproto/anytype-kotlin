package com.anytypeio.anytype.core_ui.widgets.dv.board

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.LayoutCoordinates
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

/** How close to the end of a column (in items) triggers the next page request. */
private const val BOARD_LOAD_MORE_THRESHOLD = 3

/**
 * A single board column: a header with the group label, a color dot and the card
 * count, followed by a vertically scrolling list of draggable cards. Highlights
 * itself while a card from another column hovers over it.
 */
@Composable
fun BoardColumnContent(
    column: Viewer.Board.Column,
    dragState: BoardDragState,
    targetColumnId: String?,
    boardCoordsProvider: () -> LayoutCoordinates?,
    onCardClick: (Id) -> Unit,
    onColumnLoadMore: (columnId: String) -> Unit,
    canCreateObject: Boolean = false,
    onCreateInColumn: (columnId: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isDropTarget = dragState.isDragging &&
        dragState.sourceColumnId != column.id &&
        targetColumnId == column.id

    // "Color columns": tint the whole column with its group color when set. The drop-target
    // highlight still takes precedence while a card from another column hovers over it.
    val columnBackgroundColor = column.backgroundColor
    val background = when {
        isDropTarget -> colorResource(id = R.color.shape_secondary)
        columnBackgroundColor != null -> light(columnBackgroundColor)
        else -> colorResource(id = R.color.shape_tertiary)
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp)
        ) {
            val colorCode = column.color
            if (colorCode != null) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dark(colorCode))
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = column.label,
                style = Title2,
                color = colorResource(id = R.color.text_primary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${column.count}",
                style = Caption1Regular,
                color = colorResource(id = R.color.text_secondary)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (column.cards.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.dataview_board_no_objects),
                        style = Caption1Regular,
                        color = colorResource(id = R.color.text_tertiary)
                    )
                }
                if (canCreateObject) {
                    BoardAddCardButton(onClick = { onCreateInColumn(column.id) })
                }
            }
        } else {
            val listState = rememberLazyListState()
            // Expose this column's list state for vertical auto-scroll during a drag (see BoardScreen).
            DisposableEffect(column.id) {
                dragState.columnListStates[column.id] = listState
                onDispose { dragState.columnListStates.remove(column.id) }
            }
            // More records exist on the backend than are currently loaded for this column.
            val canPaginate = rememberUpdatedState(column.cards.size < column.count)
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
            LaunchedEffect(shouldPage) {
                if (shouldPage) onColumnLoadMore(column.id)
            }
            LazyColumn(
                state = listState,
                modifier = Modifier.wrapContentHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = column.cards,
                    key = { it.objectId }
                ) { card ->
                    BoardCard(
                        card = card,
                        dragState = dragState,
                        boardCoordsProvider = boardCoordsProvider,
                        onCardClick = onCardClick
                    )
                }
                if (canCreateObject) {
                    item(key = "add-card-${column.id}") {
                        BoardAddCardButton(onClick = { onCreateInColumn(column.id) })
                    }
                }
                if (canPaginate.value) {
                    item(key = "board-load-more") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
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
    }
}

@Composable
private fun BoardCard(
    card: Viewer.Board.Card,
    dragState: BoardDragState,
    boardCoordsProvider: () -> LayoutCoordinates?,
    onCardClick: (Id) -> Unit
) {
    val isBeingDragged = dragState.draggedCard?.objectId == card.objectId

    // Keep the board's hit-test map current; the drag gesture itself lives on the board
    // container (see BoardScreen), so this item can be disposed without killing a drag.
    DisposableEffect(card.objectId) {
        onDispose { dragState.cardBounds.remove(card.objectId) }
    }

    BoardCardItem(
        card = card,
        onClick = { onCardClick(card.objectId) },
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coords ->
                val board = boardCoordsProvider()
                if (board != null && coords.isAttached) {
                    dragState.cardBounds[card.objectId] = board.localBoundingBoxOf(coords)
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
        modifier = Modifier.width(280.dp)
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
        modifier = Modifier.width(280.dp)
    )
}
