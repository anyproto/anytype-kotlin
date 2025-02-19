package com.anytypeio.anytype.core_ui.lists.objects

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.extensions.swapList
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.lists.objects.stubs.StubVerticalItems
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.animations.DotsLoadingIndicator
import com.anytypeio.anytype.core_ui.views.animations.FadeAnimationSpecs
import com.anytypeio.anytype.presentation.objects.UiObjectsListItem
import kotlinx.coroutines.launch

@Composable
fun ObjectsScreen(
    state: UiObjectsListState,
    uiState: UiContentState,
    canPaginate: Boolean,
    onLoadMore: () -> Unit,
    onObjectClicked: (UiObjectsListItem.Item) -> Unit,
    onMoveToBin: (UiObjectsListItem.Item) -> Unit,
) {
    val items = remember { mutableStateListOf<UiObjectsListItem>() }
    items.swapList(state.items)

    val scope = rememberCoroutineScope()

    val lazyListState = rememberLazyListState()

    val canPaginateState = remember { mutableStateOf(false) }
    LaunchedEffect(key1 = canPaginate) {
        canPaginateState.value = canPaginate
    }

    val shouldStartPaging = remember {
        derivedStateOf {
            canPaginateState.value && (lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                ?: -9) >= (lazyListState.layoutInfo.totalItemsCount - 2)
        }
    }

    LaunchedEffect(key1 = shouldStartPaging.value) {
        if (shouldStartPaging.value && uiState is UiContentState.Idle) {
            onLoadMore()
        }
    }

    LazyColumn(
        modifier = Modifier
            .padding(top = 8.dp)
            .fillMaxSize(),
        state = lazyListState
    ) {
        items(
            count = items.size,
            key = { index -> items[index].id },
            contentType = { index ->
                when (items[index]) {
                    is UiObjectsListItem.Loading -> "loading"
                    is UiObjectsListItem.Item -> "item"
                }
            }
        ) { index ->
            val item = items[index]
            when (item) {
                is UiObjectsListItem.Item -> {
                    SwipeToDismissItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem()
                            .noRippleThrottledClickable {
                                onObjectClicked(item)
                            },
                        item = item,
                        onObjectClicked = onObjectClicked,
                        onMoveToBin = onMoveToBin
                    )
                    Divider(paddingStart = 16.dp, paddingEnd = 16.dp)
                }
                is UiObjectsListItem.Loading -> {
                    ListItemLoading(modifier = Modifier)
                }
            }
        }
        if (uiState is UiContentState.Paging) {
            item {
                Box(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .height(52.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingState()
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(200.dp))
        }
    }

    LaunchedEffect(key1 = uiState) {
        if (uiState is UiContentState.Idle) {
            if (uiState.scrollToTop) {
                scope.launch {
                    lazyListState.scrollToItem(0)
                }
            }
        }
    }
}

@Composable
private fun BoxScope.LoadingState() {
    val loadingAlpha by animateFloatAsState(targetValue = 1f, label = "")
    DotsLoadingIndicator(
        animating = true,
        modifier = Modifier
            .graphicsLayer { alpha = loadingAlpha }
            .align(Alignment.Center),
        animationSpecs = FadeAnimationSpecs(itemCount = 3),
        color = colorResource(id = R.color.glyph_active),
        size = ButtonSize.Small
    )
}

@Composable
@DefaultPreviews
fun ObjectsListScreenPreview() {
    val contentListState = UiObjectsListState(
        items = StubVerticalItems
    )
    ObjectsScreen(
        state = contentListState,
        uiState = UiContentState.Idle(scrollToTop = false),
        canPaginate = true,
        onObjectClicked = {},
        onLoadMore = {},
        onMoveToBin = {},
    )
}