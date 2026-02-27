package com.anytypeio.anytype.ui.widgets.types

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.ui.CustomIconColor
import com.anytypeio.anytype.core_ui.features.wallpaper.gradient
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.presentation.editor.cover.CoverColor
import com.anytypeio.anytype.presentation.editor.cover.CoverView
import com.anytypeio.anytype.presentation.home.InteractionMode
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.presentation.widgets.DropDownMenuAction
import com.anytypeio.anytype.presentation.widgets.ViewId
import com.anytypeio.anytype.presentation.widgets.Widget
import com.anytypeio.anytype.presentation.widgets.WidgetId
import com.anytypeio.anytype.presentation.widgets.WidgetView
import com.anytypeio.anytype.ui.home.ChatWidgetCard
import com.anytypeio.anytype.ui.widgets.menu.WidgetLongClickMenu
import com.anytypeio.anytype.ui.widgets.menu.WidgetMenuItem

@Composable
fun DataViewListWidgetCard(
    item: WidgetView.SetOfObjects,
    mode: InteractionMode,
    onWidgetObjectClicked: (ObjectWrapper.Basic) -> Unit,
    onSeeAllClicked: (WidgetId, ViewId?) -> Unit,
    onDropDownMenuAction: (DropDownMenuAction) -> Unit,
    onChangeWidgetView: (WidgetId, ViewId) -> Unit,
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
            WidgetHeader(
                title = item.getPrettyName(),
                icon = item.icon,
                onExpandElement = { onToggleExpandedWidgetState(item.id) },
                isExpanded = item.isExpanded,
                hasReadOnlyAccess = mode is InteractionMode.ReadOnly,
                canCreateObject = item.canCreateObjectOfType,
                onCreateElement = { onCreateElement(item) }
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
                            counter = (element as? WidgetView.SetOfObjects.Element.Chat)?.counter,
                            notificationState = (element as? WidgetView.SetOfObjects.Element.Chat)?.chatNotificationState,
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
                    val activeViewId = item.tabs.firstOrNull { it.isSelected }?.id
                    SeeAllButton(
                        onClick = { onSeeAllClicked(item.id, activeViewId) }
                    )
                }
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
fun ChatListWidgetCard(
    item: WidgetView.ChatList,
    mode: InteractionMode,
    onWidgetObjectClicked: (ObjectWrapper.Basic) -> Unit,
    onSeeAllClicked: (WidgetId, ViewId?) -> Unit,
    onDropDownMenuAction: (DropDownMenuAction) -> Unit,
    onChangeWidgetView: (WidgetId, ViewId) -> Unit,
    onToggleExpandedWidgetState: (WidgetId) -> Unit,
    onObjectCheckboxClicked: (Id, Boolean) -> Unit,
    onCreateElement: (WidgetView) -> Unit = {},
    menuItems: List<WidgetMenuItem> = emptyList(),
    isCardMenuExpanded: MutableState<Boolean> = mutableStateOf(false),
    modifier: Modifier = Modifier,
    hideCounters: Boolean = false
) {
    // For now, both Compact and Preview display modes use the same rendering
    // In the future, when displayMode is Preview, we can render a different UI with message previews
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp, vertical = 6.dp)
        ) {
            WidgetHeader(
                title = item.name.getPrettyName(),
                icon = item.icon,
                onExpandElement = { onToggleExpandedWidgetState(item.id) },
                isExpanded = item.isExpanded,
                hasReadOnlyAccess = mode is InteractionMode.ReadOnly,
                canCreateObject = item.canCreateObjectOfType,
                onCreateElement = { onCreateElement(item) }
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
                val usePreviewMode = item.displayMode == WidgetView.ChatList.DisplayMode.Preview

                if (item.isCompact && !usePreviewMode) {
                    CompactListWidgetList(
                        mode = mode,
                        elements = item.elements,
                        onWidgetElementClicked = onWidgetObjectClicked,
                        onObjectCheckboxClicked = onObjectCheckboxClicked,
                        hideCounters = hideCounters
                    )
                } else {
                    item.elements.forEachIndexed { idx, element ->
                        if (usePreviewMode && element is WidgetView.SetOfObjects.Element.Chat) {
                            // Use ChatWidgetCard for preview mode
                            ChatWidgetCard(
                                modifier = Modifier.padding(vertical = 4.dp),
                                chatIcon = element.objectIcon,
                                chatName = element.getPrettyName(),
                                creatorName = element.creatorName,
                                messageText = element.messageText,
                                messageTime = element.messageTime,
                                attachmentPreviews = element.attachmentPreviews,
                                unreadMessageCount = if (hideCounters) 0 else (element.counter?.unreadMessageCount ?: 0),
                                unreadMentionCount = if (hideCounters) 0 else (element.counter?.unreadMentionCount ?: 0),
                                chatNotificationState = element.chatNotificationState,
                                onClick = { onWidgetObjectClicked(element.obj) }
                            )
                            if (idx != item.elements.lastIndex) {
                                Divider(
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(end = 16.dp, start = 16.dp),
                                    color = colorResource(id = R.color.widget_divider)
                                )
                            }
                        } else {
                            // Use ListWidgetElement for compact mode
                            ListWidgetElement(
                                onWidgetObjectClicked = onWidgetObjectClicked,
                                obj = element.obj,
                                icon = element.objectIcon,
                                mode = mode,
                                onObjectCheckboxClicked = onObjectCheckboxClicked,
                                name = element.getPrettyName(),
                                counter = (element as? WidgetView.SetOfObjects.Element.Chat)?.counter,
                                notificationState = (element as? WidgetView.SetOfObjects.Element.Chat)?.chatNotificationState,
                                hideCounters = hideCounters
                            )
                            if (idx != item.elements.lastIndex) {
                                Divider(
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(end = 16.dp, start = 16.dp),
                                    color = colorResource(id = R.color.widget_divider)
                                )
                            }
                        }
                        if (idx == item.elements.lastIndex) {
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                    }
                }
                if (item.hasMore && item.isExpanded) {
                    val activeViewId = item.tabs.firstOrNull { it.isSelected }?.id
                    SeeAllButton(
                        onClick = { onSeeAllClicked(item.id, activeViewId) }
                    )
                }
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
fun GalleryWidgetCard(
    item: WidgetView.Gallery,
    mode: InteractionMode,
    onWidgetObjectClicked: (ObjectWrapper.Basic) -> Unit,
    onSeeAllClicked: (WidgetId, ViewId?) -> Unit,
    onDropDownMenuAction: (DropDownMenuAction) -> Unit,
    onChangeWidgetView: (WidgetId, ViewId) -> Unit,
    onToggleExpandedWidgetState: (WidgetId) -> Unit,
    onCreateElement: (WidgetView) -> Unit,
    menuItems: List<WidgetMenuItem> = emptyList(),
    isCardMenuExpanded: MutableState<Boolean> = mutableStateOf(false),
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp, vertical = 6.dp)
        ) {
            WidgetHeader(
                title = item.getPrettyName(),
                icon = item.icon,
                onExpandElement = { onToggleExpandedWidgetState(item.id) },
                isExpanded = item.isExpanded,
                hasReadOnlyAccess = mode is InteractionMode.ReadOnly,
                canCreateObject = item.canCreateObjectOfType,
                onCreateElement = { onCreateElement(item) },
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
                                showIcon = item.showIcon,
                                showCover = item.showCover,
                                onItemClicked = {
                                    onWidgetObjectClicked(element.obj)
                                }
                            )
                        }
                        if (idx == item.elements.lastIndex) {
                            item {
                                // Height should match gallery items based on showCover
                                val seeAllHeight = if (item.showCover) 136.dp else 54.dp

                                Box(
                                    modifier = Modifier
                                        .width(136.dp)
                                        .height(seeAllHeight)
                                        .border(
                                            width = 1.dp,
                                            color = colorResource(id = R.color.shape_transparent_primary),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            val activeViewId =
                                                item.tabs.firstOrNull { it.isSelected }?.id
                                            onSeeAllClicked(item.id, activeViewId)
                                        }
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.widget_view_see_all_objects),
                                        style = Caption1Medium,
                                        color = colorResource(id = R.color.glyph_active),
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .padding(horizontal = 12.dp),
                                        textAlign = TextAlign.Center
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
                        item.tabs.isNotEmpty() -> EmptyWidgetPlaceholder(R.string.empty_list_widget_no_objects)
                        else -> EmptyWidgetPlaceholder(text = R.string.empty_list_widget_no_view)
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
    obj: ObjectWrapper.Basic,
    name: String,
    counter: WidgetView.ChatCounter? = null,
    notificationState: com.anytypeio.anytype.core_models.chats.NotificationState? = null,
    hideCounters: Boolean = false
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
        val hasIcon = icon != ObjectIcon.None
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
            text = name.ifEmpty { stringResource(id = R.string.untitled) },
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

        // Display chat counter badges with notification-aware colors
        if (!hideCounters) {
            ChatCounterBadges(
                counter = counter,
                notificationState = notificationState,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}

@Composable
private fun GalleryWidgetItemCard(
    item: WidgetView.SetOfObjects.Element,
    showIcon: Boolean = false,
    showCover: Boolean = true,
    onItemClicked: () -> Unit
) {
    val isImageType = item.obj.layout == ObjectType.Layout.IMAGE
    val hasCover = item.cover != null
    
    val cardHeight = if (showCover) 136.dp else 54.dp

    when {
        isImageType -> {
            when (val cover = item.cover) {
                // Case 1: Image type - show full 136x136 image
                is CoverView.Image -> {
                    Box(
                        modifier = Modifier
                            .width(136.dp)
                            .height(cardHeight)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                onItemClicked()
                            }
                    ) {
                        AsyncImage(
                            model = cover.url,
                            contentDescription = "Image object",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop,
                        )
                        GalleryItemBorder()
                    }
                }
                // Case 2: Image type - Fallback for image type without cover
                else -> {
                    Box(
                        modifier = Modifier
                            .height(cardHeight)
                            .width(136.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                onItemClicked()
                            }
                    ) {
                        GalleryIconTitleContent(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 9.dp, start = 12.dp, end = 12.dp),
                            item = item,
                            showIcon = showIcon,
                            showCover = showCover
                        )
                        GalleryItemBorder()
                    }
                }
            }
        }

        // Case 3: Has cover - show 136x80 cover + title at bottom
        hasCover -> {
            Box(
                modifier = Modifier
                    .width(136.dp)
                    .height(cardHeight)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        onItemClicked()
                    }
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Cover section (80dp height)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                    ) {
                        when (val cover = item.cover) {
                            is CoverView.Color -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            color = Color(cover.coverColor.color),
                                            shape = RoundedCornerShape(
                                                topEnd = 8.dp,
                                                topStart = 8.dp
                                            )
                                        )
                                )
                            }

                            is CoverView.Gradient -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = gradient(cover.gradient)
                                            ),
                                            shape = RoundedCornerShape(
                                                topEnd = 8.dp,
                                                topStart = 8.dp
                                            )
                                        )
                                )
                            }

                            is CoverView.Image -> {
                                AsyncImage(
                                    model = cover.url,
                                    contentDescription = "Cover image",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                                    contentScale = ContentScale.Crop,
                                )
                            }

                            else -> {
                                // Draw nothing.
                            }
                        }
                    }

                    // Title section (remaining 56dp height)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        contentAlignment = Alignment.TopStart
                    ) {
                        GalleryIconTitleContent(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 8.dp, start = 12.dp, end = 12.dp),
                            item = item,
                            showIcon = showIcon,
                            showCover = showCover
                        )
                    }
                }
                GalleryItemBorder()
            }
        }

        // Case 3: No cover - show 136x54 with title and icon
        else -> {
            Box(
                modifier = Modifier
                    .width(136.dp)
                    .height(cardHeight)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        onItemClicked()
                    }
            ) {
                GalleryIconTitleContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 8.dp, start = 12.dp, end = 12.dp),
                    item = item,
                    showIcon = showIcon,
                    showCover = showCover
                )
                GalleryItemBorder()
            }
        }
    }
}

