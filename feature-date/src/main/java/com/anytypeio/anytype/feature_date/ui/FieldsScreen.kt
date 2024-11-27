package com.anytypeio.anytype.feature_date.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.primitives.RelationKey
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.common.ShimmerEffect
import com.anytypeio.anytype.core_ui.extensions.swapList
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.feature_date.R
import com.anytypeio.anytype.feature_date.ui.models.DateEvent
import com.anytypeio.anytype.feature_date.ui.models.StubHorizontalItems
import com.anytypeio.anytype.feature_date.viewmodel.UiFieldsItem
import com.anytypeio.anytype.feature_date.viewmodel.UiFieldsState

@Composable
fun FieldsScreen(
    uiState: UiFieldsState,
    lazyListState: LazyListState,
    onDateEvent: (DateEvent) -> Unit
) {

    val items = remember { mutableStateListOf<UiFieldsItem>() }
    items.swapList(uiState.items)

    LazyRow(
        state = lazyListState,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 24.dp)
    ) {
        items(
            count = items.size,
            key = { items[it].id },
            contentType = { index ->
                when (items[index]) {
                    is UiFieldsItem.Settings -> "settings"
                    is UiFieldsItem.Item -> "item"
                    is UiFieldsItem.Loading -> "loading"
                }
            }
        ) {
            val item = items[it]
            val background = if (uiState.selectedRelationKey?.key == item.id) {
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
                        onDateEvent(DateEvent.OnFieldClick(item))
                    }
            ) {
                when (item) {
                    is UiFieldsItem.Settings -> {
                        Image(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .size(24.dp)
                                .align(Alignment.Center),
                            painter = painterResource(R.drawable.ic_burger_24),
                            contentDescription = "List of date relations"
                        )
                    }

                    is UiFieldsItem.Item.Mention -> {
                        Row(
                            modifier = Modifier
                                .fillParentMaxHeight()
                                .wrapContentWidth()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Image(
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .size(24.dp),
                                painter = painterResource(R.drawable.ic_mention_24),
                                contentDescription = "Mentioned in"
                            )
                            Text(
                                modifier = Modifier
                                    .wrapContentSize(),
                                text = stringResource(R.string.date_layout_mentioned_in),
                                color = colorResource(R.color.text_primary),
                                style = PreviewTitle2Medium
                            )
                        }
                    }

                    is UiFieldsItem.Item.Default -> {
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

                    is UiFieldsItem.Loading.Item -> {
                        ShimmerEffect(
                            modifier = Modifier
                                .padding(8.dp)
                                .align(Alignment.Center)
                                .width(88.dp)
                                .height(20.dp)
                        )
                    }

                    is UiFieldsItem.Loading.Settings -> {
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
fun FieldsScreenPreview() {
    val lazyListState = rememberLazyListState()
    FieldsScreen(
        uiState = UiFieldsState(
            items = StubHorizontalItems,
            selectedRelationKey = RelationKey("1")
        ),
        onDateEvent = {},
        lazyListState = lazyListState
    )
}

@Composable
@DefaultPreviews
fun LoadingPreview() {
    val lazyListState = rememberLazyListState()
    FieldsScreen(
        uiState = UiFieldsState.LoadingState,
        onDateEvent = {},
        lazyListState = lazyListState
    )
}