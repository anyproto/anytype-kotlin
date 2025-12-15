package com.anytypeio.anytype.core_ui.relations

import android.view.HapticFeedbackConstants
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.extensions.swapList
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyCalloutMedium
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.views.UXBody
import com.anytypeio.anytype.core_ui.widgets.SearchField
import com.anytypeio.anytype.presentation.relations.value.tagstatus.RelationsListItem
import com.anytypeio.anytype.presentation.relations.value.tagstatus.TagStatusAction
import com.anytypeio.anytype.presentation.relations.value.tagstatus.TagStatusViewState
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun TagOrStatusValueScreen(
    state: TagStatusViewState,
    query: String,
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
            SearchField(
                query = query,
                enabled = state !is TagStatusViewState.Loading,
                onFocused = {},
                onQueryChanged = onQueryChanged
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
            .verticalScroll(rememberScrollState())
    ) {
        if (isClearButtonVisible(state = state)) {
            // Left-aligned CLEAR button
            Box(
                modifier = Modifier
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
    val view = LocalView.current
    val lazyListState = rememberLazyListState()

    if (state.isRelationEditable) {
        // Use reorderable list for both Tags and Status
        val items = remember { mutableStateListOf<RelationsListItem.Item>() }
        items.swapList(state.items)

        // Track the original dragged item's ID (not index) to handle multiple callback invocations
        val draggedItemId = remember { mutableStateOf<String?>(null) }

        val onDragStoppedHandler = {
            val originalItemId = draggedItemId.value
            if (originalItemId != null) {
                // Find original index from state.items (unchanged during drag)
                val originalIndex = state.items.indexOfFirst { it.optionId == originalItemId }
                // Find new index in reordered items list
                val newIndex = items.indexOfFirst { it.optionId == originalItemId }

                if (originalIndex != -1 && newIndex != -1 && originalIndex != newIndex) {
                    action(TagStatusAction.OnMove(from = originalIndex, to = newIndex))
                }
            }
            draggedItemId.value = null
        }

        val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
            val fromId = from.key as? String
            val toId = to.key as? String
            if (fromId == null || toId == null) {
                return@rememberReorderableLazyListState
            }

            // Capture original dragged item on first move
            if (draggedItemId.value == null) {
                draggedItemId.value = fromId
            }

            // Find current indices by key
            val f = items.indexOfFirst { it.optionId == fromId }
            val t = items.indexOfFirst { it.optionId == toId }

            if (f != -1 && t != -1 && f != t) {
                val newList = items.toMutableList().apply {
                    add(t, removeAt(f))
                }
                items.swapList(newList)

                ViewCompat.performHapticFeedback(
                    view,
                    HapticFeedbackConstantsCompat.SEGMENT_FREQUENT_TICK
                )
            }
        }

        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize()
        ) {
            // Reorderable items
            items(
                count = items.size,
                key = { index -> items[index].optionId }
            ) { index ->
                val item = items[index]
                ReorderableItem(reorderableLazyListState, key = item.optionId) { isDragging ->
                    val currentItem = LocalView.current
                    if (isDragging) {
                        currentItem.isHapticFeedbackEnabled = true
                        currentItem.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    }
                    val dragHandleModifier = Modifier.longPressDraggableHandle(
                        onDragStarted = {
                            ViewCompat.performHapticFeedback(
                                view,
                                HapticFeedbackConstantsCompat.GESTURE_START
                            )
                        },
                        onDragStopped = {
                            ViewCompat.performHapticFeedback(
                                view,
                                HapticFeedbackConstantsCompat.GESTURE_END
                            )
                            onDragStoppedHandler()
                        }
                    )
                    when (item) {
                        is RelationsListItem.Item.Tag -> TagItem(
                            state = item,
                            action = action,
                            isEditable = state.isRelationEditable,
                            showDivider = true,
                            isDragging = isDragging,
                            dragHandleModifier = dragHandleModifier
                        )

                        is RelationsListItem.Item.Status -> StatusItem(
                            state = item,
                            action = action,
                            isEditable = state.isRelationEditable,
                            showDivider = true,
                            isDragging = isDragging,
                            dragHandleModifier = dragHandleModifier
                        )
                    }
                }
            }

            // CreateItem at the bottom (non-reorderable, only for Tags)
            state.createItem?.let { createItem ->
                item(key = "create_item") {
                    ItemTagOrStatusCreate(
                        state = createItem,
                        action = action
                    )
                }
            }
        }
    } else {
        // Regular list for read-only
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(
                items = state.items,
                itemContent = { index, item ->
                    val isLastItem = index == state.items.size - 1
                    when (item) {
                        is RelationsListItem.Item.Tag -> TagItem(
                            state = item,
                            action = action,
                            isEditable = state.isRelationEditable,
                            showDivider = !isLastItem
                        )

                        is RelationsListItem.Item.Status -> StatusItem(
                            state = item,
                            action = action,
                            isEditable = state.isRelationEditable,
                            showDivider = !isLastItem
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun RelationsViewEmpty(
    state: TagStatusViewState.Empty,
    action: (TagStatusAction) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        if (state.isRelationEditable) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.options_empty_title),
                    style = BodyCalloutMedium,
                    color = colorResource(id = R.color.text_primary)
                )
                Text(
                    text = stringResource(id = R.string.options_empty_description),
                    style = BodyCalloutRegular,
                    color = colorResource(id = R.color.text_secondary)
                )
                Spacer(modifier = Modifier.height(13.dp))
                ButtonSecondary(
                    text = stringResource(id = R.string.create),
                    onClick = { action(TagStatusAction.Create) },
                    size = ButtonSize.Small,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.options_empty_not_editable),
                    style = BodyCalloutMedium,
                    color = colorResource(id = R.color.text_primary)
                )
            }
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

@DefaultPreviews
@Composable
private fun TagOrStatusValueScreenEmptyEditablePreview() {
    MaterialTheme {
        TagOrStatusValueScreen(
            state = TagStatusViewState.Empty(
                title = "Assignee",
                isRelationEditable = true // Key change: user can add new options
            ),
            query = "",
            action = {},
            onQueryChanged = {}
        )
    }
}

@DefaultPreviews
@Composable
private fun TagOrStatusValueScreenEmptyReadOnlyPreview() {
    MaterialTheme {
        TagOrStatusValueScreen(
            state = TagStatusViewState.Empty(
                title = "Platform",
                isRelationEditable = false // Key change: user cannot add new options
            ),
            query = "",
            action = {},
            onQueryChanged = {}
        )
    }
}


@DefaultPreviews
@Composable
private fun TagOrStatusValueScreenPreview() {
    val items = listOf(
        RelationsListItem.Item.Tag(
            optionId = "1",
            name = "Urgent",
            isSelected = true,
            color = ThemeColor.RED,
            number = 10
        ),
        RelationsListItem.Item.Tag(
            optionId = "2",
            name = "In Progress",
            isSelected = false,
            color = ThemeColor.GREY,
            number = 9
        ),
        RelationsListItem.Item.Tag(
            optionId = "3",
            name = "Low Priority",
            isSelected = false,
            color = ThemeColor.YELLOW,
            number = 1
        )
    )

    val contentState = TagStatusViewState.Content(
        title = "Priority",
        isRelationEditable = true,
        items = items,
        createItem = null
    )

    MaterialTheme {
        TagOrStatusValueScreen(
            state = contentState,
            query = "",
            action = {},
            onQueryChanged = {}
        )
    }
}

@DefaultPreviews
@Composable
private fun StatusValueScreenPreview() {
    val items = listOf(
        RelationsListItem.Item.Status(
            optionId = "1",
            name = "Not Started",
            isSelected = false,
            color = ThemeColor.GREY
        ),
        RelationsListItem.Item.Status(
            optionId = "2",
            name = "In Progress",
            isSelected = true,
            color = ThemeColor.BLUE
        ),
        RelationsListItem.Item.Status(
            optionId = "3",
            name = "Completed",
            isSelected = false,
            color = ThemeColor.ORANGE
        )
    )

    val contentState = TagStatusViewState.Content(
        title = "Status",
        isRelationEditable = true,
        items = items,
        createItem = null
    )

    MaterialTheme {
        TagOrStatusValueScreen(
            state = contentState,
            query = "",
            action = {},
            onQueryChanged = {}
        )
    }
}