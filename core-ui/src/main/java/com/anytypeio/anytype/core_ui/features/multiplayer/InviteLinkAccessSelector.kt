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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.multiplayer.SpaceInviteLinkAccessLevel
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.Relations2
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.presentation.multiplayer.ShareSpaceViewModel.UiEvent


enum class UiLinkAccessItem(val icon: Int, val titleRes: Int, val descRes: Int) {
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

/**
 * Component for selecting space invite link access level
 * Implements Task #24: Three distinct link-based invitation options
 */
@Composable
fun InviteLinkAccessSelector(
    modifier: Modifier = Modifier,
    currentAccessLevel: SpaceInviteLinkAccessLevel,
    onAccessLevelChanged: (UiEvent) -> Unit,
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

        val options = listOf(
            UiLinkAccessItem.VIEWER,
            UiLinkAccessItem.EDITOR,
            UiLinkAccessItem.REQUEST,
            UiLinkAccessItem.DISABLED
        )

        options.forEachIndexed { index, item ->
            val isSelected = currentAccessLevel.matches(item)
            AccessLevelOption(
                modifier = Modifier.noRippleClickable {
                    if (!isSelected) {
                        onAccessLevelChanged(
                            when (item) {
                                UiLinkAccessItem.DISABLED -> UiEvent.AccessChange.Disabled
                                UiLinkAccessItem.EDITOR -> UiEvent.AccessChange.Editor
                                UiLinkAccessItem.REQUEST -> UiEvent.AccessChange.Request
                                UiLinkAccessItem.VIEWER -> UiEvent.AccessChange.Viewer
                            }
                        )
                    }
                },
                uiItemUI = item,
                isSelected = isSelected
            )
            if (index < options.lastIndex) {
                Divider(paddingStart = 16.dp, paddingEnd = 16.dp)
            }
        }
    }
}

@Composable
fun AccessLevelOption(
    modifier: Modifier = Modifier,
    uiItemUI: UiLinkAccessItem,
    isSelected: Boolean = false
) {
    Row(
        modifier = modifier
            .heightIn(min = 72.dp)
            .fillMaxWidth(),
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
    }
}

fun SpaceInviteLinkAccessLevel.getInviteLinkItemParams(): UiLinkAccessItem = when (this) {
    is SpaceInviteLinkAccessLevel.EditorAccess -> UiLinkAccessItem.EDITOR
    is SpaceInviteLinkAccessLevel.ViewerAccess -> UiLinkAccessItem.VIEWER
    is SpaceInviteLinkAccessLevel.RequestAccess -> UiLinkAccessItem.REQUEST
    SpaceInviteLinkAccessLevel.LinkDisabled -> UiLinkAccessItem.DISABLED
}

private fun SpaceInviteLinkAccessLevel.matches(item: UiLinkAccessItem): Boolean {
    return this.getInviteLinkItemParams() == item
}

@Preview(showBackground = true)
@Composable
private fun InviteLinkAccessSelectorPreview() {
    InviteLinkAccessSelector(
        currentAccessLevel = SpaceInviteLinkAccessLevel.EditorAccess(""),
        onAccessLevelChanged = {}
    )
}