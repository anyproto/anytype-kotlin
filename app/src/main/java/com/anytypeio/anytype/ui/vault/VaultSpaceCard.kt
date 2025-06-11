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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.features.SpaceIconView
import com.anytypeio.anytype.core_ui.views.BodySemiBold
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Regular
import com.anytypeio.anytype.core_ui.views.Relations2
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.presentation.objects.ObjectIcon
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
    modifier: Modifier = Modifier,
    title: String,
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
        modifier = modifier
    ) {
        SpaceIconView(
            icon = icon,
            onSpaceIconClick = {},
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
            .padding(start = 68.dp, top = 8.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
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
                    modifier = Modifier.wrapContentSize(),
                    textAlign = TextAlign.Center,
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {

            val (chatText, inlineContent) = buildChatContentWithInlineIcons(
                creatorName = creatorName,
                messageText = messageText,
                attachmentPreviews = attachmentPreviews,
                fallbackSubtitle = subtitle.ifEmpty { stringResource(id = R.string.chat) }
            )

            Text(
                text = chatText,
                inlineContent = inlineContent,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = colorResource(id = R.color.text_secondary),
            )

            Row(
                modifier = Modifier.padding(start = 8.dp),
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
private fun buildChatContentWithInlineIcons(
    creatorName: String?,
    messageText: String?,
    attachmentPreviews: List<VaultSpaceView.AttachmentPreview>,
    fallbackSubtitle: String
): Pair<AnnotatedString, Map<String, InlineTextContent>> {

    val spanTitle2Medium = PreviewTitle2Medium.toSpanStyle()
    val spanTitle2Regular = PreviewTitle2Regular.toSpanStyle()

    val attachmentCount = attachmentPreviews.size
    val imageCount = attachmentPreviews.count { it.type == VaultSpaceView.AttachmentType.IMAGE }
    val fileCount = attachmentPreviews.count { it.type == VaultSpaceView.AttachmentType.FILE }
    val linkCount = attachmentPreviews.count { it.type == VaultSpaceView.AttachmentType.LINK }

    val inlineContentMap = mutableMapOf<String, InlineTextContent>()

    val text = buildAnnotatedString {
        // Add creator name if available
        if (creatorName != null) {
            withStyle(style = spanTitle2Medium) {
                append("$creatorName: ")
            }
        }

        // Add attachment icons (max 3)
        attachmentPreviews.take(3).forEachIndexed { index, preview ->
            val iconId = "attachment_$index"
            appendInlineContent(iconId, "[icon]")

            // Create the inline content for this attachment
            inlineContentMap[iconId] = InlineTextContent(
                Placeholder(
                    width = 18.sp,
                    height = 18.sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                )
            ) {
                ListWidgetObjectIcon(
                    icon = preview.objectIcon,
                    modifier = Modifier.fillMaxSize(),
                    iconSize = 18.dp
                )
            }

            // Add small space after icon if not the last one
            if (index < attachmentPreviews.take(3).size - 1) {
                append(" ")
            }
        }

        // Add space after icons if there are any and we have content to follow
        if (attachmentPreviews.isNotEmpty() && (messageText != null || attachmentCount > 3)) {
            append(" ")
        }

        // Determine what text to show
        when {
            // Case A: attachments size <= 1
            attachmentCount <= 1 -> {
                when {
                    messageText.isNullOrEmpty() -> {
                        // Single attachment, no message text
                        when {
                            imageCount == 1 -> {
                                withStyle(style = spanTitle2Medium) {
                                    append(stringResource(R.string.image))
                                }
                            }

                            fileCount == 1 -> {
                                withStyle(style = spanTitle2Medium) {
                                    append(stringResource(R.string.file))
                                }
                            }

                            linkCount == 1 -> {
                                val linkTitle =
                                    attachmentPreviews.find { it.type == VaultSpaceView.AttachmentType.LINK }?.title
                                        ?: stringResource(R.string.objects)
                                withStyle(style = spanTitle2Medium) {
                                    append(linkTitle)
                                }
                            }

                            else -> {
                                // No attachments, no message, show fallback
                                withStyle(style = spanTitle2Regular) {
                                    append(fallbackSubtitle)
                                }
                            }
                        }
                    }

                    else -> {
                        // Single attachment, with message text
                        when {
                            linkCount == 1 -> {
                                // For links: show title + messageText
                                val linkTitle =
                                    attachmentPreviews.find { it.type == VaultSpaceView.AttachmentType.LINK }?.title
                                        ?: stringResource(R.string.object_1)
                                withStyle(style = spanTitle2Medium) {
                                    append(linkTitle)
                                    append(" ")
                                }
                                withStyle(style = spanTitle2Regular) {
                                    append(messageText)
                                }
                            }

                            else -> {
                                // For files/images: just show messageText
                                withStyle(style = spanTitle2Regular) {
                                    append(messageText)
                                }
                            }
                        }
                    }
                }
            }

            // Case B: attachments size >= 2
            else -> {
                when {
                    messageText.isNullOrEmpty() -> {
                        // Multiple attachments, no message text
                        when {
                            imageCount > 0 && fileCount == 0 && linkCount == 0 -> {
                                // Images only
                                withStyle(style = spanTitle2Medium) {
                                    append("$imageCount ${stringResource(R.string.images)}")
                                }
                            }

                            fileCount > 0 && imageCount == 0 && linkCount == 0 -> {
                                // Files only
                                withStyle(style = spanTitle2Medium) {
                                    append("$fileCount ${stringResource(R.string.files)}")
                                }
                            }

                            linkCount > 0 && imageCount == 0 && fileCount == 0 -> {
                                // Objects only
                                withStyle(style = spanTitle2Medium) {
                                    append("$linkCount ${stringResource(R.string.objects)}")
                                }
                            }

                            else -> {
                                // Mixed types
                                withStyle(style = spanTitle2Medium) {
                                    append("$attachmentCount ${stringResource(R.string.attachments)}")
                                }
                            }
                        }
                    }

                    else -> {
                        // Multiple attachments, with message text - just show message text
                        withStyle(style = spanTitle2Regular) {
                            append(messageText)
                        }
                    }
                }
            }
        }
    }

    return Pair(text, inlineContentMap)
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