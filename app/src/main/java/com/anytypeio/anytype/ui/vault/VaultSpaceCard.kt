package com.anytypeio.anytype.ui.vault

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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.extensions.getMimeIcon
import com.anytypeio.anytype.core_ui.features.SpaceIconView
import com.anytypeio.anytype.core_ui.views.BodySemiBold
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.Relations2
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.core_ui.views.chatPreviewTextStyle
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.presentation.vault.VaultSpaceView

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
    creatorName: String? = null,
    messageText: String? = null,
    messageTime: String? = null,
    chatPreview: Chat.Preview? = null,
    unreadMessageCount: Int = 0,
    unreadMentionCount: Int = 0,
    attachmentPreviews: List<VaultSpaceView.AttachmentPreview> = emptyList(),
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
            subtitle = previewText ?: chatPreview?.message?.content?.text.orEmpty(),
            creatorName = creatorName,
            messageText = messageText,
            messageTime = messageTime,
            unreadMessageCount = unreadMessageCount,
            unreadMentionCount = unreadMentionCount,
            chatPreview = chatPreview,
            attachmentPreviews = attachmentPreviews
        )
    }
}

@Composable
private fun BoxScope.ContentChat(
    title: String,
    subtitle: String,
    creatorName: String? = null,
    messageText: String? = null,
    messageTime: String? = null,
    unreadMessageCount: Int = 0,
    unreadMentionCount: Int = 0,
    chatPreview: Chat.Preview? = null,
    attachmentPreviews: List<VaultSpaceView.AttachmentPreview> = emptyList(),
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(start = 68.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterStart),
                text = title.ifEmpty { stringResource(id = R.string.untitled) },
                style = BodySemiBold,
                color = colorResource(id = R.color.text_primary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (messageTime != null) {
                Text(
                    text = messageTime,
                    style = Relations2,
                    color = colorResource(id = R.color.transparent_active),
                    modifier = Modifier
                        .align(Alignment.CenterEnd),
                    textAlign = TextAlign.Center,
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Show attachment previews if available
            if (attachmentPreviews.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(end = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    attachmentPreviews.forEach { preview ->
                        when (preview.type) {
                            VaultSpaceView.AttachmentType.IMAGE -> {
                                if (!preview.imageUrl.isNullOrEmpty()) {
                                    Image(
                                        painter = rememberAsyncImagePainter(preview.imageUrl),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(RoundedCornerShape(2.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(R.drawable.ic_mime_image),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            VaultSpaceView.AttachmentType.FILE, VaultSpaceView.AttachmentType.LINK -> {
                                val iconResource = preview.mimeType.getMimeIcon(preview.fileExtension)
                                Image(
                                    painter = painterResource(iconResource),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            if (creatorName != null && messageText != null) {
                val annotatedString = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(creatorName)
                    }
                    append(": ")
                    append(messageText)
                }

                Text(
                    modifier = Modifier.weight(1f),
                    text = annotatedString,
                    style = chatPreviewTextStyle,
                    color = colorResource(id = R.color.text_secondary),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                // Show attachment text if no message text
                val attachmentText = if (attachmentPreviews.isNotEmpty()) {
                    val imageCount = attachmentPreviews.count { it.type == VaultSpaceView.AttachmentType.IMAGE }
                    val fileCount = attachmentPreviews.count { it.type == VaultSpaceView.AttachmentType.FILE || it.type == VaultSpaceView.AttachmentType.LINK }
                    
                    when {
                        imageCount > 0 && fileCount == 0 -> {
                            if (imageCount == 1) "1 Image" else "$imageCount Images"
                        }
                        imageCount == 0 && fileCount > 0 -> {
                            if (fileCount == 1) "1 Attachment" else "$fileCount Attachments"
                        }
                        imageCount > 0 && fileCount > 0 -> {
                            val totalCount = imageCount + fileCount
                            if (totalCount == 1) "1 Attachment" else "$totalCount Attachments"
                        }
                        else -> ""
                    }
                } else {
                    ""
                }
                
                Text(
                    text = if (attachmentText.isNotEmpty()) attachmentText else subtitle.ifEmpty { stringResource(id = R.string.chat) },
                    style = Title2,
                    color = colorResource(id = R.color.text_secondary),
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.wrapContentWidth().padding(start = 8.dp),
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
                    Spacer(modifier = Modifier.width(4.dp))
                }

                if (unreadMessageCount > 0) {
                    val shape = if (unreadMentionCount > MENTION_COUNT_THRESHOLD) {
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
    }
}

@Composable
@DefaultPreviews
fun VaultSpaceCardPreview() {
    VaultSpaceCard(
        title = "B&O Museum",
        subtitle = "Private space",
        onCardClicked = {},
        icon = SpaceIconView.Placeholder()
    )
}

@Composable
@DefaultPreviews
fun ChatWithMentionAndMessage() {
    VaultChatCard(
        title = "B&O Museum",
        onCardClicked = {},
        icon = SpaceIconView.Placeholder(),
        previewText = "John Doe: Hello, this is a preview message that might be long enough to show how it looks with multiple lines.",
        creatorName = "John Doe",
        messageText = "Hello, this is a preview message that might be long enough to show how it looks with multiple lines.",
        messageTime = "18:32",
        unreadMessageCount = 32,
        unreadMentionCount = 1,
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
                    text = "Hello, this is a preview message.",
                    marks = emptyList(),
                    style = Block.Content.Text.Style.P
                ),
                order = "order-id"
            )
        )
    )
}

@Composable
@DefaultPreviews
fun ChatWithMention() {
    VaultChatCard(
        title = "B&O Museum",
        onCardClicked = {},
        icon = SpaceIconView.Placeholder(),
        previewText = "John Doe: Hello, this is a preview message that might be long enough to show how it looks with multiple lines.",
        creatorName = "John Doe",
        messageText = "Hello, this is a preview message that might be long enough to show how it looks with multiple lines.",
        messageTime = "18:32",
        unreadMentionCount = 1,
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
                    text = "Hello, this is a preview message.",
                    marks = emptyList(),
                    style = Block.Content.Text.Style.P
                ),
                order = "order-id"
            )
        )
    )
}

@Composable
@DefaultPreviews
fun ChatPreview() {
    VaultChatCard(
        title = "B&O Museum",
        onCardClicked = {},
        icon = SpaceIconView.Placeholder(),
        previewText = "John Doe: Hello, this is a preview message that might be long enough to show how it looks with multiple lines.",
        creatorName = "John Doe",
        messageText = "Hello, this is a preview message that might be long enough to show how it looks with multiple lines.",
        messageTime = "18:32",
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
                    text = "Hello, this is a preview message.",
                    marks = emptyList(),
                    style = Block.Content.Text.Style.P
                ),
                order = "order-id"
            )
        )
    )
}

const val MENTION_COUNT_THRESHOLD = 9