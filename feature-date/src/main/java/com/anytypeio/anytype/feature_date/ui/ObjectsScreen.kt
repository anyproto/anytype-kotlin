package com.anytypeio.anytype.feature_date.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.common.ShimmerEffect
import com.anytypeio.anytype.core_ui.extensions.swapList
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Regular
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.core_ui.views.animations.DotsLoadingIndicator
import com.anytypeio.anytype.core_ui.views.animations.FadeAnimationSpecs
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.feature_date.R
import com.anytypeio.anytype.feature_date.ui.models.DateEvent
import com.anytypeio.anytype.feature_date.ui.models.StubVerticalItems
import com.anytypeio.anytype.feature_date.viewmodel.UiContentState
import com.anytypeio.anytype.feature_date.viewmodel.UiObjectsListItem
import com.anytypeio.anytype.feature_date.viewmodel.UiObjectsListState
import kotlinx.coroutines.launch

@Composable
fun ObjectsScreen(
    state: UiObjectsListState,
    uiState: UiContentState,
    canPaginate: Boolean,
    onDateEvent: (DateEvent) -> Unit
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
            onDateEvent(DateEvent.ObjectsList.OnLoadMore)
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
                    ListItem(
                        modifier = Modifier
                            .noRippleThrottledClickable {
                                onDateEvent(DateEvent.ObjectsList.OnObjectClicked(item))
                            },
                        item = item
                    )
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
private fun ListItem(
    modifier: Modifier,
    item: UiObjectsListItem.Item
) {
    val name = item.name.trim().ifBlank { stringResource(R.string.untitled) }
    val createdBy = item.createdBy
    val typeName = item.typeName
    ListItem(
        colors = ListItemDefaults.colors(
            containerColor = colorResource(id = R.color.background_primary),
        ),
        modifier = modifier
            .height(72.dp)
            .fillMaxWidth(),
        headlineContent = {
            Text(
                text = name,
                style = PreviewTitle2Regular,
                color = colorResource(id = R.color.text_primary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Row {
                if (typeName != null) {
                    Text(
                        text = typeName,
                        style = Relations3,
                        color = colorResource(id = R.color.text_secondary),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (!createdBy.isNullOrBlank()) {
                    Text(
                        text = "${stringResource(R.string.date_layout_item_created_by)} • $createdBy",
                        style = Relations3,
                        color = colorResource(id = R.color.text_secondary),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        leadingContent = {
            ListWidgetObjectIcon(icon = item.icon, modifier = Modifier, iconSize = 48.dp)
        }
    )
}


@Composable
private fun ListItemLoading(
    modifier: Modifier
) {
    ListItem(
        colors = ListItemDefaults.colors(
            containerColor = colorResource(id = R.color.background_primary),
        ),
        modifier = modifier
            .height(72.dp)
            .fillMaxWidth(),
        headlineContent = {
            ShimmerEffect(
                modifier = Modifier
                    .width(164.dp)
                    .height(18.dp)
            )
        },
        supportingContent = {
            ShimmerEffect(
                modifier = Modifier
                    .width(64.dp)
                    .height(13.dp)
            )
        },
        leadingContent = {
            ShimmerEffect(
                modifier = Modifier
                    .size(48.dp)
            )
        }
    )
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
        onDateEvent = {}
    )
}