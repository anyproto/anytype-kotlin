package com.anytypeio.anytype.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.core_models.chats.NotificationState
import com.anytypeio.anytype.core_ui.views.BodySemiBold
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.views.Relations2
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.core_ui.views.Title3
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.data.auth.repo.config.GatewayProvider
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.getTypeOfObject
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.other.BasicLogger
import com.anytypeio.anytype.presentation.mapper.objectIcon
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.vault.VaultSpaceView
import com.anytypeio.anytype.ui.vault.UnreadIndicatorsRow
import com.anytypeio.anytype.ui.vault.buildChatContentWithInlineIcons
import com.anytypeio.anytype.ui.vault.getChatTextColor

/**
 * Compact chat widget card for home screen and widgets.
 *
 * Features:
 * - 72dp height (compact vs 96dp vault cards)
 * - 40dp icon size (compact vs 64dp vault cards)
 * - Chat object icon (not space icon)
 * - Unread counts from chat preview state (not space-aggregated)
 * - Single-line message preview with creator name
 * - Unread badges (message count + mention icon)
 *
 */
@Composable
fun ChatWidgetCard(
    modifier: Modifier = Modifier,
    chatIcon: ObjectIcon,
    chatName: String,
    creatorName: String? = null,
    messageText: String? = null,
    messageTime: String? = null,
    attachmentPreviews: List<VaultSpaceView.AttachmentPreview> = emptyList(),
    unreadMessageCount: Int,
    unreadMentionCount: Int,
    chatNotificationState: NotificationState,
    onClick: () -> Unit
) {

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(
                color = colorResource(id = R.color.background_secondary),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Chat icon (40dp)
        ListWidgetObjectIcon(
            icon = chatIcon,
            modifier = Modifier
                .padding(start = 16.dp)
                .size(48.dp),
            iconSize = 48.dp
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Chat content
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Line 1: Chat Name + Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Determine text color for chat name
                val chatNameColor = getChatTextColor(
                    notificationMode = chatNotificationState,
                    unreadMessageCount = unreadMessageCount,
                    unreadMentionCount = unreadMentionCount
                )

                Text(
                    text = chatName,
                    style = PreviewTitle2Medium,
                    color = chatNameColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (messageTime != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = messageTime,
                        style = Relations3,
                        color = colorResource(id = R.color.text_secondary)
                    )
                }
            }

            // Line 2: Message Preview + Indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Determine text color for message preview
                val textColor = getChatTextColor(
                    notificationMode = chatNotificationState,
                    unreadMessageCount = unreadMessageCount,
                    unreadMentionCount = unreadMentionCount
                )

                val (chatText, inlineContent) = buildChatContentWithInlineIcons(
                    creatorName = creatorName,
                    messageText = messageText,
                    attachmentPreviews = attachmentPreviews,
                    fallbackSubtitle = "",  // Empty fallback for now
                    singleLineFormat = true,
                    textColor = textColor
                )

                Text(
                    text = chatText,
                    inlineContent = inlineContent,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    lineHeight = 18.sp,
                    overflow = TextOverflow.Ellipsis,
                    color = textColor
                )

                // Unread indicators (mention icon + message count badge)
                if (unreadMessageCount > 0 || unreadMentionCount > 0) {
                    Spacer(modifier = Modifier.width(6.dp))
                    UnreadIndicatorsRow(
                        unreadMessageCount = unreadMessageCount,
                        unreadMentionCount = unreadMentionCount,
                        notificationMode = chatNotificationState,
                        isPinned = false
                    )
                }
            }
        }
    }
}

