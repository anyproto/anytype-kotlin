package com.anytypeio.anytype.feature_object_type.fields.ui

import android.os.Build
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.extensions.simpleIcon
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.foundation.util.DragDropState
import com.anytypeio.anytype.core_ui.foundation.util.DraggableItem
import com.anytypeio.anytype.core_ui.foundation.util.dragHandle
import com.anytypeio.anytype.core_ui.foundation.util.rememberDragDropState
import com.anytypeio.anytype.core_ui.views.BodyCalloutMedium
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.core_utils.insets.EDGE_TO_EDGE_MIN_SDK
import com.anytypeio.anytype.feature_object_type.R
import com.anytypeio.anytype.feature_object_type.fields.FieldEvent
import com.anytypeio.anytype.feature_object_type.fields.FieldEvent.*
import com.anytypeio.anytype.feature_object_type.fields.UiFieldEditOrNewState
import com.anytypeio.anytype.feature_object_type.fields.UiFieldsListItem
import com.anytypeio.anytype.feature_object_type.fields.UiFieldsListItem.Section
import com.anytypeio.anytype.feature_object_type.fields.UiFieldsListState
import com.anytypeio.anytype.feature_object_type.fields.UiLocalsFieldsInfoState
import com.anytypeio.anytype.feature_object_type.ui.UiIconState
import com.anytypeio.anytype.feature_object_type.ui.UiTitleState
import com.anytypeio.anytype.presentation.objects.ObjectIcon

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FieldsMainScreen(
    uiFieldsListState: UiFieldsListState,
    uiTitleState: UiTitleState,
    uiIconState: UiIconState,
    uiFieldEditOrNewState: UiFieldEditOrNewState,
    uiFieldLocalInfoState: UiLocalsFieldsInfoState,
    fieldEvent: (FieldEvent) -> Unit
) {

    var items by remember { mutableStateOf<List<UiFieldsListItem>>(uiFieldsListState.items) }

    items = uiFieldsListState.items

    val lazyListState = rememberLazyListState()
    val dragDropState = rememberDragDropState(
        lazyListState = lazyListState,
        onDragEnd = { fieldEvent(FieldOrderChanged(items)) },
        onMove = { fromIndex, toIndex ->
            items = items.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
        }
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        containerColor = colorResource(id = R.color.background_primary),
        contentColor = colorResource(id = R.color.background_primary),
        topBar = {
            val modifier = if (Build.VERSION.SDK_INT >= EDGE_TO_EDGE_MIN_SDK) {
                Modifier
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .fillMaxWidth()
            } else {
                Modifier.fillMaxWidth()
            }
            Column(modifier = modifier) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        modifier = Modifier
                            .wrapContentSize()
                            .align(Alignment.Center),
                        text = stringResource(R.string.object_type_fields_title),
                        style = Title1,
                        color = colorResource(R.color.text_primary)
                    )
                }
                InfoBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .background(color = colorResource(R.color.shape_tertiary)),
                    uiTitleState = uiTitleState,
                    uiIconState = uiIconState
                )
            }
        },
        content = { paddingValues ->
            val contentModifier = if (Build.VERSION.SDK_INT >= EDGE_TO_EDGE_MIN_SDK) {
                Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding())
            } else {
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            }
            LazyColumn(
                modifier = contentModifier,
                state = lazyListState
            ) {
                items(
                    count = items.size,
                    key = { items[it].id },
                    contentType = { index ->
                        when (items[index]) {
                            is UiFieldsListItem.Item.Default -> FieldsItemsContentType.FIELD_ITEM_DEFAULT
                            is UiFieldsListItem.Item.Draggable -> FieldsItemsContentType.FIELD_ITEM_DRAGGABLE
                            is UiFieldsListItem.Item.Local -> FieldsItemsContentType.FIELD_ITEM_LOCAL
                            is Section.SideBar -> FieldsItemsContentType.SECTION_SIDEBAR
                            is Section.Header -> FieldsItemsContentType.SECTION_HEADER
                            is Section.Hidden -> FieldsItemsContentType.SECTION_HIDDEN
                            is Section.Local -> FieldsItemsContentType.SECTION_LOCAL
                            is Section.LibraryFields -> TODO()
                            is Section.SpaceFields -> TODO()
                        }
                    },
                    itemContent = { index ->
                        val item = items[index]
                        when (item) {
                            is UiFieldsListItem.Item.Draggable -> {
                                FieldItemDraggable(
                                    modifier = Modifier
                                        .height(52.dp)
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp)
                                        .bottomBorder()
                                        .animateItem(),
                                    item = item,
                                    dragDropState = dragDropState,
                                    index = index,
                                    fieldEvent = fieldEvent,
                                )
                            }

                            is UiFieldsListItem.Item.Default -> {
                                FieldItem(
                                    modifier = Modifier
                                        .height(52.dp)
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp)
                                        .bottomBorder()
                                        .animateItem(),
                                    item = item,
                                    fieldEvent = fieldEvent
                                )
                            }

                            is UiFieldsListItem.Item.Local -> {
                                FieldItemLocal(
                                    modifier = Modifier
                                        .height(52.dp)
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp)
                                        .bottomBorder()
                                        .animateItem(),
                                    item = item,
                                    fieldEvent = fieldEvent
                                )
                            }

                            is Section.SideBar -> Section(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                item = item,
                                fieldEvent = fieldEvent
                            )

                            is Section.Header -> Section(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                item = item,
                                fieldEvent = fieldEvent
                            )

                            is Section.Hidden -> Section(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                item = item,
                                fieldEvent = fieldEvent
                            )

                            is Section.Local -> {
                                Section(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    item = item,
                                    fieldEvent = fieldEvent
                                )
                            }

                            is Section.LibraryFields -> TODO()
                            is Section.SpaceFields -> TODO()
                        }
                    }
                )
            }
        }
    )

    if (uiFieldEditOrNewState is UiFieldEditOrNewState.Visible) {
        EditFieldScreen(
            uiFieldEditOrNewState = uiFieldEditOrNewState,
            fieldEvent = fieldEvent
        )
    }

    if (uiFieldLocalInfoState is UiLocalsFieldsInfoState.Visible) {
        SectionLocalFieldsInfo(
            state = uiFieldLocalInfoState,
            fieldEvent = fieldEvent
        )
    }
}

