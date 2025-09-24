package com.anytypeio.anytype.ui.widgets.types

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.anytypeio.anytype.presentation.widgets.DropDownMenuAction
import com.anytypeio.anytype.presentation.widgets.Widget
import com.anytypeio.anytype.presentation.widgets.WidgetId
import com.anytypeio.anytype.presentation.widgets.WidgetView
import com.anytypeio.anytype.presentation.widgets.WidgetView.ListOfObjects.Type
import com.anytypeio.anytype.ui.widgets.menu.WidgetLongClickMenu

@Composable
fun ListWidgetCard(
    item: WidgetView.ListOfObjects,
    mode: InteractionMode,
    onWidgetObjectClicked: (ObjectWrapper.Basic) -> Unit,
    onWidgetSourceClicked: (WidgetId, Widget.Source) -> Unit,
    onWidgetMenuTriggered: (WidgetId) -> Unit,
    onDropDownMenuAction: (DropDownMenuAction) -> Unit,
    onToggleExpandedWidgetState: (WidgetId) -> Unit,
    onObjectCheckboxClicked: (Id, Boolean) -> Unit,
    onCreateElement: (WidgetView) -> Unit = {}
) {
    val isCardMenuExpanded = remember {
        mutableStateOf(false)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 6.dp)
            .alpha(if (isCardMenuExpanded.value) 0.8f else 1f)
            .background(
                shape = RoundedCornerShape(16.dp),
                color = colorResource(id = R.color.dashboard_card_background)
            )
            .then(
                if (mode is InteractionMode.Edit)
                    Modifier.noRippleClickable {
                        isCardMenuExpanded.value = !isCardMenuExpanded.value
                    }
                else
                    Modifier
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp, vertical = 6.dp)
        ) {
            WidgetHeader(
                title = when(item.type) {
                    Type.Favorites -> stringResource(id = R.string.favorites)
                    Type.Recent -> stringResource(id = R.string.recent)
                    Type.RecentLocal -> stringResource(id = R.string.recently_opened)
                    Type.Bin -> stringResource(R.string.bin)
                },
                icon = item.icon,
                isCardMenuExpanded = isCardMenuExpanded,
                onWidgetHeaderClicked = { onWidgetSourceClicked(item.id, item.source) },
                onExpandElement = { onToggleExpandedWidgetState(item.id) },
                isExpanded = item.isExpanded,
                isInEditMode = mode is InteractionMode.Edit,
                hasReadOnlyAccess = mode is InteractionMode.ReadOnly,
                canCreateObject = item.canCreateObjectOfType,
                onCreateElement = { onCreateElement(item) },
                onWidgetMenuTriggered = { onWidgetMenuTriggered(item.id) }
            )
            if (item.elements.isNotEmpty()) {
                if (item.isCompact) {
                    CompactListWidgetList(
                        mode = mode,
                        elements = item.elements,
                        onWidgetElementClicked = onWidgetObjectClicked,
                        onObjectCheckboxClicked = onObjectCheckboxClicked
                    )
                } else {
                    item.elements.forEachIndexed { idx, element ->
                        ListWidgetElement(
                            onWidgetObjectClicked = onWidgetObjectClicked,
                            obj = element.obj,
                            icon = element.objectIcon,
                            mode = mode,
                            onObjectCheckboxClicked = onObjectCheckboxClicked,
                            name = element.getPrettyName()
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
            } else {
                if (item.isExpanded) {
                    if (item.isLoading) {
                        EmptyWidgetPlaceholder(R.string.loading)
                    } else {
                        if (item.type is Type.Bin) {
                            EmptyWidgetPlaceholder(R.string.bin_empty_title)
                        } else {
                            EmptyWidgetPlaceholder(R.string.empty_list_widget_no_objects)
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }
        WidgetLongClickMenu(
            widgetView = item,
            isCardMenuExpanded = isCardMenuExpanded,
            onDropDownMenuAction = onDropDownMenuAction
        )
    }
}

@Composable
fun CompactListWidgetList(
    mode: InteractionMode,
    elements: List<WidgetView.Element>,
    onWidgetElementClicked: (ObjectWrapper.Basic) -> Unit,
    onObjectCheckboxClicked: (Id, Boolean) -> Unit
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