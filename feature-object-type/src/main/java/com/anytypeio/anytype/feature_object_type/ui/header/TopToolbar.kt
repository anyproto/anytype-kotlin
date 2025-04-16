package com.anytypeio.anytype.feature_object_type.ui.header

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
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
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Regular
import com.anytypeio.anytype.feature_object_type.R
import com.anytypeio.anytype.feature_object_type.ui.TypeEvent
import com.anytypeio.anytype.feature_object_type.ui.UiEditButton
import com.anytypeio.anytype.feature_object_type.ui.UiSyncStatusBadgeState
import com.anytypeio.anytype.feature_object_type.ui.UiTitleState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopToolbar(
    modifier: Modifier,
    uiEditButtonState: UiEditButton,
    uiSyncStatusBadgeState: UiSyncStatusBadgeState,
    topBarScrollBehavior: TopAppBarScrollBehavior,
    uiTitleState: UiTitleState,
    onTypeEvent: (TypeEvent) -> Unit
) {
    val isIconMenuExpanded = remember {
        mutableStateOf(false)
    }

    CenterAlignedTopAppBar(
        modifier = modifier.fillMaxWidth()
            .padding(horizontal = 0.dp),
        expandedHeight = 48.dp,
        scrollBehavior = topBarScrollBehavior,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = colorResource(id = R.color.background_primary),
            scrolledContainerColor = colorResource(id = R.color.background_primary),
            titleContentColor = colorResource(id = R.color.palette_system_red)
        ),
        title = {
            if (topBarScrollBehavior.state.overlappedFraction > 0.7f) {
                Box(
                    modifier = Modifier.fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiTitleState.title,
                        style = PreviewTitle2Regular,
                        color = colorResource(R.color.text_primary),
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        navigationIcon = {
            Box(
                modifier = Modifier
                    .width(56.dp)
                    .height(48.dp)
                    .noRippleThrottledClickable {
                        onTypeEvent(TypeEvent.OnBackClick)
                    },
                contentAlignment = Alignment.CenterStart
            ) {
                Image(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .wrapContentSize(),
                    painter = painterResource(R.drawable.ic_default_top_back),
                    contentDescription = stringResource(R.string.content_desc_back_button)
                )
            }
        },
        actions = {
            if (uiSyncStatusBadgeState is UiSyncStatusBadgeState.Visible) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
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
            if (uiEditButtonState is UiEditButton.Visible) {
                IconButton(
                    modifier = Modifier
                        .size(48.dp),
                    onClick = {
                        isIconMenuExpanded.value = !isIconMenuExpanded.value
                    }
                ) {
                    Image(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(id = R.drawable.ic_space_list_dots),
                        contentDescription = "More options"
                    )
                    DropdownMenu(
                        modifier = Modifier
                            .width(244.dp),
                        expanded = isIconMenuExpanded.value,
                        offset = DpOffset(x = 0.dp, y = 0.dp),
                        onDismissRequest = {
                            isIconMenuExpanded.value = false
                        },
                        shape = RoundedCornerShape(10.dp),
                        containerColor = colorResource(id = R.color.background_secondary),
                        border = BorderStroke(
                            width = 0.5.dp,
                            color = colorResource(id = R.color.background_secondary)
                        )
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(R.string.object_type_settings_item_remove),
                                    style = BodyRegular,
                                    color = colorResource(id = R.color.palette_system_red)
                                )
                            },
                            onClick = {
                                onTypeEvent(TypeEvent.OnMenuItemDeleteClick)
                                isIconMenuExpanded.value = false
                            },
                        )
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@DefaultPreviews
fun TopToolbarPreview() {
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(
        state = rememberTopAppBarState()
    )
    val spaceSyncUpdate = SpaceSyncUpdate.Update(
        id = "1",
        status = SpaceSyncStatus.SYNCING,
        network = SpaceSyncNetwork.ANYTYPE,
        error = SpaceSyncError.NULL,
        syncingObjectsCounter = 2
    )
    TopToolbar(
        modifier = Modifier.fillMaxWidth(),
        uiSyncStatusBadgeState = UiSyncStatusBadgeState.Visible(
            status = SpaceSyncAndP2PStatusState.Success(
                spaceSyncUpdate = spaceSyncUpdate,
                p2PStatusUpdate = P2PStatusUpdate.Initial
            )
        ),
        uiEditButtonState = UiEditButton.Visible,
        onTypeEvent = {},
        topBarScrollBehavior = topAppBarScrollBehavior,
        uiTitleState = UiTitleState(title = "Pages", originalName = "Page", isEditable = true)
    )
}