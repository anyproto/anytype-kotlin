package com.anytypeio.anytype.feature_object_type.ui.header

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            if (uiSyncStatusBadgeState is UiSyncStatusBadgeState.Visible) {
                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .width(56.dp)
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
                            .align(Alignment.Center)
                    )
                }
            }
//            if (uiEditButtonState is UiEditButton.Visible) {
//                IconButton(
//                    modifier = Modifier
//                        .size(48.dp),
//                    onClick = {
//                        isIconMenuExpanded.value = !isIconMenuExpanded.value
//                    }
//                ) {
//                    Image(
//                        modifier = Modifier.size(24.dp),
//                        painter = painterResource(id = R.drawable.ic_space_list_dots),
//                        contentDescription = "More options"
//                    )
//                    DropdownMenu(
//                        modifier = Modifier
//                            .width(244.dp),
//                        expanded = isIconMenuExpanded.value,
//                        offset = DpOffset(x = 0.dp, y = 0.dp),
//                        onDismissRequest = {
//                            isIconMenuExpanded.value = false
//                        },
//                        shape = RoundedCornerShape(10.dp),
//                        containerColor = colorResource(id = R.color.background_secondary),
//                        border = BorderStroke(
//                            width = 0.5.dp,
//                            color = colorResource(id = R.color.background_secondary)
//                        )
//                    ) {
//                        DropdownMenuItem(
//                            text = {
//                                Text(
//                                    text = stringResource(R.string.object_type_settings_item_remove),
//                                    style = BodyRegular,
//                                    color = colorResource(id = R.color.palette_system_red)
//                                )
//                            },
//                            onClick = {
//                                onTypeEvent(TypeEvent.OnMenuItemDeleteClick)
//                                isIconMenuExpanded.value = false
//                            },
//                        )
//                    }
//                }
//            }
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