package com.anytypeio.anytype.core_ui.widgets.dv.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material.Text
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.presentation.sets.model.Viewer

/**
 * Read-only Kanban board. On tablets columns are shown side-by-side in a [LazyRow];
 * on phones a single column is shown per page via a [HorizontalPager].
 */
@Composable
fun BoardScreen(
    board: Viewer.Board,
    onCardClick: (Id) -> Unit,
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

    val isTablet = LocalConfiguration.current.smallestScreenWidthDp >= 600

    if (isTablet) {
        LazyRow(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = board.columns,
                key = { it.id }
            ) { column ->
                BoardColumnContent(
                    column = column,
                    onCardClick = onCardClick,
                    modifier = Modifier
                        .width(280.dp)
                        .fillMaxHeight()
                )
            }
        }
    } else {
        val pagerState = rememberPagerState(pageCount = { board.columns.size })
        HorizontalPager(
            state = pagerState,
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            pageSpacing = 12.dp
        ) { page ->
            BoardColumnContent(
                column = board.columns[page],
                onCardClick = onCardClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            )
        }
    }
}