@Composable
private fun BoxScope.GalleryItemBorder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .border(
                width = 1.dp,
                color = colorResource(id = R.color.shape_transparent_primary),
                shape = RoundedCornerShape(8.dp)
            )
    )
}

@Composable
private fun GalleryIconTitleContent(
    modifier: Modifier = Modifier,
    item: WidgetView.SetOfObjects.Element,
    showIcon: Boolean = false,
    showCover: Boolean = true
) {
    val hasIcon = showIcon && item.objectIcon != ObjectIcon.None

    Box(
        modifier = modifier
    ) {
        // Show icon when showIcon is true and icon is not None
        if (hasIcon) {
            ListWidgetObjectIcon(
                icon = item.objectIcon,
                iconSize = 16.dp,
                iconWithoutBackgroundMaxSize = 20.dp,
                modifier = Modifier
                    .align(Alignment.TopStart)
            )
        }

        val titleText = when (val name = item.name) {
            is WidgetView.Name.Default -> name.prettyPrintName
            is WidgetView.Name.Bundled -> stringResource(id = name.source.res())
            WidgetView.Name.Empty -> stringResource(id = R.string.untitled)
        }

        Text(
            text = buildAnnotatedString {
                withStyle(
                    style = ParagraphStyle(
                        textIndent = TextIndent(
                            firstLine = if (hasIcon) 20.sp else 0.sp,
                            restLine = 0.sp
                        )
                    )
                ) {
                    append(titleText)
                }
            },
            style = Caption1Medium,
            color = colorResource(id = R.color.text_primary),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.TopStart)
        )
    }
}

