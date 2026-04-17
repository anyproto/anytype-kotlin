package com.anytypeio.anytype.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.WidgetSectionType
import com.anytypeio.anytype.core_ui.common.ReorderHapticFeedbackType
import com.anytypeio.anytype.core_ui.common.rememberReorderHapticFeedback
import com.anytypeio.anytype.core_ui.widgets.CircularFabButton
import com.anytypeio.anytype.presentation.home.HomeScreenViewModel
import com.anytypeio.anytype.presentation.home.InteractionMode
import com.anytypeio.anytype.presentation.navigation.NavPanelState
import com.anytypeio.anytype.presentation.widgets.DropDownMenuAction
import com.anytypeio.anytype.presentation.widgets.SectionType
import com.anytypeio.anytype.presentation.widgets.Widget.Source.Companion.OBJECT_TYPES_GROUP_ID
import com.anytypeio.anytype.presentation.widgets.Widget.Source.Companion.SECTION_OBJECT_TYPE
import com.anytypeio.anytype.presentation.widgets.Widget.Source.Companion.SECTION_PINNED
import com.anytypeio.anytype.presentation.widgets.Widget.Source.Companion.SECTION_RECENTLY_EDITED
import com.anytypeio.anytype.presentation.widgets.Widget.Source.Companion.SECTION_UNREAD
import com.anytypeio.anytype.presentation.widgets.Widget.Source.Companion.WIDGET_BIN_ID
import com.anytypeio.anytype.presentation.widgets.Widget.Source.Companion.WIDGET_SPACE_CHAT_ID
import com.anytypeio.anytype.presentation.widgets.WidgetView
import com.anytypeio.anytype.presentation.widgets.extractWidgetId
import com.anytypeio.anytype.ui.widgets.types.AddWidgetButton
import com.anytypeio.anytype.ui.widgets.types.BinWidgetCard
import com.anytypeio.anytype.ui.widgets.types.CreateHomeWidgetCard
import com.anytypeio.anytype.ui.widgets.types.InviteMembersWidgetCard
import com.anytypeio.anytype.ui.widgets.types.ObjectTypesGroupWidgetCard
import com.anytypeio.anytype.ui.widgets.types.SpaceChatWidgetCard
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun WidgetsScreen(
    viewModel: HomeScreenViewModel
) {

    val view = LocalView.current
    val lazyListState = rememberLazyListState()
    val hapticFeedback = rememberReorderHapticFeedback()

    val mode = viewModel.mode.collectAsState().value
    val spaceView = viewModel.spaceViewState.collectAsState().value
    val pinnedWidgets = viewModel.pinnedViews.collectAsState().value
    val typeWidgets = viewModel.typeViews.collectAsState().value
    val unreadWidget = viewModel.unreadView.collectAsState().value
    val chatWidget = viewModel.chatView.collectAsState().value
    val binWidget = viewModel.binView.collectAsState().value
    val recentlyEditedWidget = viewModel.recentlyEditedView.collectAsState().value
    val showHomepagePicker = viewModel.showHomepagePicker.collectAsState().value // used for guard
    val showCreateHomeWidget = viewModel.showCreateHomeWidget.collectAsState().value
    val showInviteMembersWidget = viewModel.showInviteMembersWidget.collectAsState().value
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

    // Recently Edited section visibility logic
    val recentlyEditedView = recentlyEditedWidget as? WidgetView.RecentlyEdited
    val isRecentlyEditedSectionCollapsed = collapsedSections.contains(SECTION_RECENTLY_EDITED)
    
    // Track previous collapse state for recently edited section
    val wasRecentlyEditedCollapsed = remember { mutableStateOf(isRecentlyEditedSectionCollapsed) }
    val hadRecentlyEditedItems = remember { mutableStateOf(recentlyEditedView?.elements?.isNotEmpty() == true) }
    
    // When section becomes expanded, keep the flag true to prevent flicker
    if (!isRecentlyEditedSectionCollapsed && wasRecentlyEditedCollapsed.value) {
        hadRecentlyEditedItems.value = true
    }
    
    // Update previous state
    wasRecentlyEditedCollapsed.value = isRecentlyEditedSectionCollapsed
    
    // Set flag when items are present
    if (recentlyEditedView?.elements?.isNotEmpty() == true) {
        hadRecentlyEditedItems.value = true
    }
    
    // Reset flag when section is collapsed and has no items
    if (isRecentlyEditedSectionCollapsed && recentlyEditedView?.elements?.isEmpty() == true) {
        hadRecentlyEditedItems.value = false
    }
    
    // Show header if: has items OR is collapsed OR was previously shown
    val shouldShowRecentlyEditedSection = recentlyEditedView != null && 
        (recentlyEditedView.elements.isNotEmpty() || isRecentlyEditedSectionCollapsed || hadRecentlyEditedItems.value)

    // Determine if counters should be hidden in other sections
    // When Unread section is expanded with items, counters should not be displayed elsewhere
    val hideCountersInOtherSections = !isUnreadSectionCollapsed
            && unreadWidgetView?.elements?.isNotEmpty() == true

    // Determine if sections should be visible
    val isPinnedSectionCollapsed = collapsedSections.contains(SECTION_PINNED)
    val isObjectsSectionCollapsed = collapsedSections.contains(SECTION_OBJECT_TYPE)

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

    // Top inset: status bar + toolbar + 8dp breathing room so the
    // first widget sits just below the overlaid HomeScreenToolbar.
    val topContentPadding =
        WindowInsets.statusBars.asPaddingValues().calculateTopPadding() +
            dimensionResource(R.dimen.nav_top_toolbar_height) +
            8.dp
    val bottomContentPadding =
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {

        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = topContentPadding,
                bottom = bottomContentPadding
            )
        ) {

            // Space profile header
            if (spaceView is HomeScreenViewModel.SpaceViewState.Success) {
                item(key = "space_profile_header") {
                    SpaceProfileHeader(
                        spaceIcon = spaceView.spaceIcon,
                        spaceName = spaceView.spaceName,
                        globalName = spaceView.memberGlobalName,
                        identity = spaceView.memberIdentity,
                        spaceAccessType = spaceView.spaceAccessType
                    )
                }
            }

            // Chat widget pinned at the top for single-chat spaces (CHAT, ONE_TO_ONE)
            if (chatWidget is WidgetView.SpaceChat) {
                item(key = WIDGET_SPACE_CHAT_ID) {
                    SpaceChatWidgetCard(
                        item = chatWidget,
                        mode = InteractionMode.Default,
                        unReadMentionCount = chatWidget.unreadMentionCount,
                        unReadMessageCount = chatWidget.unreadMessageCount,
                        isMuted = chatWidget.isMuted,
                        onWidgetClicked = viewModel::onSpaceChatWidgetClicked,
                        onDropDownMenuAction = { } // No-op: top-level chat widget has no configurable actions
                    )
                }
            }

            // "Create Home" widget — shown when homepage is not set and picker was dismissed
            // Stays visible at 50% opacity while homepage picker is open
            if (showCreateHomeWidget) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
                item(key = WidgetView.CreateHome.WIDGET_CREATE_HOME_ID) {
                    CreateHomeWidgetCard(
                        onWidgetClicked = viewModel::onCreateHomeWidgetClicked,
                        onDismissClicked = viewModel::onCreateHomeWidgetDismissed,
                        modifier = Modifier.alpha(if (showHomepagePicker) 0.5f else 1f)
                    )
                }
            }

            // "Invite Members" widget — shown in shared spaces with <= 1 participant
            if (showInviteMembersWidget) {
                item(key = WidgetView.InviteMembers.WIDGET_INVITE_MEMBERS_ID) {
                    InviteMembersWidgetCard(
                        onWidgetClicked = viewModel::onInviteMembersWidgetClicked,
                        onDismissClicked = viewModel::onInviteMembersWidgetDismissed
                    )
                }
            }

            val visibleSections = sectionConfig.getVisibleSections()

            for (sectionType in visibleSections) {
                when (sectionType) {
                    WidgetSectionType.UNREAD -> {
                        // Unread section header
                        if (shouldShowUnreadSection) {
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
                        // Unread widgets content
                        if (shouldShowUnreadSection && !isUnreadSectionCollapsed && unreadWidgetView != null) {
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
                    }

                    WidgetSectionType.PINNED -> {
                        // Pinned section header
                        if (shouldShowPinnedHeader) {
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
                        // Pinned widgets (hidden when section is collapsed)
                        if (!isPinnedSectionCollapsed) renderWidgetSection(
                            widgets = pinnedUi,
                            reorderableState = reorderableState,
                            view = view,
                            mode = mode,
                            sectionType = SectionType.PINNED,
                            isOtherSectionDragging = isDraggingTypes.value,
                            hideCounters = hideCountersInOtherSections,
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

                    WidgetSectionType.OBJECTS -> {
                        // Object types section header
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
                        // Type widgets (hidden when section is collapsed)
                        if (!isObjectsSectionCollapsed) {
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
                                        if (fromIndex != toIndex && fromIndex in typeRowsUi.indices && toIndex in typeRowsUi.indices) {
                                            val reorderedIds = typeRowsUi.map { it.id }.toMutableList()
                                            val movedId = reorderedIds.removeAt(fromIndex)
                                            reorderedIds.add(toIndex, movedId)
                                            pendingTypeRowOrder.value = reorderedIds
                                            viewModel.onTypeRowsReordered(reorderedIds)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    WidgetSectionType.RECENTLY_EDITED -> {
                        // Recently Edited section header
                        if (shouldShowRecentlyEditedSection) {
                            item {
                                ReorderableItem(
                                    enabled = false,
                                    state = reorderableState,
                                    key = SECTION_RECENTLY_EDITED,
                                ) {
                                    RecentlyEditedSectionHeader(
                                        onSectionClicked = viewModel::onSectionRecentlyEditedClicked
                                    )
                                }
                            }
                        }
                        // Recently Edited widgets content
                        if (shouldShowRecentlyEditedSection && !isRecentlyEditedSectionCollapsed && recentlyEditedView != null) {
                            item(key = "recently_edited_widget_content") {
                                ReorderableItem(
                                    enabled = false,
                                    state = reorderableState,
                                    key = "recently_edited_widget_content",
                                ) {
                                    RecentlyEditedWidget(
                                        item = recentlyEditedView,
                                        mode = mode,
                                        onWidgetObjectClicked = { obj ->
                                            viewModel.onWidgetElementClicked(recentlyEditedView.id, obj)
                                        },
                                        onObjectCheckboxClicked = viewModel::onObjectCheckboxClicked
                                    )
                                }
                            }
                        }
                    }

                    WidgetSectionType.BIN -> {
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

        val navPanelState = viewModel.navPanelState.collectAsStateWithLifecycle().value
        val isCreateEnabled = (navPanelState as? NavPanelState.Default)?.isCreateEnabled == true

        // Search FAB (bottom-start). Always enabled.
        CircularFabButton(
            iconRes = R.drawable.ic_nav_panel_search,
            contentDescription = stringResource(
                id = R.string.main_navigation_content_desc_search_button
            ),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .navigationBarsPadding()
                .padding(
                    start = dimensionResource(R.dimen.nav_fab_margin),
                    bottom = dimensionResource(R.dimen.nav_fab_margin),
                )
            ,
            onClick = viewModel::onSearchIconClicked,
        )

        // Create-object FAB (bottom-end). Tap creates a new object;
        // long-press surfaces the type picker. Uses the same icon as the
        // editor / set screens. Disabled visual reflects
        // NavPanelState.Default.isCreateEnabled.
        CircularFabButton(
            iconRes = R.drawable.ic_create_obj_32,
            contentDescription = stringResource(
                id = R.string.main_navigation_content_desc_create_button
            ),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(
                    end = dimensionResource(R.dimen.nav_fab_margin),
                    bottom = dimensionResource(R.dimen.nav_fab_margin),
                ),
            isEnabled = isCreateEnabled,
            onClick = viewModel::onCreateNewObjectClicked,
            onLongClick = viewModel::onCreateNewObjectLongClicked,
        )
    }
}
