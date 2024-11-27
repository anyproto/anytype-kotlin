package com.anytypeio.anytype.feature_date.ui

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.core_models.restrictions.SpaceStatus
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.components.BottomNavigationMenu
import com.anytypeio.anytype.core_ui.syncstatus.SpaceSyncStatusScreen
import com.anytypeio.anytype.core_utils.insets.EDGE_TO_EDGE_MIN_SDK
import com.anytypeio.anytype.feature_date.R
import com.anytypeio.anytype.feature_date.models.UiCalendarState
import com.anytypeio.anytype.feature_date.models.DateObjectBottomMenu
import com.anytypeio.anytype.feature_date.models.DateObjectHeaderState
import com.anytypeio.anytype.feature_date.models.DateObjectHorizontalListState
import com.anytypeio.anytype.feature_date.models.DateObjectSheetState
import com.anytypeio.anytype.feature_date.models.DateObjectTopToolbarState
import com.anytypeio.anytype.feature_date.models.DateObjectVerticalListState
import com.anytypeio.anytype.feature_date.models.UiContentState
import com.anytypeio.anytype.feature_date.models.UiHorizontalListItem
import com.anytypeio.anytype.feature_date.models.UiVerticalListItem
import com.anytypeio.anytype.presentation.sync.SyncStatusWidgetState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateObjectScreen(
    uiTopToolbarState: DateObjectTopToolbarState,
    uiHeaderState: DateObjectHeaderState,
    uiHorizontalListState: DateObjectHorizontalListState,
    uiVerticalListState: DateObjectVerticalListState,
    uiDateObjectBottomMenu: DateObjectBottomMenu,
    uiSheetState: DateObjectSheetState,
    uiSyncStatusState: SyncStatusWidgetState,
    uiCalendarState: UiCalendarState,
    uiHeaderActions: (DateObjectHeaderState.Action) -> Unit,
    uiTopToolbarActions: (DateObjectTopToolbarState.Action) -> Unit,
    uiHorizontalListActions: (UiHorizontalListItem) -> Unit,
    uiVerticalListActions: (UiVerticalListItem) -> Unit,
    uiBottomMenuActions: (DateObjectBottomMenu.Action) -> Unit,
    uiContentState: UiContentState,
    canPaginate: Boolean,
    onUpdateLimitSearch: () -> Unit,
    onCalendarDateSelected: (Long?) -> Unit,
    onTodayClicked: () -> Unit,
    onTomorrowClicked: () -> Unit,
    showCalendar: Boolean,
    onDismissCalendar: () -> Unit
) {

    val scope = rememberCoroutineScope()

    val bottomSheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Hidden,
        skipHiddenState = false
    )

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = bottomSheetState
    )

    BackHandler(enabled = scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
        scope.launch {
            scaffoldState.bottomSheetState.hide()
        }
    }

    val lazyHorizontalListState = rememberLazyListState()

    val showAdditionalList = remember { mutableStateOf(false) }
    val _showCalendar = remember { mutableStateOf(false) }

    LaunchedEffect(showCalendar) {
        _showCalendar.value = showCalendar
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        modifier = Modifier.fillMaxSize(),
        containerColor = colorResource(id = R.color.background_primary),
        contentColor = colorResource(id = R.color.background_primary),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = colorResource(id = R.color.background_primary))
            ) {
                if (Build.VERSION.SDK_INT >= EDGE_TO_EDGE_MIN_SDK) {
                    Spacer(
                        modifier = Modifier.windowInsetsTopHeight(
                            WindowInsets.statusBars
                        )
                    )
                }
                DateObjectTopToolbar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    state = uiTopToolbarState,
                    action = { action ->
                        when (action) {
                            DateObjectTopToolbarState.Action.Calendar -> {}
                            DateObjectTopToolbarState.Action.SyncStatus -> {
                                scope.launch {
                                    scaffoldState.bottomSheetState.expand()
                                }
                            }
                        }
                        uiTopToolbarActions(action)
                    }
                )
                Spacer(
                    modifier = Modifier.height(24.dp)
                )
                DateObjectHeader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    state = uiHeaderState,
                    action = uiHeaderActions
                )
                DateLayoutHorizontalListScreen(
                    lazyHorizontalListState = lazyHorizontalListState,
                    state = uiHorizontalListState,
                    action = { action ->
                        if (action is UiHorizontalListItem.Settings) {
                            showAdditionalList.value = true
                        }
                        uiHorizontalListActions(action)
                    }
                )
                Spacer(
                    modifier = Modifier.height(8.dp)
                )
            }
        },
        content = { paddingValues ->
            MainContent(
                paddingValues = paddingValues,
                uiVerticalListState = uiVerticalListState,
                uiDateObjectBottomMenu = uiDateObjectBottomMenu,
                uiVerticalListActions = uiVerticalListActions,
                uiBottomMenuActions = uiBottomMenuActions,
                uiContentState = uiContentState,
                canPaginate = canPaginate,
                onUpdateLimitSearch = onUpdateLimitSearch
            )
        },
        sheetContainerColor = colorResource(id = R.color.palette_system_red),
        sheetTonalElevation = 0.dp,
        sheetShadowElevation = 10.dp,
        sheetContent = {
            SpaceSyncStatusScreen(
                uiState = uiSyncStatusState,
                onDismiss = {},
                scope = scope,
                onUpdateAppClick = {}
            )
        },
        sheetDragHandle = null
    )
    if (showAdditionalList.value) {
        HorizontalItemsModalScreen(
            lazyHorizontalListState = lazyHorizontalListState,
            scope = scope,
            uiSheetState = uiSheetState,
            uiHorizontalListActions = uiHorizontalListActions,
            onDismiss = { showAdditionalList.value = false }
        )
    }
    if (_showCalendar.value) {
        CalendarScreen(
            uiCalendarState = uiCalendarState,
            onCalendarDateSelected = onCalendarDateSelected,
            onDismiss = onDismissCalendar,
            onTodayClicked = onTodayClicked,
            onTomorrowClicked = onTomorrowClicked,
        )
    }
}

