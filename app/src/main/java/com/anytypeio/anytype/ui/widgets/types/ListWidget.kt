package com.anytypeio.anytype.ui.widgets.types

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
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
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.presentation.widgets.DropDownMenuAction
import com.anytypeio.anytype.presentation.widgets.WidgetId
import com.anytypeio.anytype.presentation.widgets.WidgetView
import com.anytypeio.anytype.presentation.widgets.WidgetView.ListOfObjects.Type
import com.anytypeio.anytype.ui.widgets.menu.WidgetLongClickMenu
import com.anytypeio.anytype.ui.widgets.menu.WidgetMenuItem

@Composable
fun ListWidgetCard(
    item: WidgetView.ListOfObjects,
    mode: InteractionMode,
    onWidgetObjectClicked: (ObjectWrapper.Basic) -> Unit,
    onWidgetSourceClicked: (WidgetId) -> Unit,
    onDropDownMenuAction: (DropDownMenuAction) -> Unit,
    onToggleExpandedWidgetState: (WidgetId) -> Unit,
    onObjectCheckboxClicked: (Id, Boolean) -> Unit,
    onCreateElement: (WidgetView) -> Unit = {},
    menuItems: List<WidgetMenuItem> = emptyList(),
    isCardMenuExpanded: MutableState<Boolean> = mutableStateOf(false),
    modifier: Modifier = Modifier,
    hideCounters: Boolean = false
) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp, vertical = 6.dp)
        ) {
            val (title, icon ) = getBundleTitleAndIcon(item.type)
            WidgetHeader(
                title = title,
                icon = icon,
                onExpandElement = { onToggleExpandedWidgetState(item.id) },
                isExpanded = item.isExpanded,
                hasReadOnlyAccess = mode is InteractionMode.ReadOnly,
                canCreateObject = item.canCreateObjectOfType,
                onCreateElement = { onCreateElement(item) },
            )
            if (item.elements.isNotEmpty()) {
                if (item.isCompact) {
                    CompactListWidgetList(
                        mode = mode,
                        elements = item.elements,
                        onWidgetElementClicked = onWidgetObjectClicked,
                        onObjectCheckboxClicked = onObjectCheckboxClicked,
                        hideCounters = hideCounters
                    )
                } else {
                    item.elements.forEachIndexed { idx, element ->
                        ListWidgetElement(
                            onWidgetObjectClicked = onWidgetObjectClicked,
                            obj = element.obj,
                            icon = element.objectIcon,
                            mode = mode,
                            onObjectCheckboxClicked = onObjectCheckboxClicked,
                            name = element.getPrettyName(),
                            counter = (element as? WidgetView.Element.Chat)?.counter,
                            notificationState = when (element) {
                                is WidgetView.ListOfObjects.Element.Chat -> element.chatNotificationState
                                is WidgetView.SetOfObjects.Element.Chat -> element.chatNotificationState
                                else -> null
                            },
                            hideCounters = hideCounters
                        )
                        if (idx != item.elements.lastIndex) {
                            Divider(
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(end = 16.dp, start = 16.dp),
                                color = colorResource(id = R.color.widget_divider)
                            )
                        }
                        if (idx == item.elements.lastIndex) {
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                    }
                }
                if (item.hasMore && item.isExpanded) {
                    SeeAllButton(
                        onClick = { onWidgetSourceClicked(item.id) }
                    )
                }
            } else {
                if (item.isExpanded) {
                    if (item.type is Type.Bin) {
                        EmptyWidgetPlaceholder(R.string.bin_empty_title)
                    } else {
                        EmptyWidgetPlaceholder(R.string.empty_list_widget_no_objects)
                    }
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
fun getBundleTitleAndIcon(
    type: Type,
): Pair<String, ObjectIcon> {
    return when (type) {
        Type.Favorites -> Pair(
            stringResource(id = R.string.favorites),
            ObjectIcon.SimpleIcon("star", R.color.text_primary)
        )
        Type.Recent -> Pair(
            stringResource(id = R.string.recent),
            ObjectIcon.SimpleIcon("pencil", R.color.text_primary)
        )
        Type.RecentLocal -> Pair(
            stringResource(id = R.string.recently_opened),
            ObjectIcon.SimpleIcon("eye", R.color.text_primary)
        )
        Type.Bin -> Pair(
            stringResource(R.string.bin),
            ObjectIcon.SimpleIcon("calendar", R.color.text_primary)
        )
    }
}

@Composable
fun CompactListWidgetList(
    mode: InteractionMode,
    elements: List<WidgetView.Element>,
    onWidgetElementClicked: (ObjectWrapper.Basic) -> Unit,
    onObjectCheckboxClicked: (Id, Boolean) -> Unit,
    hideCounters: Boolean = false
) {
    elements.forEachIndexed { idx, element ->
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .padding(horizontal = 16.dp)
                    .then(
                        if (mode !is InteractionMode.Edit)
                            Modifier.noRippleClickable { onWidgetElementClicked(element.obj) }
                        else
                            Modifier
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ListWidgetObjectIcon(
                    iconSize = 18.dp,
                    icon = element.objectIcon,
                    modifier = Modifier.padding(end = 12.dp),
                    onTaskIconClicked = { isChecked ->
                        onObjectCheckboxClicked(element.obj.id, isChecked)
                    },
                    iconWithoutBackgroundMaxSize = 200.dp
                )
                val (name, color) = element.getPrettyNameAndColor()
                Text(
                    text = name,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = PreviewTitle2Medium,
                    color = color
                )

                // Display chat counter badges with notification-aware colors
                if (!hideCounters && element is WidgetView.Element.Chat) {
                    val notificationState = when (element) {
                        is WidgetView.ListOfObjects.Element.Chat -> element.chatNotificationState
                        is WidgetView.SetOfObjects.Element.Chat -> element.chatNotificationState
                        else -> null
                    }
                    ChatCounterBadges(
                        counter = element.counter,
                        notificationState = notificationState
                    )
                }
            }
            if (idx != elements.lastIndex) {
                Divider(
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = colorResource(id = R.color.widget_divider)
                )
            }
        }
    }
}