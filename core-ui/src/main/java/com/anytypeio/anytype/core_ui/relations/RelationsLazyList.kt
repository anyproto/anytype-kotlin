package com.anytypeio.anytype.core_ui.relations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.widgets.dv.WidgetHeader
import com.anytypeio.anytype.presentation.relations.model.RelationsListItem


data class RelationsAddState(
    val items: List<RelationsListItem>,
    val listState: LazyListState
)

@Composable
fun RelationsValueScreen(state: RelationsAddState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(
                color = colorResource(id = R.color.background_secondary),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(bottom = 20.dp)
        ) {
            WidgetHeader(title = "Test1983")
//            SearchField(
//                onFocused = {},
//                onQueryChanged = { s -> }
//            )
            RelationsLazyList(state = state)
        }
    }
}

@Composable
fun RelationsLazyList(state: RelationsAddState) {
    val lazyListState = rememberLazyListState()
    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        itemsIndexed(
            items = state.items,
            itemContent = { _, item ->
                when (item) {
                    is RelationsListItem.CreateItem.Status -> ItemTagOrStatusCreate(state = item)
                    is RelationsListItem.CreateItem.Tag -> ItemTagOrStatusCreate(state = item)
                    is RelationsListItem.Item.Status -> StatusItem(state = item)
                    is RelationsListItem.Item.Tag -> TagItem(state = item)
                }
            })
    }
}