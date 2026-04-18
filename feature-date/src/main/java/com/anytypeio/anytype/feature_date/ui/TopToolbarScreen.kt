package com.anytypeio.anytype.feature_date.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.anytypeio.anytype.core_models.DayOfWeekCustom
import com.anytypeio.anytype.core_models.RelativeDate
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
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Regular
import com.anytypeio.anytype.feature_date.R
import com.anytypeio.anytype.feature_date.ui.models.DateEvent
import com.anytypeio.anytype.feature_date.viewmodel.UiCalendarIconState
import com.anytypeio.anytype.feature_date.viewmodel.UiHeaderState
import com.anytypeio.anytype.feature_date.viewmodel.UiSyncStatusBadgeState

@Composable
fun TopToolbarScreen(
    modifier: Modifier,
    uiHeaderState: UiHeaderState,
    uiCalendarIconState: UiCalendarIconState,
    uiSyncStatusBadgeState: UiSyncStatusBadgeState,
    onDateEvent: (DateEvent) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
    ) {
        // Back pill — circular button at start.
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
                    onDateEvent(DateEvent.TopToolbar.OnBackClick)
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                modifier = Modifier.wrapContentSize(),
                painter = painterResource(R.drawable.ic_default_top_back),
                contentDescription = stringResource(R.string.content_desc_back_button)
            )
        }

        // Center title pill — tapping opens the widgets overlay.
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
                    onDateEvent(DateEvent.TopToolbar.OnTitleClick)
                }
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            val titleText = (uiHeaderState as? UiHeaderState.Content)?.title.orEmpty()
            if (titleText.isNotEmpty()) {
                Text(
                    text = titleText,
                    style = PreviewTitle2Regular,
                    color = colorResource(id = com.anytypeio.anytype.core_ui.R.color.text_primary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Trailing — optional sync badge pill + calendar pill (rightmost slot).
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
                            onDateEvent(
                                DateEvent.TopToolbar.OnSyncStatusClick(
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
            if (uiCalendarIconState is UiCalendarIconState.Visible) {
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
                            onDateEvent(
                                DateEvent.TopToolbar.OnCalendarClick(
                                    timestampInSeconds = uiCalendarIconState.timestampInSeconds
                                )
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(id = R.drawable.ic_calendar_24),
                        contentDescription = null
                    )
                }
            }
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
        uiHeaderState = UiHeaderState.Content(
            title = "Tue, 12 Oct",
            relativeDate = RelativeDate.Today(
                initialTimeInMillis = 1634016000000,
                dayOfWeek = DayOfWeekCustom.MONDAY
            )
        ),
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

@Composable
@DefaultPreviews
fun TopToolbarHiddenSyncPreview() {
    TopToolbarScreen(
        modifier = Modifier.fillMaxWidth(),
        uiHeaderState = UiHeaderState.Loading,
        uiCalendarIconState = UiCalendarIconState.Hidden,
        uiSyncStatusBadgeState = UiSyncStatusBadgeState.Hidden,
        onDateEvent = {}
    )
}