@Composable
private fun InfoBar(modifier: Modifier, uiTitleState: UiTitleState, uiIconState: UiIconState) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = CenterVertically
    ) {
        Text(
            text = stringResource(R.string.object_type_fields_info_text),
            style = Caption1Medium,
            color = colorResource(id = R.color.text_primary),
        )
        ListWidgetObjectIcon(
            modifier = Modifier
                .size(18.dp)
                .padding(start = 4.dp),
            icon = uiIconState.icon,
            backgroundColor = R.color.transparent_black
        )
        Text(
            modifier = Modifier.padding(start = 4.dp),
            text = uiTitleState.title,
            style = Caption1Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = colorResource(id = R.color.text_primary),
        )
    }
}

@Composable
private fun Section(
    modifier: Modifier,
    item: UiFieldsListItem.Section,
    fieldEvent: (FieldEvent) -> Unit
) {
    val (title, textColor) = when (item) {
        is Section.Header -> stringResource(R.string.object_type_fields_section_header) to colorResource(
            id = R.color.text_secondary
        )

        is Section.SideBar ->
            stringResource(R.string.object_type_fields_section_fields_menu) to colorResource(
                id = R.color.text_secondary
            )

        is Section.Hidden -> stringResource(R.string.object_type_fields_section_hidden) to colorResource(
            id = R.color.text_secondary
        )

        is Section.Local -> stringResource(R.string.object_type_fields_section_local_fields) to colorResource(
            id = R.color.text_primary
        )

        is Section.LibraryFields -> TODO()
        is Section.SpaceFields -> TODO()
    }
    Box(modifier = modifier) {
        Text(
            modifier = Modifier
                .padding(bottom = 7.dp, start = 20.dp)
                .align(Alignment.BottomStart),
            text = title,
            style = BodyCalloutMedium,
            color = textColor,
        )
        if (item.canAdd) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .width(54.dp)
                    .height(40.dp)
                    .noRippleThrottledClickable {
                        fieldEvent(FieldEvent.Section.OnAddIconClick)
                    }
            ) {
                Image(
                    modifier = Modifier
                        .padding(bottom = 6.dp, end = 20.dp)
                        .wrapContentSize()
                        .align(Alignment.BottomEnd),
                    painter = painterResource(R.drawable.ic_default_plus),
                    contentDescription = "$title plus button"
                )
            }
        }
        if (item is Section.Local) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .height(37.dp)
                    .width(44.dp)
                    .noRippleThrottledClickable {
                        fieldEvent(FieldEvent.Section.OnLocalInfoClick)
                    }
            ) {
                Image(
                    modifier = Modifier
                        .padding(bottom = 9.dp, end = 20.dp)
                        .wrapContentSize()
                        .align(Alignment.BottomEnd),
                    painter = painterResource(R.drawable.ic_section_local_fields),
                    contentDescription = "Section local fields info"
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FieldItem(
    modifier: Modifier,
    item: UiFieldsListItem.Item.Default,
    fieldEvent: (FieldEvent) -> Unit
) {

    val isMenuExpanded = remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .combinedClickable(
                onClick = { fieldEvent(OnFieldItemClick(item = item)) },
                onLongClick = { isMenuExpanded.value = true }
            ),
        verticalAlignment = CenterVertically
    ) {
        val formatIcon = item.format.simpleIcon()
        if (formatIcon != null) {
            Image(
                modifier = Modifier
                    .padding(end = 10.dp)
                    .size(24.dp),
                painter = painterResource(id = formatIcon),
                contentDescription = "Relation format icon",
            )
        }

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f)
                .padding(end = 16.dp),
            text = item.fieldTitle,
            style = BodyRegular,
            color = colorResource(id = R.color.text_primary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        ItemDropDownMenu(
            item = item,
            showMenu = isMenuExpanded.value,
            onDismissRequest = {
                isMenuExpanded.value = false
            },
            onFieldEvent = {
                isMenuExpanded.value = false
                fieldEvent(it)
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FieldItemLocal(
    modifier: Modifier,
    item: UiFieldsListItem.Item.Local,
    fieldEvent: (FieldEvent) -> Unit
) {
    val isMenuExpanded = remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .combinedClickable(
                onClick = { fieldEvent(OnFieldItemClick(item = item)) },
                onLongClick = { isMenuExpanded.value = true }
            ),
        verticalAlignment = CenterVertically
    ) {
        val formatIcon = item.format.simpleIcon()
        if (formatIcon != null) {
            Image(
                modifier = Modifier
                    .padding(end = 10.dp)
                    .size(24.dp),
                painter = painterResource(id = formatIcon),
                contentDescription = "Relation format icon",
            )
        }

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f)
                .padding(end = 16.dp),
            text = item.fieldTitle,
            style = BodyRegular,
            color = colorResource(id = R.color.text_primary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Image(
            modifier = Modifier
                .size(24.dp)
                .noRippleThrottledClickable {
                    isMenuExpanded.value = true
                },
            painter = painterResource(R.drawable.ic_space_list_dots),
            contentDescription = "Local item menu"
        )
        ItemDropDownMenu(
            item = item,
            showMenu = isMenuExpanded.value,
            onDismissRequest = {
                isMenuExpanded.value = false
            },
            onFieldEvent = {
                isMenuExpanded.value = false
                fieldEvent(it)
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LazyItemScope.FieldItemDraggable(
    modifier: Modifier,
    item: UiFieldsListItem.Item.Draggable,
    dragDropState: DragDropState,
    index: Int,
    fieldEvent: (FieldEvent) -> Unit
) {
    val isMenuExpanded = remember { mutableStateOf(false) }

    DraggableItem(
        dragDropState = dragDropState,
        index = index
    ) { isDragging ->
        Row(
            modifier = modifier,
            verticalAlignment = CenterVertically
        ) {
            val formatIcon = item.format.simpleIcon()
            if (formatIcon != null) {
                Image(
                    modifier = Modifier
                        .padding(end = 10.dp)
                        .size(24.dp),
                    painter = painterResource(id = formatIcon),
                    contentDescription = "Relation format icon",
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f) // fill remaining space
                    .combinedClickable(
                        onClick = {
                            // normal click => open/edit
                            fieldEvent(OnFieldItemClick(item = item))
                        },
                        onLongClick = {
                            // show your menu, only if NOT dragging
                            isMenuExpanded.value = true
                        }
                    )
                    .padding(end = 16.dp)
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 16.dp),
                    text = item.fieldTitle,
                    style = BodyRegular,
                    color = colorResource(id = R.color.text_primary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Image(
                modifier = Modifier
                    .size(24.dp)
                    .dragHandle(dragDropState, index),
                painter = painterResource(R.drawable.ic_dnd),
                contentDescription = "Icon drag"
            )

            ItemDropDownMenu(
                item = item,
                showMenu = isMenuExpanded.value,
                onDismissRequest = {
                    isMenuExpanded.value = false
                },
                onFieldEvent = {
                    isMenuExpanded.value = false
                    fieldEvent(it)
                }
            )
        }
    }
}

@Composable
fun Modifier.bottomBorder(
    strokeWidth: Dp = 0.5.dp,
    color: Color = colorResource(R.color.shape_primary)
) = composed(
    factory = {
        val density = LocalDensity.current
        val strokeWidthPx = density.run { strokeWidth.toPx() }

        Modifier.drawBehind {
            val width = size.width
            val height = size.height - strokeWidthPx / 2

            drawLine(
                color = color,
                start = Offset(x = 0f, y = height),
                end = Offset(x = width, y = height),
                strokeWidth = strokeWidthPx
            )
        }
    }
)

@Composable
fun ItemDropDownMenu(
    item: UiFieldsListItem.Item,
    showMenu: Boolean,
    onDismissRequest: () -> Unit,
    onFieldEvent: (FieldEvent) -> Unit,
) {
    DropdownMenu(
        modifier = Modifier
            .width(244.dp),
        expanded = showMenu,
        offset = DpOffset(x = 0.dp, y = 0.dp),
        onDismissRequest = {
            onDismissRequest()
        },
        shape = RoundedCornerShape(10.dp),
        containerColor = colorResource(id = R.color.background_secondary),
    ) {
        when (item) {
            is UiFieldsListItem.Item.Default -> {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(R.string.object_type_fields_menu_delete),
                            style = BodyCalloutRegular,
                            color = colorResource(id = R.color.palette_system_red)
                        )
                    },
                    onClick = {
                        onFieldEvent(FieldItemMenu.OnDeleteFromTypeClick(item))
                    },
                )
            }

            is UiFieldsListItem.Item.Draggable -> {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(R.string.object_type_fields_menu_delete),
                            style = BodyCalloutRegular,
                            color = colorResource(id = R.color.palette_system_red)
                        )
                    },
                    onClick = {
                        onFieldEvent(FieldItemMenu.OnDeleteFromTypeClick(item))
                    },
                )
            }

            is UiFieldsListItem.Item.Local -> {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(R.string.object_type_fields_menu_add_to_type),
                            style = BodyCalloutRegular,
                            color = colorResource(id = R.color.text_primary)
                        )
                    },
                    onClick = {
                        onFieldEvent(FieldItemMenu.OnAddLocalToTypeClick(item))
                    },
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(R.string.object_type_fields_menu_remove),
                            style = BodyCalloutRegular,
                            color = colorResource(id = R.color.palette_system_red)
                        )
                    },
                    onClick = {
                        onFieldEvent(FieldItemMenu.OnRemoveLocalClick(item))
                    },
                )
            }
        }
    }
}

