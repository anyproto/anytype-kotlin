package com.anytypeio.anytype.ui.vault

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_ui.features.SpaceIconView
import com.anytypeio.anytype.core_ui.views.BodySemiBold
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.presentation.spaces.SpaceIconView

@Composable
fun VaultSpaceCard(
    title: String,
    subtitle: String,
    onCardClicked: () -> Unit,
    icon: SpaceIconView,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp)
            .clickable {
                onCardClicked()
            }
    ) {
        SpaceIconView(
            icon = icon,
            onSpaceIconClick = {
                onCardClicked()
            },
            mainSize = 56.dp,
            modifier = Modifier
                .align(Alignment.CenterStart)
        )
        ContentSpace(
            title = title,
            subtitle = subtitle
        )
    }
}

@Composable
private fun BoxScope.ContentSpace(
    title: String,
    subtitle: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 68.dp, top = 9.dp)
    ) {
        Text(
            text = title.ifEmpty { stringResource(id = R.string.untitled) },
            style = BodySemiBold,
            color = colorResource(id = R.color.text_primary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = subtitle,
            style = Title2,
            color = colorResource(id = R.color.text_secondary),
            modifier = Modifier
        )
    }
}

@Composable
fun VaultChatCard(
    title: String,
    onCardClicked: () -> Unit,
    icon: SpaceIconView,
    previewText: String? = null,
    chatPreview: Chat.Preview? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp)
            .clickable {
                onCardClicked()
            }
    ) {
        SpaceIconView(
            icon = icon,
            onSpaceIconClick = {
                onCardClicked()
            },
            mainSize = 56.dp,
            modifier = Modifier
                .align(Alignment.CenterStart)
        )
        ContentChat(
            title = title,
            subtitle = previewText ?: chatPreview?.message?.content?.text.orEmpty()
        )
    }
}

@Composable
private fun BoxScope.ContentChat(
    title: String,
    subtitle: String,
    unreadMessageCount: Int = 0,
    unreadMentionCount: Int = 0,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(start = 68.dp, end = 56.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title.ifEmpty { stringResource(id = R.string.untitled) },
            style = BodySemiBold,
            color = colorResource(id = R.color.text_primary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = subtitle,
            style = Title2,
            color = colorResource(id = R.color.text_secondary),
            modifier = Modifier,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
    Row(
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .padding(end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (unreadMentionCount > 0) {
            Box(
                modifier = Modifier
                    .background(
                        color = colorResource(R.color.glyph_active),
                        shape = CircleShape
                    )
                    .size(18.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_chat_widget_mention),
                    contentDescription = null
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        if (unreadMessageCount > 0) {
            val shape = if (unreadMentionCount > 9) {
                CircleShape
            } else {
                RoundedCornerShape(100.dp)
            }
            Box(
                modifier = Modifier
                    .height(18.dp)
                    .background(
                        color = colorResource(R.color.glyph_active),
                        shape = shape
                    )
                    .padding(horizontal = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = unreadMessageCount.toString(),
                    style = Caption1Regular,
                    color = colorResource(id = R.color.text_white),
                )
            }
        }
    }
}

//@Composable
////@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Light Mode")
//@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Dark Mode")
//fun VaultSpaceCardPreview() {
//    VaultSpaceCard(
//        title = "B&O Museum",
//        subtitle = "Private space",
//        onCardClicked = {},
//        wallpaper = Wallpaper.Default,
//        icon = SpaceIconView.Placeholder()
//    )
//}

@Composable
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "Chat Space Card Preview"
)
fun VaultChatSpaceCardPreview() {
    VaultChatCard(
        title = "B&O Museum",
        onCardClicked = {},
        icon = SpaceIconView.Placeholder(),
        previewText = "John Doe: Hello, this is a preview message that might be long enough to show how it looks with multiple lines.",
        chatPreview = Chat.Preview(
            space = SpaceId("space-id"),
            chat = "chat-id",
            message = Chat.Message(
                id = "message-id",
                createdAt = 0L,
                modifiedAt = 0L,
                attachments = emptyList(),
                reactions = emptyMap(),
                creator = "creator-id",
                replyToMessageId = "",
                content = Chat.Message.Content(
                    text = "Hello, this is a preview message.",
                    marks = emptyList(),
                    style = Block.Content.Text.Style.P
                ),
                order = "order-id"
            )
        )
    )
}