package com.anytypeio.anytype.feature_object_type.ui.header

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.multiplayer.P2PStatusUpdate
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncAndP2PStatusState
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncError
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncNetwork
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncStatus
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncUpdate
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.syncstatus.StatusBadge
import com.anytypeio.anytype.feature_object_type.R
import com.anytypeio.anytype.feature_object_type.ui.TypeEvent
import com.anytypeio.anytype.feature_object_type.ui.UiSyncStatusBadgeState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopToolbar(
    uiSyncStatusBadgeState: UiSyncStatusBadgeState,
    onTypeEvent: (TypeEvent) -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Box(
            modifier = Modifier
                .width(56.dp)
                .fillMaxHeight()
                .noRippleThrottledClickable {
                    onTypeEvent(TypeEvent.OnBackClick)
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                modifier = Modifier
                    .wrapContentSize(),
                painter = painterResource(R.drawable.ic_default_top_back),
                contentDescription = stringResource(R.string.content_desc_back_button)
            )
        }
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (uiSyncStatusBadgeState is UiSyncStatusBadgeState.Visible) {
                Box(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(end = 14.dp)
                        .noRippleThrottledClickable {
                            onTypeEvent(
                                TypeEvent.OnSyncStatusClick(
                                    status = uiSyncStatusBadgeState.status
                                )
                            )
                        },
                ) {
                    StatusBadge(
                        status = uiSyncStatusBadgeState.status,
                        modifier = Modifier
                            .size(28.dp)
                            .align(Alignment.Center)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .fillMaxHeight()
                    .noRippleThrottledClickable {
                        onTypeEvent(TypeEvent.OnMenuClick)
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(id = R.drawable.ic_space_list_dots),
                    contentDescription = stringResource(R.string.more)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@DefaultPreviews
fun TopToolbarPreview() {
    val spaceSyncUpdate = SpaceSyncUpdate.Update(
        id = "1",
        status = SpaceSyncStatus.SYNCING,
        network = SpaceSyncNetwork.ANYTYPE,
        error = SpaceSyncError.NULL,
        syncingObjectsCounter = 2
    )
    TopToolbar(
        uiSyncStatusBadgeState = UiSyncStatusBadgeState.Visible(
            status = SpaceSyncAndP2PStatusState.Success(
                spaceSyncUpdate = spaceSyncUpdate,
                p2PStatusUpdate = P2PStatusUpdate.Initial
            )
        ),
        onTypeEvent = {},
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@DefaultPreviews
fun TopToolbarPreviewError() {
    TopToolbar(
        uiSyncStatusBadgeState = UiSyncStatusBadgeState.Visible(
            status = SpaceSyncAndP2PStatusState.Error("")
        ),
        onTypeEvent = {},
    )
}