@DefaultPreviews
@Composable
fun PreviewTypeFieldsMainScreen() {
    FieldsMainScreen(
        uiTitleState = UiTitleState(title = "Page", isEditable = false),
        uiIconState = UiIconState(icon = ObjectIcon.Empty.ObjectType, isEditable = false),
        uiFieldsListState = UiFieldsListState(
            items = listOf(
                UiFieldsListItem.Section.Header(),
                UiFieldsListItem.Item.Draggable(
                    id = "id1",
                    fieldKey = "key1",
                    fieldTitle = "Status",
                    format = RelationFormat.STATUS,
                    canDelete = true,
                    isEditableField = true
                ),
                UiFieldsListItem.Item.Draggable(
                    id = "id2",
                    fieldKey = "key2",
                    fieldTitle = "Very long field title, just to test how it looks",
                    format = RelationFormat.LONG_TEXT,
                    canDelete = true,
                    isEditableField = true
                ),
                UiFieldsListItem.Section.SideBar(
                    canAdd = true
                ),
                UiFieldsListItem.Item.Default(
                    id = "id3",
                    fieldKey = "key3",
                    fieldTitle = "Links",
                    format = RelationFormat.URL,
                    isEditableField = true
                ),
                UiFieldsListItem.Item.Default(
                    id = "id4",
                    fieldKey = "key4",
                    fieldTitle = "Very long field title, just to test how it looks",
                    format = RelationFormat.DATE,
                    isEditableField = true
                ),
                UiFieldsListItem.Section.Hidden(),
                UiFieldsListItem.Item.Draggable(
                    id = "id555",
                    fieldKey = "key555",
                    fieldTitle = "Hidden field",
                    format = RelationFormat.LONG_TEXT,
                    isEditableField = true
                ),
                UiFieldsListItem.Section.Local(),
                UiFieldsListItem.Item.Local(
                    id = "id5",
                    fieldKey = "key5",
                    fieldTitle = "Local field",
                    format = RelationFormat.LONG_TEXT,
                    isEditableField = true
                ),
                UiFieldsListItem.Item.Local(
                    id = "id6",
                    fieldKey = "key6",
                    fieldTitle = "Local Very long field title, just to test how it looks",
                    format = RelationFormat.LONG_TEXT,
                    isEditableField = true
                )
            )
        ),
        fieldEvent = {},
        uiFieldEditOrNewState = UiFieldEditOrNewState.Hidden,
        uiFieldLocalInfoState = UiLocalsFieldsInfoState.Hidden
    )
}

object FieldsItemsContentType {
    const val FIELD_ITEM_DRAGGABLE = "content_type_field_item_draggable"
    const val FIELD_ITEM_DEFAULT = "content_type_field_item_default"
    const val FIELD_ITEM_LOCAL = "content_type_field_item_local"
    const val SECTION_HEADER = "content_type_section_header"
    const val SECTION_SIDEBAR = "content_type_section_sidebar"
    const val SECTION_HIDDEN = "content_type_section_hidden"
    const val SECTION_LOCAL = "content_type_section_local"
}