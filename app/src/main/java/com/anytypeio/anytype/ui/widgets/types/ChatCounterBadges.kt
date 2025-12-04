package com.anytypeio.anytype.ui.widgets.types

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.chats.NotificationState
import com.anytypeio.anytype.core_ui.views.Caption2Regular
import com.anytypeio.anytype.presentation.widgets.WidgetView

/**
 * Reusable chat counter badges component for widgets.
 * Displays mention icon and/or message count badge with notification-aware background colors.
 *
 * Color logic based on NotificationState:
 * - ALL: Both badges use accent color (user wants all notifications)
 * - MENTIONS: Mention badge uses accent, message count uses muted (user only wants mentions)
 * - DISABLE: Both badges use muted color (user has muted this chat)
 *
 * @param counter The chat counter data containing unread mention and message counts
 * @param notificationState The notification state determining badge background colors
 * @param modifier Modifier for the container Row
 * @param badgeSize Size of each badge (default 20.dp)
 * @param spacing Space between mention badge and message count badge (default 8.dp)
 */
@Composable
fun ChatCounterBadges(
    counter: WidgetView.ChatCounter?,
    notificationState: NotificationState?,
    modifier: Modifier = Modifier,
    badgeSize: Dp = 18.dp,
    spacing: Dp = 4.dp
) {
    if (counter == null) return
    if (counter.unreadMentionCount == 0 && counter.unreadMessageCount == 0) return

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mention badge (@ icon)
        if (counter.unreadMentionCount > 0) {
            MentionBadge(
                notificationState = notificationState,
                size = badgeSize
            )
            if (counter.unreadMessageCount > 0) {
                Spacer(modifier = Modifier.width(spacing))
            }
        }

        // Message count badge (number)
        if (counter.unreadMessageCount > 0) {
            MessageCountBadge(
                count = counter.unreadMessageCount,
                notificationState = notificationState,
                size = badgeSize
            )
        }
    }
}

@Composable
private fun MentionBadge(
    notificationState: NotificationState?,
    size: Dp
) {
    Box(
        modifier = Modifier
            .background(
                color = getMentionBadgeColor(notificationState),
                shape = CircleShape
            )
            .size(size),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.ic_chat_widget_mention),
            contentDescription = "Mention"
        )
    }
}

@Composable
private fun MessageCountBadge(
    count: Int,
    notificationState: NotificationState?,
    size: Dp
) {
    Box(
        modifier = Modifier
            .height(size)
            .defaultMinSize(minWidth = size)
            .background(
                color = getMessageCountBadgeColor(notificationState),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 5.dp),
            text = count.toString(),
            style = Caption2Regular,
            color = colorResource(id = R.color.text_white),
        )
    }
}

/**
 * Determines badge color for mention indicator.
 * Mentions always use accent color unless notifications are disabled.
 */
@Composable
private fun getMentionBadgeColor(notificationState: NotificationState?): Color {
    return when (notificationState) {
        NotificationState.ALL -> colorResource(id = R.color.control_accent)
        NotificationState.MENTIONS -> colorResource(id = R.color.control_accent)
        NotificationState.DISABLE -> colorResource(id = R.color.control_transparent_tetriary)
        else -> colorResource(id = R.color.control_accent)
    }
}

/**
 * Determines badge color for message count indicator.
 * Uses accent for ALL mode, muted for MENTIONS (since user only cares about mentions) and DISABLE.
 */
@Composable
private fun getMessageCountBadgeColor(notificationState: NotificationState?): Color {
    return when (notificationState) {
        NotificationState.ALL -> colorResource(id = R.color.control_accent)
        NotificationState.MENTIONS -> colorResource(id = R.color.control_transparent_tetriary)
        NotificationState.DISABLE -> colorResource(id = R.color.control_transparent_tetriary)
        else -> colorResource(id = R.color.control_accent)
    }
}

// --- Previews for the main ChatCounterBadges component ---
@Preview(name = "Mentions and Messages (All Notifications)", showBackground = true)
@Composable
private fun ChatCounterBadges_MentionsAndMessages_All_Preview() {
    MaterialTheme {
        ChatCounterBadges(
            counter = WidgetView.ChatCounter(unreadMentionCount = 1, unreadMessageCount = 5),
            notificationState = NotificationState.ALL
        )
    }
}

@Preview(name = "Mentions and Messages (Mentions Only)", showBackground = true)
@Composable
private fun ChatCounterBadges_MentionsAndMessages_Mentions_Preview() {
    MaterialTheme {
        ChatCounterBadges(
            counter = WidgetView.ChatCounter(unreadMentionCount = 1, unreadMessageCount = 5),
            notificationState = NotificationState.MENTIONS
        )
    }
}

@Preview(name = "Only Mentions", showBackground = true)
@Composable
private fun ChatCounterBadges_OnlyMentions_Preview() {
    MaterialTheme {
        ChatCounterBadges(
            counter = WidgetView.ChatCounter(unreadMentionCount = 1, unreadMessageCount = 0),
            notificationState = NotificationState.ALL
        )
    }
}

@Preview(name = "Only Messages (99+)", showBackground = true)
@Composable
private fun ChatCounterBadges_OnlyMessages_Preview() {
    MaterialTheme {
        ChatCounterBadges(
            counter = WidgetView.ChatCounter(unreadMentionCount = 0, unreadMessageCount = 99),
            notificationState = NotificationState.ALL
        )
    }
}

@Preview(name = "Disabled Notifications", showBackground = true)
@Composable
private fun ChatCounterBadges_Disabled_Preview() {
    MaterialTheme {
        ChatCounterBadges(
            counter = WidgetView.ChatCounter(unreadMentionCount = 2, unreadMessageCount = 10),
            notificationState = NotificationState.DISABLE
        )
    }
}

// --- Previews for individual badge components ---
@Preview(name = "Mention Badge (Active)", showBackground = true)
@Composable
private fun MentionBadge_Active_Preview() {
    MaterialTheme {
        MentionBadge(
            notificationState = NotificationState.ALL,
            size = 20.dp
        )
    }
}

@Preview(name = "Mention Badge (Muted)", showBackground = true)
@Composable
private fun MentionBadge_Muted_Preview() {
    MaterialTheme {
        MentionBadge(
            notificationState = NotificationState.DISABLE,
            size = 20.dp
        )
    }
}

@Preview(name = "Message Count Badge (Active)", showBackground = true)
@Composable
private fun MessageCountBadge_Active_Preview() {
    MaterialTheme {
        MessageCountBadge(
            count = 5,
            notificationState = NotificationState.ALL,
            size = 20.dp
        )
    }
}

@Preview(name = "Message Count Badge (Muted)", showBackground = true)
@Composable
private fun MessageCountBadge_Muted_Preview() {
    MaterialTheme {
        MessageCountBadge(
            count = 12,
            notificationState = NotificationState.MENTIONS,
            size = 20.dp
        )
    }
}