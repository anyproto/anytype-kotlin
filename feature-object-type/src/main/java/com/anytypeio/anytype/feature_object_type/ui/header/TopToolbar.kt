package com.anytypeio.anytype.feature_object_type.ui.header

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.multiplayer.P2PStatusUpdate
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncAndP2PStatusState
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncError
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncNetwork
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncStatus
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncUpdate
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.syncstatus.StatusBadge
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Regular
import com.anytypeio.anytype.core_ui.widgets.objectIcon.TypeIconView
import com.anytypeio.anytype.feature_object_type.R
import com.anytypeio.anytype.feature_object_type.ui.TypeEvent
import com.anytypeio.anytype.feature_object_type.ui.UiIconState
import com.anytypeio.anytype.feature_object_type.ui.UiSyncStatusBadgeState
import com.anytypeio.anytype.feature_object_type.ui.UiTitleState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopToolbar(
    uiSyncStatusBadgeState: UiSyncStatusBadgeState,
    uiTitleState: UiTitleState,
    uiIconState: UiIconState,
    onTypeEvent: (TypeEvent) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
    ) {
        // Back button — circular pill at start, mirrors widget_object_top_toolbar.xml.
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
                .size(44.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = CircleShape,
                    clip = false
                )
                .background(
                    color = colorResource(id = com.anytypeio.anytype.core_ui.R.color.navigation_panel),
                    shape = CircleShape
                )
                .noRippleThrottledClickable {
                    onTypeEvent(TypeEvent.OnBackClick)
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                modifier = Modifier.wrapContentSize(),
                painter = painterResource(R.drawable.ic_default_top_back),
                contentDescription = stringResource(R.string.content_desc_back_button)
            )
        }

        // Center pill — type icon + title, matching the editor/set top toolbar.
        // Tapping opens the widgets overlay, mirroring the XML toolbar's
        // titlePillContainer click in EditorFragment / ObjectSetFragment.
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(start = 64.dp, end = 120.dp)
                .fillMaxHeight()
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(22.dp),
                    clip = false
                )
                .background(
                    color = colorResource(id = com.anytypeio.anytype.core_ui.R.color.navigation_panel),
                    shape = RoundedCornerShape(22.dp)
                )
                .noRippleThrottledClickable {
                    onTypeEvent(TypeEvent.OnTopBarTitleClick)
                }
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TypeIconView(
                modifier = Modifier.size(18.dp),
                icon = uiIconState.icon,
                backgroundSize = 18.dp,
                iconWithoutBackgroundMaxSize = 18.dp,
                backgroundColor = com.anytypeio.anytype.core_ui.R.color.amp_transparent
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = uiTitleState.title.ifBlank { stringResource(R.string.untitled) },
                style = PreviewTitle2Regular,
                color = colorResource(id = com.anytypeio.anytype.core_ui.R.color.text_primary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Trailing — status badge + menu, each in its own circular pill.
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (uiSyncStatusBadgeState is UiSyncStatusBadgeState.Visible) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(
                            elevation = 20.dp,
                            shape = CircleShape,
                            clip = false
                        )
                        .background(
                            color = colorResource(id = com.anytypeio.anytype.core_ui.R.color.navigation_panel),
                            shape = CircleShape
                        )
                        .noRippleThrottledClickable {
                            onTypeEvent(
                                TypeEvent.OnSyncStatusClick(
                                    status = uiSyncStatusBadgeState.status
                                )
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    StatusBadge(
                        status = uiSyncStatusBadgeState.status,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(
                        elevation = 20.dp,
                        shape = CircleShape,
                        clip = false
                    )
                    .background(
                        color = colorResource(id = com.anytypeio.anytype.core_ui.R.color.navigation_panel),
                        shape = CircleShape
                    )
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
        uiTitleState = UiTitleState(title = "Page", originalName = "Page", isEditable = false),
        uiIconState = UiIconState(icon = ObjectIcon.TypeIcon.Default.DEFAULT, isEditable = false),
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
        uiTitleState = UiTitleState(title = "", originalName = "", isEditable = false),
        uiIconState = UiIconState(icon = ObjectIcon.TypeIcon.Default.DEFAULT, isEditable = false),
        onTypeEvent = {},
    )
}
