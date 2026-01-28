package com.anytypeio.anytype.feature_object_type.fields.ui

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.common.ReorderHapticFeedback
import com.anytypeio.anytype.core_ui.common.ReorderHapticFeedbackType
import com.anytypeio.anytype.core_ui.common.rememberReorderHapticFeedback
import com.anytypeio.anytype.core_ui.extensions.simpleIcon
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.BodyCalloutMedium
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.Relations1
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.views.UxSmallTextRegular
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.core_utils.insets.EDGE_TO_EDGE_MIN_SDK
import com.anytypeio.anytype.feature_object_type.R
import com.anytypeio.anytype.feature_object_type.fields.FieldEvent
import com.anytypeio.anytype.feature_object_type.fields.FieldEvent.*
import com.anytypeio.anytype.feature_object_type.fields.FieldEvent.EditProperty.OnLimitTypesClick
import com.anytypeio.anytype.feature_object_type.fields.FieldEvent.EditProperty.OnLimitTypesDismiss
import com.anytypeio.anytype.feature_object_type.fields.FieldEvent.FieldItemMenu.*
import com.anytypeio.anytype.feature_object_type.fields.UiFieldsListItem
import com.anytypeio.anytype.feature_object_type.fields.UiFieldsListItem.Section
import com.anytypeio.anytype.feature_object_type.fields.UiFieldsListState
import com.anytypeio.anytype.feature_object_type.fields.UiLocalsFieldsInfoState
import com.anytypeio.anytype.feature_object_type.ui.UiIconState
import com.anytypeio.anytype.feature_object_type.ui.UiTitleState
import com.anytypeio.anytype.feature_properties.edit.UiEditPropertyState
import com.anytypeio.anytype.feature_properties.edit.ui.PropertyScreen
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import kotlinx.coroutines.delay
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyListState
import sh.calvin.reorderable.rememberReorderableLazyListState
import timber.log.Timber

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FieldsMainScreen(
    uiFieldsListState: UiFieldsListState,
    uiTitleState: UiTitleState,
    uiIconState: UiIconState,
    uiFieldLocalInfoState: UiLocalsFieldsInfoState,
    uiEditPropertyState: UiEditPropertyState,
    fieldEvent: (FieldEvent) -> Unit
) {

    val hapticFeedback = rememberReorderHapticFeedback()

    val lazyListState = rememberLazyListState()

    val reorderableLazyColumnState = rememberReorderableLazyListState(lazyListState) { from, to ->
        fieldEvent(DragEvent.OnMove(from.key as String, to.key as String))
        hapticFeedback.performHapticFeedback(ReorderHapticFeedbackType.MOVE)
    }

    var isDragging by remember { mutableStateOf(false) }

    LaunchedEffect(reorderableLazyColumnState.isAnyItemDragging) {
        if (reorderableLazyColumnState.isAnyItemDragging) {
            isDragging = true
            // Optional: Add a small delay to avoid triggering on very short drags
            delay(50)
        } else if (isDragging) {
            isDragging = false
            fieldEvent(DragEvent.OnDragEnd)
            hapticFeedback.performHapticFeedback(ReorderHapticFeedbackType.MOVE)
        }
    }

    BackHandler(enabled = true) {
        Timber.d("Back pressed on Properties Screen")
        fieldEvent.invoke(FieldEvent.OnDismissScreen)
    }

    Scaffold(
        modifier = Modifier
            .nestedScroll(rememberNestedScrollInteropConnection())
            .background(
                color = colorResource(id = R.color.background_primary)
            )
            .fillMaxSize(),
        containerColor = colorResource(id = R.color.transparent_black),
        contentColor = colorResource(id = R.color.background_primary),
        topBar = {
            TopBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .systemBarsPadding(),
                uiTitleState = uiTitleState,
                uiIconState = uiIconState,
                onBackClick = {
                    fieldEvent(FieldEvent.OnDismissScreen)
                }
            )
        },
        content = { paddingValues ->
            Items(
                uiFieldsListState = uiFieldsListState,
                lazyListState = lazyListState,
                reorderableLazyColumnState = reorderableLazyColumnState,
                hapticFeedback = hapticFeedback,
                paddingValues = paddingValues,
                fieldEvent = fieldEvent
            )
        }
    )

    if (uiEditPropertyState is UiEditPropertyState.Visible) {
        PropertyScreen(
            modifier = Modifier.fillMaxWidth(),
            uiState = uiEditPropertyState,
            onDismissRequest = { fieldEvent(OnEditPropertyScreenDismiss) },
            onFormatClick = {},
            onSaveButtonClicked = { fieldEvent(EditProperty.OnSaveButtonClicked) },
            onCreateNewButtonClicked = {},
            onPropertyNameUpdate = { fieldEvent(EditProperty.OnPropertyNameUpdate(it)) },
            onMenuUnlinkClick = { fieldEvent(OnRemoveFromTypeClick(it)) },
            onLimitTypesClick = { fieldEvent(OnLimitTypesClick) },
            onDismissLimitTypes = { fieldEvent(OnLimitTypesDismiss) },
        )
    }

    if (uiFieldLocalInfoState is UiLocalsFieldsInfoState.Visible) {
        SectionLocalFieldsInfo(
            modifier = Modifier.fillMaxWidth(),
            state = uiFieldLocalInfoState,
            fieldEvent = fieldEvent
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FieldsMainModalScreen(
    uiFieldsListState: UiFieldsListState,
    uiTitleState: UiTitleState,
    uiIconState: UiIconState,
    uiFieldLocalInfoState: UiLocalsFieldsInfoState,
    uiEditPropertyState: UiEditPropertyState,
    fieldEvent: (FieldEvent) -> Unit
) {

    val hapticFeedback = rememberReorderHapticFeedback()

    val lazyListState = rememberLazyListState()

    val reorderableLazyColumnState = rememberReorderableLazyListState(lazyListState) { from, to ->
        fieldEvent(DragEvent.OnMove(from.key as String, to.key as String))
        hapticFeedback.performHapticFeedback(ReorderHapticFeedbackType.MOVE)
    }

    var isDragging by remember { mutableStateOf(false) }

    LaunchedEffect(reorderableLazyColumnState.isAnyItemDragging) {
        if (reorderableLazyColumnState.isAnyItemDragging) {
            isDragging = true
            // Optional: Add a small delay to avoid triggering on very short drags
            delay(50)
        } else if (isDragging) {
            isDragging = false
            fieldEvent(DragEvent.OnDragEnd)
            hapticFeedback.performHapticFeedback(ReorderHapticFeedbackType.MOVE)
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxSize()
            .nestedScroll(rememberNestedScrollInteropConnection())
            .background(
                color = colorResource(id = R.color.background_primary),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
    ) {
        Dragger(
            modifier = Modifier
                .padding(vertical = 6.dp)
                .verticalScroll(state = scrollState)
                .align(Alignment.CenterHorizontally)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .verticalScroll(state = scrollState)
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
                .verticalScroll(state = scrollState)
                .background(color = colorResource(R.color.shape_transparent_secondary)),
            uiTitleState = uiTitleState,
            uiIconState = uiIconState
        )
        Items(
            uiFieldsListState = uiFieldsListState,
            lazyListState = lazyListState,
            reorderableLazyColumnState = reorderableLazyColumnState,
            hapticFeedback = hapticFeedback,
            paddingValues = PaddingValues(0.dp),
            fieldEvent = fieldEvent
        )
    }

    if (uiEditPropertyState is UiEditPropertyState.Visible) {
        PropertyScreen(
            modifier = Modifier.fillMaxWidth(),
            uiState = uiEditPropertyState,
            onDismissRequest = { fieldEvent(OnEditPropertyScreenDismiss) },
            onFormatClick = {},
            onSaveButtonClicked = { fieldEvent(EditProperty.OnSaveButtonClicked) },
            onCreateNewButtonClicked = {},
            onPropertyNameUpdate = { fieldEvent(EditProperty.OnPropertyNameUpdate(it)) },
            onMenuUnlinkClick = { fieldEvent(OnRemoveFromTypeClick(it)) },
            onLimitTypesClick = { fieldEvent(OnLimitTypesClick) },
            onDismissLimitTypes = { fieldEvent(OnLimitTypesDismiss) },
        )
    }

    if (uiFieldLocalInfoState is UiLocalsFieldsInfoState.Visible) {
        SectionLocalFieldsInfo(
            modifier = Modifier.fillMaxWidth(),
            state = uiFieldLocalInfoState,
            fieldEvent = fieldEvent
        )
    }
}

@Composable
private fun Items(
    lazyListState: LazyListState,
    reorderableLazyColumnState: ReorderableLazyListState,
    uiFieldsListState: UiFieldsListState,
    paddingValues: PaddingValues,
    hapticFeedback: ReorderHapticFeedback,
    fieldEvent: (FieldEvent) -> Unit,
) {
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
        state = lazyListState,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            count = uiFieldsListState.items.size,
            key = { index -> uiFieldsListState.items[index].id },
            contentType = { index -> getContentType(uiFieldsListState.items[index]) },
            itemContent = { index ->
                val item = uiFieldsListState.items[index]
                when (item) {
                    is UiFieldsListItem.Item.Draggable -> {
                        FieldItemDraggable(
                            modifier = commonItemModifier(),
                            item = item,
                            reorderingState = reorderableLazyColumnState,
                            fieldEvent = fieldEvent,
                            hapticFeedback = hapticFeedback
                        )
                    }

                    is UiFieldsListItem.Item.Local -> {
                        FieldItemLocal(
                            modifier = commonItemModifier(),
                            item = item,
                            fieldEvent = fieldEvent
                        )
                    }

                    is Section.SideBar -> {
                        SectionItem(
                            item = item,
                            reorderingState = reorderableLazyColumnState,
                            fieldEvent = fieldEvent,
                            isReorderable = true,
                            onAddIconClick = {
                                fieldEvent(FieldEvent.Section.OnAddToSidebarIconClick)
                            }
                        )
                    }
                    is Section.Hidden -> {
                        SectionItem(
                            item = item,
                            reorderingState = reorderableLazyColumnState,
                            fieldEvent = fieldEvent,
                            isReorderable = true
                        )
                    }
                    is Section.Header -> {
                        SectionItem(
                            item = item,
                            reorderingState = reorderableLazyColumnState,
                            fieldEvent = fieldEvent,
                            isReorderable = false
                        )
                    }
                    is Section.Local,
                    is Section.File -> {
                        SectionItem(
                            item = item,
                            reorderingState = reorderableLazyColumnState,
                            fieldEvent = fieldEvent,
                            isReorderable = false
                        )
                    }
                }
            }
        )
        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

/** Returns a content type string based on the item type. **/
private fun getContentType(item: UiFieldsListItem): String {
    return when (item) {
        is UiFieldsListItem.Item.Draggable -> FieldsItemsContentType.FIELD_ITEM_DRAGGABLE
        is UiFieldsListItem.Item.Local -> FieldsItemsContentType.FIELD_ITEM_LOCAL
        is Section.SideBar -> FieldsItemsContentType.SECTION_SIDEBAR
        is Section.Header -> FieldsItemsContentType.SECTION_HEADER
        is Section.Hidden -> FieldsItemsContentType.SECTION_HIDDEN
        is Section.Local -> FieldsItemsContentType.SECTION_LOCAL
        is Section.File -> FieldsItemsContentType.SECTION_FILE
    }
}

/** A common modifier for list items. **/
@Composable
fun LazyItemScope.commonItemModifier() = Modifier
    .height(48.dp)
    .fillMaxWidth()
    .padding(horizontal = 16.dp)
    .border(
        width = 1.dp,
        color = colorResource(id = R.color.shape_primary),
        shape = RoundedCornerShape(12.dp)
    )
    .animateItem()

@Composable
private fun TopBar(
    modifier: Modifier,
    uiTitleState: UiTitleState,
    uiIconState: UiIconState,
    onBackClick: () -> Unit = {}
) {
    val modifier = if (Build.VERSION.SDK_INT >= EDGE_TO_EDGE_MIN_SDK) {
        modifier.windowInsetsPadding(WindowInsets.statusBars)
    } else {
        modifier
    }
    Column(
        modifier = modifier
            .background(
                color = colorResource(id = R.color.background_primary),
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(56.dp)
                    .height(48.dp)
                    .align(Alignment.CenterStart)
                    .noRippleThrottledClickable {
                        onBackClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    modifier = Modifier.wrapContentSize(),
                    painter = painterResource(R.drawable.ic_default_top_back),
                    contentDescription = stringResource(R.string.content_desc_back_button)
                )
            }

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
                .background(color = colorResource(R.color.shape_transparent_secondary)),
            uiTitleState = uiTitleState,
            uiIconState = uiIconState
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
        Spacer(modifier = Modifier.width(4.dp))
        ListWidgetObjectIcon(
            modifier = Modifier,
            icon = uiIconState.icon,
            backgroundColor = R.color.transparent_black,
            iconSize = 16.dp
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            modifier = Modifier,
            text = uiTitleState.originalName,
            style = Caption1Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = colorResource(id = R.color.text_primary),
        )
    }
}

@Composable
private fun LazyItemScope.SectionItem(
    item: UiFieldsListItem.Section,
    reorderingState: ReorderableLazyListState,
    isReorderable: Boolean = true,
    onAddIconClick: () -> Unit = {},
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
            id = R.color.text_secondary
        )

        is Section.File -> stringResource(R.string.object_type_fields_section_file) to colorResource(
            id = R.color.text_secondary
        )
    }
    ReorderableItem(
        state = reorderingState,
        key = item.id,
        enabled = isReorderable
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    modifier = Modifier
                        .padding(bottom = 4.dp, start = 20.dp)
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
                                onAddIconClick()
                            }
                    ) {
                        Image(
                            modifier = Modifier
                                .padding(bottom = 2.dp, end = 20.dp)
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
            if (item.isEmptyState) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 16.dp, bottom = 8.dp),
                    style = Relations1,
                    color = colorResource(R.color.text_tertiary),
                    text = stringResource(R.string.object_type_fields_section_empty_state_text)
                )
            }
        }
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
                    .padding(start = 14.dp, end = 8.dp)
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
            style = Relations1,
            color = colorResource(id = R.color.text_primary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Image(
            modifier = Modifier
                .padding(end = 14.dp)
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
    reorderingState: ReorderableLazyListState,
    hapticFeedback: ReorderHapticFeedback,
    item: UiFieldsListItem.Item.Draggable,
    fieldEvent: (FieldEvent) -> Unit
) {
    val isMenuExpanded = remember { mutableStateOf(false) }

    val haptic = LocalHapticFeedback.current

    ReorderableItem(
        state = reorderingState,
        key = item.id,
    ) { isDragging ->
        Row(
            modifier = modifier,
            verticalAlignment = CenterVertically
        ) {
            val formatIcon = item.format.simpleIcon()
            if (formatIcon != null) {
                Image(
                    modifier = Modifier
                        .padding(start = 14.dp, end = 8.dp)
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
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
                    style = Relations1,
                    color = colorResource(id = R.color.text_primary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (item.isPossibleToDrag) {
                Image(
                    modifier = Modifier
                        .padding(end = 14.dp)
                        .size(24.dp)
                        .draggableHandle(
                            onDragStarted = {
                                hapticFeedback.performHapticFeedback(ReorderHapticFeedbackType.START)
                            },
                            onDragStopped = {
                                hapticFeedback.performHapticFeedback(ReorderHapticFeedbackType.END)
                            }
                        ),
                    painter = painterResource(R.drawable.ic_dnd),
                    contentDescription = "Icon drag"
                )
            }

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
            is UiFieldsListItem.Item.Draggable -> {
                if (item.isPossibleToUnlinkFromType) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(R.string.object_type_fields_menu_remove_from_type),
                                style = UxSmallTextRegular,
                                color = colorResource(id = R.color.text_primary)
                            )
                        },
                        onClick = {
                            onFieldEvent(OnRemoveFromTypeClick(id = item.id))
                        },
                    )
                }
                Divider(paddingStart = 0.dp, paddingEnd = 0.dp)
                if (item.isPossibleToMoveToBin) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(R.string.object_type_fields_menu_move_to_bin),
                                style = UxSmallTextRegular,
                                color = colorResource(id = R.color.palette_system_red)
                            )
                        },
                        onClick = {
                            onFieldEvent(OnMoveToBinClick(id = item.id))
                        },
                    )
                }
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
                        onFieldEvent(OnAddLocalToTypeClick(item))
                    },
                )