// Preview 1: Image type object
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "Image Type - Light")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "Image Type - Dark")
@Composable
fun GalleryWidgetItemCard_ImageType_Preview() {
    GalleryWidgetItemCard(
        item = WidgetView.SetOfObjects.Element.Regular(
            objectIcon = ObjectIcon.None,
            obj = ObjectWrapper.Basic(
                map = mapOf(
                    Relations.NAME to "Mountain Landscape.jpg",
                    Relations.TYPE to ObjectTypeIds.IMAGE
                )
            ),
            name = WidgetView.Name.Default(
                prettyPrintName = "Mountain Landscape.jpg"
            ),
            cover = CoverView.Image(url = "https://picsum.photos/200")
        ),
        onItemClicked = {}
    )
}

// Preview 2: Object with image cover
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "With Image Cover - Light")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "With Image Cover - Dark")
@Composable
fun GalleryWidgetItemCard_WithImageCover_Preview() {
    GalleryWidgetItemCard(
        item = WidgetView.SetOfObjects.Element.Regular(
            objectIcon = ObjectIcon.None,
            obj = ObjectWrapper.Basic(
                map = mapOf(
                    Relations.NAME to "Travel Blog Post",
                    Relations.TYPE to ObjectTypeIds.PAGE
                )
            ),
            name = WidgetView.Name.Default(
                prettyPrintName = "Summer Vacation: Adventures in the Mountains"
            ),
            cover = CoverView.Image(url = "https://picsum.photos/200")
        ),
        onItemClicked = {}
    )
}

