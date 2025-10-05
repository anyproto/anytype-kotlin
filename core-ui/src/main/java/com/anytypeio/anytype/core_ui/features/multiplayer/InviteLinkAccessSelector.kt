package com.anytypeio.anytype.core_ui.features.multiplayer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.multiplayer.SpaceInviteLinkAccessLevel
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.Relations2
import com.anytypeio.anytype.core_ui.views.Title2


enum class UiInviteLinkAccess(val icon: Int, val titleRes: Int, val descRes: Int) {
    VIEWER(
        R.drawable.ic_link_viewer_24,
        R.string.multiplayer_viewer_access,
        R.string.multiplayer_viewer_access_desc
    ),
    EDITOR(
        R.drawable.ic_link_editor_24,
        R.string.multiplayer_editor_access,
        R.string.multiplayer_editor_access_desc
    ),
    REQUEST(
        R.drawable.ic_link_request_24,
        R.string.multiplayer_request_access,
        R.string.multiplayer_request_access_desc
    ),
    DISABLED(
        R.drawable.ic_link_disabled_24,
        R.string.multiplayer_link_disabled,
        R.string.multiplayer_link_disabled_desc
    )
}

val DEFAULT_OPTIONS = listOf(
    UiInviteLinkAccess.EDITOR,
    UiInviteLinkAccess.VIEWER,
    UiInviteLinkAccess.REQUEST,
    UiInviteLinkAccess.DISABLED
)

/**
 * Component for selecting space invite link access level
 */
@Composable
fun InviteLinkAccessSelector(
    modifier: Modifier = Modifier,
    currentAccessLevel: SpaceInviteLinkAccessLevel,
    onAccessLevelChanged: (SpaceInviteLinkAccessLevel) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .background(
                shape = RoundedCornerShape(16.dp),
                color = colorResource(id = R.color.background_secondary)
            )
            .padding(bottom = 8.dp)
    ) {
        Dragger(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 6.dp)
        )

        DEFAULT_OPTIONS.forEachIndexed { index, item ->
            val isSelected = currentAccessLevel.getInviteLinkItemParams() == item
            AccessLevelOption(
                modifier = Modifier.noRippleThrottledClickable {
                    if (!isSelected) {
                        onAccessLevelChanged(
                            when (item) {
                                UiInviteLinkAccess.EDITOR -> SpaceInviteLinkAccessLevel.EditorAccess.EMPTY
                                UiInviteLinkAccess.VIEWER -> SpaceInviteLinkAccessLevel.ViewerAccess.EMPTY
                                UiInviteLinkAccess.REQUEST -> SpaceInviteLinkAccessLevel.RequestAccess.EMPTY
                                UiInviteLinkAccess.DISABLED -> SpaceInviteLinkAccessLevel.LinkDisabled()
                            }
                        )
                    }
                },
                uiItemUI = item,
                isSelected = isSelected
            )
            if (index < DEFAULT_OPTIONS.lastIndex) {
                Divider(paddingStart = 16.dp, paddingEnd = 16.dp)
            }
        }
    }
}

@Composable
fun AccessLevelOption(
    modifier: Modifier = Modifier,
    uiItemUI: UiInviteLinkAccess,
    isSelected: Boolean = false,
    isCurrentUserOwner: Boolean = false,
    isDisabled: Boolean = false
) {
    Row(
        modifier = modifier
            .heightIn(min = 72.dp)
            .fillMaxWidth()
            .alpha(if (isDisabled) 0.3f else 1.0f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(16.dp))
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color = colorResource(id = R.color.shape_transparent_secondary)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                modifier = Modifier.size(24.dp),
                painter = painterResource(id = uiItemUI.icon),
                contentDescription = "Link icon",
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(id = uiItemUI.titleRes),
                style = Title2,
                color = colorResource(id = R.color.text_primary)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(id = uiItemUI.descRes),
                style = Relations2,
                color = colorResource(id = R.color.text_secondary)
            )
        }
        if (isSelected) {
            Spacer(modifier = Modifier.width(12.dp))
            Image(
                modifier = Modifier.size(24.dp),
                painter = painterResource(id = R.drawable.ic_check_black_14),
                contentDescription = "End icon",
                contentScale = ContentScale.Inside
            )
            Spacer(modifier = Modifier.width(16.dp))
        } else {
            Spacer(modifier = Modifier.width(16.dp))
        }

        if (!isDisabled && isCurrentUserOwner && !isSelected) {
            Spacer(modifier = Modifier.width(12.dp))
            Image(
                modifier = Modifier.size(18.dp),
                painter = painterResource(id = R.drawable.ic_arrow_right_18),
                contentDescription = "Open change invite link screen icon",
                contentScale = ContentScale.Inside
            )
        }
    }
}

fun SpaceInviteLinkAccessLevel.getInviteLinkItemParams(): UiInviteLinkAccess = when (this) {
    is SpaceInviteLinkAccessLevel.EditorAccess -> UiInviteLinkAccess.EDITOR
    is SpaceInviteLinkAccessLevel.ViewerAccess -> UiInviteLinkAccess.VIEWER
    is SpaceInviteLinkAccessLevel.RequestAccess -> UiInviteLinkAccess.REQUEST
    is SpaceInviteLinkAccessLevel.LinkDisabled -> UiInviteLinkAccess.DISABLED
}

@DefaultPreviews
@Composable
private fun InviteLinkAccessSelectorPreview() {
    InviteLinkAccessSelector(
        currentAccessLevel = SpaceInviteLinkAccessLevel.LinkDisabled(possibleToUpdate = false),
        onAccessLevelChanged = {}
    )
}

@DefaultPreviews
@Composable
private fun AccessLevelOptionPreview() {
    AccessLevelOption(
        modifier = Modifier.fillMaxWidth(),
        isSelected = false,
        uiItemUI = UiInviteLinkAccess.DISABLED,
        isDisabled = true,
    )
}