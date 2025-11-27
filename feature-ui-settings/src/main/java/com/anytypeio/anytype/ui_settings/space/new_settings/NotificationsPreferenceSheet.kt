package com.anytypeio.anytype.ui_settings.space.new_settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.chats.NotificationState
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.spaces.ChatNotificationItem
import com.anytypeio.anytype.presentation.spaces.UiEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsPreferenceSheet(
    targetSpaceId: String?,
    currentState: NotificationState,
    chatsWithCustomNotifications: List<ChatNotificationItem>,
    uiEvent: (UiEvent) -> Unit,
    onDismiss: () -> Unit
) {
    val contentModifier = Modifier
        .windowInsetsPadding(WindowInsets.systemBars)
        .fillMaxSize()
    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = contentModifier,
        containerColor = colorResource(R.color.background_secondary),
        onDismissRequest = onDismiss,
        dragHandle = {},
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Dragger(
                    modifier = Modifier
                        .padding(vertical = 6.dp)
                )
            }
            item {
                Text(
                    text = stringResource(R.string.notifications_title),
                    style = Title1,
                    color = colorResource(id = R.color.text_primary),
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                )
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = stringResource(R.string.notify_me_about),
                        style = Caption1Medium,
                        color = colorResource(id = R.color.text_secondary),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 16.dp, bottom = 12.dp)
                    )
                }
            }
            item {
                NotificationOption(
                    title = stringResource(R.string.notifications_all),
                    checked = currentState == NotificationState.ALL,
                    onClick = { uiEvent(UiEvent.OnNotificationsSetting.All(targetSpaceId)) }
                )
            }
            item {
                Divider(
                    paddingStart = 16.dp,
                    paddingEnd = 16.dp,
                )
            }
            item {
                NotificationOption(
                    title = stringResource(R.string.notifications_mentions),
                    checked = currentState == NotificationState.MENTIONS,
                    onClick = { uiEvent(UiEvent.OnNotificationsSetting.Mentions(targetSpaceId)) }
                )
            }
            item {
                Divider(
                    paddingStart = 16.dp,
                    paddingEnd = 16.dp,
                )
            }
            item {
                NotificationOption(
                    title = stringResource(R.string.notifications_disable),
                    checked = currentState == NotificationState.DISABLE,
                    onClick = { uiEvent(UiEvent.OnNotificationsSetting.None(targetSpaceId)) }
                )
            }

            // Show chats with custom notification settings
            if (chatsWithCustomNotifications.isNotEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.chat_specific_notifications),
                            style = Caption1Medium,
                            color = colorResource(id = R.color.text_secondary),
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 16.dp, bottom = 12.dp)
                        )
                    }
                }
                items(chatsWithCustomNotifications) { chatItem ->
                    ChatNotificationItem(
                        chatItem = chatItem,
                        onResetClick = {
                            uiEvent(UiEvent.OnResetChatNotification(chatItem.id))
                        }
                    )
                    Divider(
                        paddingStart = 16.dp,
                        paddingEnd = 16.dp,
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun NotificationOption(
    title: String,
    checked: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = BodyRegular,
            modifier = Modifier.weight(1f),
            color = colorResource(id = R.color.text_primary)
        )
        if (checked) {
            Image(
                painter = painterResource(id = R.drawable.ic_check_16),
                contentDescription = null,
            )
        }
    }
}

@Composable
fun ChatNotificationItem(
    chatItem: ChatNotificationItem,
    onResetClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .height(72.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ListWidgetObjectIcon(
                icon = chatItem.icon,
                modifier = Modifier.padding(end = 12.dp),
                iconSize = 48.dp,
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = chatItem.name,
                    style = Title2,
                    color = colorResource(id = R.color.text_primary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = when (chatItem.customState) {
                        NotificationState.ALL -> stringResource(R.string.notifications_all_short)
                        NotificationState.MENTIONS -> stringResource(R.string.notifications_mentions_short)
                        NotificationState.DISABLE -> stringResource(R.string.notifications_disable_short)
                    },
                    style = Relations3,
                    color = colorResource(id = R.color.text_secondary)
                )
            }
            Icon(
                painter = painterResource(id = R.drawable.ic_notification_status_clear_24),
                contentDescription = "Reset to space default",
                tint = colorResource(id = R.color.control_secondary),
                modifier = Modifier
                    .size(24.dp)
                    .noRippleClickable {
                        onResetClick()
                    }
            )
        }
    }
}

@DefaultPreviews
@Composable
fun NotificationsPreferenceSheetPreview() {
    NotificationsPreferenceSheet(
        targetSpaceId = "space_view_id",
        currentState = NotificationState.ALL,
        chatsWithCustomNotifications = listOf(
            ChatNotificationItem(
                id = "chat1",
                name = "Team Chat",
                customState = NotificationState.MENTIONS,
                icon = ObjectIcon.TypeIcon.Default.DEFAULT
            ),
            ChatNotificationItem(
                id = "chat2",
                name = "Project Discussion",
                customState = NotificationState.DISABLE,
                icon = ObjectIcon.TypeIcon.Default.DEFAULT
            )
        ),
        uiEvent = {},
        onDismiss = {}
    )
}