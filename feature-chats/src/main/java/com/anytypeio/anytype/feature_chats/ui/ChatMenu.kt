package com.anytypeio.anytype.feature_chats.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
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
import androidx.compose.ui.window.PopupProperties
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.feature_chats.R

enum class NotificationSetting {
    ALL, MENTIONS, MUTE
}

@Composable
fun ChatMenu(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    currentNotificationSetting: NotificationSetting,
    onDismissRequest: () -> Unit,
    onPropertiesClick: () -> Unit,
    onEditInfoClick: () -> Unit,
    onNotificationSettingChanged: (NotificationSetting) -> Unit,
    onPinClick: () -> Unit,
    onMoveToBinClick: () -> Unit
) {
    var notificationsExpanded by remember { mutableStateOf(false) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (notificationsExpanded) 180f else 0f,
        label = "chevron rotation"
    )

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        MaterialTheme(
            shapes = MaterialTheme.shapes.copy(
                medium = RoundedCornerShape(10.dp)
            ),
            colors = MaterialTheme.colors.copy(
                surface = colorResource(id = R.color.background_secondary)
            )
        ) {
            DropdownMenu(
                modifier = Modifier.align(Alignment.TopEnd),
                offset = DpOffset((-16).dp, 8.dp),
                expanded = expanded,
                onDismissRequest = onDismissRequest,
                properties = PopupProperties(focusable = false)
            ) {
                // Properties
                DropdownMenuItem(
                    content = {
                        Text(
                            text = stringResource(R.string.properties),
                            color = colorResource(id = R.color.text_primary),
                            modifier = Modifier.padding(end = 64.dp)
                        )
                    },
                    onClick = { onPropertiesClick() }
                )
                Divider(paddingStart = 0.dp, paddingEnd = 0.dp)

                // Edit Info
                DropdownMenuItem(
                    content = {
                        Text(
                            text = stringResource(R.string.chat_edit_info),
                            color = colorResource(id = R.color.text_primary),
                            modifier = Modifier.padding(end = 64.dp)
                        )
                    },
                    onClick = { onEditInfoClick() }
                )
                Divider(paddingStart = 0.dp, paddingEnd = 0.dp)

                // Notifications - expandable
                DropdownMenuItem(
                    content = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = com.anytypeio.anytype.core_ui.R.drawable.ic_bell_24),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(colorResource(id = R.color.text_primary)),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.notifications_title),
                                color = colorResource(id = R.color.text_primary),
                                modifier = Modifier.weight(1f)
                            )
                            Image(
                                painter = painterResource(id = com.anytypeio.anytype.core_ui.R.drawable.ic_arrow_down_18),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(colorResource(id = R.color.text_primary)),
                                modifier = Modifier
                                    .size(18.dp)
                                    .rotate(chevronRotation)
                            )
                        }
                    },
                    onClick = { notificationsExpanded = !notificationsExpanded }
                )

                // Notification sub-items
                if (notificationsExpanded) {
                    DropdownMenuItem(
                        content = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 32.dp)
                            ) {
                                if (currentNotificationSetting == NotificationSetting.ALL) {
                                    Image(
                                        painter = painterResource(id = com.anytypeio.anytype.core_ui.R.drawable.ic_check_16),
                                        contentDescription = null,
                                        colorFilter = ColorFilter.tint(colorResource(id = R.color.text_primary)),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                } else {
                                    Spacer(modifier = Modifier.width(24.dp))
                                }
                                Text(
                                    text = stringResource(R.string.chat_notifications_receive_all),
                                    color = colorResource(id = R.color.text_primary)
                                )
                            }
                        },
                        onClick = {
                            onNotificationSettingChanged(NotificationSetting.ALL)
                            onDismissRequest()
                        }
                    )

                    DropdownMenuItem(
                        content = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 32.dp)
                            ) {
                                if (currentNotificationSetting == NotificationSetting.MENTIONS) {
                                    Image(
                                        painter = painterResource(id = com.anytypeio.anytype.core_ui.R.drawable.ic_check_16),
                                        contentDescription = null,
                                        colorFilter = ColorFilter.tint(colorResource(id = R.color.text_primary)),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                } else {
                                    Spacer(modifier = Modifier.width(24.dp))
                                }
                                Text(
                                    text = stringResource(R.string.notifications_mentions),
                                    color = colorResource(id = R.color.text_primary)
                                )
                            }
                        },
                        onClick = {
                            onNotificationSettingChanged(NotificationSetting.MENTIONS)
                            onDismissRequest()
                        }
                    )

                    DropdownMenuItem(
                        content = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 32.dp)
                            ) {
                                if (currentNotificationSetting == NotificationSetting.MUTE) {
                                    Image(
                                        painter = painterResource(id = com.anytypeio.anytype.core_ui.R.drawable.ic_check_16),
                                        contentDescription = null,
                                        colorFilter = ColorFilter.tint(colorResource(id = R.color.text_primary)),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                } else {
                                    Spacer(modifier = Modifier.width(24.dp))
                                }
                                Text(
                                    text = stringResource(R.string.chat_notifications_mute_all),
                                    color = colorResource(id = R.color.text_primary)
                                )
                            }
                        },
                        onClick = {
                            onNotificationSettingChanged(NotificationSetting.MUTE)
                            onDismissRequest()
                        }
                    )
                }

                Divider(paddingStart = 0.dp, paddingEnd = 0.dp)

                // Pin
                DropdownMenuItem(
                    content = {
                        Text(
                            text = stringResource(R.string.object_action_pin),
                            color = colorResource(id = R.color.text_primary),
                            modifier = Modifier.padding(end = 64.dp)
                        )
                    },
                    onClick = { onPinClick() }
                )
                Divider(paddingStart = 0.dp, paddingEnd = 0.dp)

                // Move to Bin
                DropdownMenuItem(
                    content = {
                        Text(
                            text = stringResource(R.string.chat_move_to_bin),
                            color = colorResource(id = R.color.palette_system_red),
                            modifier = Modifier.padding(end = 64.dp)
                        )
                    },
                    onClick = { onMoveToBinClick() }
                )
            }
        }
    }
}

@DefaultPreviews
@Composable
fun ChatMenuPreview() {
    ChatMenu(
        expanded = true,
        currentNotificationSetting = NotificationSetting.ALL,
        onDismissRequest = {},
        onPropertiesClick = {},
        onEditInfoClick = {},
        onNotificationSettingChanged = {},
        onPinClick = {},
        onMoveToBinClick = {}
    )
}
