package com.anytypeio.anytype.ui.vault

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Regular
import com.anytypeio.anytype.presentation.vault.VaultUiState

@Composable
fun SpaceActionsDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    isMuted: Boolean?,
    isPinned: Boolean,
    currentPinnedCount: Int,
    onMuteToggle: () -> Unit,
    onPinToggle: () -> Unit,
    onSpaceSettings: () -> Unit
) {
    DropdownMenu(
        modifier = Modifier.width(254.dp),
        expanded = expanded,
        onDismissRequest = onDismiss,
        containerColor = colorResource(R.color.background_secondary),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 8.dp,
        offset = DpOffset(
            x = 16.dp,
            y = 8.dp
        )
    ) {
        // Show pin/unpin option or limit message
        val canPin = currentPinnedCount < VaultUiState.MAX_PINNED_SPACES

        if (!isPinned && !canPin) {
            // Show info message when limit reached and space is not pinned
            Row(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(
                        R.string.vault_pinned_limit_message,
                        VaultUiState.MAX_PINNED_SPACES
                    ),
                    style = PreviewTitle2Regular,
                    color = colorResource(id = R.color.text_secondary)
                )
            }
        } else {
            // Show pin/unpin option when space is pinned or when there's room to pin
            DropdownMenuItem(
                onClick = {
                    onPinToggle()
                    onDismiss()
                },
                text = {
                    val stringRes = if (isPinned) {
                        R.string.vault_unpin_space
                    } else {
                        R.string.vault_pin_space
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = stringResource(id = stringRes),
                            style = BodyRegular,
                            color = colorResource(id = R.color.text_primary)
                        )
                        Image(
                            painter = painterResource(id = if (isPinned) R.drawable.ic_unpin_24 else R.drawable.ic_pin_24),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .size(24.dp)
                        )
                    }
                }
            )
        }

        // Mute/Unmute only if chat enabled
        if (isMuted != null) {
            Divider(paddingStart = 0.dp, paddingEnd = 0.dp)
            DropdownMenuItem(
                onClick = {
                    onMuteToggle()
                    onDismiss()
                },
                text = {
                    val (stringRes, iconRes) = if (isMuted) {
                        R.string.space_notify_unmute to R.drawable.ic_notifications
                    } else {
                        R.string.space_notify_mute to R.drawable.ic_notifications_off
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = stringResource(id = stringRes),
                            style = BodyRegular,
                            color = colorResource(id = R.color.text_primary)
                        )
                        Image(
                            painter = painterResource(id = iconRes),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .size(24.dp)
                        )
                    }
                }
            )
        }
        Divider(paddingStart = 0.dp, paddingEnd = 0.dp)
        // Space Settings (always shown)
        DropdownMenuItem(
            onClick = {
                onSpaceSettings()
                onDismiss()
            },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        modifier = Modifier.weight(1f),
                        style = BodyRegular,
                        color = colorResource(id = R.color.text_primary),
                        text = stringResource(R.string.vault_space_settings)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.ic_space_settings_24),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(24.dp)
                    )
                }
            }
        )
    }
}