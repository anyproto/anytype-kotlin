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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.Id
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
import com.anytypeio.anytype.feature_date.viewmodel.UiSnackbarState
import com.anytypeio.anytype.feature_date.viewmodel.UiSyncStatusBadgeState
import com.anytypeio.anytype.feature_date.viewmodel.UiSyncStatusWidgetState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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
    uiSnackbarState: UiSnackbarState,
    canPaginate: Boolean,
    onDateEvent: (DateEvent) -> Unit
) {

    val scope = rememberCoroutineScope()

    val snackBarHostState = remember { SnackbarHostState() }

    val snackBarText = stringResource(R.string.all_content_snackbar_title)
    val undoText = stringResource(R.string.undo)

    LaunchedEffect(key1 = uiSnackbarState) {
        if (uiSnackbarState is UiSnackbarState.Visible) {
            showMoveToBinSnackbar(
                message = "'${uiSnackbarState.message}' $snackBarText",
                undo = undoText,
                scope = this,
                snackBarHostState = snackBarHostState,
                objectId = uiSnackbarState.objId,
                onDateEvent = onDateEvent
            )
        }
    }

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
                    modifier = Modifier.height(16.dp)
                )
                HeaderScreen(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    uiState = uiHeaderState,
                    onDateEvent = onDateEvent
                )
                Spacer(
                    modifier = Modifier.height(32.dp)
                )
                FieldsScreen(
                    modifier = Modifier.fillMaxWidth().height(40.dp),
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
        },
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        }
    )
    if (uiSyncStatusState is UiSyncStatusWidgetState.Visible) {
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
    if (uiFieldsSheetState is UiFieldsSheetState.Visible) {
        FieldsSheetScreen(
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

private fun showMoveToBinSnackbar(
    objectId: Id,
    message: String,
    undo: String,
    scope: CoroutineScope,
    snackBarHostState: SnackbarHostState,
    onDateEvent: (DateEvent) -> Unit
) {
    scope.launch {
        val result = snackBarHostState
            .showSnackbar(
                message = message,
                actionLabel = undo,
                duration = SnackbarDuration.Short,
                withDismissAction = true
            )
        when (result) {
            SnackbarResult.ActionPerformed -> {
                onDateEvent(DateEvent.Snackbar.UndoMoveToBin(objectId))
            }

            SnackbarResult.Dismissed -> {
                onDateEvent(DateEvent.Snackbar.OnSnackbarDismiss)
            }
        }
    }
}