@Composable
private fun MainContent(
    paddingValues: PaddingValues,
    uiVerticalListState: DateObjectVerticalListState,
    uiDateObjectBottomMenu: DateObjectBottomMenu,
    uiVerticalListActions: (UiVerticalListItem) -> Unit,
    uiBottomMenuActions: (DateObjectBottomMenu.Action) -> Unit,
    uiContentState: UiContentState,
    canPaginate: Boolean,
    onUpdateLimitSearch: () -> Unit
) {
    val contentModifier =
        if (Build.VERSION.SDK_INT >= EDGE_TO_EDGE_MIN_SDK)
            Modifier
                .windowInsetsPadding(WindowInsets.navigationBars)
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        else
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
    Box(
        modifier = contentModifier,
        contentAlignment = Alignment.TopCenter
    ) {
        if (uiContentState is UiContentState.Empty) {
            EmptyState()
        }
        if (uiContentState is UiContentState.Error) {
            ErrorState(message = uiContentState.message)
        }
        DateLayoutVerticalListScreen(
            state = uiVerticalListState,
            uiContentState = uiContentState,
            canPaginate = canPaginate,
            onUpdateLimitSearch = onUpdateLimitSearch,
            uiVerticalListActions = uiVerticalListActions
        )
        BottomNavigationMenu(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            backClick = { uiBottomMenuActions(DateObjectBottomMenu.Action.Back) },
            backLongClick = { uiBottomMenuActions(DateObjectBottomMenu.Action.BackLong) },
            searchClick = { uiBottomMenuActions(DateObjectBottomMenu.Action.GlobalSearch) },
            addDocClick = { uiBottomMenuActions(DateObjectBottomMenu.Action.AddDoc) },
            addDocLongClick = { uiBottomMenuActions(DateObjectBottomMenu.Action.CreateObjectLong) },
            isOwnerOrEditor = uiDateObjectBottomMenu.isOwnerOrEditor
        )
    }
}

