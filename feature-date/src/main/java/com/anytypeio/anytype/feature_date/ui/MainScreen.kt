package com.anytypeio.anytype.feature_date.ui

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.foundation.components.BottomNavigationMenu
import com.anytypeio.anytype.core_ui.syncstatus.SpaceSyncStatusScreen
import com.anytypeio.anytype.core_utils.insets.EDGE_TO_EDGE_MIN_SDK
import com.anytypeio.anytype.feature_date.R
import com.anytypeio.anytype.feature_date.ui.models.DateEvent
import com.anytypeio.anytype.feature_date.viewmodel.UiCalendarIconState
import com.anytypeio.anytype.feature_date.viewmodel.UiCalendarState
import com.anytypeio.anytype.feature_date.viewmodel.UiContentState
import com.anytypeio.anytype.feature_date.viewmodel.UiFieldsSheetState
import com.anytypeio.anytype.feature_date.viewmodel.UiFieldsState
import com.anytypeio.anytype.feature_date.viewmodel.UiHeaderState
import com.anytypeio.anytype.feature_date.viewmodel.UiNavigationWidget
import com.anytypeio.anytype.feature_date.viewmodel.UiObjectsListState
import com.anytypeio.anytype.feature_date.viewmodel.UiSyncStatusBadgeState
import com.anytypeio.anytype.feature_date.viewmodel.UiSyncStatusWidgetState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateMainScreen(
    uiCalendarIconState: UiCalendarIconState,
    uiSyncStatusBadgeState: UiSyncStatusBadgeState,
    uiHeaderState: UiHeaderState,
    uiFieldsState: UiFieldsState,
    uiObjectsListState: UiObjectsListState,
    uiNavigationWidget: UiNavigationWidget,
    uiFieldsSheetState: UiFieldsSheetState,
    uiSyncStatusState: UiSyncStatusWidgetState,
    uiCalendarState: UiCalendarState,
    uiContentState: UiContentState,
    canPaginate: Boolean,
    onDateEvent: (DateEvent) -> Unit
) {

    val scope = rememberCoroutineScope()
    val lazyFieldsListState = rememberLazyListState()

    Scaffold(
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
                TopToolbarScreen(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    uiCalendarIconState = uiCalendarIconState,
                    uiSyncStatusBadgeState = uiSyncStatusBadgeState,
                    onDateEvent = onDateEvent
                )
                Spacer(
                    modifier = Modifier.height(24.dp)
                )
                HeaderScreen(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    uiState = uiHeaderState,
                    onDateEvent = onDateEvent
                )
                FieldsScreen(
                    lazyListState = lazyFieldsListState,
                    uiState = uiFieldsState,
                    onDateEvent = onDateEvent
                )
                Spacer(
                    modifier = Modifier.height(8.dp)
                )
            }
        },
        content = { paddingValues ->
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
                    EmptyScreen()
                }
                ObjectsScreen(
                    state = uiObjectsListState,
                    uiState = uiContentState,
                    canPaginate = canPaginate,
                    onDateEvent = onDateEvent,
                )
                BottomNavigationMenu(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp),
                    backClick = {
                        onDateEvent(DateEvent.NavigationWidget.OnBackClick)
                    },
                    backLongClick = {
                        onDateEvent(DateEvent.NavigationWidget.OnBackLongClick)
                    },
                    searchClick = {
                        onDateEvent(DateEvent.NavigationWidget.OnGlobalSearchClick)
                    },
                    addDocClick = {
                        onDateEvent(DateEvent.NavigationWidget.OnAddDocClick)
                    },
                    addDocLongClick = {
                        onDateEvent(DateEvent.NavigationWidget.OnAddDocLongClick)
                    },
                    isOwnerOrEditor = uiNavigationWidget is UiNavigationWidget.Editor
                )
            }
        }
    )
    if (uiSyncStatusState is UiSyncStatusWidgetState.Visible){
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            SpaceSyncStatusScreen(
                uiState = uiSyncStatusState.status,
                onDismiss = { onDateEvent(DateEvent.SyncStatusWidget.OnSyncStatusDismiss) },
                scope = scope,
                onUpdateAppClick = {}
            )
        }
    }
    if (uiFieldsSheetState is UiFieldsSheetState.Content) {
        FieldsSheetScreen(
            lazyListState = lazyFieldsListState,
            scope = scope,
            uiState = uiFieldsSheetState,
            onDateEvent = onDateEvent
        )
    }
    if (uiCalendarState is UiCalendarState.Calendar) {
        CalendarScreen(
            uiState = uiCalendarState,
            onDateEvent = onDateEvent
        )
    }
}