// Preview 3: Object with color cover
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "With Color Cover - Light")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "With Color Cover - Dark")
@Composable
fun GalleryWidgetItemCard_WithColorCover_Preview() {
    GalleryWidgetItemCard(
        showIcon = true,
        item = WidgetView.SetOfObjects.Element.Regular(
            objectIcon = ObjectIcon.TypeIcon.Default.DEFAULT,
            obj = ObjectWrapper.Basic(
                map = mapOf(
                    Relations.NAME to "Meeting Notes",
                    Relations.TYPE to ObjectTypeIds.PAGE
                )
            ),
            name = WidgetView.Name.Default(
                prettyPrintName = "Team Sync Meeting Notes"
            ),
            cover = CoverView.Color(coverColor = CoverColor.BLUE)
        ),
        onItemClicked = {}
    )
}

// Preview 4: Object with gradient cover
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "With Gradient Cover - Light")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "With Gradient Cover - Dark")
@Composable
fun GalleryWidgetItemCard_WithGradientCover_Preview() {
    GalleryWidgetItemCard(
        item = WidgetView.SetOfObjects.Element.Regular(
            objectIcon = ObjectIcon.None,
            obj = ObjectWrapper.Basic(
                map = mapOf(
                    Relations.NAME to "Project Plan",
                    Relations.TYPE to ObjectTypeIds.PAGE
                )
            ),
            name = WidgetView.Name.Default(
                prettyPrintName = "Q4 Product Roadmap"
            ),
            cover = CoverView.Gradient(gradient = "pinkOrange")
        ),
        onItemClicked = {}
    )
}

