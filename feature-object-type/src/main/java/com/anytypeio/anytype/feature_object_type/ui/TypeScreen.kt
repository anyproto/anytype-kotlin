package com.anytypeio.anytype.feature_object_type.ui

import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.syncstatus.SpaceSyncStatusScreen
import com.anytypeio.anytype.core_utils.insets.EDGE_TO_EDGE_MIN_SDK
import com.anytypeio.anytype.feature_object_type.ui.header.TopToolbar
import com.anytypeio.anytype.presentation.sync.SyncStatusWidgetState

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

