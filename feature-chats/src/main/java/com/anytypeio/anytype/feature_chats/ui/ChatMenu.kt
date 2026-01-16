package com.anytypeio.anytype.feature_chats.ui

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.feature_chats.R

enum class NotificationSetting {
    ALL, MENTIONS, MUTE
}

@Composable
fun BoxScope.ChatMenu(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    currentNotificationSetting: NotificationSetting,
    isPinned: Boolean = false,
    canEdit: Boolean = true,
    onDismissRequest: () -> Unit,
    onPropertiesClick: () -> Unit,
    onEditInfoClick: () -> Unit,
    onNotificationSettingChanged: (NotificationSetting) -> Unit,
    onPinClick: () -> Unit,
    onMoveToBinClick: () -> Unit
) {
    var notificationsExpanded by remember { mutableStateOf(false) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (notificationsExpanded) 90f else 0f,
        label = "chevron rotation"
    )
    MaterialTheme(
        shapes = MaterialTheme.shapes.copy(
            medium = RoundedCornerShape(12.dp)
        ),
        colors = MaterialTheme.colors.copy(
            background = colorResource(id = R.color.background_secondary)
        )
    ) {
        DropdownMenu(
            modifier = Modifier
                .defaultMinSize(minWidth = 252.dp)
                .background(
                    shape = RoundedCornerShape(12.dp),
                    color = colorResource(id = R.color.background_secondary)
                )
                .align(Alignment.TopEnd),
            offset = DpOffset((-16).dp, 8.dp),
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            properties = PopupProperties(focusable = false)
        ) {
//            // Properties
//            DropdownMenuItem(
//                content = {
//                    ChatMenuItemContent(
//                        text = stringResource(R.string.properties),
//                        iconRes = R.drawable.ic_chat_menu_properties_24
//                    )
//                },
//                onClick = { onPropertiesClick() }
//            )
//            Divider(paddingStart = 0.dp, paddingEnd = 0.dp)

            // Edit Info - only show if user can edit
            if (canEdit) {
                DropdownMenuItem(
                    content = {
                        ChatMenuItemContent(
                            text = stringResource(R.string.chat_edit_info),
                            iconRes = R.drawable.ic_edit_info_24
                        )
                    },
                    onClick = { onEditInfoClick() }
                )
                Divider(paddingStart = 0.dp, paddingEnd = 0.dp, height = 8.dp)

                // Pin/Unpin - only show if user can edit
                DropdownMenuItem(
                    content = {
                        ChatMenuItemContent(
                            text = stringResource(
                                if (isPinned) R.string.object_action_unpin
                                else R.string.object_action_pin
                            ),
                            iconRes = R.drawable.ic_pin_24
                        )
                    },
                    onClick = { onPinClick() }
                )
                Divider(paddingStart = 0.dp, paddingEnd = 0.dp)
            }

            // Notifications - expandable
            DropdownMenuItem(
                content = {
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
                        text = stringResource(R.string.notifications_title),
                        color = colorResource(id = R.color.text_primary),
                        modifier = Modifier.weight(1f),
                        style = BodyRegular
                    )
                    Image(
                        painter = painterResource(id = R.drawable.ic_notifications),
                        contentDescription = "Notification icon",
                        colorFilter = ColorFilter.tint(colorResource(id = R.color.text_primary)),
                        modifier = Modifier.size(24.dp)
                    )
                },
                onClick = { notificationsExpanded = !notificationsExpanded }
            )

            // Notification sub-items
            if (notificationsExpanded) {
                DropdownMenuItem(
                    content = {
                        NotificationOptionItem(
                            text = stringResource(R.string.chat_notifications_receive_all),
                            isSelected = currentNotificationSetting == NotificationSetting.ALL
                        )
                    },
                    onClick = {
                        onNotificationSettingChanged(NotificationSetting.ALL)
                    }
                )

                DropdownMenuItem(
                    content = {
                        NotificationOptionItem(
                            text = stringResource(R.string.notifications_mentions),
                            isSelected = currentNotificationSetting == NotificationSetting.MENTIONS
                        )
                    },
                    onClick = {
                        onNotificationSettingChanged(NotificationSetting.MENTIONS)
                    }
                )

                DropdownMenuItem(
                    content = {
                        NotificationOptionItem(
                            text = stringResource(R.string.chat_notifications_mute_all),
                            isSelected = currentNotificationSetting == NotificationSetting.MUTE
                        )
                    },
                    onClick = {
                        onNotificationSettingChanged(NotificationSetting.MUTE)
                    }
                )
            }

            Divider(paddingStart = 0.dp, paddingEnd = 0.dp)

            // Move to Bin - only show if user can edit
            if (canEdit) {
                DropdownMenuItem(
                    content = {
                        ChatMenuItemContent(
                            text = stringResource(R.string.chat_move_to_bin),
                            iconRes = R.drawable.ic_chat_menu_to_bin_24,
                            textColor = R.color.palette_system_red
                        )
                    },
                    onClick = { onMoveToBinClick() }
                )
            }
        }
    }
}

@Composable
private fun NotificationOptionItem(
    text: String,
    isSelected: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
    ) {
        if (isSelected) {
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
            text = text,
            color = colorResource(id = R.color.text_primary),
            style = BodyRegular
        )
    }
}

@Composable
private fun RowScope.ChatMenuItemContent(
    text: String,
    textColor: Int = R.color.text_primary,
    @DrawableRes iconRes: Int
) {
    Text(
        modifier = Modifier.weight(1f),
        text = text,
        color = colorResource(id = textColor),
        style = BodyRegular
    )
    Image(
        painter = painterResource(id = iconRes),
        contentDescription = "menu item icon",
        modifier = Modifier.size(22.dp),
        colorFilter = ColorFilter.tint(colorResource(id = textColor))
    )
}

@DefaultPreviews
@Composable
fun ChatMenuPreview() {
    Box(
        modifier = Modifier
            .padding(top = 232.dp)
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        ChatMenu(
            expanded = true,
            currentNotificationSetting = NotificationSetting.ALL,
            isPinned = false,
            canEdit = true,
            onDismissRequest = {},
            onPropertiesClick = {},
            onEditInfoClick = {},
            onNotificationSettingChanged = {},
            onPinClick = {},
            onMoveToBinClick = {}
        )
    }
}
