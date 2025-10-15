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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_ui.features.wallpaper.gradient
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.presentation.editor.cover.CoverColor
import com.anytypeio.anytype.presentation.editor.cover.CoverView
import com.anytypeio.anytype.presentation.home.InteractionMode
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.widgets.DropDownMenuAction
import com.anytypeio.anytype.presentation.widgets.ViewId
import com.anytypeio.anytype.presentation.widgets.Widget
import com.anytypeio.anytype.presentation.widgets.WidgetId
import com.anytypeio.anytype.presentation.widgets.WidgetView
import com.anytypeio.anytype.ui.widgets.menu.WidgetLongClickMenu
import com.anytypeio.anytype.ui.widgets.menu.WidgetMenuItem

@Composable
fun DataViewListWidgetCard(
    item: WidgetView.SetOfObjects,
    mode: InteractionMode,
    onWidgetObjectClicked: (ObjectWrapper.Basic) -> Unit,
    onWidgetSourceClicked: (WidgetId) -> Unit,
    onWidgetMenuTriggered: (WidgetId) -> Unit,
    onDropDownMenuAction: (DropDownMenuAction) -> Unit,
    onChangeWidgetView: (WidgetId, ViewId) -> Unit,
    onToggleExpandedWidgetState: (WidgetId) -> Unit,
    onObjectCheckboxClicked: (Id, Boolean) -> Unit,
    onCreateElement: (WidgetView) -> Unit = {},
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
                isCardMenuExpanded = isCardMenuExpanded,
                onWidgetHeaderClicked = {
                    if (mode !is InteractionMode.Edit) {
                        onWidgetSourceClicked(item.id)
                    }
                },
                onExpandElement = { onToggleExpandedWidgetState(item.id) },
                isExpanded = item.isExpanded,
                isInEditMode = mode is InteractionMode.Edit,
                hasReadOnlyAccess = mode is InteractionMode.ReadOnly,
                canCreateObject = item.canCreateObjectOfType,
                onCreateElement = { onCreateElement(item) },
                onWidgetMenuTriggered = { onWidgetMenuTriggered(item.id) }
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
fun GalleryWidgetCard(
    item: WidgetView.Gallery,
    mode: InteractionMode,
    onWidgetObjectClicked: (ObjectWrapper.Basic) -> Unit,
    onWidgetSourceClicked: (WidgetId) -> Unit,
    onWidgetMenuTriggered: (WidgetId) -> Unit,
    onDropDownMenuAction: (DropDownMenuAction) -> Unit,
    onChangeWidgetView: (WidgetId, ViewId) -> Unit,
    onToggleExpandedWidgetState: (WidgetId) -> Unit,
    onObjectCheckboxClicked: (Id, Boolean) -> Unit,
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
                isCardMenuExpanded = isCardMenuExpanded,
                onWidgetHeaderClicked = {
                    if (mode !is InteractionMode.Edit) {
                        onWidgetSourceClicked(item.id)
                    }
                },
                onExpandElement = { onToggleExpandedWidgetState(item.id) },
                isExpanded = item.isExpanded,
                isInEditMode = mode is InteractionMode.Edit,
                hasReadOnlyAccess = mode is InteractionMode.ReadOnly,
                onWidgetMenuTriggered = { onWidgetMenuTriggered(item.id) },
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
                                onItemClicked = {
                                    onWidgetObjectClicked(element.obj)
                                }
                            )
                        }
                        if (idx == item.elements.lastIndex) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .size(136.dp)
                                        .border(
                                            width = 1.dp,
                                            color = colorResource(id = R.color.shape_transparent_primary),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            onWidgetSourceClicked(item.id)
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
                        item.isLoading -> EmptyWidgetPlaceholder(R.string.loading)
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
    name: String
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
    }
}

@Composable
private fun GalleryWidgetItemCard(
    item: WidgetView.SetOfObjects.Element,
    onItemClicked: () -> Unit
) {
    val isImageType = item.obj.layout == ObjectType.Layout.IMAGE
    val hasCover = item.cover != null

    Box(
        modifier = Modifier
            .size(136.dp)
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

        when {
            // Case 1: Image type - show full 136x136 image
            isImageType -> {
                when (val cover = item.cover) {
                    is CoverView.Image -> {
                        AsyncImage(
                            model = cover.url,
                            contentDescription = "Image object",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop,
                        )
                    }
                    else -> {
                        // Fallback for image type without cover
                        TitleOnlyContent(item)
                    }
                }
            }

            // Case 2: Has cover - show 136x80 cover + title at bottom
            hasCover -> {
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
                                            shape = RoundedCornerShape(topEnd = 8.dp, topStart = 8.dp)
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
                                            shape = RoundedCornerShape(topEnd = 8.dp, topStart = 8.dp)
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
                            .height(56.dp)
                            .padding(top = 8.dp, start = 12.dp, end = 12.dp),
                        contentAlignment = Alignment.TopStart
                    ) {
                        Text(
                            text = when (val name = item.name) {
                                is WidgetView.Name.Default -> name.prettyPrintName
                                is WidgetView.Name.Bundled -> stringResource(id = name.source.res())
                                WidgetView.Name.Empty -> stringResource(id = R.string.untitled)
                            },
                            style = Caption1Medium,
                            color = colorResource(id = R.color.text_primary),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Case 3: No cover - show 136x136 with centered title
            else -> {
                TitleOnlyContent(item)
            }
        }
    }
}

@Composable
private fun TitleOnlyContent(item: WidgetView.SetOfObjects.Element) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 9.dp, start = 12.dp, end = 12.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Text(
            text = when (val name = item.name) {
                is WidgetView.Name.Default -> name.prettyPrintName
                is WidgetView.Name.Bundled -> stringResource(id = name.source.res())
                WidgetView.Name.Empty -> stringResource(id = R.string.untitled)
            },
            style = Caption1Medium,
            color = colorResource(id = R.color.text_primary),
            textAlign = TextAlign.Start,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// Preview 1: Image type object
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "Image Type - Light")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "Image Type - Dark")
@Composable
fun GalleryWidgetItemCard_ImageType_Preview() {
    GalleryWidgetItemCard(
        item = WidgetView.SetOfObjects.Element(
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
        item = WidgetView.SetOfObjects.Element(
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
        item = WidgetView.SetOfObjects.Element(
            objectIcon = ObjectIcon.None,
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
        item = WidgetView.SetOfObjects.Element(
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
        item = WidgetView.SetOfObjects.Element(
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
        item = WidgetView.SetOfObjects.Element(
            objectIcon = ObjectIcon.None,
            obj = ObjectWrapper.Basic(
                map = mapOf(
                    Relations.NAME to "Tasks",
                    Relations.TYPE to ObjectTypeIds.TASK
                )
            ),
            name = WidgetView.Name.Default(
                prettyPrintName = "Tasks"
            ),
            cover = null
        ),
        onItemClicked = {}
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