//                DropdownMenuItem(
//                    text = {
//                        Text(
//                            text = stringResource(R.string.object_type_fields_menu_remove),
//                            style = BodyCalloutRegular,
//                            color = colorResource(id = R.color.palette_system_red)
//                        )
//                    },
//                    onClick = {
//                        onFieldEvent(FieldItemMenu.OnRemoveLocalClick(item))
//                    },
//                )
            }
        }
    }
}

@DefaultPreviews
@Composable
fun PreviewTypeFieldsMainScreen() {
    FieldsMainScreen(
        uiTitleState = UiTitleState(title = "Pages", originalName = "Page", isEditable = false),
        uiIconState = UiIconState(icon = ObjectIcon.TypeIcon.Default.DEFAULT, isEditable = false),
        uiFieldsListState = UiFieldsListState(
            items = listOf(
                UiFieldsListItem.Section.Header(isEmptyState = true),
                UiFieldsListItem.Item.Draggable(
                    id = "id1",
                    fieldKey = "key1",
                    fieldTitle = "Status",
                    format = RelationFormat.STATUS,
                    isPossibleToUnlinkFromType = true,
                    isPossibleToMoveToBin = true,
                    isEditableField = true,
                    limitObjectTypes = listOf(),
                    isPossibleToDrag = false
                ),
                UiFieldsListItem.Item.Draggable(
                    id = "id2",
                    fieldKey = "key2",
                    fieldTitle = "Very long field title, just to test how it looks",
                    format = RelationFormat.LONG_TEXT,
                    isPossibleToUnlinkFromType = true,
                    isPossibleToMoveToBin = true,
                    isEditableField = true,
                    limitObjectTypes = listOf(),
                    isPossibleToDrag = true
                ),
                UiFieldsListItem.Section.SideBar(
                    canAdd = true
                ),
                UiFieldsListItem.Item.Draggable(
                    id = "id3",
                    fieldKey = "key3",
                    fieldTitle = "Links",
                    format = RelationFormat.URL,
                    isEditableField = true,
                    isPossibleToUnlinkFromType = true,
                    isPossibleToMoveToBin = true,
                    limitObjectTypes = listOf(),
                    isPossibleToDrag = true
                ),
                UiFieldsListItem.Item.Draggable(
                    id = "id4",
                    fieldKey = "key4",
                    fieldTitle = "Very long field title, just to test how it looks",
                    format = RelationFormat.DATE,
                    isEditableField = true,
                    isPossibleToUnlinkFromType = true,
                    isPossibleToMoveToBin = true,
                    limitObjectTypes = listOf(),
                    isPossibleToDrag = false
                ),
                UiFieldsListItem.Section.Hidden(),
                UiFieldsListItem.Item.Draggable(
                    id = "id555",
                    fieldKey = "key555",
                    fieldTitle = "Hidden field",
                    format = RelationFormat.LONG_TEXT,
                    isEditableField = true,
                    isPossibleToUnlinkFromType = true,
                    isPossibleToMoveToBin = true,
                    limitObjectTypes = listOf(),
                    isPossibleToDrag = true
                ),
                UiFieldsListItem.Section.Local(),
                UiFieldsListItem.Item.Local(
                    id = "id5",
                    fieldKey = "key5",
                    fieldTitle = "Local field",
                    format = RelationFormat.LONG_TEXT,
                    isEditableField = true,
                    limitObjectTypes = listOf()
                ),
                UiFieldsListItem.Item.Local(
                    id = "id6",
                    fieldKey = "key6",
                    fieldTitle = "Local Very long field title, just to test how it looks",
                    format = RelationFormat.LONG_TEXT,
                    isEditableField = true,
                    limitObjectTypes = listOf()
                )
            )
        ),
        fieldEvent = {},
        uiEditPropertyState = UiEditPropertyState.Hidden,
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
    const val SECTION_FILE = "content_type_section_file"
}