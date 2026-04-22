package com.anytypeio.anytype.feature_vault.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.chats.NotificationState
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.domain.notifications.SpaceNotificationMenuShape
import com.anytypeio.anytype.feature_vault.R

@Composable
fun SpaceActionsDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    menuShape: SpaceNotificationMenuShape = SpaceNotificationMenuShape.DmToggle,
    currentNotificationMode: NotificationState = NotificationState.ALL,
    isMuted: Boolean? = null,
    isPinned: Boolean,
    isOwner: Boolean = true,
    onMuteToggle: () -> Unit = {},
    onSetSpaceNotificationMode: (NotificationState) -> Unit = {},
    onPinToggle: () -> Unit,
    onSpaceSettings: () -> Unit,
    onDeleteOrLeaveSpace: () -> Unit = {}
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

        // Notifications section — shape-driven. Tripartite spaces get a
        // three-option expandable submenu; DM/Channel spaces get a binary
        // Mute/Unmute row whose semantics depend on the containing type.
        when (menuShape) {
            SpaceNotificationMenuShape.Tripartite -> {
                Divider(paddingStart = 0.dp, paddingEnd = 0.dp)
                NotificationsSubmenu(
                    currentMode = currentNotificationMode,
                    onSelect = { mode ->
                        onSetSpaceNotificationMode(mode)
                        onDismiss()
                    }
                )
            }
            SpaceNotificationMenuShape.DmToggle,
            SpaceNotificationMenuShape.ChannelToggle -> {
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
            }
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
        Divider(paddingStart = 0.dp, paddingEnd = 0.dp, height = 8.dp)
        // Delete/Leave Space (always shown)
        DropdownMenuItem(
            onClick = {
                onDeleteOrLeaveSpace()
                onDismiss()
            },
            text = {
                val stringRes = if (isOwner) {
                    R.string.delete_space
                } else {
                    R.string.multiplayer_leave_space
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        modifier = Modifier.weight(1f),
                        style = BodyRegular,
                        color = colorResource(id = R.color.text_primary),
                        text = stringResource(id = stringRes)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.ic_leave_space_24),
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

@Composable
private fun NotificationsSubmenu(
    currentMode: NotificationState,
    onSelect: (NotificationState) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        label = "vault notifications chevron rotation"
    )
    // Header row — tap to expand/collapse.
    DropdownMenuItem(
        onClick = { expanded = !expanded },
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.ic_arrow_right_18),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(colorResource(id = R.color.text_primary)),
                    modifier = Modifier
                        .width(14.dp)
                        .height(22.dp)
                        .rotate(chevronRotation)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(id = R.string.notifications_title),
                    style = BodyRegular,
                    color = colorResource(id = R.color.text_primary)
                )
                Image(
                    painter = painterResource(id = R.drawable.ic_notifications),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(colorResource(id = R.color.text_primary)),
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .size(24.dp)
                )
            }
        }
    )
    if (expanded) {
        NotificationOptionRow(
            label = stringResource(id = R.string.notification_option_enable),
            isSelected = currentMode == NotificationState.ALL,
            onClick = { onSelect(NotificationState.ALL) }
        )
        NotificationOptionRow(
            label = stringResource(id = R.string.notifications_mentions),
            isSelected = currentMode == NotificationState.MENTIONS,
            onClick = { onSelect(NotificationState.MENTIONS) }
        )
        NotificationOptionRow(
            label = stringResource(id = R.string.notification_option_disabled),
            isSelected = currentMode == NotificationState.DISABLE,
            onClick = { onSelect(NotificationState.DISABLE) }
        )
    }
}

@Composable
private fun NotificationOptionRow(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        onClick = onClick,
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isSelected) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_check_16),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colorResource(id = R.color.text_primary)),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                } else {
                    Spacer(modifier = Modifier.width(24.dp))
                }
                Text(
                    modifier = Modifier.weight(1f),
                    text = label,
                    style = BodyRegular,
                    color = colorResource(id = R.color.text_primary)
                )
            }
        }
    )
}
