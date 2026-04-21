package com.anytypeio.anytype.feature_vault.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.core_models.chats.NotificationState
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.ui.AttachmentPreview
import com.anytypeio.anytype.core_models.ui.SpaceIconView
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.views.BodySemiBold
import com.anytypeio.anytype.core_ui.widgets.SpaceBackground
import com.anytypeio.anytype.core_ui.widgets.objectIcon.SpaceIconView
import com.anytypeio.anytype.domain.notifications.SpaceNotificationMenuShape
import com.anytypeio.anytype.feature_vault.R
import com.anytypeio.anytype.feature_vault.presentation.VaultSpaceView

@Composable
fun VaultOneToOneSpaceCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: SpaceIconView,
    spaceBackground: SpaceBackground,
    messageText: String? = null,
    messageTime: String? = null,
    chatPreview: Chat.Preview? = null,
    unreadMessageCount: Int = 0,
    unreadMentionCount: Int = 0,
    attachmentPreviews: List<AttachmentPreview> = emptyList(),
    isPinned: Boolean = false,
    spaceView: VaultSpaceView.OneToOneSpace,
    expandedSpaceId: String? = null,
    isLastMessageOutgoing: Boolean = false,
    isLastMessageSynced: Boolean = true,
    isCompactMode: Boolean = false,
    onDismissMenu: () -> Unit = {},
    onMuteSpace: (Id) -> Unit = {},
    onUnmuteSpace: (Id) -> Unit = {},
    onSetSpaceNotificationMode: (Id, NotificationState) -> Unit = { _, _ -> },
    onPinSpace: (Id) -> Unit = {},
    onUnpinSpace: (Id) -> Unit = {},
    onSpaceSettings: (Id) -> Unit = {},
    onDeleteOrLeaveSpace: (Id, Boolean) -> Unit = { _, _ -> }
) {
    val hasUnread = unreadMessageCount > 0 || unreadMentionCount > 0
    val iconSize = if (isCompactMode) 44.dp else 64.dp

    val updatedModifier = vaultCardBackgroundModifier(
        modifier, spaceBackground, fixedHeight = !isCompactMode
    )

    Row(
        modifier = updatedModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SpaceIconView(
            icon = icon,
            mainSize = iconSize,
            modifier = Modifier
        )
        val shouldShowAsMuted = spaceView.spaceNotificationState == NotificationState.DISABLE ||
                spaceView.spaceNotificationState == NotificationState.MENTIONS

        if (isCompactMode && !hasUnread) {
            // Compact read state: name + pin
            Text(
                text = title.ifEmpty { stringResource(id = R.string.untitled) },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
                style = BodySemiBold,
                color = colorResource(id = R.color.text_primary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (isPinned) {
                Image(
                    painter = painterResource(R.drawable.ic_pin_18),
                    contentDescription = stringResource(R.string.content_desc_pin),
                    modifier = Modifier.size(18.dp)
                )
            }
        } else {
            // For ONE_TO_ONE spaces, we never show the creator name
            ContentOneToOne(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp),
                title = title.ifEmpty { stringResource(id = R.string.untitled) },
                subtitle = messageText ?: chatPreview?.message?.content?.text.orEmpty(),
                messageText = messageText,
                messageTime = messageTime,
                chatPreview = chatPreview,
                unreadMessageCount = unreadMessageCount,
                unreadMentionCount = unreadMentionCount,
                attachmentPreviews = attachmentPreviews,
                isMuted = spaceView.spaceNotificationState == NotificationState.DISABLE,
                spaceNotificationState = spaceView.spaceNotificationState,
                isPinned = isPinned,
                showPendingIndicator = isLastMessageOutgoing && !isLastMessageSynced,
                isCompactMode = isCompactMode
            )
        }

        // Include dropdown menu inside the card
        SpaceActionsDropdownMenu(
            expanded = expandedSpaceId == spaceView.space.id,
            onDismiss = onDismissMenu,
            menuShape = SpaceNotificationMenuShape.DmToggle,
            currentNotificationMode = spaceView.spaceNotificationState,
            isMuted = shouldShowAsMuted,
            isPinned = spaceView.isPinned,
            isOwner = spaceView.isOwner,
            onMuteToggle = {
                spaceView.space.targetSpaceId?.let {
                    // DmToggle semantic: Mute => DISABLE (fixes pre-existing bug
                    // where onMuteSpace dispatched MENTIONS for DMs, which is
                    // wrong per DROID-4361 spec). Route through
                    // onSetSpaceNotificationMode to get the correct state.
                    val newState = if (shouldShowAsMuted) {
                        NotificationState.ALL
                    } else {
                        NotificationState.DISABLE
                    }
                    onSetSpaceNotificationMode(it, newState)
                }
            },
            onSetSpaceNotificationMode = { mode ->
                spaceView.space.targetSpaceId?.let {
                    onSetSpaceNotificationMode(it, mode)
                }
            },
            onPinToggle = {
                spaceView.space.id.let {
                    if (spaceView.isPinned) onUnpinSpace(it) else onPinSpace(it)
                }
            },
            onSpaceSettings = {
                spaceView.space.id.let { onSpaceSettings(it) }
            },
            onDeleteOrLeaveSpace = {
                spaceView.space.targetSpaceId?.let { onDeleteOrLeaveSpace(it, spaceView.isOwner) }
            }
        )
    }
}

@Composable
private fun ContentOneToOne(
    modifier: Modifier,
    title: String,
    subtitle: String,
    messageText: String? = null,
    messageTime: String? = null,
    chatPreview: Chat.Preview? = null,
    unreadMessageCount: Int = 0,
    unreadMentionCount: Int = 0,
    attachmentPreviews: List<AttachmentPreview> = emptyList(),
    isMuted: Boolean? = null,
    spaceNotificationState: NotificationState? = null,
    isPinned: Boolean = false,
    showPendingIndicator: Boolean = false,
    isCompactMode: Boolean = false
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center
    ) {
        val hasContent = !messageText.isNullOrEmpty() ||
                attachmentPreviews.isNotEmpty() ||
                subtitle.isNotEmpty()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TitleRow(
                modifier = Modifier.weight(1f),
                message = title,
                messageTime = messageTime,
                isMuted = isMuted,
                showPendingIndicator = showPendingIndicator
            )

            // Show pin icon when no content but is pinned
            if (!hasContent && isPinned) {
                Image(
                    painter = painterResource(R.drawable.ic_pin_18),
                    contentDescription = stringResource(R.string.content_desc_pin),
                    modifier = Modifier.size(18.dp),
                    colorFilter = ColorFilter.tint(colorResource(R.color.control_transparent_secondary))
                )
            }
        }

        if (hasContent) {
            OneToOneSubtitleRow(
                subtitle = subtitle,
                messageText = messageText,
                attachmentPreviews = attachmentPreviews,
                chatPreview = chatPreview,
                unreadMessageCount = unreadMessageCount,
                unreadMentionCount = unreadMentionCount,
                notificationMode = spaceNotificationState,
                isPinned = isPinned,
                maxLines = if (isCompactMode) 1 else 2
            )
        }
    }
}

