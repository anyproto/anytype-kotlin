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

private data class AccessLevelUi(
    val level: SpaceInviteLinkAccessLevel,
    val icon: Int,
    val titleRes: Int,
    val descRes: Int
)

/**
 * Component for selecting space invite link access level
 * Implements Task #24: Three distinct link-based invitation options
 */
@Composable
fun InviteLinkAccessSelector(
    modifier: Modifier = Modifier,
    currentAccessLevel: SpaceInviteLinkAccessLevel,
    onAccessLevelChanged: (SpaceInviteLinkAccessLevel) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth()
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
            AccessLevelUi(
                level = SpaceInviteLinkAccessLevel.EDITOR_ACCESS,
                icon = R.drawable.ic_link_editor_24,
                titleRes = R.string.multiplayer_editor_access,
                descRes = R.string.multiplayer_editor_access_desc
            ),
            AccessLevelUi(
                level = SpaceInviteLinkAccessLevel.VIEWER_ACCESS,
                icon = R.drawable.ic_link_viewer_24,
                titleRes = R.string.multiplayer_viewer_access,
                descRes = R.string.multiplayer_viewer_access_desc
            ),
            AccessLevelUi(
                level = SpaceInviteLinkAccessLevel.REQUEST_ACCESS,
                icon = R.drawable.ic_link_request_24,
                titleRes = R.string.multiplayer_request_access,
                descRes = R.string.multiplayer_request_access_desc
            ),
            AccessLevelUi(
                level = SpaceInviteLinkAccessLevel.LINK_DISABLED,
                icon = R.drawable.ic_link_disabled_24,
                titleRes = R.string.multiplayer_link_disabled,
                descRes = R.string.multiplayer_link_disabled_desc
            )
        )

        options.forEachIndexed { index, item ->
            AccessLevelOption(
                modifier = Modifier.noRippleClickable {
                    if (currentAccessLevel != item.level) {
                        onAccessLevelChanged(item.level)
                    }
                },
                icon = item.icon,
                endIcon = if (currentAccessLevel == item.level) R.drawable.ic_check_black_14 else null,
                title = stringResource(item.titleRes),
                description = stringResource(item.descRes)
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
    icon: Int,
    endIcon: Int? = null,
    title: String,
    description: String
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
                painter = painterResource(id = icon),
                contentDescription = "Link icon",
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = Title2,
                color = colorResource(id = R.color.text_primary)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = Relations2,
                color = colorResource(id = R.color.text_secondary)
            )
        }
        if (endIcon != null) {
            Spacer(modifier = Modifier.width(12.dp))
            Image(
                modifier = Modifier.size(24.dp),
                painter = painterResource(id = endIcon),
                contentDescription = "End icon",
                contentScale = ContentScale.Inside
            )
            Spacer(modifier = Modifier.width(16.dp))
        } else {
            Spacer(modifier = Modifier.width(16.dp))
        }
    }
}

@Composable
fun SpaceInviteLinkAccessLevel.getInviteLinkItemParams() : Triple<String, String, Int> {
    return when (this) {
        SpaceInviteLinkAccessLevel.LINK_DISABLED ->
            Triple(
                stringResource(R.string.multiplayer_link_disabled),
                stringResource(R.string.multiplayer_link_disabled_desc),
                R.drawable.ic_link_disabled_24
            )
        SpaceInviteLinkAccessLevel.EDITOR_ACCESS ->
            Triple(
                stringResource(R.string.multiplayer_editor_access),
                stringResource(R.string.multiplayer_editor_access_desc),
                R.drawable.ic_link_editor_24
            )
        SpaceInviteLinkAccessLevel.VIEWER_ACCESS ->
            Triple(
                stringResource(R.string.multiplayer_viewer_access),
                stringResource(R.string.multiplayer_viewer_access_desc),
                R.drawable.ic_link_viewer_24
            )
        SpaceInviteLinkAccessLevel.REQUEST_ACCESS ->
            Triple(
                stringResource(R.string.multiplayer_request_access),
                stringResource(R.string.multiplayer_request_access_desc),
                R.drawable.ic_link_request_24
            )
    }
}

@Preview(showBackground = true)
@Composable
private fun InviteLinkAccessSelectorPreview() {
    InviteLinkAccessSelector(
        currentAccessLevel = SpaceInviteLinkAccessLevel.EDITOR_ACCESS,
        onAccessLevelChanged = {}
    )
}