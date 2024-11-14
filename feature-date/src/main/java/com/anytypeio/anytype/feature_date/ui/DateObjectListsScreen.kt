package com.anytypeio.anytype.feature_date.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.primitives.RelationKey
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.common.ShimmerEffect
import com.anytypeio.anytype.core_ui.extensions.swapList
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Regular
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.core_ui.views.animations.DotsLoadingIndicator
import com.anytypeio.anytype.core_ui.views.animations.FadeAnimationSpecs
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.feature_date.R
import com.anytypeio.anytype.feature_date.models.DateObjectHorizontalListState
import com.anytypeio.anytype.feature_date.models.DateObjectVerticalListState
import com.anytypeio.anytype.feature_date.models.UiContentState
import com.anytypeio.anytype.feature_date.models.UiHorizontalListItem
import com.anytypeio.anytype.feature_date.models.UiVerticalListItem
import kotlinx.coroutines.launch

@Composable
fun DateLayoutHorizontalListScreen(
    state: DateObjectHorizontalListState,
    action: (UiHorizontalListItem) -> Unit
) {

    val items = remember { mutableStateListOf<UiHorizontalListItem>() }
    items.swapList(state.items)

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 24.dp)
    ) {
        items(
            count = items.size,
            key = { items[it].id },
            contentType = { index ->
                when (items[index]) {
                    is UiHorizontalListItem.Settings -> "settings"
                    is UiHorizontalListItem.Item -> "item"
                    is UiHorizontalListItem.Loading -> "loading"
                }
            }
        ) {
            val item = items[it]
            val background = if (state.selectedRelationKey?.key == item.id) {
                colorResource(R.color.shape_secondary)
            } else {
                Color.Transparent
            }
            Box(
                modifier = Modifier
                    .height(40.dp)
                    .wrapContentWidth()
                    .background(
                        color = background,
                        shape = RoundedCornerShape(size = 10.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = colorResource(R.color.shape_primary),
                        shape = RoundedCornerShape(size = 10.dp)
                    )
                    .noRippleThrottledClickable {
                        action(item)
                    }
            ) {
                when (item) {
                    is UiHorizontalListItem.Settings -> {
                        Image(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .size(24.dp)
                                .align(Alignment.Center),
                            painter = painterResource(R.drawable.ic_burger_24),
                            contentDescription = "List of date relations"
                        )
                    }

                    is UiHorizontalListItem.Item -> {
                        Text(
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .wrapContentSize()
                                .align(Alignment.Center),
                            text = item.title,
                            color = colorResource(R.color.text_primary),
                            style = PreviewTitle2Medium
                        )
                    }
                    is UiHorizontalListItem.Loading.Item -> {
                        ShimmerEffect(
                            modifier = Modifier
                                .padding(8.dp)
                                .align(Alignment.Center)
                                .width(88.dp)
                                .height(20.dp)
                        )
                    }
                    is UiHorizontalListItem.Loading.Settings -> {
                        ShimmerEffect(
                            modifier = Modifier
                                .padding(8.dp)
                                .align(Alignment.Center)
                                .size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
@DefaultPreviews
fun DateLayoutHorizontalListScreenPreview() {
    DateLayoutHorizontalListScreen(
        state = DateObjectHorizontalListState(
            items = listOf(
                UiHorizontalListItem.Settings(),
                UiHorizontalListItem.Item(
                    id = "1",
                    title = "Today",
                    key = RelationKey("1")
                ),
                UiHorizontalListItem.Item(
                    id = "2",
                    title = "Tomorrow",
                    key = RelationKey("2")
                )
            ),
            selectedRelationKey = RelationKey("1")
        ),
        action = {}
    )
}

@Composable
@DefaultPreviews
fun DateLayoutHorizontalListScreenLoadingPreview() {
    DateLayoutHorizontalListScreen(
        state = DateObjectHorizontalListState.loadingState(),
        action = {}
    )
}

@Composable
@DefaultPreviews
fun DateLayoutVerticalListScreenPreview() {
    val contentListState = DateObjectVerticalListState(
        items = StubVerticalItems
    )
    DateLayoutVerticalListScreen(
        state = contentListState,
        uiContentState = UiContentState.Idle(scrollToTop = false),
        canPaginate = true,
        onUpdateLimitSearch = {},
        uiVerticalListActions = {}
    )
}

@Composable
fun DateLayoutVerticalListScreen(
    state: DateObjectVerticalListState,
    uiContentState: UiContentState,
    canPaginate: Boolean,
    onUpdateLimitSearch: () -> Unit,
    uiVerticalListActions: (UiVerticalListItem) -> Unit
) {
    val items = remember { mutableStateListOf<UiVerticalListItem>() }
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
        if (shouldStartPaging.value && uiContentState is UiContentState.Idle) {
            onUpdateLimitSearch()
        }
    }

    LazyColumn(
        modifier = Modifier
            .padding(top = 8.dp)
            .fillMaxSize(),
        state = lazyListState
    ) {
        if (uiContentState is UiContentState.Empty) {
            item {
                Box(
                    modifier = Modifier
                        .fillParentMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState()
                }
            }
        }
        if (uiContentState is UiContentState.Error) {
            item {
                Box(
                    modifier = Modifier
                        .fillParentMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    ErrorState(uiContentState.message)
                }
            }
        }
        items(
            count = items.size,
            key = { index -> items[index].id },
            contentType = { index ->
                when (items[index]) {
                    is UiVerticalListItem.Loading -> "loading"
                    is UiVerticalListItem.Item -> "item"
                }
            }
        ) { index ->
            val item = items[index]
            when (item) {
                is UiVerticalListItem.Item -> {
                    ListItem(
                        modifier = Modifier.clickable { uiVerticalListActions(item) },
                        item = item
                    )
                }
                is UiVerticalListItem.Loading -> {
                    ListItemLoading(modifier = Modifier)
                }
            }
        }
        if (uiContentState is UiContentState.Paging) {
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

    LaunchedEffect(key1 = uiContentState) {
        if (uiContentState is UiContentState.Idle) {
            if (uiContentState.scrollToTop) {
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
    item: UiVerticalListItem.Item
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
                        text = " • $createdBy",
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