// Preview 5: Object without cover
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "No Cover - Light")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "No Cover - Dark")
@Composable
fun GalleryWidgetItemCard_NoCover_Preview() {
    GalleryWidgetItemCard(
        item = WidgetView.SetOfObjects.Element.Regular(
            objectIcon = ObjectIcon.None,
            obj = ObjectWrapper.Basic(
                map = mapOf(
                    Relations.NAME to "Quick Note",
                    Relations.TYPE to ObjectTypeIds.NOTE
                )
            ),
            name = WidgetView.Name.Default(
                prettyPrintName = "Important Reminders for Tomorrow's Presentation"
            ),
            cover = null
        ),
        onItemClicked = {}
    )
}

// Preview 6: Object without cover - short title
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "No Cover Short - Light")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "No Cover Short - Dark")
@Composable
fun GalleryWidgetItemCard_NoCoverShort_Preview() {
    GalleryWidgetItemCard(
        showIcon = true,
        item = WidgetView.SetOfObjects.Element.Regular(
            objectIcon = ObjectIcon.TypeIcon.Default(
                rawValue = "american-football",
                color = CustomIconColor.Blue
            ),
            obj = ObjectWrapper.Basic(
                map = mapOf(
                    Relations.NAME to "Tasks",
                    Relations.TYPE to ObjectTypeIds.TASK
                )
            ),
            name = WidgetView.Name.Default(
                prettyPrintName = "Buy, study, and share this game as an example of video games as true art."
            ),
            cover = null
        ),
        onItemClicked = {}
    )
}

// ========================================
// DataViewListWidgetCard Previews
// ========================================

// Preview 1: Standard expanded list with multiple elements
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "Standard List - Light")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "Standard List - Dark")
@Composable
fun DataViewListWidgetCard_Standard_Preview() {
    DataViewListWidgetCard(
        item = WidgetView.SetOfObjects(
            id = "widget-1",
            icon = ObjectIcon.TypeIcon.Default.DEFAULT,
            source = Widget.Source.Default(
                obj = ObjectWrapper.Basic(mapOf(Relations.NAME to "My Notes"))
            ),
            tabs = emptyList(),
            elements = listOf(
                WidgetView.SetOfObjects.Element.Regular(
                    objectIcon = ObjectIcon.Basic.Emoji("💡"),
                    obj = ObjectWrapper.Basic(
                        mapOf(
                            Relations.ID to "obj-1",
                            Relations.NAME to "Product Ideas",
                            Relations.DESCRIPTION to "Brainstorming session notes"
                        )
                    ),
                    name = WidgetView.Name.Default("Product Ideas")
                ),
                WidgetView.SetOfObjects.Element.Regular(
                    objectIcon = ObjectIcon.Basic.Emoji("📊"),
                    obj = ObjectWrapper.Basic(
                        mapOf(
                            Relations.ID to "obj-2",
                            Relations.NAME to "Q4 Planning",
                            Relations.DESCRIPTION to ""
                        )
                    ),
                    name = WidgetView.Name.Default("Q4 Planning")
                ),
                WidgetView.SetOfObjects.Element.Regular(
                    objectIcon = ObjectIcon.Basic.Emoji("✅"),
                    obj = ObjectWrapper.Basic(
                        mapOf(
                            Relations.ID to "obj-3",
                            Relations.NAME to "Weekly Tasks"
                        )
                    ),
                    name = WidgetView.Name.Default("Weekly Tasks")
                )
            ),
            isExpanded = true,
            isCompact = false,
            name = WidgetView.Name.Default("My Notes")
        ),
        mode = InteractionMode.Default,
        onWidgetObjectClicked = {},
        onSeeAllClicked = { _, _ -> },
        onDropDownMenuAction = {},
        onChangeWidgetView = { _, _ -> },
        onToggleExpandedWidgetState = {},
        onObjectCheckboxClicked = { _, _ -> }
    )
}

