package com.anytypeio.anytype.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_ui.extensions.throttledClick
import com.anytypeio.anytype.core_ui.foundation.components.BottomNavigationMenu
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.UXBody
import com.anytypeio.anytype.core_ui.widgets.dv.DefaultDragAndDropModifier
import com.anytypeio.anytype.presentation.home.InteractionMode
import com.anytypeio.anytype.presentation.navigation.NavPanelState
import com.anytypeio.anytype.presentation.widgets.DropDownMenuAction
import com.anytypeio.anytype.presentation.widgets.FromIndex
import com.anytypeio.anytype.presentation.widgets.SectionType
import com.anytypeio.anytype.presentation.widgets.ToIndex
import com.anytypeio.anytype.presentation.widgets.TreePath
import com.anytypeio.anytype.presentation.widgets.ViewId
import com.anytypeio.anytype.presentation.widgets.Widget.Source.Companion.SECTION_OBJECT_TYPE
import com.anytypeio.anytype.presentation.widgets.Widget.Source.Companion.SECTION_PINNED
import com.anytypeio.anytype.presentation.widgets.WidgetId
import com.anytypeio.anytype.presentation.widgets.WidgetView
import com.anytypeio.anytype.ui.widgets.menu.getWidgetMenuItems
import com.anytypeio.anytype.ui.widgets.types.AllContentWidgetCard
import com.anytypeio.anytype.ui.widgets.types.BinWidgetCard
import com.anytypeio.anytype.ui.widgets.types.DataViewListWidgetCard
import com.anytypeio.anytype.ui.widgets.types.EmptyStateWidgetScreen
import com.anytypeio.anytype.ui.widgets.types.GalleryWidgetCard
import com.anytypeio.anytype.ui.widgets.types.LinkWidgetCard
import com.anytypeio.anytype.ui.widgets.types.ListWidgetCard
import com.anytypeio.anytype.ui.widgets.types.SpaceChatWidgetCard
import com.anytypeio.anytype.ui.widgets.types.TreeWidgetCard
import kotlinx.coroutines.delay
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun HomeScreen(
    navPanelState: NavPanelState,
    modifier: Modifier,
    mode: InteractionMode,
    widgets: List<WidgetView>,
    onExpand: (TreePath) -> Unit,
    onWidgetElementClicked: (WidgetId, ObjectWrapper.Basic) -> Unit,
    onWidgetMenuTriggered: (WidgetId) -> Unit,
    onWidgetSourceClicked: (WidgetId) -> Unit,
    onCreateWidget: () -> Unit,
    onWidgetMenuAction: (WidgetId, DropDownMenuAction) -> Unit,
    onChangeWidgetView: (WidgetId, ViewId) -> Unit,
    onToggleExpandedWidgetState: (WidgetId) -> Unit,
    onExitEditMode: () -> Unit,
    onSearchClicked: () -> Unit,
    onHomeButtonClicked: () -> Unit,
    onCreateNewObjectClicked: () -> Unit,
    onCreateNewObjectLongClicked: () -> Unit,
    onNavBarShareButtonClicked: () -> Unit,
    onObjectCheckboxClicked: (Id, Boolean) -> Unit,
    onMove: (List<WidgetView>, FromIndex, ToIndex) -> Unit,
    onCreateElement: (WidgetView) -> Unit = {},
    onCreateNewTypeClicked: () -> Unit,
    onSectionClicked: (Id) -> Unit = {}
) {

    Box(modifier = modifier.fillMaxSize()) {
        WidgetList(
            widgets = widgets,
            onExpand = onExpand,
            onWidgetMenuAction = onWidgetMenuAction,
            onWidgetElementClicked = onWidgetElementClicked,
            onWidgetSourceClicked = onWidgetSourceClicked,
            onToggleExpandedWidgetState = onToggleExpandedWidgetState,
            mode = mode,
            onChangeWidgetView = onChangeWidgetView,
            onMove = onMove,
            onObjectCheckboxClicked = onObjectCheckboxClicked,
            onCreateWidget = onCreateWidget,
            onCreateElement = onCreateElement,
            onWidgetMenuTriggered = onWidgetMenuTriggered,
            onCreateNewTypeClicked = onCreateNewTypeClicked,
            onSectionClicked = onSectionClicked
        )
        AnimatedVisibility(
            visible = mode is InteractionMode.Edit,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 26.dp, vertical = 20.dp),
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it }
        ) {
            Row(
                modifier = Modifier
                    .height(52.dp)
                    .fillMaxWidth()
            ) {
                WidgetEditModeButton(
                    text = stringResource(id = R.string.add),
                    modifier = Modifier.weight(1f),
                    onClick = throttledClick(onCreateWidget)
                )
                Spacer(modifier = Modifier.width(10.dp))
                WidgetEditModeButton(
                    text = stringResource(id = R.string.done),
                    modifier = Modifier.weight(1f),
                    onClick = throttledClick(onExitEditMode)
                )
            }
        }
        AnimatedVisibility(
            visible = mode !is InteractionMode.Edit,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp),
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it }
        ) {
            BottomNavigationMenu(
                state = navPanelState,
                modifier = Modifier,
                onSearchClick = onSearchClicked,
                onAddDocClick = onCreateNewObjectClicked,
                onAddDocLongClick = onCreateNewObjectLongClicked,
                onShareButtonClicked = onNavBarShareButtonClicked,
                onHomeButtonClicked = onHomeButtonClicked
            )
        }
    }

    BackHandler(enabled = mode is InteractionMode.Edit) { onExitEditMode() }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WidgetList(
    widgets: List<WidgetView>,
    onExpand: (TreePath) -> Unit,
    onWidgetMenuAction: (WidgetId, DropDownMenuAction) -> Unit,
    onWidgetElementClicked: (WidgetId, ObjectWrapper.Basic) -> Unit,
    onWidgetSourceClicked: (WidgetId) -> Unit,
    onWidgetMenuTriggered: (WidgetId) -> Unit,
    onToggleExpandedWidgetState: (WidgetId) -> Unit,
    mode: InteractionMode,
    onChangeWidgetView: (WidgetId, ViewId) -> Unit,
    onMove: (List<WidgetView>, FromIndex, ToIndex) -> Unit,
    onObjectCheckboxClicked: (Id, Boolean) -> Unit,
    onCreateWidget: () -> Unit,
    onCreateElement: (WidgetView) -> Unit = {},
    onCreateNewTypeClicked: () -> Unit,
    onSectionClicked: (Id) -> Unit = {}
) {

    val view = LocalView.current

    val views = remember { mutableStateOf(widgets) }
    views.value = widgets

    val lazyListState = rememberLazyListState()

    val lastFromIndex = remember { mutableStateOf<Int?>(null) }
    val lastToIndex = remember { mutableStateOf<Int?>(null) }

    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        lastFromIndex.value = from.index
        lastToIndex.value = to.index

        views.value = views.value.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
        ViewCompat.performHapticFeedback(
            view,
            HapticFeedbackConstantsCompat.SEGMENT_FREQUENT_TICK
        )
    }

    val onDragStoppedHandler = {
        val from = lastFromIndex.value
        val to = lastToIndex.value
        if (from != null && to != null && from != to) {
            onMove(views.value, from, to)
        }
        // Reset after firing
        lastFromIndex.value = null
        lastToIndex.value = null
    }

    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxSize()
    ) {
        itemsIndexed(
            items = views.value,
            key = { _, item -> item.id }
        ) { index, item ->
            when (item) {
                is WidgetView.Tree -> {
                    val isCardMenuExpanded = remember { mutableStateOf(false) }
                    val menuItems = item.getWidgetMenuItems()
                    val isReorderEnabled = item.sectionType == SectionType.PINNED && mode !is InteractionMode.ReadOnly

                    ReorderableItem(
                        enabled = isReorderEnabled,
                        state = reorderableLazyListState,
                        key = item.id
                    ) { isDragged ->
                        val hasStartedDragging = remember { mutableStateOf(false) }

                        if (isReorderEnabled) {
                            LaunchedEffect(isDragged) {
                                if (isDragged) {
                                    hasStartedDragging.value = true
                                    delay(1000)
                                    isCardMenuExpanded.value = false
                                } else if (hasStartedDragging.value) {
                                    hasStartedDragging.value = false
                                }
                            }
                        }

                        val modifier = WidgetCardModifier(
                            isMenuExpanded = isCardMenuExpanded.value,
                            mode = mode,
                            onWidgetClicked = { onWidgetSourceClicked(item.id) },
                            onWidgetLongClicked = {
                                isCardMenuExpanded.value = !isCardMenuExpanded.value
                            },
                            dragModifier = if (isReorderEnabled) DefaultDragAndDropModifier(
                                view,
                                onDragStoppedHandler
                            ) else null,
                            shouldEnableLongClick = menuItems.isNotEmpty() && mode !is InteractionMode.ReadOnly
                        )

                        TreeWidgetCard(
                            modifier = modifier,
                            mode = mode,
                            item = item,
                            onExpandElement = onExpand,
                            onWidgetElementClicked = { obj ->
                                onWidgetElementClicked(item.id, obj)
                            },
                            onWidgetSourceClicked = onWidgetSourceClicked,
                            onWidgetMenuClicked = onWidgetMenuTriggered,
                            onDropDownMenuAction = { action ->
                                onWidgetMenuAction(item.id, action)
                            },
                            onToggleExpandedWidgetState = onToggleExpandedWidgetState,
                            onObjectCheckboxClicked = onObjectCheckboxClicked,
                            onCreateElement = onCreateElement,
                            menuItems = menuItems,
                            isCardMenuExpanded = isCardMenuExpanded
                        )
                    }
                }

                is WidgetView.Link -> {
                    val isCardMenuExpanded = remember { mutableStateOf(false) }
                    val menuItems = item.getWidgetMenuItems()
                    val isReorderEnabled = item.sectionType == SectionType.PINNED && mode !is InteractionMode.ReadOnly

                    ReorderableItem(
                        enabled = isReorderEnabled,
                        state = reorderableLazyListState,
                        key = item.id
                    ) { isDragged ->
                        val hasStartedDragging = remember { mutableStateOf(false) }

                        if (isReorderEnabled) {
                            LaunchedEffect(isDragged) {
                                if (isDragged) {
                                    hasStartedDragging.value = true
                                    delay(1000)
                                    isCardMenuExpanded.value = false
                                } else if (hasStartedDragging.value) {
                                    hasStartedDragging.value = false
                                }
                            }
                        }

                        val modifier = WidgetCardModifier(
                            isMenuExpanded = isCardMenuExpanded.value,
                            mode = mode,
                            onWidgetClicked = { onWidgetSourceClicked(item.id) },
                            onWidgetLongClicked = {
                                isCardMenuExpanded.value = !isCardMenuExpanded.value
                            },
                            dragModifier = if (isReorderEnabled) DefaultDragAndDropModifier(
                                view,
                                onDragStoppedHandler
                            ) else null,
                            shouldEnableLongClick = menuItems.isNotEmpty() && mode !is InteractionMode.ReadOnly
                        )

                        LinkWidgetCard(
                            modifier = modifier,
                            item = item,
                            onDropDownMenuAction = { action ->
                                onWidgetMenuAction(item.id, action)
                            },
                            onObjectCheckboxClicked = onObjectCheckboxClicked,
                            menuItems = menuItems,
                            isCardMenuExpanded = isCardMenuExpanded
                        )
                    }
                }

                is WidgetView.SetOfObjects -> {
                    val isCardMenuExpanded = remember { mutableStateOf(false) }
                    val menuItems = item.getWidgetMenuItems()
                    val isReorderEnabled = item.sectionType == SectionType.PINNED && mode !is InteractionMode.ReadOnly

                    ReorderableItem(
                        enabled = isReorderEnabled,
                        state = reorderableLazyListState,
                        key = item.id
                    ) { isDragged ->
                        val hasStartedDragging = remember { mutableStateOf(false) }

                        if (isReorderEnabled) {
                            LaunchedEffect(isDragged) {
                                if (isDragged) {
                                    hasStartedDragging.value = true
                                    delay(1000)
                                    isCardMenuExpanded.value = false
                                } else if (hasStartedDragging.value) {
                                    hasStartedDragging.value = false
                                }
                            }
                        }

                        val modifier = WidgetCardModifier(
                            isMenuExpanded = isCardMenuExpanded.value,
                            mode = mode,
                            onWidgetClicked = { onWidgetSourceClicked(item.id) },
                            onWidgetLongClicked = {
                                isCardMenuExpanded.value = !isCardMenuExpanded.value
                            },
                            dragModifier = if (isReorderEnabled) DefaultDragAndDropModifier(
                                view,
                                onDragStoppedHandler
                            ) else null,
                            shouldEnableLongClick = menuItems.isNotEmpty() && mode !is InteractionMode.ReadOnly
                        )

                        DataViewListWidgetCard(
                            modifier = modifier,
                            item = item,
                            mode = mode,
                            onWidgetObjectClicked = { obj ->
                                onWidgetElementClicked(item.id, obj)
                            },
                            onWidgetSourceClicked = onWidgetSourceClicked,
                            onWidgetMenuTriggered = onWidgetMenuTriggered,
                            onDropDownMenuAction = { action ->
                                onWidgetMenuAction(item.id, action)
                            },
                            onChangeWidgetView = onChangeWidgetView,
                            onToggleExpandedWidgetState = onToggleExpandedWidgetState,
                            onObjectCheckboxClicked = onObjectCheckboxClicked,
                            onCreateElement = onCreateElement,
                            menuItems = menuItems,
                            isCardMenuExpanded = isCardMenuExpanded
                        )
                    }
                }

                is WidgetView.Gallery -> {
                    val isCardMenuExpanded = remember { mutableStateOf(false) }
                    val menuItems = item.getWidgetMenuItems()
                    val isReorderEnabled = item.sectionType == SectionType.PINNED && mode !is InteractionMode.ReadOnly

                    ReorderableItem(
                        enabled = isReorderEnabled,
                        state = reorderableLazyListState,
                        key = item.id
                    ) { isDragged ->
                        val hasStartedDragging = remember { mutableStateOf(false) }

                        if (isReorderEnabled) {
                            LaunchedEffect(isDragged) {
                                if (isDragged) {
                                    hasStartedDragging.value = true
                                    delay(1000)
                                    isCardMenuExpanded.value = false
                                } else if (hasStartedDragging.value) {
                                    hasStartedDragging.value = false
                                }
                            }
                        }

                        val modifier = WidgetCardModifier(
                            isMenuExpanded = isCardMenuExpanded.value,
                            mode = mode,
                            onWidgetClicked = { onWidgetSourceClicked(item.id) },
                            onWidgetLongClicked = {
                                isCardMenuExpanded.value = !isCardMenuExpanded.value
                            },
                            dragModifier = if (isReorderEnabled) DefaultDragAndDropModifier(
                                view,
                                onDragStoppedHandler
                            ) else null,
                            shouldEnableLongClick = menuItems.isNotEmpty() && mode !is InteractionMode.ReadOnly
                        )

                        GalleryWidgetCard(
                            modifier = modifier,
                            item = item,
                            mode = mode,
                            onWidgetObjectClicked = { obj ->
                                onWidgetElementClicked(item.id, obj)
                            },
                            onWidgetSourceClicked = onWidgetSourceClicked,
                            onWidgetMenuTriggered = onWidgetMenuTriggered,
                            onDropDownMenuAction = { action ->
                                onWidgetMenuAction(item.id, action)
                            },
                            onChangeWidgetView = onChangeWidgetView,
                            onToggleExpandedWidgetState = onToggleExpandedWidgetState,
                            onObjectCheckboxClicked = onObjectCheckboxClicked,
                            onCreateElement = onCreateElement,
                            menuItems = menuItems,
                            isCardMenuExpanded = isCardMenuExpanded
                        )
                    }
                }

                is WidgetView.ListOfObjects -> {
                    val isCardMenuExpanded = remember { mutableStateOf(false) }
                    val menuItems = item.getWidgetMenuItems()
                    val isReorderEnabled = item.sectionType == SectionType.PINNED && mode !is InteractionMode.ReadOnly

                    ReorderableItem(
                        enabled = isReorderEnabled,
                        state = reorderableLazyListState,
                        key = item.id
                    ) { isDragged ->
                        val hasStartedDragging = remember { mutableStateOf(false) }

                        if (isReorderEnabled) {
                            LaunchedEffect(isDragged) {
                                if (isDragged) {
                                    hasStartedDragging.value = true
                                    delay(1000)
                                    isCardMenuExpanded.value = false
                                } else if (hasStartedDragging.value) {
                                    hasStartedDragging.value = false
                                }
                            }
                        }

                        val modifier = WidgetCardModifier(
                            isMenuExpanded = isCardMenuExpanded.value,
                            mode = mode,
                            onWidgetClicked = { onWidgetSourceClicked(item.id) },
                            onWidgetLongClicked = {
                                isCardMenuExpanded.value = !isCardMenuExpanded.value
                            },
                            dragModifier = if (isReorderEnabled) DefaultDragAndDropModifier(
                                view,
                                onDragStoppedHandler
                            ) else null,
                            shouldEnableLongClick = menuItems.isNotEmpty() && mode !is InteractionMode.ReadOnly
                        )

                        ListWidgetCard(
                            modifier = modifier,
                            item = item,
                            mode = mode,
                            onWidgetObjectClicked = { obj ->
                                onWidgetElementClicked(item.id, obj)
                            },
                            onWidgetSourceClicked = onWidgetSourceClicked,
                            onWidgetMenuTriggered = onWidgetMenuTriggered,
                            onDropDownMenuAction = { action ->
                                onWidgetMenuAction(item.id, action)
                            },
                            onToggleExpandedWidgetState = onToggleExpandedWidgetState,
                            onObjectCheckboxClicked = onObjectCheckboxClicked,
                            onCreateElement = onCreateElement,
                            menuItems = menuItems,
                            isCardMenuExpanded = isCardMenuExpanded
                        )
                    }
                }

                is WidgetView.Bin -> {
                    BinWidgetCard(
                        item = item,
                        onDropDownMenuAction = { action ->
                            onWidgetMenuAction(item.id, action)
                        },
                        onWidgetSourceClicked = onWidgetSourceClicked,
                    )
                }

                is WidgetView.AllContent -> {
                    val isCardMenuExpanded = remember { mutableStateOf(false) }
                    val menuItems = item.getWidgetMenuItems()
                    val isReorderEnabled = item.sectionType == SectionType.PINNED && mode !is InteractionMode.ReadOnly

                    ReorderableItem(
                        enabled = isReorderEnabled,
                        state = reorderableLazyListState,
                        key = item.id
                    ) { isDragged ->
                        val hasStartedDragging = remember { mutableStateOf(false) }

                        if (isReorderEnabled) {
                            LaunchedEffect(isDragged) {
                                if (isDragged) {
                                    hasStartedDragging.value = true
                                    delay(1000)
                                    isCardMenuExpanded.value = false
                                } else if (hasStartedDragging.value) {
                                    hasStartedDragging.value = false
                                }
                            }
                        }

                        val modifier = WidgetCardModifier(
                            isMenuExpanded = isCardMenuExpanded.value,
                            mode = mode,
                            onWidgetClicked = { onWidgetSourceClicked(item.id) },
                            onWidgetLongClicked = {
                                isCardMenuExpanded.value = !isCardMenuExpanded.value
                            },
                            dragModifier = if (isReorderEnabled) DefaultDragAndDropModifier(
                                view,
                                onDragStoppedHandler
                            ) else null,
                            shouldEnableLongClick = menuItems.isNotEmpty() && mode !is InteractionMode.ReadOnly
                        )

                        AllContentWidgetCard(
                            modifier = modifier,
                            widgetView = item,
                            onDropDownMenuAction = { action ->
                                onWidgetMenuAction(item.id, action)
                            },
                            menuItems = menuItems,
                            isCardMenuExpanded = isCardMenuExpanded
                        )
                    }
                }

                is WidgetView.SpaceChat -> {
                    SpaceChatWidgetCard(
                        item = item,
                        mode = mode,
                        unReadMentionCount = item.unreadMentionCount,
                        unReadMessageCount = item.unreadMessageCount,
                        isMuted = item.isMuted,
                        onWidgetClicked = { onWidgetSourceClicked(item.id) },
                        onDropDownMenuAction = { action ->
                            onWidgetMenuAction(item.id, action)
                        }
                    )
                }

                is WidgetView.EmptyState -> {
                    if (mode !is InteractionMode.Edit) {
                        EmptyStateWidgetScreen(
                            modifier = Modifier.fillParentMaxSize(),
                            onAddWidgetClicked = {
                                onCreateWidget()
                            }
                        )
                    }
                }

                WidgetView.Section.ObjectTypes -> {
                    SpaceObjectTypesSectionHeader(
                        mode = mode,
                        onCreateNewTypeClicked = onCreateNewTypeClicked,
                        onSectionClicked = { onSectionClicked(SECTION_OBJECT_TYPE) }
                    )
                }

                WidgetView.Section.Pinned -> {
                    PinnedSectionHeader(
                        onSectionClicked = { onSectionClicked(SECTION_PINNED) }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(200.dp))
        }
    }
}

@Composable
fun WidgetEditModeButton(
    text: String,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .background(
                color = colorResource(id = R.color.widgets_edit_mode_button),
                shape = RoundedCornerShape(14.dp)
            )
            .noRippleClickable { onClick() }
    ) {
        Text(
            text = text,
            modifier = Modifier.align(Alignment.Center),
            style = UXBody,
            color = colorResource(id = R.color.text_white)
        )
    }
}

