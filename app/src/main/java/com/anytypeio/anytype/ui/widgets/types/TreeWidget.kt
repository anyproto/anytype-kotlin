package com.anytypeio.anytype.ui.widgets.types

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.presentation.widgets.TreePath
import com.anytypeio.anytype.presentation.widgets.WidgetView
import com.anytypeio.anytype.ui.widgets.menu.DropDownMenuAction
import com.anytypeio.anytype.ui.widgets.menu.WidgetMenu

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun TreeWidgetCard(
    item: WidgetView.Tree,
    onExpandElement: (TreePath) -> Unit,
    onWidgetObjectClicked: (ObjectWrapper.Basic) -> Unit,
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
            TreeWidgetHeader(
                item = item,
                isDropDownMenuExpanded = isDropDownMenuExpanded,
                onWidgetObjectClicked = onWidgetObjectClicked
            )
            TreeWidgetTreeItem(
                item = item,
                onExpand = onExpandElement,
                onWidgetElementClicked = onWidgetObjectClicked
            )
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
    onExpand: (TreePath) -> Unit,
    onWidgetElementClicked: (ObjectWrapper.Basic) -> Unit
) {
    item.elements.forEachIndexed { idx, element ->
        Row(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .clickable { onWidgetElementClicked(element.obj) }
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
                            ).clickable { onExpand(element.path) }

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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TreeWidgetHeader(
    item: WidgetView.Tree,
    isDropDownMenuExpanded: MutableState<Boolean>,
    onWidgetObjectClicked: (ObjectWrapper.Basic) -> Unit
) {
    Box(Modifier.fillMaxWidth()) {
        Text(
            // TODO trimming should be a part of presentation module.
            text = item.obj.name.orEmpty().trim(),
            style = MaterialTheme.typography.h6,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp, end = 28.dp)
                .combinedClickable(
                    onClick = {
                        onWidgetObjectClicked(item.obj)
                    },
                    onLongClick = {
                        isDropDownMenuExpanded.value = !isDropDownMenuExpanded.value
                    }
                )
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


@Immutable
private object TreeWidgetTreeItemDefaults {
    val Indent = 20.dp
}

@Immutable
private object ArrowIconDefaults {
    const val Collapsed = 0f
    const val Expanded = 90f
}