// Preview 2: Compact view
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "Compact List - Light")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "Compact List - Dark")
@Composable
fun DataViewListWidgetCard_Compact_Preview() {
    DataViewListWidgetCard(
        item = WidgetView.SetOfObjects(
            id = "widget-2",
            icon = ObjectIcon.TypeIcon.Default.DEFAULT,
            source = Widget.Source.Default(
                obj = ObjectWrapper.Basic(mapOf(Relations.NAME to "Quick Access"))
            ),
            tabs = emptyList(),
            elements = listOf(
                WidgetView.SetOfObjects.Element.Regular(
                    objectIcon = ObjectIcon.Basic.Emoji("🏠"),
                    obj = ObjectWrapper.Basic(mapOf(Relations.ID to "obj-1", Relations.NAME to "Home Dashboard")),
                    name = WidgetView.Name.Default("Home Dashboard")
                ),
                WidgetView.SetOfObjects.Element.Regular(
                    objectIcon = ObjectIcon.Basic.Emoji("📁"),
                    obj = ObjectWrapper.Basic(mapOf(Relations.ID to "obj-2", Relations.NAME to "Projects Folder")),
                    name = WidgetView.Name.Default("Projects Folder")
                ),
                WidgetView.SetOfObjects.Element.Regular(
                    objectIcon = ObjectIcon.Basic.Emoji("📖"),
                    obj = ObjectWrapper.Basic(mapOf(Relations.ID to "obj-3", Relations.NAME to "Reading List")),
                    name = WidgetView.Name.Default("Reading List")
                ),
                WidgetView.SetOfObjects.Element.Regular(
                    objectIcon = ObjectIcon.Basic.Emoji("🔖"),
                    obj = ObjectWrapper.Basic(mapOf(Relations.ID to "obj-4", Relations.NAME to "Bookmarks")),
                    name = WidgetView.Name.Default("Bookmarks")
                )
            ),
            isExpanded = true,
            isCompact = true,
            name = WidgetView.Name.Default("Quick Access")
        ),
        mode = InteractionMode.Default,
        onWidgetObjectClicked = {},
        onSeeAllClicked = { _, _ -> },
        onDropDownMenuAction = {},
        onChangeWidgetView = { _, _ -> },
        onToggleExpandedWidgetState = {},
        onObjectCheckboxClicked = { _, _ -> }
    )
}

// Preview 3: Loading state
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "Loading State - Light")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "Loading State - Dark")
@Composable
fun DataViewListWidgetCard_Loading_Preview() {
    DataViewListWidgetCard(
        item = WidgetView.SetOfObjects(
            id = "widget-3",
            icon = ObjectIcon.TypeIcon.Default.DEFAULT,
            source = Widget.Source.Default(
                obj = ObjectWrapper.Basic(mapOf(Relations.NAME to "Loading Widget"))
            ),
            tabs = emptyList(),
            elements = emptyList(),
            isExpanded = true,
            isCompact = false,
            name = WidgetView.Name.Default("Loading Widget")
        ),
        mode = InteractionMode.Default,
        onWidgetObjectClicked = {},
        onSeeAllClicked = { _, _ -> },
        onDropDownMenuAction = {},
        onChangeWidgetView = { _, _ -> },
        onToggleExpandedWidgetState = {},
        onObjectCheckboxClicked = { _, _ -> }
    )
}