@Composable
private fun SpaceObjectTypesSectionHeader(
    mode: InteractionMode,
    onSectionClicked: () -> Unit,
    onCreateNewTypeClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .noRippleClickable { onSectionClicked() }
    ) {
        Text(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 12.dp),
            text = stringResource(R.string.widgets_section_object_types),
            style = Caption1Medium,
            color = colorResource(id = R.color.control_transparent_secondary)
        )
        if (mode !is InteractionMode.ReadOnly) {
            Image(
                painter = painterResource(id = R.drawable.ic_default_plus),
                contentDescription = "Create new type",
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 12.dp)
                    .size(18.dp)
                    .noRippleClickable { onCreateNewTypeClicked() },
                contentScale = ContentScale.Inside
            )
        }
    }
}

@Composable
private fun PinnedSectionHeader(
    onSectionClicked: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .noRippleClickable { onSectionClicked() }
    ) {
        Text(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 12.dp),
            text = stringResource(R.string.widgets_section_pinned),
            style = Caption1Medium,
            color = colorResource(id = R.color.control_transparent_secondary)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WidgetCardModifier(
    isMenuExpanded: Boolean,
    mode: InteractionMode,
    onWidgetClicked: () -> Unit,
    onWidgetLongClicked: () -> Unit,
    dragModifier: Modifier? = null,
    shouldEnableLongClick: Boolean = true
): Modifier {
    val haptic = LocalHapticFeedback.current

    var modifier = Modifier
        .fillMaxWidth()
        .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 6.dp)
        .alpha(if (isMenuExpanded) 0.8f else 1f)
        .background(
            shape = RoundedCornerShape(16.dp),
            color = colorResource(id = R.color.dashboard_card_background)
        )
        .then(
            if (mode is InteractionMode.ReadOnly) {
                Modifier.noRippleClickable { onWidgetClicked() }
            } else {
                if (shouldEnableLongClick) {
                    Modifier.combinedClickable(
                        onClick = { onWidgetClicked() },
                        onLongClick = {
                            onWidgetLongClicked()
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
                } else {
                    Modifier.noRippleClickable { onWidgetClicked() }
                }
            }
        )

    if (dragModifier != null) {
        modifier = modifier.then(dragModifier)
    }

    return modifier
}