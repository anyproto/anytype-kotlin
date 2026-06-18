package com.anytypeio.anytype.core_ui.widgets.dv.board

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.zIndex
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.presentation.sets.model.Viewer

private const val AUTO_SCROLL_STEP_PX = 18f
private val COLUMN_WIDTH = 280.dp

/**
 * Kanban board with cross-column drag-and-drop. Columns are laid out in a
 * horizontally scrolling [LazyRow]; long-pressing a card lifts it and dropping
 * it on another column moves it there (handled by [onCardMoved]).
 */
@Composable
fun BoardScreen(
    board: Viewer.Board,
    onCardClick: (Id) -> Unit,
    onCardMoved: (cardId: Id, targetColumnId: String) -> Unit,
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
                    boardCoordsProvider = { boardCoords },
                    onCardClick = onCardClick,
                    onCardMoved = onCardMoved,
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

        // Floating overlay for the card being dragged.
        val dragged = dragState.draggedCard
        if (dragged != null && dragState.cardSize.width > 0) {
            val w = with(density) { dragState.cardSize.width.toDp() }
            val h = with(density) { dragState.cardSize.height.toDp() }
            Box(
                modifier = Modifier
                    .zIndex(1f)
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
                while (true) {
                    withFrameNanos { }
                    scrollBy(dir * AUTO_SCROLL_STEP_PX)
                }
            }
        }
    }
}
