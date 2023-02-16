package com.anytypeio.anytype.ui.widgets.types

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.widgets.TreePath
import com.anytypeio.anytype.presentation.widgets.WidgetId
import com.anytypeio.anytype.presentation.widgets.WidgetView
import com.anytypeio.anytype.ui.library.views.list.items.noRippleClickable
import com.anytypeio.anytype.ui.widgets.menu.DropDownMenuAction
import com.anytypeio.anytype.ui.widgets.menu.WidgetMenu

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun TreeWidgetCard(
    item: WidgetView.Tree,
    onExpandElement: (TreePath) -> Unit,
    onWidgetObjectClicked: (ObjectWrapper.Basic) -> Unit,
    onDropDownMenuAction: (DropDownMenuAction) -> Unit,
    onToggleExpandedWidgetState: (WidgetId) -> Unit
) {
    val isDropDownMenuExpanded = remember {
        mutableStateOf(false)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 6.dp)
            .animateContentSize()
        ,
        shape = RoundedCornerShape(16.dp),
        onClick = {
            isDropDownMenuExpanded.value = !isDropDownMenuExpanded.value
        },
        backgroundColor = if (isDropDownMenuExpanded.value) {
            colorResource(id = R.color.shape_secondary)
        } else {
            colorResource(id = R.color.dashboard_card_background)
        }
    ) {
        Column(
            Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            WidgetHeader(
                item = item.obj,
                isDropDownMenuExpanded = isDropDownMenuExpanded,
                onWidgetObjectClicked = onWidgetObjectClicked,
                onExpandElement = { onToggleExpandedWidgetState(item.id) },
                isExpanded = item.isExpanded
            )
            if (item.elements.isNotEmpty()) {
                TreeWidgetTreeItems(
                    item = item,
                    onExpand = onExpandElement,
                    onWidgetElementClicked = onWidgetObjectClicked
                )
            } else {
                if (item.isExpanded) {
                    EmptyWidgetPlaceholder(R.string.empty_tree_widget)
                }
            }
        }
        WidgetMenu(
            isExpanded = isDropDownMenuExpanded,
            onDropDownMenuAction = onDropDownMenuAction
        )
    }
}

@Composable
private fun TreeWidgetTreeItems(
    item: WidgetView.Tree,
    onExpand: (TreePath) -> Unit,
    onWidgetElementClicked: (ObjectWrapper.Basic) -> Unit
) {
    item.elements.forEachIndexed { idx, element ->
        Row(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .noRippleClickable {
                    onWidgetElementClicked(element.obj)
                }
        ) {
            if (element.indent > 0) {
                Spacer(
                    Modifier.width(TreeWidgetTreeItemDefaults.Indent.times(element.indent))
                )
            }
            when (val icon = element.elementIcon) {
                is WidgetView.Tree.ElementIcon.Branch -> {
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
                            .noRippleClickable { onExpand(element.path) }

                    )
                }
                is WidgetView.Tree.ElementIcon.Leaf -> {
                    Image(
                        painterResource(R.drawable.ic_widget_tree_dot),
                        contentDescription = "Dot icon",
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
                is WidgetView.Tree.ElementIcon.Set -> {
                    Image(
                        painterResource(R.drawable.ic_widget_tree_set),
                        contentDescription = "Set icon",
                        modifier = Modifier.align(Alignment.CenterVertically)

                    )
                }
            }
            if (element.objectIcon != ObjectIcon.None && element.objectIcon !is ObjectIcon.Basic.Avatar) {
                TreeWidgetObjectIcon(
                    element = element,
                    paddingStart = 8.dp,
                    paddingEnd = 4.dp
                )
            }
            Text(
                text = element.obj.name?.trim() ?: stringResource(id = R.string.untitled),
                modifier = Modifier.padding(start = 8.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorResource(id = R.color.text_primary),
                )
            )
        }
        Divider(
            thickness = 0.5.dp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        if (idx == item.elements.lastIndex) {
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WidgetHeader(
    item: ObjectWrapper.Basic,
    isDropDownMenuExpanded: MutableState<Boolean>,
    onWidgetObjectClicked: (ObjectWrapper.Basic) -> Unit,
    onExpandElement: () -> Unit = {},
    isExpanded: Boolean = false,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(40.dp)) {
        Text(
            // TODO trimming should be a part of presentation module.
            text = item.name?.trim() ?: stringResource(id = R.string.untitled),
            style = TextStyle(
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.text_primary),
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 28.dp)
                .align(Alignment.CenterStart)
                .combinedClickable(
                    onClick = {
                        onWidgetObjectClicked(item)
                    },
                    onLongClick = {
                        isDropDownMenuExpanded.value = !isDropDownMenuExpanded.value
                    },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
        )
        Image(
            painterResource(R.drawable.ic_widget_tree_expand),
            contentDescription = "Expand icon",
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .rotate(if (isExpanded) 90f else 0f)
                .noRippleClickable { onExpandElement() }
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