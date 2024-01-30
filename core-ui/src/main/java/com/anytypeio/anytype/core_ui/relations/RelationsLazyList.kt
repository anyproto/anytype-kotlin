package com.anytypeio.anytype.core_ui.relations

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.views.UXBody
import com.anytypeio.anytype.core_ui.widgets.SearchField
import com.anytypeio.anytype.presentation.relations.RelationValueView
import com.anytypeio.anytype.presentation.relations.value.tagstatus.TagStatusAction
import com.anytypeio.anytype.presentation.relations.value.tagstatus.TagStatusViewState

@Composable
fun RelationsValueScreen(
    state: TagStatusViewState,
    action: (TagStatusAction) -> Unit
) {
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
            Header(state = state, action = action)
            SearchField(
                onFocused = {},
                onQueryChanged = { s -> }
            )
            Divider(paddingEnd = 0.dp, paddingStart = 0.dp)
            RelationsLazyList(state = state, action = action)
        }
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
        is TagStatusViewState.Empty -> RelationsViewEmpty()
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
//                    is RelationValueView.Create -> ItemTagOrStatusCreate(state = item)
//                    is RelationValueView.Option.Status -> StatusItem(state = item)
                    is RelationValueView.Option.Tag -> TagItem(state = item, action = action)
                    else -> TODO()
                }
            })
    }
}

@Composable
fun RelationsViewEmpty() {
    // TODO
}

@Composable
fun RelationsViewLoading() {
    // TODO
}

private fun isClearButtonVisible(state: TagStatusViewState): Boolean {
    if (state !is TagStatusViewState.Content) return false
    return state.items.any { it is RelationValueView.Option.Tag && it.isSelected } && state.isRelationEditable
}

private fun isPlusButtonVisible(state: TagStatusViewState): Boolean {
    return when (state)  {
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

@Preview(backgroundColor = 0xFFFFFFFF, showBackground = true)
@Composable
fun MyWidgetHeader() {
    Header(state = TagStatusViewState.Content(
        isRelationEditable = true,
        title = "Tags",
        items = listOf(
            RelationValueView.Option.Tag(
                name = "Urgent",
                color = "red",
                //number = 1,
                isSelected = true,
                id = "1",
                removable = false,
                isCheckboxShown = false
            ),
            RelationValueView.Option.Tag(
                name = "Personal",
                color = "orange",
                //number = 1,
                isSelected = false,
                id = "1",
                removable = false,
                isCheckboxShown = false
            ),
            RelationValueView.Create(
                name = "Done"
            )
        )
    ), action = {})
}