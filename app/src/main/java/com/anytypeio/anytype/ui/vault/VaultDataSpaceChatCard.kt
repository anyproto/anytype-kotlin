package com.anytypeio.anytype.ui.vault

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
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
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.core_models.chats.NotificationState
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.views.CodeChatPreviewMedium
import com.anytypeio.anytype.core_ui.views.CodeChatPreviewRegular
import com.anytypeio.anytype.core_ui.widgets.SpaceBackground
import com.anytypeio.anytype.core_ui.widgets.objectIcon.SpaceIconView
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.presentation.vault.VaultSpaceView

@Composable
fun VaultDataSpaceChatCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: SpaceIconView,
    spaceBackground: SpaceBackground,
    chatName: String,
    creatorName: String? = null,
    messageText: String? = null,
    messageTime: String? = null,
    unreadMessageCount: Int = 0,
    unreadMentionCount: Int = 0,
    attachmentPreviews: List<VaultSpaceView.AttachmentPreview> = emptyList(),
    spaceNotificationState: NotificationState,
    isPinned: Boolean = false,
    spaceView: VaultSpaceView.DataSpaceWithChat,
    expandedSpaceId: String? = null,
    isLastMessageOutgoing: Boolean = false,
    isLastMessageSynced: Boolean = true,
    onDismissMenu: () -> Unit = {},
    onMuteSpace: (Id) -> Unit = {},
    onUnmuteSpace: (Id) -> Unit = {},
    onPinSpace: (Id) -> Unit = {},
    onUnpinSpace: (Id) -> Unit = {},
    onSpaceSettings: (Id) -> Unit = {},
    onDeleteOrLeaveSpace: (Id, Boolean) -> Unit = { _, _ -> }
) {

    val updatedModifier = when (spaceBackground) {
        is SpaceBackground.SolidColor -> modifier
            .fillMaxSize()
            .height(96.dp)
            .padding(horizontal = 16.dp)
            .background(
                color = spaceBackground.color.copy(alpha = 0.3f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 16.dp)

        is SpaceBackground.Gradient -> modifier
            .fillMaxSize()
            .height(96.dp)
            .padding(horizontal = 16.dp)
            .background(
                brush = spaceBackground.brush,
                shape = RoundedCornerShape(20.dp),
                alpha = 0.3f
            )
            .padding(horizontal = 16.dp)

        SpaceBackground.None -> Modifier
            .fillMaxSize()
            .height(96.dp)
            .padding(horizontal = 16.dp)
            .background(
                color = colorResource(id = R.color.background_secondary),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 16.dp)
    }

    Row(
        modifier = updatedModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SpaceIconView(
            icon = icon,
            mainSize = 64.dp,
            modifier = Modifier
        )
        val isSpaceMuted = spaceNotificationState == NotificationState.DISABLE

        ContentDataSpaceChat(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp),
            title = title,
            chatName = chatName,
            creatorName = creatorName,
            messageText = messageText,
            messageTime = messageTime,
            chatPreview = spaceView.chatPreview,
            unreadMessageCount = unreadMessageCount,
            unreadMentionCount = unreadMentionCount,
            attachmentPreviews = attachmentPreviews,
            spaceNotificationState = spaceNotificationState,
            isMuted = isSpaceMuted,
            isPinned = isPinned,
            showPendingIndicator = isLastMessageOutgoing && !isLastMessageSynced
        )

        val shouldShowAsMuted =
            spaceNotificationState == NotificationState.MENTIONS || isSpaceMuted

        // Include dropdown menu inside the card
        SpaceActionsDropdownMenu(
            expanded = expandedSpaceId == spaceView.space.id,
            onDismiss = onDismissMenu,
            isMuted = shouldShowAsMuted,
            isPinned = spaceView.isPinned,
            isOwner = spaceView.isOwner,
            onMuteToggle = {
                spaceView.space.targetSpaceId?.let {
                    if (shouldShowAsMuted) {
                        onUnmuteSpace(it)
                    } else {
                        onMuteSpace(it)
                    }
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
private fun ContentDataSpaceChat(
    modifier: Modifier,
    title: String,
    chatName: String,
    creatorName: String? = null,
    messageText: String? = null,
    messageTime: String? = null,
    chatPreview: Chat.Preview? = null,
    unreadMessageCount: Int = 0,
    unreadMentionCount: Int = 0,
    attachmentPreviews: List<VaultSpaceView.AttachmentPreview> = emptyList(),
    isMuted: Boolean? = null,
    spaceNotificationState: NotificationState,
    isPinned: Boolean = false,
    showPendingIndicator: Boolean = false
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.Center) {
        val hasContent = !creatorName.isNullOrEmpty() ||
                        !messageText.isNullOrEmpty() ||
                        attachmentPreviews.isNotEmpty()

        // Line 1: Title + Time + Muted Icon
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
        }

        // Extract preview-specific counts for text color (not aggregated counts)
        val previewUnreadMessages = chatPreview?.state?.unreadMessages?.counter ?: 0
        val previewUnreadMentions = chatPreview?.state?.unreadMentions?.counter ?: 0

        // Line 2: Chat Name + Indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = chatName,
                style = CodeChatPreviewMedium,
                color = getChatTextColor(
                    notificationMode = spaceNotificationState,
                    unreadMessageCount = previewUnreadMessages,
                    unreadMentionCount = previewUnreadMentions
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            // Show indicators if there's content, or pin if pinned but no content
            if (hasContent) {
                UnreadIndicatorsRow(
                    unreadMessageCount = unreadMessageCount,
                    unreadMentionCount = unreadMentionCount,
                    notificationMode = spaceNotificationState,
                    isPinned = isPinned
                )
            } else if (isPinned) {
                // Show just pin icon when no content
                Image(
                    painter = painterResource(R.drawable.ic_pin_18),
                    contentDescription = stringResource(R.string.content_desc_pin),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(18.dp),
                    colorFilter = ColorFilter.tint(colorResource(R.color.control_transparent_secondary))
                )
            }
        }

        // Line 3: Author + Message Preview
        if (hasContent) {
            DataSpaceChatPreviewRow(
                creatorName = creatorName,
                messageText = messageText,
                attachmentPreviews = attachmentPreviews,
                chatPreview = chatPreview,
                chatPreviewNotificationState = spaceNotificationState
            )
        }
    }
}

@Composable
private fun DataSpaceChatPreviewRow(
    creatorName: String?,
    messageText: String?,
    attachmentPreviews: List<VaultSpaceView.AttachmentPreview>,
    chatPreview: Chat.Preview?,
    chatPreviewNotificationState: NotificationState?
) {
    // Extract preview-specific counts for text color (not aggregated counts)
    val previewUnreadMessages = chatPreview?.state?.unreadMessages?.counter ?: 0
    val previewUnreadMentions = chatPreview?.state?.unreadMentions?.counter ?: 0

    val textColor = getChatTextColor(
        notificationMode = chatPreviewNotificationState,
        unreadMessageCount = previewUnreadMessages,
        unreadMentionCount = previewUnreadMentions
    )
    val (chatText, inlineContent) = buildChatContentWithInlineIcons(
        creatorName = creatorName,
        messageText = messageText,
        attachmentPreviews = attachmentPreviews,
        fallbackSubtitle = "",
        singleLineFormat = true,
        textColor = textColor,
        mediumStyle = CodeChatPreviewRegular.toSpanStyle().copy(color = textColor),
        regularStyle = CodeChatPreviewRegular.toSpanStyle().copy(color = textColor)
    )

    Text(
        text = chatText,
        inlineContent = inlineContent,
        modifier = Modifier.fillMaxWidth(),
        maxLines = 1,
        lineHeight = 20.sp,
        overflow = TextOverflow.Ellipsis,
        color = textColor,
    )
}

// Preview Composables

@Composable
@DefaultPreviews
fun DataSpaceChatWithMessage() {
    Column {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
        )
        VaultDataSpaceChatCard(
            modifier = Modifier.fillMaxWidth(),
            title = "Dream Team Space",
            chatName = "#general-chat",
            icon = SpaceIconView.DataSpace.Placeholder(),
            creatorName = "Alice",
            messageText = "Don't forget grandma's birthday is next Thursday!",
            messageTime = "17:01",
            unreadMessageCount = 2,
            unreadMentionCount = 1,
            spaceNotificationState = NotificationState.ALL,
            isPinned = false,
            spaceBackground = SpaceBackground.SolidColor(color = androidx.compose.ui.graphics.Color(0xFFE0F7FA)),
            spaceView = VaultSpaceView.DataSpaceWithChat(
                space = com.anytypeio.anytype.core_models.ObjectWrapper.SpaceView(
                    map = mapOf("name" to "Dream Team Space", "id" to "spaceId1")
                ),
                icon = SpaceIconView.DataSpace.Placeholder(),
                isOwner = true,
                chatNotificationState = NotificationState.ALL,
                chatName = "@feature-chat",
                spaceNotificationState = NotificationState.ALL
            )
        )
    }
}

@Composable
@DefaultPreviews
fun DataSpaceChatMuted() {
    Column {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
        )
        VaultDataSpaceChatCard(
            modifier = Modifier.fillMaxWidth(),
            title = "Project Alpha",
            chatName = "#announcements",
            icon = SpaceIconView.DataSpace.Placeholder(),
            creatorName = "Bob",
            messageText = "Meeting scheduled for tomorrow at 10 AM",
            messageTime = "09:15",
            unreadMessageCount = 5,
            unreadMentionCount = 0,
            isLastMessageSynced = false,
            isLastMessageOutgoing = true,
            spaceNotificationState = NotificationState.DISABLE,
            isPinned = true,
            spaceBackground = SpaceBackground.SolidColor(color = androidx.compose.ui.graphics.Color(0xFFFFF3E0)),
            spaceView = VaultSpaceView.DataSpaceWithChat(
                space = com.anytypeio.anytype.core_models.ObjectWrapper.SpaceView(
                    map = mapOf("name" to "Project Alpha", "id" to "spaceId2")
                ),
                icon = SpaceIconView.DataSpace.Placeholder(),
                isOwner = false,
                chatName = "@feature-chat",
                chatNotificationState = NotificationState.DISABLE,
                spaceNotificationState = NotificationState.DISABLE
            )
        )
    }
}

@Composable
@DefaultPreviews
fun DataSpaceChatNoMessage() {
    Column {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
        )
        VaultDataSpaceChatCard(
            modifier = Modifier.fillMaxWidth(),
            title = "Empty Chat Space",
            chatName = "#random",
            icon = SpaceIconView.DataSpace.Placeholder(),
            creatorName = null,
            messageText = null,
            messageTime = null,
            unreadMessageCount = 0,
            unreadMentionCount = 0,
            spaceNotificationState = NotificationState.ALL,
            isPinned = true,
            spaceBackground = SpaceBackground.SolidColor(color = androidx.compose.ui.graphics.Color(0xFFE8F5E9)),
            spaceView = VaultSpaceView.DataSpaceWithChat(
                space = com.anytypeio.anytype.core_models.ObjectWrapper.SpaceView(
                    map = mapOf("name" to "Empty Chat Space", "id" to "spaceId3")
                ),
                icon = SpaceIconView.DataSpace.Placeholder(),
                isOwner = true,
                chatName = "@feature-chat",
                chatNotificationState = NotificationState.DISABLE,
                spaceNotificationState = NotificationState.ALL
            )
        )
    }
}
