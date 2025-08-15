package com.anytypeio.anytype.core_ui.features.multiplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.multiplayer.SpaceInviteLinkAccessLevel
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.views.Title1

/**
 * Component for selecting space invite link access level
 * Implements Task #24: Three distinct link-based invitation options
 */
@Composable
fun InviteLinkAccessSelector(
    modifier: Modifier = Modifier,
    currentAccessLevel: SpaceInviteLinkAccessLevel,
    hasActiveLink: Boolean,
    onAccessLevelChanged: (SpaceInviteLinkAccessLevel) -> Unit,
    onGenerateLinkClicked: () -> Unit,
    onShareLinkClicked: () -> Unit,
    onShowQrCodeClicked: () -> Unit,
    onDeleteLinkClicked: () -> Unit,
    isLoading: Boolean = false
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = colorResource(id = R.color.background_primary),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 0.5.dp,
                color = colorResource(id = R.color.shape_primary),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.multiplayer_invite_link_access),
            style = PreviewTitle2Medium,
            color = colorResource(id = R.color.text_primary)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Link Disabled Option
        AccessLevelOption(
            level = SpaceInviteLinkAccessLevel.LINK_DISABLED,
            currentLevel = currentAccessLevel,
            title = stringResource(R.string.multiplayer_link_disabled),
            description = stringResource(R.string.multiplayer_link_disabled_desc),
            onSelected = { onAccessLevelChanged(SpaceInviteLinkAccessLevel.LINK_DISABLED) }
        )
        
        Divider(modifier = Modifier.padding(vertical = 12.dp))
        
        // Editor Access Option
        AccessLevelOption(
            level = SpaceInviteLinkAccessLevel.EDITOR_ACCESS,
            currentLevel = currentAccessLevel,
            title = stringResource(R.string.multiplayer_editor_access),
            description = stringResource(R.string.multiplayer_editor_access_desc),
            onSelected = { onAccessLevelChanged(SpaceInviteLinkAccessLevel.EDITOR_ACCESS) }
        )
        
        Divider(modifier = Modifier.padding(vertical = 12.dp))
        
        // Viewer Access Option
        AccessLevelOption(
            level = SpaceInviteLinkAccessLevel.VIEWER_ACCESS,
            currentLevel = currentAccessLevel,
            title = stringResource(R.string.multiplayer_viewer_access),
            description = stringResource(R.string.multiplayer_viewer_access_desc),
            onSelected = { onAccessLevelChanged(SpaceInviteLinkAccessLevel.VIEWER_ACCESS) }
        )
        
        Divider(modifier = Modifier.padding(vertical = 12.dp))
        
        // Request Access Option
        AccessLevelOption(
            level = SpaceInviteLinkAccessLevel.REQUEST_ACCESS,
            currentLevel = currentAccessLevel,
            title = stringResource(R.string.multiplayer_request_access),
            description = stringResource(R.string.multiplayer_request_access_desc),
            onSelected = { onAccessLevelChanged(SpaceInviteLinkAccessLevel.REQUEST_ACCESS) }
        )
        
        // Action buttons based on current state
        if (currentAccessLevel != SpaceInviteLinkAccessLevel.LINK_DISABLED) {
            Spacer(modifier = Modifier.height(20.dp))
            Divider()
            Spacer(modifier = Modifier.height(20.dp))
            
            if (hasActiveLink) {
                // Show share options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ButtonPrimary(
                        text = stringResource(R.string.multiplayer_share_link),
                        onClick = onShareLinkClicked,
                        size = ButtonSize.Small,
                        modifier = Modifier.weight(1f)
                    )
                    ButtonSecondary(
                        text = stringResource(R.string.multiplayer_qr_code),
                        onClick = onShowQrCodeClicked,
                        size = ButtonSize.Small,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                ButtonSecondary(
                    text = stringResource(R.string.multiplayer_delete_link),
                    onClick = onDeleteLinkClicked,
                    size = ButtonSize.Small,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // Show generate button
                ButtonPrimary(
                    text = stringResource(R.string.multiplayer_generate_invite_link),
                    onClick = onGenerateLinkClicked,
                    size = ButtonSize.Small,
                    modifier = Modifier.fillMaxWidth(),
                    loading = isLoading
                )
            }
        }
    }
}

@Composable
private fun AccessLevelOption(
    level: SpaceInviteLinkAccessLevel,
    currentLevel: SpaceInviteLinkAccessLevel,
    title: String,
    description: String,
    onSelected: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .noRippleClickable { onSelected() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        RadioButton(
            selected = level == currentLevel,
            onClick = onSelected,
            colors = RadioButtonDefaults.colors(
                selectedColor = colorResource(id = R.color.palette_system_amber_100),
                unselectedColor = colorResource(id = R.color.text_secondary)
            ),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = BodyRegular,
                color = colorResource(id = R.color.text_primary)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = Caption1Regular,
                color = colorResource(id = R.color.text_secondary)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InviteLinkAccessSelectorPreview() {
    InviteLinkAccessSelector(
        currentAccessLevel = SpaceInviteLinkAccessLevel.EDITOR_ACCESS,
        hasActiveLink = true,
        onAccessLevelChanged = {},
        onGenerateLinkClicked = {},
        onShareLinkClicked = {},
        onShowQrCodeClicked = {},
        onDeleteLinkClicked = {}
    )
}