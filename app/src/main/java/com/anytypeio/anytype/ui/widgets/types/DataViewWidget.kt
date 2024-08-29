package com.anytypeio.anytype.ui.widgets.types

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import com.anytypeio.anytype.presentation.editor.cover.CoverGradient
import com.anytypeio.anytype.presentation.editor.cover.CoverView
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
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

@Composable
fun DataViewListWidgetCard(
    item: WidgetView.SetOfObjects,
    mode: InteractionMode,
    onWidgetObjectClicked: (ObjectWrapper.Basic) -> Unit,
    onWidgetSourceClicked: (Widget.Source) -> Unit,
    onDropDownMenuAction: (DropDownMenuAction) -> Unit,
    onChangeWidgetView: (WidgetId, ViewId) -> Unit,
    onToggleExpandedWidgetState: (WidgetId) -> Unit,
    onObjectCheckboxClicked: (Id, Boolean) -> Unit,
    onCreateDataViewObject: (WidgetId, ViewId?) -> Unit
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
                    if (mode !is InteractionMode.Edit) {
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
                        item.tabs.isNotEmpty() -> EmptyWidgetPlaceholderWithCreateButton(
                            R.string.empty_list_widget,
                            onCreateClicked = {
                                onCreateDataViewObject(
                                    item.id, item.tabs.find { it.isSelected }?.id
                                )
                            }
                        )
                        else -> EmptyWidgetPlaceholderWithCreateButton(
                            text = R.string.empty_list_widget_no_view,
                            onCreateClicked = {
                                onCreateDataViewObject(
                                    item.id, item.tabs.find { it.isSelected }?.id
                                )
                            }
                        )
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
    onObjectCheckboxClicked: (Id, Boolean) -> Unit,
    onSeeAllObjectsClicked: (WidgetView.Gallery) -> Unit
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
                    if (mode !is InteractionMode.Edit) {
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
                val withCover = item.showCover && item.elements.any { it.cover != null }
                val withIcon = item.showIcon
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item.elements.forEachIndexed { idx, element ->
                        if (idx == 0) {
                            item {
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                        }
                        item(key = element.obj.id) {
                            GalleryWidgetItemCard(
                                item = element,
                                onItemClicked = {
                                    onWidgetObjectClicked(element.obj)
                                },
                                withCover = withCover,
                                withIcon = withIcon
                            )
                        }
                        if (idx == item.elements.lastIndex) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .width(136.dp)
                                        .height(if (withCover) 136.dp else 56.dp)
                                        .border(
                                            width = 1.dp,
                                            color = colorResource(id = R.color.shape_transparent_primary),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            onSeeAllObjectsClicked(item)
                                        }
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.widget_view_see_all_objects),
                                        style = Caption1Medium,
                                        color = colorResource(id = R.color.glyph_active),
                                        modifier = Modifier
                                            .align(Alignment.Center)
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
                            if (!tab.isSelected) {
                                onChangeWidgetView(tab.id)
                            }
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

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun GalleryWidgetItemCard(
    item: WidgetView.SetOfObjects.Element,
    onItemClicked: () -> Unit,
    withCover: Boolean = false,
    withIcon: Boolean = false
) {
    Box(
        modifier = Modifier
            .width(136.dp)
            .height(if (withCover) 136.dp else 56.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable {
                onItemClicked()
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 1.dp,
                    color = colorResource(id = R.color.shape_transparent_primary),
                    shape = RoundedCornerShape(8.dp)
                )
        )
        if (withCover) {
            when (val cover = item.cover) {
                is CoverView.Color -> {
                    Box(
                        modifier = Modifier
                            .width(136.dp)
                            .height(80.dp)
                            .background(
                                color = Color(cover.coverColor.color),
                                shape = RoundedCornerShape(topEnd = 8.dp, topStart = 8.dp)
                            )
                    )
                }

                is CoverView.Gradient -> {
                    Box(
                        modifier = Modifier
                            .width(136.dp)
                            .height(80.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = gradient(cover.gradient)
                                ),
                                shape = RoundedCornerShape(topEnd = 8.dp, topStart = 8.dp)
                            )
                    )
                }

                is CoverView.Image -> {
                    GlideImage(
                        model = cover.url,
                        contentDescription = "Cover image",
                        modifier = Modifier
                            .width(136.dp)
                            .height(80.dp)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                        contentScale = ContentScale.Crop,
                    )
                }

                else -> {
                    // Draw nothing.
                }
            }
        }
        if (withIcon && item.objectIcon != ObjectIcon.None) {
            Row(
                modifier = Modifier.align(
                    if (item.cover != null) {
                        Alignment.BottomStart
                    } else {
                        Alignment.TopStart
                    }
                )
            ) {
                TreeWidgetObjectIcon(
                    icon = item.objectIcon,
                    paddingStart = 0.dp,
                    paddingEnd = 0.dp,
                    onTaskIconClicked = {},
                    modifier = Modifier
                        .padding(
                            start = 12.dp,
                            top = 9.dp
                        ),
                    size = 16.dp
                )
                Text(
                    text = item.obj.getProperName().ifEmpty { stringResource(id = R.string.untitled) },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = Caption1Medium,
                    color = colorResource(id = R.color.text_primary),
                    modifier = Modifier
                        .padding(
                            start = 6.dp,
                            end = 10.dp,
                            top = 9.dp,
                            bottom = 11.dp
                        )
                )
            }
        } else {
            Text(
                text = item.obj.getProperName().ifEmpty { stringResource(id = R.string.untitled) },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = Caption1Medium,
                color = colorResource(id = R.color.text_primary),
                modifier = Modifier
                    .align(
                        if (item.cover != null) {
                            Alignment.BottomStart
                        } else {
                            Alignment.TopStart
                        }
                    )
                    .padding(
                        start = 12.dp,
                        end = 10.dp,
                        top = 9.dp,
                        bottom = 11.dp
                    )
            )
        }
    }
}

fun gradient(
    gradient: String,
    alpha: Float = 1.0f
) : List<Color> {
    return when(gradient) {
        CoverGradient.YELLOW -> {
            return listOf(
                Color(0xFFecd91b).copy(alpha = alpha),
                Color(0xFFffb522).copy(alpha = alpha)
            )
        }
        CoverGradient.RED -> {
            return listOf(
                Color(0xFFe51ca0).copy(alpha = alpha),
                Color(0xFFf55522).copy(alpha = alpha)
            )
        }
        CoverGradient.BLUE -> {
            return listOf(
                Color(0xFF3e58eb).copy(alpha = alpha),
                Color(0xFFab50cc).copy(alpha = alpha)
            )
        }
        CoverGradient.TEAL -> {
            return listOf(
                Color(0xFF0fc8ba).copy(alpha = alpha),
                Color(0xFF2aa7ee).copy(alpha = alpha)
            )
        }
        CoverGradient.PINK_ORANGE -> {
            return listOf(
                Color(0xFFD8A4E1).copy(alpha = alpha),
                Color(0xFFFDD0CD).copy(alpha = alpha),
                Color(0xFFFFCC81).copy(alpha = alpha)
            )
        }
        CoverGradient.BLUE_PINK -> {
            return listOf(
                Color(0xFF73B7F0).copy(alpha = alpha),
                Color(0xFFABB6ED).copy(alpha = alpha),
                Color(0xFFF3BFAC).copy(alpha = alpha)
            )
        }
        CoverGradient.GREEN_ORANGE -> {
            return listOf(
                Color(0xFF63B3CB).copy(alpha = alpha),
                Color(0xFFC5D3AC).copy(alpha = alpha),
                Color(0xFFF6C47A).copy(alpha = alpha)
            )
        }
        CoverGradient.SKY -> {
            return listOf(
                Color(0xFF6EB6E4).copy(alpha = alpha),
                Color(0xFFA4CFEC).copy(alpha = alpha),
                Color(0xFFDAEAF3).copy(alpha = alpha)
            )
        }
        else -> return emptyList()
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
        ),
        onItemClicked = {}
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