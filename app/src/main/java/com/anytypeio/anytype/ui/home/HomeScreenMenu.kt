package com.anytypeio.anytype.ui.home

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.R as CoreR

@Composable
fun HomeScreenMenu(
    expanded: Boolean,
    spaceAccessType: SpaceAccessType,
    spaceUxType: SpaceUxType,
    onDismiss: () -> Unit,
    onSpaceSettingsClicked: () -> Unit,
    onMembersClicked: () -> Unit,
    onMuteClicked: () -> Unit,
    onQrCodeClicked: () -> Unit,
    onCopyInviteLinkClicked: () -> Unit,
    onManageSectionsClicked: () -> Unit
) {
    val isSharedSpace = spaceAccessType == SpaceAccessType.SHARED
    val isOneToOne = spaceUxType == SpaceUxType.ONE_TO_ONE

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        containerColor = colorResource(CoreR.color.background_secondary),
        shape = RoundedCornerShape(12.dp),
        offset = DpOffset(x = 0.dp, y = 8.dp)
    ) {
        // Space Settings - always visible
        DropdownMenuItem(
            text = {
                MenuItemContent(
                    icon = CoreR.drawable.ic_settings_24,
                    text = stringResource(com.anytypeio.anytype.localization.R.string.space_settings)
                )
            },
            onClick = {
                onSpaceSettingsClicked()
                onDismiss()
            }
        )

        // Members - hidden for 1-1 spaces
        if (!isOneToOne) {
            DropdownMenuItem(
                text = {
                MenuItemContent(
                    icon = CoreR.drawable.ic_members_24,
                    text = "Members" // TODO: Add string resource
                )
                },
                onClick = {
                    onMembersClicked()
                    onDismiss()
                }
            )
        }

        // Mute/Unmute - always visible
        // TODO: Implement mute state tracking to show correct label
        DropdownMenuItem(
            text = {
                MenuItemContent(
                    icon = CoreR.drawable.ic_bell_24,
                    text = "Mute" // TODO: Add string resource or implement mute/unmute toggle
                )
            },
            onClick = {
                onMuteClicked()
                onDismiss()
            }
        )

        // QR Code - only for shared spaces
        if (isSharedSpace) {
            DropdownMenuItem(
                text = {
                MenuItemContent(
                    icon = CoreR.drawable.ic_qr_code_24,
                    text = "QR Code" // TODO: Add string resource
                )
                },
                onClick = {
                    onQrCodeClicked()
                    onDismiss()
                }
            )
        }

        // Copy Invite Link - only for shared spaces
        if (isSharedSpace) {
            DropdownMenuItem(
                text = {
                MenuItemContent(
                    icon = CoreR.drawable.ic_copy_link_24,
                    text = stringResource(com.anytypeio.anytype.localization.R.string.copy_link)
                )
                },
                onClick = {
                    onCopyInviteLinkClicked()
                    onDismiss()
                }
            )
        }

        // Manage Sections - always visible
        DropdownMenuItem(
            text = {
                MenuItemContent(
                    icon = CoreR.drawable.ic_obj_settings_layout_24,
                    text = stringResource(com.anytypeio.anytype.localization.R.string.manage_sections_title)
                )
            },
            onClick = {
                onManageSectionsClicked()
                onDismiss()
            }
        )
    }
}

@Composable
private fun MenuItemContent(
    icon: Int,
    text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = colorResource(CoreR.color.text_primary)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = BodyRegular,
            color = colorResource(CoreR.color.text_primary)
        )
    }
}
