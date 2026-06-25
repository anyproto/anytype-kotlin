package com.anytypeio.anytype.core_ui.widgets.dv.board

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.presentation.sets.model.Viewer

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
    modifier: Modifier = Modifier
) {
    val isDropTarget = dragState.isDragging &&
        dragState.sourceColumnId != column.id &&
        targetColumnId == column.id

    val background = if (isDropTarget) {
        colorResource(id = R.color.shape_secondary)
    } else {
        colorResource(id = R.color.shape_tertiary)
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
                text = "${column.cards.size}",
                style = Caption1Regular,
                color = colorResource(id = R.color.text_secondary)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (column.cards.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.dataview_board_no_objects),
                    style = Caption1Regular,
                    color = colorResource(id = R.color.text_tertiary)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxHeight(),
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
