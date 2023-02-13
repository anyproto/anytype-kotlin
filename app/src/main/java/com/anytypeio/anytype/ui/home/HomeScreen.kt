package com.anytypeio.anytype.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.widgets.TreePath
import com.anytypeio.anytype.presentation.widgets.WidgetView

@Composable
fun HomeScreen(
    widgets: List<WidgetView>,
    onExpand: (TreePath) -> Unit,
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
                            onExpand = onExpand,
                            onDropDownMenuAction = { action ->
                                onWidgetMenuAction(item.id, action)
                            }
                        )
                    }
                    is WidgetView.Link -> {
                        LinkWidgetCard(
                            item = item,
                            onDropDownMenuAction = { action ->
                                onWidgetMenuAction(item.id, action)
                            }
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LinkWidgetCard(
    item: WidgetView.Link,
    onDropDownMenuAction: (DropDownMenuAction) -> Unit
) {
    val isDropDownMenuExpanded = remember {
        mutableStateOf(false)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .combinedClickable(
                    onClick = {},
                    onLongClick = {
                        isDropDownMenuExpanded.value = !isDropDownMenuExpanded.value
                    }
                )
        ) {
            Text(
                text = item.obj.name.orEmpty().trim(),
                style = MaterialTheme.typography.h6,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp)
            )
        }
        WidgetMenu(
            isExpanded = isDropDownMenuExpanded,
            onDropDownMenuAction = onDropDownMenuAction
        )
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun TreeWidgetCard(
    item: WidgetView.Tree,
    onExpand: (TreePath) -> Unit,
    onDropDownMenuAction: (DropDownMenuAction) -> Unit
) {
    val isDropDownMenuExpanded = remember {
        mutableStateOf(false)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            Modifier
                .padding(16.dp)
                .combinedClickable(
                    onClick = {},
                    onLongClick = {
                        isDropDownMenuExpanded.value = !isDropDownMenuExpanded.value
                    }
                )
        ) {
            TreeWidgetHeader(item)
            TreeWidgetTreeItem(item, onExpand)
        }
        WidgetMenu(
            isExpanded = isDropDownMenuExpanded,
            onDropDownMenuAction = onDropDownMenuAction
        )
    }
}

@Composable
private fun TreeWidgetTreeItem(
    item: WidgetView.Tree,
    onExpand: (TreePath) -> Unit
) {
    item.elements.forEachIndexed { idx, element ->
        Row(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .clickable {
                    onExpand(element.path)
                }
        ) {
            if (element.indent > 0) {
                Spacer(
                    Modifier.width(TreeWidgetTreeItemDefaults.Indent.times(element.indent))
                )
            }
            when (val icon = element.icon) {
                is WidgetView.Tree.Icon.Branch -> {
                    Image(
                        painterResource(R.drawable.ic_widget_tree_expand),
                        contentDescription = "Expand icon",
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .rotate(
                                if (icon.isExpanded)
                                    ArrowIconDefaults.Expanded
                                else
                                    ArrowIconDefaults.Collapsed
                            )

                    )
                }
                is WidgetView.Tree.Icon.Leaf -> {
                    Image(
                        painterResource(R.drawable.ic_widget_tree_dot),
                        contentDescription = "Dot icon",
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
                is WidgetView.Tree.Icon.Set -> {
                    Image(
                        painterResource(R.drawable.ic_widget_tree_set),
                        contentDescription = "Set icon",
                        modifier = Modifier.align(Alignment.CenterVertically)

                    )
                }
            }
            Text(
                text = element.obj.name?.trim() ?: "Untitled",
                modifier = Modifier.padding(start = 8.dp),
                maxLines = 1
            )
        }
        if (idx != item.elements.lastIndex) {
            Divider(
                thickness = 0.5.dp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
private fun WidgetMenu(
    isExpanded: MutableState<Boolean>,
    onDropDownMenuAction: (DropDownMenuAction) -> Unit
) {
    DropdownMenu(
        expanded = isExpanded.value,
        onDismissRequest = { isExpanded.value = false }
    ) {
        DropdownMenuItem(
            onClick = {
                onDropDownMenuAction(DropDownMenuAction.ChangeWidgetSource).also {
                    isExpanded.value = false
                }
            }
        ) {
            Text(stringResource(R.string.widget_change_source))
        }
        Divider(thickness = 0.5.dp)
        DropdownMenuItem(
            onClick = {
                onDropDownMenuAction(DropDownMenuAction.ChangeWidgetType).also {
                    isExpanded.value = false
                }
            }
        ) {
            Text(stringResource(R.string.widget_change_type))
        }
        Divider(thickness = 0.5.dp)
        DropdownMenuItem(
            onClick = {
                onDropDownMenuAction(DropDownMenuAction.RemoveWidget).also {
                    isExpanded.value = false
                }
            }
        ) {
            Text(stringResource(id = R.string.remove_widget))
        }
    }
}

@Composable
private fun TreeWidgetHeader(item: WidgetView.Tree) {
    Box(Modifier.fillMaxWidth()) {
        Text(
            // TODO trimming should be a part of presentation module.
            text = item.obj.name.orEmpty().trim(),
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Image(
            painterResource(R.drawable.ic_widget_tree_expand),
            contentDescription = "Expand icon",
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .rotate(90f)
        )
    }
}

@Composable
fun WidgetActionButton(
    modifier: Modifier,
    label: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = colorResource(id = R.color.text_primary),
            contentColor = colorResource(id = R.color.widget_button)
        ),
        modifier = modifier,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text = label)
    }
}

@Immutable
private object ArrowIconDefaults {
    const val Collapsed = 0f
    const val Expanded = 90f
}

@Immutable
private object TreeWidgetTreeItemDefaults {
    val Indent = 20.dp
}

sealed class DropDownMenuAction {
    object ChangeWidgetType : DropDownMenuAction()
    object ChangeWidgetSource : DropDownMenuAction()
    object RemoveWidget : DropDownMenuAction()
    object EditWidgets : DropDownMenuAction()
}