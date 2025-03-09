package com.anytypeio.anytype.feature_object_type.ui

import android.os.Build
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.multiplayer.P2PStatusUpdate
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncAndP2PStatusState
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncError
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncNetwork
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncStatus
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncUpdate
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.syncstatus.SpaceSyncStatusScreen
import com.anytypeio.anytype.core_utils.insets.EDGE_TO_EDGE_MIN_SDK
import com.anytypeio.anytype.feature_object_type.R
import com.anytypeio.anytype.feature_object_type.ui.alerts.DeleteAlertScreen
import com.anytypeio.anytype.feature_object_type.ui.header.HorizontalButtons
import com.anytypeio.anytype.feature_object_type.ui.header.IconAndTitleWidget
import com.anytypeio.anytype.feature_object_type.ui.header.TopToolbar
import com.anytypeio.anytype.feature_object_type.ui.layouts.TypeLayoutsScreen
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.sync.SyncStatusWidgetState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObjectTypeMainScreen(
    //top bar
    uiEditButtonState: UiEditButton,
    uiSyncStatusBadgeState: UiSyncStatusBadgeState,
    uiSyncStatusState: SyncStatusWidgetState,
    //header
    uiIconState: UiIconState,
    uiTitleState: UiTitleState,
    //layout and fields buttons
    uiFieldsButtonState: UiFieldsButtonState,
    uiLayoutButtonState: UiLayoutButtonState,
    uiLayoutTypeState: UiLayoutTypeState,
    //delete alert
    uiDeleteAlertState: UiDeleteAlertState,
    //events
    onTypeEvent: (TypeEvent) -> Unit
) {

    val topAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = rememberTopAppBarState()
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        containerColor = colorResource(id = R.color.background_primary),
        contentColor = colorResource(id = R.color.background_primary),
        topBar = {
            TopBarContent(
                uiSyncStatusBadgeState = uiSyncStatusBadgeState,
                uiEditButtonState = uiEditButtonState,
                uiTitleState = uiTitleState,
                topBarScrollBehavior = topAppBarScrollBehavior,
                onTypeEvent = onTypeEvent
            )
        },
        content = { paddingValues ->
            MainContent(
                paddingValues = paddingValues,
                uiIconState = uiIconState,
                uiTitleState = uiTitleState,
                uiFieldsButtonState = uiFieldsButtonState,
                uiLayoutButtonState = uiLayoutButtonState,
                onTypeEvent = onTypeEvent
            )
        }
    )

    BottomSyncStatus(
        uiSyncStatusState = uiSyncStatusState,
        onDismiss = { onTypeEvent(TypeEvent.OnSyncStatusDismiss) }
    )

    if (uiDeleteAlertState is UiDeleteAlertState.Show) {
        DeleteAlertScreen(
            onTypeEvent = onTypeEvent
        )
    }

    if (uiLayoutTypeState is UiLayoutTypeState.Visible) {
        TypeLayoutsScreen(
            modifier = Modifier.fillMaxWidth(),
            uiState = uiLayoutTypeState,
            onTypeEvent = onTypeEvent
        )
    }
}

@Composable
private fun MainContent(
    paddingValues: PaddingValues,
    uiIconState: UiIconState,
    uiTitleState: UiTitleState,
    uiFieldsButtonState: UiFieldsButtonState,
    uiLayoutButtonState: UiLayoutButtonState,
    onTypeEvent: (TypeEvent) -> Unit
) {
    // Adjust content modifier based on SDK version for proper insets handling
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

    LazyColumn(modifier = contentModifier) {
        item {
            IconAndTitleWidget(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(top = 32.dp)
                    .padding(horizontal = 20.dp),
                uiIconState = uiIconState,
                uiTitleState = uiTitleState,
                onTypeEvent = onTypeEvent
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        item {
            HorizontalButtons(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .padding(horizontal = 20.dp),
                uiFieldsButtonState = uiFieldsButtonState,
                uiLayoutButtonState = uiLayoutButtonState,
                onTypeEvent = onTypeEvent
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarContent(
    uiSyncStatusBadgeState: UiSyncStatusBadgeState,
    uiEditButtonState: UiEditButton,
    uiTitleState: UiTitleState,
    topBarScrollBehavior: TopAppBarScrollBehavior,
    onTypeEvent: (TypeEvent) -> Unit
) {
    // Use windowInsetsPadding if running on a recent SDK
    val modifier = if (Build.VERSION.SDK_INT >= EDGE_TO_EDGE_MIN_SDK) {
        Modifier
            .windowInsetsPadding(WindowInsets.statusBars)
            .fillMaxWidth()
    } else {
        Modifier.fillMaxWidth()
    }

    Column(modifier = modifier) {
        TopToolbar(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            uiSyncStatusBadgeState = uiSyncStatusBadgeState,
            uiEditButtonState = uiEditButtonState,
            uiTitleState = uiTitleState,
            onTypeEvent = onTypeEvent,
            topBarScrollBehavior = topBarScrollBehavior
        )
    }
}

@Composable
fun BottomSyncStatus(
    uiSyncStatusState: SyncStatusWidgetState,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        SpaceSyncStatusScreen(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .windowInsetsPadding(WindowInsets.navigationBars),
            modifierCard = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 16.dp),
            uiState = uiSyncStatusState,
            onDismiss = onDismiss,
            onUpdateAppClick = {}
        )
    }
}

@DefaultPreviews
@Composable
fun ObjectTypeMainScreenPreview() {
    val spaceSyncUpdate = SpaceSyncUpdate.Update(
        id = "1",
        status = SpaceSyncStatus.SYNCING,
        network = SpaceSyncNetwork.ANYTYPE,
        error = SpaceSyncError.NULL,
        syncingObjectsCounter = 2
    )
    ObjectTypeMainScreen(
        uiSyncStatusBadgeState = UiSyncStatusBadgeState.Visible(
            status = SpaceSyncAndP2PStatusState.Success(
                spaceSyncUpdate = spaceSyncUpdate,
                p2PStatusUpdate = P2PStatusUpdate.Initial
            )
        ),
        uiSyncStatusState = SyncStatusWidgetState.Hidden,
        uiIconState = UiIconState(icon = ObjectIcon.Empty.Page, isEditable = true),
        uiTitleState = UiTitleState(title = "title", isEditable = true),
        uiFieldsButtonState = UiFieldsButtonState.Visible(4),
        uiLayoutButtonState = UiLayoutButtonState.Visible(layout = ObjectType.Layout.VIDEO),
        uiDeleteAlertState = UiDeleteAlertState.Hidden,
        uiEditButtonState = UiEditButton.Visible,
        uiLayoutTypeState = UiLayoutTypeState.Hidden,
        onTypeEvent = {}
    )
}

