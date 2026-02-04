package com.anytypeio.anytype.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.common.ReorderHapticFeedbackType
import com.anytypeio.anytype.core_ui.common.rememberReorderHapticFeedback
import com.anytypeio.anytype.core_ui.foundation.components.BottomNavigationMenu
import com.anytypeio.anytype.presentation.home.HomeScreenViewModel
import com.anytypeio.anytype.presentation.widgets.DropDownMenuAction
import com.anytypeio.anytype.presentation.widgets.SectionType
import com.anytypeio.anytype.presentation.widgets.Widget.Source.Companion.OBJECT_TYPES_GROUP_ID
import com.anytypeio.anytype.presentation.widgets.Widget.Source.Companion.SECTION_OBJECT_TYPE
import com.anytypeio.anytype.presentation.widgets.Widget.Source.Companion.SECTION_PINNED
import com.anytypeio.anytype.presentation.widgets.Widget.Source.Companion.SECTION_UNREAD
import com.anytypeio.anytype.presentation.widgets.Widget.Source.Companion.WIDGET_BIN_ID
import com.anytypeio.anytype.presentation.widgets.WidgetView
import com.anytypeio.anytype.presentation.widgets.extractWidgetId
import com.anytypeio.anytype.ui.widgets.types.AddWidgetButton
import com.anytypeio.anytype.ui.widgets.types.BinWidgetCard
import com.anytypeio.anytype.ui.widgets.types.ListWidgetElement
import com.anytypeio.anytype.ui.widgets.types.ObjectTypesGroupWidgetCard
import com.anytypeio.anytype.ui.widgets.types.getPrettyName
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState


