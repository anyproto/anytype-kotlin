package com.anytypeio.anytype.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.R as CoreR

@Composable
fun HomeScreenMenu(
    expanded: Boolean,
    spaceAccessType: SpaceAccessType,
    spaceUxType: SpaceUxType,
    isMuted: Boolean,
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
        modifier = Modifier.width(254.dp),
        expanded = expanded,
        onDismissRequest = onDismiss,
        containerColor = colorResource(CoreR.color.background_secondary),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 8.dp,
        offset = DpOffset(
            x = (-16).dp,
            y = 48.dp  // 52dp toolbar height + 8dp spacing
        )
    ) {
        // Space Settings - always visible
        DropdownMenuItem(
            text = {
                MenuItemContent(
                    icon = CoreR.drawable.ic_space_settings_24,
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
                    text = stringResource(com.anytypeio.anytype.localization.R.string.multiplayer_members)
                )
                },
                onClick = {
                    onMembersClicked()
                    onDismiss()
                }
            )
        }

        // Mute/Unmute - always visible
        DropdownMenuItem(
            text = {
                MenuItemContent(
                    icon = if (isMuted) {
                        CoreR.drawable.ic_notifications
                    } else {
                        CoreR.drawable.ic_notifications_off
                    },
                    text = stringResource(
                        if (isMuted) {
                            com.anytypeio.anytype.localization.R.string.multiplayer_unmute
                        } else {
                            com.anytypeio.anytype.localization.R.string.multiplayer_mute
                        }
                    )
                )
            },
            onClick = {
                onMuteClicked()
                onDismiss()
            }
        )

        // Divider after mute
        Divider(paddingStart = 0.dp, paddingEnd = 0.dp, height = 8.dp)

        // QR Code - only for shared spaces, hidden for 1-1 chats
        if (isSharedSpace && !isOneToOne) {
            DropdownMenuItem(
                text = {
                MenuItemContent(
                    icon = CoreR.drawable.ic_qr_code_24,
                    text = stringResource(com.anytypeio.anytype.localization.R.string.multiplayer_qr_code)
                )
                },
                onClick = {
                    onQrCodeClicked()
                    onDismiss()
                }
            )
        }

        // Copy Invite Link - only for shared spaces, hidden for 1-1 chats
        if (isSharedSpace && !isOneToOne) {
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
            
            // Divider after shared space items
            Divider(paddingStart = 0.dp, paddingEnd = 0.dp, height = 8.dp)
        }

        // Manage Sections - always visible
        DropdownMenuItem(
            text = {
                MenuItemContent(
                    icon = CoreR.drawable.ic_burger_24,
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
        Text(
            text = text,
            style = BodyRegular,
            color = colorResource(CoreR.color.text_primary),
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Image(
            painter = painterResource(id = icon),
            contentDescription = null,
            colorFilter = ColorFilter.tint(colorResource(CoreR.color.text_primary)),
            modifier = Modifier.size(24.dp)
        )
    }
}