// Preview 4: Empty state (no loading)
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "Empty State - Light")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "Empty State - Dark")
@Composable
fun DataViewListWidgetCard_Empty_Preview() {
    DataViewListWidgetCard(
        item = WidgetView.SetOfObjects(
            id = "widget-4",
            icon = ObjectIcon.TypeIcon.Default.DEFAULT,
            source = Widget.Source.Default(
                obj = ObjectWrapper.Basic(mapOf(Relations.NAME to "Empty Collection"))
            ),
            tabs = emptyList(),
            elements = emptyList(),
            isExpanded = true,
            isCompact = false,
            name = WidgetView.Name.Default("Empty Collection")
        ),
        mode = InteractionMode.Default,
        onWidgetObjectClicked = {},
        onSeeAllClicked = { _, _ -> },
        onDropDownMenuAction = {},
        onChangeWidgetView = { _, _ -> },
        onToggleExpandedWidgetState = {},
        onObjectCheckboxClicked = { _, _ -> }
    )
}

// Preview 5: Collapsed state
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "Collapsed - Light")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "Collapsed - Dark")
@Composable
fun DataViewListWidgetCard_Collapsed_Preview() {
    DataViewListWidgetCard(
        item = WidgetView.SetOfObjects(
            id = "widget-5",
            icon = ObjectIcon.TypeIcon.Default.DEFAULT,
            source = Widget.Source.Default(
                obj = ObjectWrapper.Basic(mapOf(Relations.NAME to "My Library"))
            ),
            tabs = emptyList(),
            elements = listOf(
                WidgetView.SetOfObjects.Element.Regular(
                    objectIcon = ObjectIcon.Basic.Emoji("📖"),
                    obj = ObjectWrapper.Basic(mapOf(Relations.ID to "obj-1", Relations.NAME to "Book 1")),
                    name = WidgetView.Name.Default("Book 1")
                )
            ),
            isExpanded = false,
            isCompact = false,
            name = WidgetView.Name.Default("My Library")
        ),
        mode = InteractionMode.Default,
        onWidgetObjectClicked = {},
        onSeeAllClicked = { _, _ -> },
        onDropDownMenuAction = {},
        onChangeWidgetView = { _, _ -> },
        onToggleExpandedWidgetState = {},
        onObjectCheckboxClicked = { _, _ -> }
    )
}

// Preview 6: With multiple tabs (views)
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "Multiple Tabs - Light")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "Multiple Tabs - Dark")
@Composable
fun DataViewListWidgetCard_WithTabs_Preview() {
    DataViewListWidgetCard(
        item = WidgetView.SetOfObjects(
            id = "widget-6",
            icon = ObjectIcon.TypeIcon.Default.DEFAULT,
            source = Widget.Source.Default(
                obj = ObjectWrapper.Basic(mapOf(Relations.NAME to "Project Tasks"))
            ),
            tabs = listOf(
                WidgetView.SetOfObjects.Tab(
                    id = "view-1",
                    name = "All Tasks",
                    isSelected = true
                ),
                WidgetView.SetOfObjects.Tab(
                    id = "view-2",
                    name = "In Progress",
                    isSelected = false
                ),
                WidgetView.SetOfObjects.Tab(
                    id = "view-3",
                    name = "Completed",
                    isSelected = false
                )
            ),
            elements = listOf(
                WidgetView.SetOfObjects.Element.Regular(
                    objectIcon = ObjectIcon.Task(isChecked = false),
                    obj = ObjectWrapper.Basic(
                        mapOf(
                            Relations.ID to "task-1",
                            Relations.NAME to "Design mockups",
                            Relations.DESCRIPTION to "Create UI designs for new feature"
                        )
                    ),
                    name = WidgetView.Name.Default("Design mockups")
                ),
                WidgetView.SetOfObjects.Element.Regular(
                    objectIcon = ObjectIcon.Task(isChecked = false),
                    obj = ObjectWrapper.Basic(mapOf(Relations.ID to "task-2", Relations.NAME to "Code review")),
                    name = WidgetView.Name.Default("Code review")
                )
            ),
            isExpanded = true,
            isCompact = false,
            name = WidgetView.Name.Default("Project Tasks")
        ),
        mode = InteractionMode.Default,
        onWidgetObjectClicked = {},
        onSeeAllClicked = { _, _ -> },
        onDropDownMenuAction = {},
        onChangeWidgetView = { _, _ -> },
        onToggleExpandedWidgetState = {},
        onObjectCheckboxClicked = { _, _ -> }
    )
}

