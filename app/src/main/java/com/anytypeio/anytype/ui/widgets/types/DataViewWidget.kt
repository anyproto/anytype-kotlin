package com.anytypeio.anytype.ui.widgets.types

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_utils.ext.orNull
import com.anytypeio.anytype.presentation.home.InteractionMode
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.getProperName
import com.anytypeio.anytype.presentation.widgets.DropDownMenuAction
import com.anytypeio.anytype.presentation.widgets.ViewId
import com.anytypeio.anytype.presentation.widgets.WidgetId
import com.anytypeio.anytype.presentation.widgets.WidgetView
import com.anytypeio.anytype.ui.widgets.menu.WidgetMenu

@Composable
fun DataViewListWidgetCard(
    item: WidgetView.SetOfObjects,
    mode: InteractionMode,
    onWidgetObjectClicked: (ObjectWrapper.Basic) -> Unit,
    onDropDownMenuAction: (DropDownMenuAction) -> Unit,
    onChangeWidgetView: (WidgetId, ViewId) -> Unit,
    onToggleExpandedWidgetState: (WidgetId) -> Unit
) {
    val isCardMenuExpanded = remember {
        mutableStateOf(false)
    }
    val isHeaderMenuExpanded = remember {
        mutableStateOf(false)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 6.dp)
            .alpha(if (isCardMenuExpanded.value || isHeaderMenuExpanded.value) 0.8f else 1f)
            .background(
                shape = RoundedCornerShape(16.dp),
                color = colorResource(id = R.color.dashboard_card_background)
            )
            .noRippleClickable {
                isCardMenuExpanded.value = !isCardMenuExpanded.value
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp, vertical = 6.dp)
        ) {
            WidgetHeader(
                title = item.obj.getProperName(),
                isCardMenuExpanded = isCardMenuExpanded,
                isHeaderMenuExpanded = isHeaderMenuExpanded,
                onWidgetHeaderClicked = { onWidgetObjectClicked(item.obj) },
                onExpandElement = { onToggleExpandedWidgetState(item.id) },
                isExpanded = item.isExpanded,
                onDropDownMenuAction = onDropDownMenuAction
            )
            if (item.tabs.isNotEmpty() && item.isExpanded) {
                DataViewTabs(item, onChangeWidgetView)
            }
            if (item.elements.isNotEmpty()) {
                item.elements.forEachIndexed { idx, element ->
                    ListWidgetElement(
                        onWidgetObjectClicked = onWidgetObjectClicked,
                        obj = element.obj,
                        icon = element.icon,
                        mode = mode
                    )
                    Divider(
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(end = 16.dp, start = 8.dp)
                    )
                    if (idx == item.elements.lastIndex) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            } else {
                if (item.isExpanded) {
                    if (item.tabs.isNotEmpty())
                        EmptyWidgetPlaceholder(R.string.empty_list_widget)
                    else
                        EmptyWidgetPlaceholder(text = R.string.empty_list_widget_no_view)
                }
            }
        }
        WidgetMenu(
            isExpanded = isCardMenuExpanded,
            onDropDownMenuAction = onDropDownMenuAction,
            canEditWidgets = mode is InteractionMode.Default
        )
    }
}

@Composable
private fun DataViewTabs(
    item: WidgetView.SetOfObjects,
    onChangeWidgetView: (WidgetId, ViewId) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .height(40.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(
            items = item.tabs,
            itemContent = { tab ->
                Text(
                    text = tab.name,
                    fontSize = 15.sp,
                    color = if (tab.isSelected)
                        colorResource(id = R.color.text_primary)
                    else
                        colorResource(id = R.color.glyph_active),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .noRippleClickable {
                            onChangeWidgetView(item.id, tab.id)
                        }
                )
            }
        )
    }
}

@Composable
fun ListWidgetElement(
    mode: InteractionMode,
    onWidgetObjectClicked: (ObjectWrapper.Basic) -> Unit,
    icon: ObjectIcon,
    obj: ObjectWrapper.Basic
) {
    Box(
        modifier = Modifier
            .height(72.dp)
            .fillMaxWidth()
            .padding(end = 8.dp)
            .then(
                if (mode is InteractionMode.Default)
                    Modifier.noRippleClickable { onWidgetObjectClicked(obj) }
                else
                    Modifier
            )
    ) {
        val hasDescription = obj.description?.isNotEmpty() ?: false
        val hasIcon = icon != ObjectIcon.None && icon !is ObjectIcon.Basic.Avatar
        val name = obj.name?.trim()?.orNull()
        val snippet = obj.snippet?.trim().orNull()
        if (hasIcon) {
            ListWidgetObjectIcon(
                icon = icon,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp)
            )
        }
        Text(
            text = name ?: snippet ?: stringResource(id = R.string.untitled),
            modifier = if (hasDescription)
                Modifier
                    .padding(
                        top = 18.dp,
                        start = if (hasIcon) 76.dp else 16.dp,
                        end = 8.dp
                    )
            else
                Modifier
                    .padding(start = if (hasIcon) 76.dp else 16.dp, end = 8.dp)
                    .align(Alignment.CenterStart),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(
                fontSize = 15.sp,
                color = colorResource(id = R.color.text_primary),
                fontWeight = FontWeight.Medium,
            )
        )
        if (hasDescription) {
            Text(
                text = obj.description.orEmpty(),
                modifier = Modifier.padding(
                    top = 39.dp,
                    start = if (hasIcon) 76.dp else 16.dp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontSize = 12.sp,
                    color = colorResource(id = R.color.text_secondary)
                )
            )
        }
    }
}