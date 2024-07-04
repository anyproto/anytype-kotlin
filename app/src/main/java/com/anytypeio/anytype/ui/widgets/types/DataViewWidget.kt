package com.anytypeio.anytype.ui.widgets.types

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.core_utils.ext.orNull
import com.anytypeio.anytype.presentation.home.InteractionMode
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.getProperName
import com.anytypeio.anytype.presentation.widgets.DropDownMenuAction
import com.anytypeio.anytype.presentation.widgets.ViewId
import com.anytypeio.anytype.presentation.widgets.Widget
import com.anytypeio.anytype.presentation.widgets.WidgetId
import com.anytypeio.anytype.presentation.widgets.WidgetView
import com.anytypeio.anytype.presentation.widgets.getWidgetObjectName
import com.anytypeio.anytype.ui.widgets.menu.WidgetMenu

@Composable
fun DataViewListWidgetCard(
    item: WidgetView.SetOfObjects,
    mode: InteractionMode,
    onWidgetObjectClicked: (ObjectWrapper.Basic) -> Unit,
    onWidgetSourceClicked: (Widget.Source) -> Unit,
    onDropDownMenuAction: (DropDownMenuAction) -> Unit,
    onChangeWidgetView: (WidgetId, ViewId) -> Unit,
    onToggleExpandedWidgetState: (WidgetId) -> Unit,
    onObjectCheckboxClicked: (Id, Boolean) -> Unit
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
                title = when (val source = item.source) {
                    is Widget.Source.Default -> {
                        source.obj.getWidgetObjectName() ?: stringResource(id = R.string.untitled)
                    }

                    is Widget.Source.Bundled -> {
                        stringResource(id = source.res())
                    }
                },
                isCardMenuExpanded = isCardMenuExpanded,
                isHeaderMenuExpanded = isHeaderMenuExpanded,
                onWidgetHeaderClicked = {
                    if (mode is InteractionMode.Default) {
                        onWidgetSourceClicked(item.source)
                    }
                },
                onExpandElement = { onToggleExpandedWidgetState(item.id) },
                isExpanded = item.isExpanded,
                isInEditMode = mode is InteractionMode.Edit,
                hasReadOnlyAccess = mode is InteractionMode.ReadOnly,
                onDropDownMenuAction = onDropDownMenuAction
            )
            if (item.tabs.size > 1 && item.isExpanded) {
                DataViewTabs(
                    tabs = item.tabs,
                    onChangeWidgetView = { tab ->
                        onChangeWidgetView(item.id, tab)
                    }
                )
            }
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
                            onObjectCheckboxClicked = onObjectCheckboxClicked
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
                    when {
                        item.isLoading -> EmptyWidgetPlaceholder(R.string.loading)
                        item.tabs.isNotEmpty() -> EmptyWidgetPlaceholder(R.string.empty_list_widget)
                        else -> EmptyWidgetPlaceholder(text = R.string.empty_list_widget_no_view)
                    }

                    Spacer(modifier = Modifier.height(2.dp))
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
fun GalleryWidgetCard(
    item: WidgetView.Gallery,
    mode: InteractionMode,
    onWidgetObjectClicked: (ObjectWrapper.Basic) -> Unit,
    onWidgetSourceClicked: (Widget.Source) -> Unit,
    onDropDownMenuAction: (DropDownMenuAction) -> Unit,
    onChangeWidgetView: (WidgetId, ViewId) -> Unit,
    onToggleExpandedWidgetState: (WidgetId) -> Unit,
    onObjectCheckboxClicked: (Id, Boolean) -> Unit
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
                title = when (val source = item.source) {
                    is Widget.Source.Default -> {
                        source.obj.getWidgetObjectName() ?: stringResource(id = R.string.untitled)
                    }

                    is Widget.Source.Bundled -> {
                        stringResource(id = source.res())
                    }
                },
                isCardMenuExpanded = isCardMenuExpanded,
                isHeaderMenuExpanded = isHeaderMenuExpanded,
                onWidgetHeaderClicked = {
                    if (mode is InteractionMode.Default) {
                        onWidgetSourceClicked(item.source)
                    }
                },
                onExpandElement = { onToggleExpandedWidgetState(item.id) },
                isExpanded = item.isExpanded,
                isInEditMode = mode is InteractionMode.Edit,
                hasReadOnlyAccess = mode is InteractionMode.ReadOnly,
                onDropDownMenuAction = onDropDownMenuAction
            )
            if (item.tabs.size > 1 && item.isExpanded) {
                DataViewTabs(
                    tabs = item.tabs,
                    onChangeWidgetView = { tab ->
                        onChangeWidgetView(item.id, tab)
                    }
                )
            }
            if (item.elements.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item.elements.forEachIndexed { idx, element ->
                        if (idx == 0) {
                            item {
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                        }
                        item(key = element.obj.id) {
                            GalleryWidgetItemCard(
                                item = element
                            )
                        }
                        if (idx == item.elements.lastIndex) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .size(136.dp)
                                        .border(
                                            width = 1.dp,
                                            color = colorResource(id = R.color.shape_primary),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.widget_view_see_all_objects),
                                        style = Caption1Medium,
                                        color = colorResource(id = R.color.glyph_active),
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                            item {
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                        }
                    }
                }
            } else {
                if (item.isExpanded) {
                    when {
                        item.isLoading -> EmptyWidgetPlaceholder(R.string.loading)
                        item.tabs.isNotEmpty() -> EmptyWidgetPlaceholder(R.string.empty_list_widget)
                        else -> EmptyWidgetPlaceholder(text = R.string.empty_list_widget_no_view)
                    }

                    Spacer(modifier = Modifier.height(2.dp))
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
    tabs: List<WidgetView.SetOfObjects.Tab>,
    onChangeWidgetView: (ViewId) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .height(40.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(
            items = tabs,
            itemContent = { index, tab ->
                Text(
                    text = tab.name.ifEmpty { stringResource(id = R.string.untitled) },
                    style = PreviewTitle2Medium,
                    color = if (tab.isSelected)
                        colorResource(id = R.color.text_primary)
                    else
                        colorResource(id = R.color.text_secondary_widgets),
                    modifier = Modifier
                        .padding(
                            start = 16.dp,
                            end = if (index == tabs.lastIndex) 16.dp else 0.dp
                        )
                        .noRippleClickable {
                            onChangeWidgetView(tab.id)
                        },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        )
    }
}

@Composable
fun ListWidgetElement(
    mode: InteractionMode,
    onWidgetObjectClicked: (ObjectWrapper.Basic) -> Unit,
    onObjectCheckboxClicked: (Id, Boolean) -> Unit,
    icon: ObjectIcon,
    obj: ObjectWrapper.Basic
) {
    Box(
        modifier = Modifier
            .height(72.dp)
            .fillMaxWidth()
            .padding(end = 16.dp)
            .then(
                if (mode !is InteractionMode.Edit)
                    Modifier.noRippleClickable { onWidgetObjectClicked(obj) }
                else
                    Modifier
            )
    ) {
        val hasDescription = !obj.description.isNullOrEmpty()
        val hasIcon = icon != ObjectIcon.None && icon !is ObjectIcon.Basic.Avatar
        val name = obj.name?.trim()?.orNull()
        val snippet = obj.snippet?.trim().orNull()
        if (hasIcon) {
            ListWidgetObjectIcon(
                icon = icon,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp),
                onTaskIconClicked = { isChecked ->
                    onObjectCheckboxClicked(
                        obj.id,
                        isChecked
                    )
                }
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
            style = PreviewTitle2Medium,
            color = colorResource(id = R.color.text_primary),
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
                style = Relations3.copy(
                    color = colorResource(id = R.color.text_secondary_widgets)
                )
            )
        }
    }
}

@Composable
private fun GalleryWidgetItemCard(
    item: WidgetView.SetOfObjects.Element
) {
    Box(
        modifier = Modifier
            .size(136.dp)
            .border(
                width = 1.dp,
                color = colorResource(id = R.color.shape_primary),
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .width(136.dp)
                .height(80.dp)
                .background(
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                    color = Color.Red
                )
        )
        Text(
            text = item.obj.getProperName(),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = Caption1Medium,
            color = colorResource(id = R.color.text_primary),
            modifier = Modifier.padding(
                start = 12.dp,
                end = 12.dp,
                top = 10.dp
            )
        )
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "Light Mode")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "Dark Mode")
@Composable
fun GalleryWidgetItemCardPreview() {
    GalleryWidgetItemCard(
        item = WidgetView.SetOfObjects.Element(
            objectIcon = ObjectIcon.None,
            obj = ObjectWrapper.Basic(
                map = mapOf(
                    Relations.NAME to "Stephen Bann"
                )
            )
        )
    )
}

@StringRes
fun Widget.Source.Bundled.res(): Int = when (this) {
    Widget.Source.Bundled.Favorites -> R.string.favorites
    Widget.Source.Bundled.Recent -> R.string.recent
    Widget.Source.Bundled.RecentLocal -> R.string.recently_opened
    Widget.Source.Bundled.Sets -> R.string.sets
    Widget.Source.Bundled.Collections -> R.string.collections
}