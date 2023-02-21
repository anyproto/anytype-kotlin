package com.anytypeio.anytype.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.presentation.home.InteractionMode
import com.anytypeio.anytype.presentation.widgets.TreePath
import com.anytypeio.anytype.presentation.widgets.ViewId
import com.anytypeio.anytype.presentation.widgets.WidgetId
import com.anytypeio.anytype.presentation.widgets.WidgetView
import com.anytypeio.anytype.ui.library.views.list.items.noRippleClickable
import com.anytypeio.anytype.ui.widgets.menu.DropDownMenuAction
import com.anytypeio.anytype.ui.widgets.menu.WidgetActionButton
import com.anytypeio.anytype.ui.widgets.types.LinkWidgetCard
import com.anytypeio.anytype.ui.widgets.types.ListWidgetCard
import com.anytypeio.anytype.ui.widgets.types.TreeWidgetCard

@Composable
fun HomeScreen(
    mode: InteractionMode,
    widgets: List<WidgetView>,
    onExpand: (TreePath) -> Unit,
    onWidgetObjectClicked: (ObjectWrapper.Basic) -> Unit,
    onCreateWidget: () -> Unit,
    onEditWidgets: () -> Unit,
    onRefresh: () -> Unit,
    onWidgetMenuAction: (WidgetId, DropDownMenuAction) -> Unit,
    onChangeWidgetView: (WidgetId, ViewId) -> Unit,
    onToggleExpandedWidgetState: (WidgetId) -> Unit,
    onExitEditMode: () -> Unit,
    onSearchClicked: () -> Unit,
    onCreateNewObjectClicked: () -> Unit,
    onSpaceClicked: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        WidgetList(
            widgets = widgets,
            onExpand = onExpand,
            onWidgetMenuAction = onWidgetMenuAction,
            onWidgetObjectClicked = onWidgetObjectClicked,
            onToggleExpandedWidgetState = onToggleExpandedWidgetState,
            mode = mode,
            onChangeWidgetView = onChangeWidgetView,
            onCreateWidget = onCreateWidget,
            onEditWidgets = onEditWidgets,
            onRefresh = onRefresh
        )
        if (mode is InteractionMode.Edit) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 26.dp, vertical = 34.dp)
                    .height(52.dp)
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                HomeScreenButton(
                    text = stringResource(id = R.string.add),
                    modifier = Modifier.weight(1f),
                    onClick = { onCreateWidget() }
                )
                Spacer(modifier = Modifier.width(10.dp))
                HomeScreenButton(
                    text = stringResource(id = R.string.done),
                    modifier = Modifier.weight(1f),
                    onClick = { onExitEditMode() }
                )
            }
        } else {
            HomeScreenBottomToolbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 34.dp),
                onSearchClicked = onSearchClicked,
                onCreateNewObjectClicked = onCreateNewObjectClicked,
                onSpaceClicked = onSpaceClicked
            )
        }
    }
}

@Composable
private fun WidgetList(
    widgets: List<WidgetView>,
    onExpand: (TreePath) -> Unit,
    onWidgetMenuAction: (WidgetId, DropDownMenuAction) -> Unit,
    onWidgetObjectClicked: (ObjectWrapper.Basic) -> Unit,
    onToggleExpandedWidgetState: (WidgetId) -> Unit,
    mode: InteractionMode,
    onChangeWidgetView: (WidgetId, ViewId) -> Unit,
    onCreateWidget: () -> Unit,
    onEditWidgets: () -> Unit,
    onRefresh: () -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        itemsIndexed(widgets) { index, item ->
            when (item) {
                is WidgetView.Tree -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = if (index == 0) 12.dp else 0.dp)
                    ) {
                        TreeWidgetCard(
                            item = item,
                            onExpandElement = onExpand,
                            onDropDownMenuAction = { action ->
                                onWidgetMenuAction(item.id, action)
                            },
                            onWidgetObjectClicked = onWidgetObjectClicked,
                            onToggleExpandedWidgetState = onToggleExpandedWidgetState,
                            mode = mode
                        )
                        if (mode is InteractionMode.Edit) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_remove_widget),
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .height(24.dp)
                                    .width(24.dp)
                                    .background(
                                        shape = CircleShape,
                                        color = Color.Gray
                                    )
                                    .align(Alignment.TopEnd)
                                    .noRippleClickable {
                                        onWidgetMenuAction(
                                            item.id, DropDownMenuAction.RemoveWidget
                                        )
                                    },
                                contentDescription = "Remove widget icon"
                            )
                        }
                    }
                }
                is WidgetView.Link -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = if (index == 0) 12.dp else 0.dp)
                    ) {
                        LinkWidgetCard(
                            item = item,
                            onDropDownMenuAction = { action ->
                                onWidgetMenuAction(item.id, action)
                            },
                            onWidgetObjectClicked = onWidgetObjectClicked,
                            isEditable = mode is InteractionMode.Edit
                        )
                        if (mode is InteractionMode.Edit) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_remove_widget),
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .height(24.dp)
                                    .width(24.dp)
                                    .background(
                                        shape = CircleShape,
                                        color = Color.Gray
                                    )
                                    .align(Alignment.TopEnd)
                                    .noRippleClickable {
                                        onWidgetMenuAction(
                                            item.id, DropDownMenuAction.RemoveWidget
                                        )
                                    },
                                contentDescription = "Remove widget icon"
                            )
                        }
                    }
                }
                is WidgetView.Set -> {
                    ListWidgetCard(
                        item = item,
                        onWidgetObjectClicked = onWidgetObjectClicked,
                        onDropDownMenuAction = { action ->
                            onWidgetMenuAction(item.id, action)
                        },
                        onChangeWidgetView = onChangeWidgetView,
                        onToggleExpandedWidgetState = onToggleExpandedWidgetState
                    )
                }
                is WidgetView.Action.CreateWidget -> {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        WidgetActionButton(
                            label = stringResource(R.string.create_widget),
                            onClick = onCreateWidget,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
                is WidgetView.Action.EditWidgets -> {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp, bottom = 128.dp)
                    ) {
                        if (mode is InteractionMode.Default) {
                            WidgetActionButton(
                                label = stringResource(R.string.edit_widgets),
                                onClick = onEditWidgets,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
                is WidgetView.Action.Refresh -> {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        WidgetActionButton(
                            label = "Refresh (for testing)",
                            onClick = onRefresh,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreenButton(
    text: String,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                color = colorResource(id = R.color.home_screen_button),
                shape = RoundedCornerShape(14.dp)
            )
            .height(52.dp)
            .noRippleClickable { onClick() }
    ) {
        Text(
            text = text,
            modifier = Modifier.align(Alignment.Center),
            style = TextStyle(
                fontSize = 17.sp,
                color = colorResource(id = R.color.text_white)
            )
        )
    }
}

@Composable
fun HomeScreenBottomToolbar(
    modifier: Modifier,
    onSearchClicked: () -> Unit,
    onCreateNewObjectClicked: () -> Unit,
    onSpaceClicked: () -> Unit
) {
    Row(
        modifier = modifier
            .height(52.dp)
            .width(216.dp)
            .background(
                shape = RoundedCornerShape(16.dp),
                color = colorResource(id = R.color.home_screen_button)
            )
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .noRippleClickable { onSearchClicked() }
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_home_widget_search),
                contentDescription = "Search icon",
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .noRippleClickable { onCreateNewObjectClicked() }
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_home_widget_plus),
                contentDescription = "Plus icon",
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .noRippleClickable { onSpaceClicked() }
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_home_widget_space),
                contentDescription = "Space icon",
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}