@Composable
private fun OneToOneSubtitleRow(
    subtitle: String,
    messageText: String?,
    attachmentPreviews: List<AttachmentPreview>,
    chatPreview: Chat.Preview?,
    unreadMessageCount: Int,
    unreadMentionCount: Int,
    notificationMode: NotificationState? = null,
    isPinned: Boolean,
    maxLines: Int = 2
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
        // Extract preview-specific counts for text color (not aggregated counts)
        val previewUnreadMessages = chatPreview?.state?.unreadMessages?.counter ?: 0
        val previewUnreadMentions = chatPreview?.state?.unreadMentions?.counter ?: 0

        val textColor = getChatTextColor(
            notificationMode = notificationMode,
            unreadMessageCount = previewUnreadMessages,
            unreadMentionCount = previewUnreadMentions
        )

        // For ONE_TO_ONE, always pass null for creatorName
        val (chatText, inlineContent) = buildChatContentWithInlineIcons(
            creatorName = null,
            messageText = messageText,
            attachmentPreviews = attachmentPreviews,
            fallbackSubtitle = subtitle,
            textColor = textColor
        )

        Text(
            text = chatText,
            inlineContent = inlineContent,
            modifier = Modifier.weight(1f),
            maxLines = maxLines,
            lineHeight = 20.sp,
            overflow = TextOverflow.Ellipsis,
            color = textColor,
        )

        // Never show mentions for ONE_TO_ONE spaces
        UnreadIndicatorsRow(
            unreadMessageCount = unreadMessageCount,
            unreadMentionCount = 0,
            notificationMode = notificationMode,
            isPinned = isPinned
        )
    }
}

@Composable
@DefaultPreviews
fun OneToOneSpaceCardPreview() {
    Column {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
        )
        VaultOneToOneSpaceCard(
            modifier = Modifier.fillMaxWidth(),
            title = "Alice Smith",
            icon = SpaceIconView.ChatSpace.Placeholder(),
            isPinned = false,
            messageText = "Hey, how are you doing?",
            messageTime = "14:25",
            unreadMessageCount = 2,
            unreadMentionCount = 0,
            isLastMessageSynced = false,
            isLastMessageOutgoing = true,
            chatPreview = Chat.Preview(
                space = SpaceId("space-id"),
                chat = "chat-id",
                message = Chat.Message(
                    id = "message-id",
                    createdAt = System.currentTimeMillis(),
                    modifiedAt = 0L,
                    attachments = emptyList(),
                    reactions = emptyMap(),
                    creator = "creator-id",
                    replyToMessageId = "",
                    content = Chat.Message.Content(
                        text = "Hey, how are you doing?",
                        marks = emptyList(),
                        style = Block.Content.Text.Style.P
                    ),
                    order = "order-id",
                    synced = false
                )
            ),
            spaceBackground = SpaceBackground.SolidColor(
                color = androidx.compose.ui.graphics.Color(0xFFE8F5E9)
            ),
            spaceView = VaultSpaceView.OneToOneSpace(
                space = ObjectWrapper.SpaceView(
                    map = mapOf(
                        "name" to "Alice Smith",
                        "id" to "spaceId1"
                    )
                ),
                icon = SpaceIconView.ChatSpace.Placeholder(),
                isOwner = true,
                spaceNotificationState = NotificationState.ALL
            )
        )
    }
}
