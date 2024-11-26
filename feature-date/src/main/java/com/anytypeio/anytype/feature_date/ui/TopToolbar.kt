package com.anytypeio.anytype.feature_date.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.syncstatus.StatusBadge
import com.anytypeio.anytype.feature_date.R
import com.anytypeio.anytype.feature_date.models.DateObjectTopToolbarState


@Composable
fun DateObjectTopToolbar(
    modifier: Modifier,
    state: DateObjectTopToolbarState,
    action: (DateObjectTopToolbarState.Action) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        if (state is DateObjectTopToolbarState.Content) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .noRippleThrottledClickable {
                        action(DateObjectTopToolbarState.Action.SyncStatus)
                    },
            ) {
                StatusBadge(
                    status = state.status,
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.Center)
                )
            }
            Image(
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.CenterEnd)
                    .clickable {
                        action(DateObjectTopToolbarState.Action.Calendar)
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
fun DateLayoutTopToolbarPreview() {
    val spaceSyncUpdate = SpaceSyncUpdate.Update(
        id = "1",
        status = SpaceSyncStatus.SYNCING,
        network = SpaceSyncNetwork.ANYTYPE,
        error = SpaceSyncError.NULL,
        syncingObjectsCounter = 2
    )
    DateObjectTopToolbar(
        modifier = Modifier.fillMaxWidth(),
        state = DateObjectTopToolbarState.Content(
            SpaceSyncAndP2PStatusState.Success(
                spaceSyncUpdate = spaceSyncUpdate,
                p2PStatusUpdate = P2PStatusUpdate.Initial
            )
        ),
        action = {}
    )
}