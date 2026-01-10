package com.anytypeio.anytype.ui.objects

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.extensions.throttledClick
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.EmptyState
import com.anytypeio.anytype.core_ui.foundation.Toolbar
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.core_ui.widgets.SearchField
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.ObjectTypeChangeItem
import com.anytypeio.anytype.presentation.objects.ObjectTypeChangeViewState

@Preview
@Composable
fun ObjectTypeChangeScreenPreview() {
    ObjectTypeChangeScreen(
        title = "Change type",
        state = ObjectTypeChangeViewState.Loading,
        onTypeClicked = {},
        onQueryChanged = {},
        onFocused = {}
    )
}

@Composable
fun ObjectTypeChangeScreen(
    title: String,
    state: ObjectTypeChangeViewState,
    onTypeClicked: (ObjectTypeChangeItem.Type) -> Unit,
    onQueryChanged: (String) -> Unit,
    onFocused: () -> Unit
) {
    Column(
        modifier = Modifier.nestedScroll(rememberNestedScrollInteropConnection())
    ) {
        Dragger(
            Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 6.dp)
                .verticalScroll(rememberScrollState())
        )
        Toolbar(title = title)
        SearchField(
            onQueryChanged = onQueryChanged,
            onFocused = onFocused
        )
        Spacer(modifier = Modifier.height(8.dp))
        ScreenContent(
            state = state,
            onTypeClicked = onTypeClicked
        )
    }
}

@Composable
private fun ScreenContent(
    state: ObjectTypeChangeViewState,
    onTypeClicked: (ObjectTypeChangeItem.Type) -> Unit
) {
    when (state) {
        is ObjectTypeChangeViewState.Content -> {
            FlowRowContent(
                items = state.items,
                onTypeClicked = onTypeClicked
            )
        }
        ObjectTypeChangeViewState.Empty -> {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(500)),
                exit = fadeOut(animationSpec = tween(500))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    EmptyState(
                        modifier = Modifier.align(Alignment.Center),
                        title = stringResource(id = R.string.nothing_found),
                        description = stringResource(id = R.string.nothing_found_object_types),
                        icon = R.drawable.ic_popup_duck_56
                    )
                }
            }
        }
        ObjectTypeChangeViewState.Loading -> {
            // Show nothing while loading
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun FlowRowContent(
    items: List<ObjectTypeChangeItem>,
    onTypeClicked: (ObjectTypeChangeItem.Type) -> Unit
) {
    FlowRow(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            when (item) {
                is ObjectTypeChangeItem.Section.Lists -> {
                    SectionHeader(title = stringResource(id = R.string.create_object_section_lists))
                }
                is ObjectTypeChangeItem.Section.Objects -> {
                    SectionHeader(title = stringResource(id = R.string.create_object_section_objects))
                }
                is ObjectTypeChangeItem.Type -> {
                    TypeItem(
                        name = item.name,
                        icon = item.icon,
                        isSelected = item.isSelected,
                        onItemClicked = throttledClick(
                            onClick = { onTypeClicked(item) }
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
    ) {
        Text(
            modifier = Modifier.align(Alignment.BottomStart),
            text = title,
            color = colorResource(id = R.color.text_secondary),
            style = Caption1Medium
        )
    }
}

@Composable
private fun TypeItem(
    name: String,
    icon: ObjectIcon,
    isSelected: Boolean,
    onItemClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(48.dp)
            .border(
                width = 1.dp,
                color = if (isSelected)
                    colorResource(id = R.color.palette_system_amber_50)
                else
                    colorResource(id = R.color.shape_primary),
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .clickable { onItemClicked() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(14.dp))
        ListWidgetObjectIcon(
            icon = icon,
            iconSize = 18.dp,
            modifier = Modifier,
            iconWithoutBackgroundMaxSize = 200.dp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = name,
            style = Title2,
            color = colorResource(id = R.color.text_primary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(end = 16.dp)
        )
    }
}