// Preview 7: Edit mode
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "Edit Mode - Light")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "Edit Mode - Dark")
@Composable
fun DataViewListWidgetCard_EditMode_Preview() {
    DataViewListWidgetCard(
        item = WidgetView.SetOfObjects(
            id = "widget-7",
            icon = ObjectIcon.TypeIcon.Default.DEFAULT,
            source = Widget.Source.Default(
                obj = ObjectWrapper.Basic(mapOf(Relations.NAME to "Editable List"))
            ),
            tabs = emptyList(),
            elements = listOf(
                WidgetView.SetOfObjects.Element.Regular(
                    objectIcon = ObjectIcon.Basic.Emoji("📄"),
                    obj = ObjectWrapper.Basic(mapOf(Relations.ID to "obj-1", Relations.NAME to "Document 1")),
                    name = WidgetView.Name.Default("Document 1")
                ),
                WidgetView.SetOfObjects.Element.Regular(
                    objectIcon = ObjectIcon.Basic.Emoji("📝"),
                    obj = ObjectWrapper.Basic(mapOf(Relations.ID to "obj-2", Relations.NAME to "Document 2")),
                    name = WidgetView.Name.Default("Document 2")
                )
            ),
            isExpanded = true,
            isCompact = false,
            name = WidgetView.Name.Default("Editable List")
        ),
        mode = InteractionMode.Edit,
        onWidgetObjectClicked = {},
        onSeeAllClicked = { _, _ -> },
        onDropDownMenuAction = {},
        onChangeWidgetView = { _, _ -> },
        onToggleExpandedWidgetState = {},
        onObjectCheckboxClicked = { _, _ -> }
    )
}

// Preview 8: Bundled source (Favorites)
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "Favorites Widget - Light")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "Favorites Widget - Dark")
@Composable
fun DataViewListWidgetCard_Favorites_Preview() {
    DataViewListWidgetCard(
        item = WidgetView.SetOfObjects(
            id = "widget-8",
            icon = ObjectIcon.TypeIcon.Default.DEFAULT,
            source = Widget.Source.Bundled.Favorites,
            tabs = emptyList(),
            elements = listOf(
                WidgetView.SetOfObjects.Element.Regular(
                    objectIcon = ObjectIcon.Basic.Emoji("💼"),
                    obj = ObjectWrapper.Basic(mapOf(Relations.ID to "obj-1", Relations.NAME to "Work Notes")),
                    name = WidgetView.Name.Default("Work Notes")
                ),
                WidgetView.SetOfObjects.Element.Regular(
                    objectIcon = ObjectIcon.Basic.Emoji("🎨"),
                    obj = ObjectWrapper.Basic(mapOf(Relations.ID to "obj-2", Relations.NAME to "Design System")),
                    name = WidgetView.Name.Default("Design System")
                ),
                WidgetView.SetOfObjects.Element.Regular(
                    objectIcon = ObjectIcon.Basic.Emoji("🚀"),
                    obj = ObjectWrapper.Basic(mapOf(Relations.ID to "obj-3", Relations.NAME to "Launch Plan")),
                    name = WidgetView.Name.Default("Launch Plan")
                )
            ),
            isExpanded = true,
            isCompact = false,
            name = WidgetView.Name.Bundled(Widget.Source.Bundled.Favorites)
        ),
        mode = InteractionMode.Default,
        onWidgetObjectClicked = {},
        onSeeAllClicked = { _, _ -> },
        onDropDownMenuAction = {},
        onChangeWidgetView = { _, _ -> },
        onToggleExpandedWidgetState = {},
        onObjectCheckboxClicked = { _, _ -> }
    )
}

@StringRes
fun Widget.Source.Bundled.res(): Int = when (this) {
    Widget.Source.Bundled.Favorites -> R.string.favorites
    Widget.Source.Bundled.Recent -> R.string.recent
    Widget.Source.Bundled.RecentLocal -> R.string.recently_opened
    Widget.Source.Bundled.Bin -> R.string.bin
    Widget.Source.Bundled.AllObjects -> R.string.all_content
    Widget.Source.Bundled.Chat -> R.string.chat
}