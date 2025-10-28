package com.anytypeio.anytype.ui.widgets.types

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.presentation.home.InteractionMode
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.widgets.DropDownMenuAction
import com.anytypeio.anytype.presentation.widgets.TreePath
import com.anytypeio.anytype.presentation.widgets.Widget
import com.anytypeio.anytype.presentation.widgets.WidgetId
import com.anytypeio.anytype.presentation.widgets.WidgetView
import com.anytypeio.anytype.ui.widgets.menu.WidgetLongClickMenu
import com.anytypeio.anytype.ui.widgets.menu.WidgetMenuItem

@Composable
fun TreeWidgetCard(
    mode: InteractionMode,
    item: WidgetView.Tree,
    onExpandElement: (TreePath) -> Unit,
    onWidgetElementClicked: (ObjectWrapper.Basic) -> Unit,
    onWidgetSourceClicked: (WidgetId) -> Unit,
    onWidgetMenuClicked: (WidgetId) -> Unit,
    onDropDownMenuAction: (DropDownMenuAction) -> Unit,
    onToggleExpandedWidgetState: (WidgetId) -> Unit,
    onObjectCheckboxClicked: (Id, Boolean) -> Unit,
    onCreateElement: (WidgetView) -> Unit,
    menuItems: List<WidgetMenuItem> = emptyList(),
    isCardMenuExpanded: MutableState<Boolean> = mutableStateOf(false),
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Column(
            Modifier.padding(
                top = 6.dp,
                bottom = 6.dp,
            )
        ) {
            val (title, icon) = getTitleAndIcon(item, item.icon)
            WidgetHeader(
                title = title,
                icon = icon,
                isCardMenuExpanded = isCardMenuExpanded,
                onWidgetHeaderClicked = { onWidgetSourceClicked(item.id) },
                onExpandElement = { onToggleExpandedWidgetState(item.id) },
                isExpanded = item.isExpanded,
                isInEditMode = mode is InteractionMode.Edit,
                hasReadOnlyAccess = mode == InteractionMode.ReadOnly,
                onWidgetMenuTriggered = { onWidgetMenuClicked(item.id) },
                canCreateObject = item.canCreateObjectOfType,
                onCreateElement = { onCreateElement(item) },
                onObjectCheckboxClicked = { isChecked ->
                    onObjectCheckboxClicked(item.source.id, isChecked)
                }
            )
            if (item.elements.isNotEmpty()) {
                TreeWidgetTreeItems(
                    item = item,
                    mode = mode,
                    onExpand = onExpandElement,
                    onWidgetElementClicked = onWidgetElementClicked,
                    onObjectCheckboxClicked = onObjectCheckboxClicked
                )
            } else {
                if (item.isExpanded) {
                    EmptyWidgetPlaceholder(R.string.empty_list_widget_no_objects)
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }
        WidgetLongClickMenu(
            menuItems = menuItems,
            isCardMenuExpanded = isCardMenuExpanded,
            onDropDownMenuAction = onDropDownMenuAction
        )
    }
}

@Composable
private fun TreeWidgetTreeItems(
    mode: InteractionMode,
    item: WidgetView.Tree,
    onExpand: (TreePath) -> Unit,
    onWidgetElementClicked: (ObjectWrapper.Basic) -> Unit,
    onObjectCheckboxClicked: (Id, Boolean) -> Unit
) {
    item.elements.forEachIndexed { idx, element ->
        Row(
            modifier = Modifier
                .padding(vertical = 10.dp, horizontal = 16.dp)
                .then(
                    if (mode !is InteractionMode.Edit)
                        Modifier.noRippleClickable { onWidgetElementClicked(element.obj) }
                    else
                        Modifier
                ),
            verticalAlignment = Alignment.CenterVertically
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
                            .then(
                                if (mode !is InteractionMode.Edit)
                                    Modifier.noRippleClickable { onExpand(element.path) }
                                else
                                    Modifier
                            )
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
                is WidgetView.Tree.ElementIcon.Collection -> {
                    Image(
                        painterResource(R.drawable.ic_widget_tree_collection),
                        contentDescription = "Collection icon",
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
            if (element.objectIcon != ObjectIcon.None) {
                ListWidgetObjectIcon(
                    iconSize = 18.dp,
                    icon = element.objectIcon,
                    iconWithoutBackgroundMaxSize = 200.dp,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(start = 8.dp, end = 4.dp),
                    onTaskIconClicked = { isChecked ->
                        onObjectCheckboxClicked(element.id, isChecked)
                    }
                )
            }
            Text(
                text = element.getPrettyName(),
                modifier = Modifier
                    .padding(start = 8.dp)
                    .fillMaxWidth(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = PreviewTitle2Medium,
                color = colorResource(id = R.color.text_primary),
            )
        }
        if (idx != item.elements.lastIndex) {
            Divider(
                thickness = 0.5.dp,
                modifier = Modifier.padding(horizontal = 16.dp),
                color = colorResource(id = R.color.widget_divider)
            )
        }
        if (idx == item.elements.lastIndex) {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun getTitleAndIcon(
    item: WidgetView.Tree,
    icon: ObjectIcon
): Pair<String, ObjectIcon> {
    return when (item.source) {
        Widget.Source.Bundled.Favorites -> Pair(
            stringResource(id = R.string.favorites),
            ObjectIcon.SimpleIcon("star", R.color.text_primary)
        )
        Widget.Source.Bundled.Recent -> Pair(
            stringResource(id = R.string.recent),
            ObjectIcon.SimpleIcon("pencil", R.color.text_primary)
        )
        Widget.Source.Bundled.RecentLocal -> Pair(
            stringResource(id = R.string.recently_opened),
            ObjectIcon.SimpleIcon("eye", R.color.text_primary)
        )
        Widget.Source.Bundled.Bin -> Pair(
            stringResource(R.string.bin),
            ObjectIcon.SimpleIcon("calendar", R.color.text_primary)
        )
        else -> Pair(
            item.getPrettyName(),
            icon
        )
    }
}


@Immutable
private object TreeWidgetTreeItemDefaults {
    val Indent = 20.dp
}