@Composable
fun WidgetsScreen(
    viewModel: HomeScreenViewModel,
    paddingValues: PaddingValues
) {

    val view = LocalView.current
    val lazyListState = rememberLazyListState()
    val hapticFeedback = rememberReorderHapticFeedback()

    val mode = viewModel.mode.collectAsState().value
    val pinnedWidgets = viewModel.pinnedViews.collectAsState().value
    val typeWidgets = viewModel.typeViews.collectAsState().value
    val unreadWidget = viewModel.unreadView.collectAsState().value
    val binWidget = viewModel.binView.collectAsState().value
    val collapsedSections = viewModel.collapsedSections.collectAsState().value
    val sectionConfig = viewModel.widgetSections.collectAsState().value

    val pinnedUi = remember(pinnedWidgets) { pinnedWidgets.toMutableStateList() }
    
    // Extract type rows from the ObjectTypesGroup widget for drag-and-drop management
    val objectTypesGroupWidget = (typeWidgets.firstOrNull() as? WidgetView.ObjectTypesGroup)
    val typeRowsFromVm = objectTypesGroupWidget?.typeRows ?: emptyList()
    
    // Pending order tracks local drag state until ViewModel catches up
    val pendingTypeRowOrder = remember { mutableStateOf<List<String>?>(null) }
    
    // Compute actual display list: use pending order if set, otherwise ViewModel order
    val typeRowsUi = remember(typeRowsFromVm, pendingTypeRowOrder.value) {
        val pending = pendingTypeRowOrder.value
        if (pending != null) {
            // Reorder based on pending order
            pending.mapNotNull { id -> typeRowsFromVm.find { it.id == id } }.toMutableStateList()
        } else {
            typeRowsFromVm.toMutableStateList()
        }
    }
    
    // Clear pending order when ViewModel catches up (has same order)
    LaunchedEffect(typeRowsFromVm) {
        val pending = pendingTypeRowOrder.value
        if (pending != null) {
            val vmOrder = typeRowsFromVm.map { it.id }
            if (vmOrder == pending) {
                pendingTypeRowOrder.value = null
            }
        }
    }
    
    // Unread section visibility logic
    val unreadWidgetView = unreadWidget as? WidgetView.UnreadChatList
    val isUnreadSectionCollapsed = collapsedSections.contains(SECTION_UNREAD)
    
    // Track previous collapse state for unread section
    val wasUnreadCollapsed = remember { mutableStateOf(isUnreadSectionCollapsed) }
    val hadUnreadItems = remember { mutableStateOf(unreadWidgetView?.elements?.isNotEmpty() == true) }
    
    // When section becomes expanded, keep the flag true to prevent flicker
    if (!isUnreadSectionCollapsed && wasUnreadCollapsed.value) {
        hadUnreadItems.value = true
    }
    
    // Update previous state
    wasUnreadCollapsed.value = isUnreadSectionCollapsed
    
    // Set flag when items are present
    if (unreadWidgetView?.elements?.isNotEmpty() == true) {
        hadUnreadItems.value = true
    }
    
    // Reset flag when section is collapsed and has no items
    if (isUnreadSectionCollapsed && unreadWidgetView?.elements?.isEmpty() == true) {
        hadUnreadItems.value = false
    }
    
    // Show header if: has items OR is collapsed OR was previously shown
    val shouldShowUnreadSection = unreadWidgetView != null && 
        (unreadWidgetView.elements.isNotEmpty() || isUnreadSectionCollapsed || hadUnreadItems.value)

    // Determine if pinned section should be visible
    val isPinnedSectionCollapsed = collapsedSections.contains(SECTION_PINNED)

    // Track previous collapse state to detect expand transitions
    val wasCollapsed = remember { mutableStateOf(isPinnedSectionCollapsed) }
    val hadPinnedItems = remember { mutableStateOf(pinnedUi.isNotEmpty()) }

    // When section becomes expanded (transition from collapsed to expanded)
    // Keep the flag true to prevent flicker until items load
    if (!isPinnedSectionCollapsed && wasCollapsed.value) {
        hadPinnedItems.value = true
    }

    // Update previous state for next composition
    wasCollapsed.value = isPinnedSectionCollapsed

    // Set flag when items are present
    if (pinnedUi.isNotEmpty()) {
        hadPinnedItems.value = true
    }

    // Reset flag when section is collapsed and has no items
    if (isPinnedSectionCollapsed && pinnedUi.isEmpty()) {
        hadPinnedItems.value = false
    }

    // Show header if: has items OR is collapsed OR was previously shown (prevents flicker on expand)
    val shouldShowPinnedHeader = pinnedUi.isNotEmpty() || isPinnedSectionCollapsed || hadPinnedItems.value

    val isDraggingPinned = remember { mutableStateOf(false) }
    val isDraggingTypes = remember { mutableStateOf(false) }

    val reorderableState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        onMove = { from, to ->

            val fromType = from.contentType as? SectionType

            // Extract widget IDs from composite keys using extractWidgetId() extension
            val fromId = (from.key as? String)?.extractWidgetId()
            val toId = (to.key as? String)?.extractWidgetId()

            when (fromType) {
                SectionType.PINNED -> {
                    isDraggingPinned.value = true
                    val f = pinnedUi.indexOfFirst { it.id == fromId }
                    val t = pinnedUi.indexOfFirst { it.id == toId }
                    if (f != -1 && t != -1 && f != t) {
                        val item = pinnedUi.removeAt(f)
                        pinnedUi.add(t, item)
                        viewModel.onMovePinned(pinnedUi, f, t)
                        hapticFeedback.performHapticFeedback(ReorderHapticFeedbackType.MOVE)
                    }
                }
                SectionType.TYPES -> {
                    // Type rows are now handled by ReorderableColumn inside ObjectTypesGroupWidgetCard
                    // This branch is kept for backward compatibility but should not be triggered
                }
                else -> Unit
            }
        }
    )

    LaunchedEffect(reorderableState.isAnyItemDragging) {
        if (!reorderableState.isAnyItemDragging) {
            if (isDraggingPinned.value) {
                isDraggingPinned.value = false
            }
            if (isDraggingTypes.value) {
                viewModel.onTypeWidgetDragEnd()
                isDraggingTypes.value = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {

        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize()
        ) {

            // Unread section - shown at top when there are unread chats and section is visible
            if (shouldShowUnreadSection && sectionConfig.isSectionVisible(com.anytypeio.anytype.core_models.WidgetSectionType.UNREAD)) {
                item {
                    ReorderableItem(
                        enabled = false,
                        state = reorderableState,
                        key = SECTION_UNREAD,
                    ) {
                        UnreadSectionHeader(
                            onSectionClicked = viewModel::onSectionUnreadClicked
                        )
                    }
                }
            }
            
            // Unread widgets - only render when section is expanded and widget exists and section is visible
            if (shouldShowUnreadSection && !isUnreadSectionCollapsed && unreadWidgetView != null && sectionConfig.isSectionVisible(com.anytypeio.anytype.core_models.WidgetSectionType.UNREAD)) {
                item(key = "unread_widget_content") {
                    ReorderableItem(
                        enabled = false,
                        state = reorderableState,
                        key = "unread_widget_content",
                    ) {
                        UnreadChatListWidget(
                            item = unreadWidgetView,
                            mode = mode,
                            onWidgetObjectClicked = { obj ->
                                viewModel.onWidgetElementClicked(unreadWidgetView.id, obj)
                            },
                            onObjectCheckboxClicked = viewModel::onObjectCheckboxClicked
                        )
                    }
                }
            }

            // Only show pinned section header if there are items or section is collapsed and section is visible
            if (shouldShowPinnedHeader && sectionConfig.isSectionVisible(com.anytypeio.anytype.core_models.WidgetSectionType.PINNED)) {
                item {
                    ReorderableItem(
                        enabled = false,
                        state = reorderableState,
                        key = SECTION_PINNED,
                    ) {
                        PinnedSectionHeader(
                            onSectionClicked = viewModel::onSectionPinnedClicked
                        )
                    }
                }
            }
            // Pinned widgets section - only render if section is visible
            if (sectionConfig.isSectionVisible(com.anytypeio.anytype.core_models.WidgetSectionType.PINNED)) {
                renderWidgetSection(
                    widgets = pinnedUi,
                reorderableState = reorderableState,
                view = view,
                mode = mode,
                sectionType = SectionType.PINNED,
                isOtherSectionDragging = isDraggingTypes.value,
                onExpand = viewModel::onExpand,
                onWidgetMenuAction = { widget: Id, action: DropDownMenuAction ->
                    viewModel.onDropDownMenuAction(widget, action)
                },
                onWidgetElementClicked = viewModel::onWidgetElementClicked,
                onWidgetSourceClicked = viewModel::onWidgetSourceClicked,
                onSeeAllClicked = viewModel::onSeeAllClicked,
                onToggleExpandedWidgetState = viewModel::onToggleWidgetExpandedState,
                onChangeWidgetView = viewModel::onChangeCurrentWidgetView,
                onObjectCheckboxClicked = viewModel::onObjectCheckboxClicked,
                    onCreateElement = viewModel::onCreateWidgetElementClicked,
                    onCreateWidget = viewModel::onCreateWidgetClicked
                )
            }

            // Object types section header - only show if section is visible
            if (sectionConfig.isSectionVisible(com.anytypeio.anytype.core_models.WidgetSectionType.OBJECTS)) {
                item {
                ReorderableItem(
                    enabled = false,
                    state = reorderableState,
                    key = SECTION_OBJECT_TYPE,
                ) {
                    SpaceObjectTypesSectionHeader(
                        mode = mode,
                        onSectionClicked = viewModel::onSectionTypesClicked
                    )
                }
            }
            }

            // Type widgets section - render ObjectTypesGroup widget with drag-and-drop support
            if (sectionConfig.isSectionVisible(com.anytypeio.anytype.core_models.WidgetSectionType.OBJECTS)) {
            item(key = OBJECT_TYPES_GROUP_ID) {
                    ObjectTypesGroupWidgetCard(
                        typeRows = typeRowsUi,
                        onTypeClicked = { typeId ->
                            viewModel.onTypeRowClicked(typeId)
                        },
                        onCreateObjectClicked = { typeId ->
                            viewModel.onCreateObjectFromTypeRow(typeId)
                        },
                        onCreateNewTypeClicked = {
                            viewModel.onCreateNewTypeClicked()
                        },
                        onTypeRowsReordered = { fromIndex, toIndex ->
                            // Calculate the new order
                            if (fromIndex != toIndex && fromIndex in typeRowsUi.indices && toIndex in typeRowsUi.indices) {
                                val reorderedIds = typeRowsUi.map { it.id }.toMutableList()
                                val movedId = reorderedIds.removeAt(fromIndex)
                                reorderedIds.add(toIndex, movedId)
                                // Set pending order to maintain local state until ViewModel catches up
                                pendingTypeRowOrder.value = reorderedIds
                                // Notify ViewModel to persist the new order
                                viewModel.onTypeRowsReordered(reorderedIds)
                            }
                        }
                    )
                }
            }

            // Bin widget - only show if section is visible
            if (sectionConfig.isSectionVisible(com.anytypeio.anytype.core_models.WidgetSectionType.BIN)) {
                binWidget?.let { bin ->
                    item {
                        if (bin is WidgetView.Bin) {
                            ReorderableItem(
                                enabled = false,
                                state = reorderableState,
                                key = WIDGET_BIN_ID,
                            ) {
                                BinWidgetCard(
                                    item = bin,
                                    onDropDownMenuAction = { action ->
                                        viewModel.onDropDownMenuAction(bin.id, action)
                                    },
                                    onWidgetSourceClicked = {
                                        viewModel.onBinWidgetClicked()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            //only in debug mode
            if (BuildConfig.DEBUG) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AddWidgetButton(
                            modifier = Modifier.padding(16.dp),
                            onAddWidgetClicked = viewModel::onCreateWidgetClicked
                        )
                    }

                }
            }

            item {
                Box(
                    modifier = Modifier
                        .height(200.dp)
                )
            }
        }

        BottomNavigationMenu(
            state = viewModel.navPanelState.collectAsStateWithLifecycle().value,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 20.dp),
            onSearchClick = viewModel::onSearchIconClicked,
            onAddDocClick = viewModel::onCreateNewObjectClicked,
            onAddDocLongClick = viewModel::onCreateNewObjectLongClicked,
            onShareButtonClicked = viewModel::onNavBarShareIconClicked,
            onHomeButtonClicked = viewModel::onHomeButtonClicked
        )
    }
}