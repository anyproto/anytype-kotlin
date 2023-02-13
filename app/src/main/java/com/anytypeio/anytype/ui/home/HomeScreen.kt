package com.anytypeio.anytype.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.presentation.widgets.TreePath
import com.anytypeio.anytype.presentation.widgets.WidgetView
import com.anytypeio.anytype.ui.widgets.menu.DropDownMenuAction
import com.anytypeio.anytype.ui.widgets.menu.WidgetActionButton
import com.anytypeio.anytype.ui.widgets.types.LinkWidgetCard
import com.anytypeio.anytype.ui.widgets.types.TreeWidgetCard

@Composable
fun HomeScreen(
    widgets: List<WidgetView>,
    onExpand: (TreePath) -> Unit,
    onWidgetObjectClicked: (ObjectWrapper.Basic) -> Unit,
    onCreateWidget: () -> Unit,
    onEditWidgets: () -> Unit,
    onRefresh: () -> Unit,
    onWidgetMenuAction: (Id, DropDownMenuAction) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .padding(top = 12.dp)
    ) {
        items(
            items = widgets, itemContent = { item ->
                when (item) {
                    is WidgetView.Tree -> {
                        TreeWidgetCard(
                            item = item,
                            onExpandElement = onExpand,
                            onDropDownMenuAction = { action ->
                                onWidgetMenuAction(item.id, action)
                            },
                            onWidgetObjectClicked = onWidgetObjectClicked
                        )
                    }
                    is WidgetView.Link -> {
                        LinkWidgetCard(
                            item = item,
                            onDropDownMenuAction = { action ->
                                onWidgetMenuAction(item.id, action)
                            },
                            onWidgetObjectClicked = onWidgetObjectClicked
                        )
                    }
                    is WidgetView.Action.CreateWidget -> {
                        Box(Modifier.fillMaxWidth()) {
                            WidgetActionButton(
                                label = stringResource(R.string.create_widget),
                                onClick = onCreateWidget,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                    is WidgetView.Action.EditWidgets -> {
                        Box(Modifier.fillMaxWidth()) {
                            WidgetActionButton(
                                label = stringResource(R.string.edit_widgets),
                                onClick = onEditWidgets,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                    is WidgetView.Action.Refresh -> {
                        Box(Modifier.fillMaxWidth()) {
                            WidgetActionButton(
                                label = "Refresh (for testing)",
                                onClick = onRefresh,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
            }
        )
    }
}