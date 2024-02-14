package com.anytypeio.anytype.core_ui.relations

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.AlertConfig
import com.anytypeio.anytype.core_ui.foundation.AlertDescription
import com.anytypeio.anytype.core_ui.foundation.AlertIcon
import com.anytypeio.anytype.core_ui.foundation.AlertTitle
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.GRADIENT_TYPE_RED
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyCalloutMedium
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.views.UXBody
import com.anytypeio.anytype.core_ui.widgets.SearchField
import com.anytypeio.anytype.presentation.relations.value.tagstatus.RelationsListItem
import com.anytypeio.anytype.presentation.relations.value.tagstatus.TagStatusAction
import com.anytypeio.anytype.presentation.relations.value.tagstatus.TagStatusViewState

@Composable
fun TagOrStatusValueScreen(
    state: TagStatusViewState,
    action: (TagStatusAction) -> Unit,
    onQueryChanged: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .nestedScroll(rememberNestedScrollInteropConnection())
            .fillMaxWidth()
            .wrapContentHeight()
            .background(
                color = colorResource(id = R.color.background_secondary),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 20.dp)
        ) {
            Header(state = state, action = action)
            Search(state = state, onQueryChanged = onQueryChanged)
            RelationsLazyList(state = state, action = action)
        }
    }
}

@Composable
private fun Search(
    state: TagStatusViewState,
    onQueryChanged: (String) -> Unit
) {
    when (state) {
        is TagStatusViewState.Content -> {
            if (state.isRelationEditable) {
                SearchField(
                    onFocused = {},
                    onQueryChanged = onQueryChanged
                )
                Divider(paddingEnd = 0.dp, paddingStart = 0.dp)
            }
        }
        is TagStatusViewState.Empty -> {
            if (state.isRelationEditable) {
                SearchField(
                    onFocused = {},
                    onQueryChanged = onQueryChanged
                )
                Divider(paddingEnd = 0.dp, paddingStart = 0.dp)
            }
        }
        else -> { /* Do nothing */}
    }
}

@Composable
private fun Header(state: TagStatusViewState, action: (TagStatusAction) -> Unit) {

    // Dragger at the top, centered
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Dragger(modifier = Modifier.align(Alignment.Center))
    }

    // Main content box
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        if (isClearButtonVisible(state = state)) {
            // Left-aligned CLEAR button
            Box(modifier = Modifier
                .wrapContentWidth()
                .fillMaxHeight()
                .align(Alignment.CenterStart)
                .noRippleClickable { action(TagStatusAction.Clear) }
            ) {
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 16.dp, end = 16.dp),
                    text = stringResource(id = R.string.clear),
                    style = UXBody,
                    color = colorResource(R.color.glyph_active),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Centered, ellipsized RELATION name
        Text(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 74.dp),
            text = getTitle(state = state),
            style = Title1.copy(),
            color = colorResource(R.color.text_primary),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )

        // Right-aligned PLUS button
        if (isPlusButtonVisible(state = state)) {
            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .fillMaxHeight()
                    .align(Alignment.CenterEnd)
                    .noRippleThrottledClickable {
                        action.invoke(TagStatusAction.Plus)
                    }
            ) {
                Image(
                    modifier = Modifier
                        .wrapContentSize()
                        .align(Alignment.Center)
                        .padding(end = 20.dp, start = 20.dp),
                    painter = painterResource(id = R.drawable.ic_default_plus),
                    contentDescription = "plus button",
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
fun RelationsLazyList(state: TagStatusViewState, action: (TagStatusAction) -> Unit) {
    when (state) {
        is TagStatusViewState.Content -> RelationsViewContent(state = state, action = action)
        is TagStatusViewState.Empty -> RelationsViewEmpty(state = state, action = action)
        is TagStatusViewState.Loading -> RelationsViewLoading()
    }
}

@Composable
fun RelationsViewContent(
    state: TagStatusViewState.Content,
    action: (TagStatusAction) -> Unit
) {
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
                    is RelationsListItem.Item.Tag -> TagItem(item, action)
                    is RelationsListItem.Item.Status -> StatusItem(item, action)
                    is RelationsListItem.CreateItem.Status -> ItemTagOrStatusCreate(item, action)
                    is RelationsListItem.CreateItem.Tag -> ItemTagOrStatusCreate(item, action)
                }
            })
    }
}

@Composable
fun RelationsViewEmpty(
    state: TagStatusViewState.Empty,
    action: (TagStatusAction) -> Unit
) {
    val icon = AlertConfig.Icon(
        GRADIENT_TYPE_RED,
        icon = R.drawable.ic_alert_error
    )
    if (state.isRelationEditable) {
        Column {
            Spacer(modifier = Modifier.height(154.dp))
            AlertIcon(icon)
            Spacer(modifier = Modifier.height(12.dp))
            AlertTitle(
                title = stringResource(id = R.string.options_empty_title),
                style = BodyCalloutMedium
            )
            AlertDescription(
                description = stringResource(id = R.string.options_empty_description),
                style = BodyCalloutMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            ButtonSecondary(
                text = stringResource(id = R.string.create),
                onClick = { action(TagStatusAction.Create) },
                size = ButtonSize.Small,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    } else {
        Column {
            Spacer(modifier = Modifier.height(87.dp))
            AlertIcon(icon)
            Spacer(modifier = Modifier.height(12.dp))
            AlertTitle(
                title = stringResource(id = R.string.options_empty_not_editable),
                style = BodyCalloutMedium
            )
        }
    }
}

@Composable
fun RelationsViewLoading() {
    // TODO
}

private fun isClearButtonVisible(state: TagStatusViewState): Boolean {
    if (state !is TagStatusViewState.Content) return false
    return state.items.any {
        it is RelationsListItem.Item.Tag && it.isSelected
                || it is RelationsListItem.Item.Status && it.isSelected
    } && state.isRelationEditable
}

private fun isPlusButtonVisible(state: TagStatusViewState): Boolean {
    return when (state) {
        is TagStatusViewState.Content -> state.isRelationEditable
        is TagStatusViewState.Empty -> state.isRelationEditable
        is TagStatusViewState.Loading -> false
    }
}

private fun getTitle(state: TagStatusViewState): String {
    return when (state) {
        is TagStatusViewState.Content -> state.title
        is TagStatusViewState.Empty -> state.title
        is TagStatusViewState.Loading -> ""
    }
}