//@DefaultPreviews
//@Composable
//fun DateObjectScreenPreview() {
//    DateObjectScreen(
//        uiTopToolbarState = DateObjectTopToolbarState.Content(
//            syncStatus = SpaceSyncStatus.SYNCING,
//        ),
//        uiHeaderState = DateObjectHeaderState.Content(
//            title = "06 Nov 2024"
//        ),
//        uiHorizontalListState = DateObjectHorizontalListState(
//            items = StubHorizontalItems,
//            selectedRelationKey = null
//        ),
//        uiVerticalListState = DateObjectVerticalListState(
//            items = StubVerticalItems
//        ),
//        uiDateObjectBottomMenu = DateObjectBottomMenu(isOwnerOrEditor = true),
//        uiSheetState = DateObjectSheetState.Content(
//            items = emptyList()
//        ),
//        uiHeaderActions = {},
//        uiTopToolbarActions = {},
//        uiHorizontalListActions = {},
//        uiVerticalListActions = {},
//        uiBottomMenuActions = {},
//        uiContentState = UiContentState.Idle(),
//        canPaginate = false,
//        onUpdateLimitSearch = {},
//        onCalendarDateSelected = {},
//        uiUiCalendarState = UiCalendarState.Empty
//    )
//}
//
//@DefaultPreviews
//@Composable
//fun DateObjectScreenEmptyPreview() {
//    DateObjectScreen(
//        uiTopToolbarState = DateObjectTopToolbarState.Content(
//            syncStatus = SpaceSyncStatus.SYNCING,
//        ),
//        uiHeaderState = DateObjectHeaderState.Content(
//            title = "06 Nov 2024"
//        ),
//        uiHorizontalListState = DateObjectHorizontalListState.loadingState(),
//        uiVerticalListState = DateObjectVerticalListState(
//            items = emptyList()
//        ),
//        uiDateObjectBottomMenu = DateObjectBottomMenu(isOwnerOrEditor = true),
//        uiSheetState = DateObjectSheetState.Content(
//            items = emptyList()
//        ),
//        uiHeaderActions = {},
//        uiTopToolbarActions = {},
//        uiHorizontalListActions = {},
//        uiVerticalListActions = {},
//        uiBottomMenuActions = {},
//        uiContentState = UiContentState.Empty,
//        canPaginate = false,
//        onUpdateLimitSearch = {},
//        onCalendarDateSelected = {},
//        uiUiCalendarState = UiCalendarState.Empty
//    )
//}
//
//@DefaultPreviews
//@Composable
//fun DateObjectScreenErrorPreview() {
//    DateObjectScreen(
//        uiTopToolbarState = DateObjectTopToolbarState.Content(
//            syncStatus = SpaceSyncStatus.SYNCING,
//        ),
//        uiHeaderState = DateObjectHeaderState.Content(
//            title = "06 Nov 2024"
//        ),
//        uiHorizontalListState = DateObjectHorizontalListState.loadingState(),
//        uiVerticalListState = DateObjectVerticalListState(
//            items = emptyList()
//        ),
//        uiDateObjectBottomMenu = DateObjectBottomMenu(isOwnerOrEditor = true),
//        uiSheetState = DateObjectSheetState.Content(
//            items = emptyList()
//        ),
//        uiHeaderActions = {},
//        uiTopToolbarActions = {},
//        uiHorizontalListActions = {},
//        uiVerticalListActions = {},
//        uiBottomMenuActions = {},
//        uiContentState = UiContentState.Error(
//            message = "Error message"
//        ),
//        canPaginate = false,
//        onUpdateLimitSearch = {},
//        onCalendarDateSelected = {},
//        uiUiCalendarState = UiCalendarState.Empty
//    )
//}