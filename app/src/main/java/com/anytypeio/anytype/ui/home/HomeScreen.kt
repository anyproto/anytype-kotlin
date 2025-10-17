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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import sh.calvin.reorderable.ReorderableLazyListState
import sh.calvin.reorderable.rememberReorderableLazyListState
import android.view.View
import androidx.compose.foundation.lazy.LazyItemScope
import com.anytypeio.anytype.core_ui.common.ReorderHapticFeedbackType
import com.anytypeio.anytype.core_ui.common.rememberReorderHapticFeedback
import timber.log.Timber

@Composable
fun HomeScreen(
    navPanelState: NavPanelState,
    modifier: Modifier,
    mode: InteractionMode,
    pinnedWidgets: List<WidgetView>,
    typeWidgets: List<WidgetView>,
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
    onMovePinned: (List<WidgetView>, FromIndex, ToIndex) -> Unit,
    onMovePinnedEnd: () -> Unit,
    onMoveTypes: (List<WidgetView>, FromIndex, ToIndex) -> Unit,
    onMoveTypesEnd: () -> Unit = {},
    onCreateElement: (WidgetView) -> Unit = {},
    onCreateNewTypeClicked: () -> Unit,
    onSectionPinnedClicked: () -> Unit,
    onSectionTypesClicked: () -> Unit
) {

    Box(modifier = modifier.fillMaxSize()) {
        TwoSectionWidgetList(
            pinnedWidgets = pinnedWidgets,
            typeWidgets = typeWidgets,
            onExpand = onExpand,
            onWidgetMenuAction = onWidgetMenuAction,
            onWidgetElementClicked = onWidgetElementClicked,
            onWidgetSourceClicked = onWidgetSourceClicked,
            onToggleExpandedWidgetState = onToggleExpandedWidgetState,
            mode = mode,
            onChangeWidgetView = onChangeWidgetView,
            onMovePinned = onMovePinned,
            onMovePinnedEnd = onMovePinnedEnd,
            onMoveTypes = onMoveTypes,
            onMoveTypesEnd = onMoveTypesEnd,
            onObjectCheckboxClicked = onObjectCheckboxClicked,
            onCreateWidget = onCreateWidget,
            onCreateElement = onCreateElement,
            onWidgetMenuTriggered = onWidgetMenuTriggered,
            onCreateNewTypeClicked = onCreateNewTypeClicked,
            onSectionPinnedClicked = onSectionPinnedClicked,
            onSectionTypesClicked = onSectionTypesClicked
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
private fun TwoSectionWidgetList(
    pinnedWidgets: List<WidgetView>,
    typeWidgets: List<WidgetView>,
    onExpand: (TreePath) -> Unit,
    onWidgetMenuAction: (WidgetId, DropDownMenuAction) -> Unit,
    onWidgetElementClicked: (WidgetId, ObjectWrapper.Basic) -> Unit,
    onWidgetSourceClicked: (WidgetId) -> Unit,
    onWidgetMenuTriggered: (WidgetId) -> Unit,
    onToggleExpandedWidgetState: (WidgetId) -> Unit,
    mode: InteractionMode,
    onChangeWidgetView: (WidgetId, ViewId) -> Unit,
    onMovePinned: (List<WidgetView>, FromIndex, ToIndex) -> Unit,
    onMovePinnedEnd: () -> Unit,
    onMoveTypes: (List<WidgetView>, FromIndex, ToIndex) -> Unit,
    onMoveTypesEnd: () -> Unit,
    onObjectCheckboxClicked: (Id, Boolean) -> Unit,
    onCreateWidget: () -> Unit,
    onCreateElement: (WidgetView) -> Unit = {},
    onCreateNewTypeClicked: () -> Unit,
    onSectionPinnedClicked: () -> Unit,
    onSectionTypesClicked: () -> Unit
) {
    val view = LocalView.current
    val lazyListState = rememberLazyListState()
    val hapticFeedback = rememberReorderHapticFeedback()

    // Local state for immediate visual updates during drag
    // These sync with ViewModel state when not dragging
    var localPinnedWidgets by remember { mutableStateOf(pinnedWidgets) }
    var localTypeWidgets by remember { mutableStateOf(typeWidgets) }

    var isDraggingPinned by remember { mutableStateOf(false) }
    var isDraggingTypes by remember { mutableStateOf(false) }

    // Sync local state with ViewModel state when not dragging
    LaunchedEffect(pinnedWidgets, isDraggingPinned) {
        if (!isDraggingPinned) {
            localPinnedWidgets = pinnedWidgets
        }
    }

    LaunchedEffect(typeWidgets, isDraggingTypes) {
        if (!isDraggingTypes) {
            localTypeWidgets = typeWidgets
        }
    }

    // Compute section ranges within the LazyColumn
    // [0] Pinned section header (not reorderable)
    val pinnedHeaderCount = 1
    val pinnedStart = pinnedHeaderCount
    val pinnedEnd = pinnedStart + localPinnedWidgets.size - 1

    // [pinnedEnd + 1] Types section header (not reorderable)
    val typesHeaderCount = 1
    val afterPinnedHeader = pinnedStart + localPinnedWidgets.size + typesHeaderCount
    val typesStart = afterPinnedHeader
    val typesEnd = typesStart + localTypeWidgets.size - 1

    // Single reorderable state for both sections
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        // Only process moves within the same section
        when {
            from.index in pinnedStart..pinnedEnd && to.index in pinnedStart..pinnedEnd -> {
                val localFrom = from.index - pinnedStart
                val localTo = to.index - pinnedStart
                Timber.d("DROID-3965, Pinned move: globalFrom=${from.index}, globalTo=${to.index}, localFrom=$localFrom, localTo=$localTo")

                // Update local state immediately for smooth visual feedback
                localPinnedWidgets = localPinnedWidgets.toMutableList().apply {
                    add(localTo, removeAt(localFrom))
                }

                // Notify ViewModel about the move with the reordered list
                onMovePinned(localPinnedWidgets, localFrom, localTo)
                isDraggingPinned = true
                hapticFeedback.performHapticFeedback(ReorderHapticFeedbackType.MOVE)
            }
            from.index in typesStart..typesEnd && to.index in typesStart..typesEnd -> {
                val localFrom = from.index - typesStart
                val localTo = to.index - typesStart
                Timber.d("DROID-3965, Types move: globalFrom=${from.index}, globalTo=${to.index}, localFrom=$localFrom, localTo=$localTo")

                // Update local state immediately for smooth visual feedback
                localTypeWidgets = localTypeWidgets.toMutableList().apply {
                    add(localTo, removeAt(localFrom))
                }

                // Notify ViewModel about the move with the reordered list
                onMoveTypes(localTypeWidgets, localFrom, localTo)
                isDraggingTypes = true
                hapticFeedback.performHapticFeedback(ReorderHapticFeedbackType.MOVE)
            }
            else -> {
                // Cross-section drag - ignore
                Timber.d("DROID-3965, Ignoring cross-section drag: from=${from.index}, to=${to.index}")
            }
        }
    }

    // Track drag completion for pinned widgets
    LaunchedEffect(reorderableState.isAnyItemDragging, isDraggingPinned) {
        if (!reorderableState.isAnyItemDragging && isDraggingPinned) {
            Timber.d("DROID-3965, Pinned drag stopped, persisting final order")
            // Call ViewModel to persist the final order
            onMovePinnedEnd()
            // Reset dragging flag AFTER persistence to avoid triggering sync too early
            isDraggingPinned = false
        }
    }

    // Track drag completion for type widgets
    LaunchedEffect(reorderableState.isAnyItemDragging, isDraggingTypes) {
        if (!reorderableState.isAnyItemDragging && isDraggingTypes) {
            Timber.d("DROID-3965, Types drag stopped, persisting final order: ${localTypeWidgets.map { it.id.takeLast(4) }}")
            // Pass the final reordered list to ViewModel
            // onMoveTypes will store the order and call onTypeWidgetDragEnd internally
            onMoveTypes(localTypeWidgets, 0, 1) // Dummy indices to bypass validation
            // Reset dragging flag AFTER persistence to avoid triggering sync too early
            isDraggingTypes = false
        }
    }

    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxSize()
    ) {

        item {
            PinnedSectionHeader(
                onSectionClicked = onSectionPinnedClicked
            )
        }
        // Pinned widgets section
        renderWidgetSection(
            widgets = localPinnedWidgets,
            reorderableState = reorderableState,
            view = view,
            mode = mode,
            sectionType = SectionType.PINNED,
            isOtherSectionDragging = isDraggingTypes,
            onExpand = onExpand,
            onWidgetMenuAction = onWidgetMenuAction,
            onWidgetElementClicked = onWidgetElementClicked,
            onWidgetSourceClicked = onWidgetSourceClicked,
            onWidgetMenuTriggered = onWidgetMenuTriggered,
            onToggleExpandedWidgetState = onToggleExpandedWidgetState,
            onChangeWidgetView = onChangeWidgetView,
            onObjectCheckboxClicked = onObjectCheckboxClicked,
            onCreateElement = onCreateElement,
            onCreateWidget = onCreateWidget
        )

        item {
            SpaceObjectTypesSectionHeader(
                mode = mode,
                onCreateNewTypeClicked = onCreateNewTypeClicked,
                onSectionClicked = onSectionTypesClicked
            )
        }

        // Type widgets section
        renderWidgetSection(
            widgets = localTypeWidgets.also {
                Timber.d("DROID-3965, Rendering type widgets: ${it.map { w -> w.id.takeLast(4) }}")
            },
            reorderableState = reorderableState,
            view = view,
            mode = mode,
            sectionType = SectionType.TYPES,
            isOtherSectionDragging = isDraggingPinned,
            onExpand = onExpand,
            onWidgetMenuAction = onWidgetMenuAction,
            onWidgetElementClicked = onWidgetElementClicked,
            onWidgetSourceClicked = onWidgetSourceClicked,
            onWidgetMenuTriggered = onWidgetMenuTriggered,
            onToggleExpandedWidgetState = onToggleExpandedWidgetState,
            onChangeWidgetView = onChangeWidgetView,
            onObjectCheckboxClicked = onObjectCheckboxClicked,
            onCreateElement = onCreateElement,
            onCreateWidget = onCreateWidget
        )

        item {
            Spacer(modifier = Modifier.height(200.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.renderWidgetSection(
    widgets: List<WidgetView>,
    reorderableState: ReorderableLazyListState,
    view: View,
    mode: InteractionMode,
    sectionType: SectionType,
    isOtherSectionDragging: Boolean = false,
    onExpand: (TreePath) -> Unit,
    onWidgetMenuAction: (WidgetId, DropDownMenuAction) -> Unit,
    onWidgetElementClicked: (WidgetId, ObjectWrapper.Basic) -> Unit,
    onWidgetSourceClicked: (WidgetId) -> Unit,
    onWidgetMenuTriggered: (WidgetId) -> Unit,
    onToggleExpandedWidgetState: (WidgetId) -> Unit,
    onChangeWidgetView: (WidgetId, ViewId) -> Unit,
    onObjectCheckboxClicked: (Id, Boolean) -> Unit,
    onCreateElement: (WidgetView) -> Unit,
    onCreateWidget: () -> Unit
) {
    itemsIndexed(
        items = widgets,
        key = { _, item -> item.id },
        contentType = { _, item -> sectionType } // Optimize recompositions based on item type
    ) { index, item ->
        when (item) {
            is WidgetView.Tree -> {
                val isCardMenuExpanded = remember { mutableStateOf(false) }
                val menuItems = item.getWidgetMenuItems()
                val isReorderEnabled = mode !is InteractionMode.ReadOnly && !isOtherSectionDragging

                ReorderableItem(
                    enabled = isReorderEnabled,
                    state = reorderableState,
                    key = item.id,
                    animateItemModifier = Modifier
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
                        dragModifier = if (isReorderEnabled) DefaultDragAndDropModifier(view, {}) else null,
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
                val isReorderEnabled = mode !is InteractionMode.ReadOnly && !isOtherSectionDragging

                ReorderableItem(
                    enabled = isReorderEnabled,
                    state = reorderableState,
                    key = item.id,
                    animateItemModifier = Modifier
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
                        dragModifier = if (isReorderEnabled) DefaultDragAndDropModifier(view, {}) else null,
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
                val isReorderEnabled = mode !is InteractionMode.ReadOnly && !isOtherSectionDragging

                ReorderableItem(
                    enabled = isReorderEnabled,
                    state = reorderableState,
                    key = item.id,
                    animateItemModifier = Modifier
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
                        dragModifier = if (isReorderEnabled) DefaultDragAndDropModifier(view, {}) else null,
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
                val isReorderEnabled = mode !is InteractionMode.ReadOnly && !isOtherSectionDragging

                ReorderableItem(
                    enabled = isReorderEnabled,
                    state = reorderableState,
                    key = item.id,
                    animateItemModifier = Modifier
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
                        dragModifier = if (isReorderEnabled) DefaultDragAndDropModifier(view, {}) else null,
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
                val isReorderEnabled = mode !is InteractionMode.ReadOnly && !isOtherSectionDragging

                ReorderableItem(
                    enabled = isReorderEnabled,
                    state = reorderableState,
                    key = item.id,
                    animateItemModifier = Modifier
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
                        dragModifier = if (isReorderEnabled) DefaultDragAndDropModifier(view, {}) else null,
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
                val isReorderEnabled = mode !is InteractionMode.ReadOnly && !isOtherSectionDragging

                ReorderableItem(
                    enabled = isReorderEnabled,
                    state = reorderableState,
                    key = item.id,
                    animateItemModifier = Modifier
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
                        dragModifier = if (isReorderEnabled) DefaultDragAndDropModifier(view, {}) else null,
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
fun SpaceObjectTypesSectionHeader(
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
fun PinnedSectionHeader(
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
private fun LazyItemScope.WidgetCardModifier(
    isMenuExpanded: Boolean,
    mode: InteractionMode,
    onWidgetClicked: () -> Unit,
    onWidgetLongClicked: () -> Unit,
    dragModifier: Modifier? = null,
    shouldEnableLongClick: Boolean = true
): Modifier {
    val haptic = LocalHapticFeedback.current

    var modifier = Modifier
        .animateItem(
            placementSpec = null
        )
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
