package com.anytypeio.anytype.core_ui.relations

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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
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
import com.anytypeio.anytype.core_ui.views.BodyCalloutMedium
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.views.UXBody
import com.anytypeio.anytype.core_ui.widgets.SearchField
import com.anytypeio.anytype.presentation.relations.value.`object`.ObjectValueItem
import com.anytypeio.anytype.presentation.relations.value.`object`.ObjectValueItemAction
import com.anytypeio.anytype.presentation.relations.value.`object`.ObjectValueViewState

@Composable
fun RelationObjectValueScreen(
    state: ObjectValueViewState,
    action: (ObjectValueItemAction) -> Unit,
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
            when (state) {
                is ObjectValueViewState.Content -> RelationsViewContent(
                    state = state,
                    action = action
                )
                is ObjectValueViewState.Empty -> RelationsViewEmpty(state = state, action = action)
                is ObjectValueViewState.Loading -> RelationsViewLoading()
            }
        }
    }
}

@Composable
fun RelationsViewContent(
    state: ObjectValueViewState.Content,
    action: (ObjectValueItemAction) -> Unit
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
                    is ObjectValueItem.Object -> ObjectItem(item, action, state.isEditableRelation)
                    is ObjectValueItem.ObjectType -> ObjectTypeItem(item)
                }
            })
    }
}

@Composable
private fun Search(
    state: ObjectValueViewState,
    onQueryChanged: (String) -> Unit
) {
    if (state.isEditableRelation) {
        SearchField(
            onFocused = {},
            onQueryChanged = onQueryChanged
        )
        Divider(paddingEnd = 0.dp, paddingStart = 0.dp)
    }
}

@Composable
private fun Header(state: ObjectValueViewState, action: (ObjectValueItemAction) -> Unit) {

    // Dragger at the top, centered
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Dragger(modifier = Modifier.align(Alignment.Center))
    }

    // Main content box
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .verticalScroll(rememberScrollState())
    ) {
        if (isClearButtonVisible(state = state)) {
            // Left-aligned CLEAR button
            Box(modifier = Modifier
                .wrapContentWidth()
                .fillMaxHeight()
                .align(Alignment.CenterStart)
                .noRippleClickable { action(ObjectValueItemAction.Clear) }
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
    }
}

@Composable
fun RelationsViewEmpty(
    state: ObjectValueViewState.Empty,
    action: (ObjectValueItemAction) -> Unit
) {
    val icon = AlertConfig.Icon(
        GRADIENT_TYPE_RED,
        icon = R.drawable.ic_alert_error
    )
    if (state.isEditableRelation) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(154.dp))
            AlertIcon(icon)
            Spacer(modifier = Modifier.height(12.dp))
            AlertTitle(
                title = stringResource(id = R.string.object_values_empty_title),
                style = BodyCalloutMedium
            )
            AlertDescription(
                description = stringResource(id = R.string.object_values_empty_description),
                style = BodyCalloutMedium
            )
        }
    } else {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
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

private fun isClearButtonVisible(state: ObjectValueViewState): Boolean {
    if (state !is ObjectValueViewState.Content) return false
    return state.items.any {
        it is ObjectValueItem.Object && it.isSelected
    } && state.isEditableRelation
}

private fun getTitle(state: ObjectValueViewState): String {
    return when (state) {
        is ObjectValueViewState.Content -> state.title
        is ObjectValueViewState.Empty -> state.title
        is ObjectValueViewState.Loading -> ""
    }
}