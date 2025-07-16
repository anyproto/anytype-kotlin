package com.anytypeio.anytype.feature_date.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.multiplayer.P2PStatusUpdate
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncAndP2PStatusState
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncError
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncNetwork
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncStatus
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncUpdate
import com.anytypeio.anytype.core_models.primitives.TimestampInSeconds
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.syncstatus.StatusBadge
import com.anytypeio.anytype.feature_date.R
import com.anytypeio.anytype.feature_date.ui.models.DateEvent
import com.anytypeio.anytype.feature_date.viewmodel.UiCalendarIconState
import com.anytypeio.anytype.feature_date.viewmodel.UiSyncStatusBadgeState

@Composable
fun TopToolbarScreen(
    modifier: Modifier,
    uiCalendarIconState: UiCalendarIconState,
    uiSyncStatusBadgeState: UiSyncStatusBadgeState,
    onDateEvent: (DateEvent) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        if (uiSyncStatusBadgeState is UiSyncStatusBadgeState.Visible) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .noRippleThrottledClickable {
                        onDateEvent(
                            DateEvent.TopToolbar.OnSyncStatusClick(
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
        if (uiCalendarIconState is UiCalendarIconState.Visible) {
            Image(
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.CenterEnd)
                    .noRippleThrottledClickable {
                        onDateEvent(
                            DateEvent.TopToolbar.OnCalendarClick(
                                timestampInSeconds = uiCalendarIconState.timestampInSeconds
                            )
                        )
                    },
                contentDescription = null,
                painter = painterResource(id = R.drawable.ic_calendar_24),
                contentScale = ContentScale.None
            )
        }
    }
}

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
    TopToolbarScreen(
        modifier = Modifier.fillMaxWidth(),
        uiCalendarIconState = UiCalendarIconState.Visible(
            timestampInSeconds = TimestampInSeconds(3232L)
        ),
        uiSyncStatusBadgeState = UiSyncStatusBadgeState.Visible(
            status = SpaceSyncAndP2PStatusState.Success(
                spaceSyncUpdate = spaceSyncUpdate,
                p2PStatusUpdate = P2PStatusUpdate.Initial
            )
        ),
        onDateEvent = {}
    )
}