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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.extensions.getMimeIcon
import com.anytypeio.anytype.core_ui.features.SpaceIconView
import com.anytypeio.anytype.core_ui.views.BodySemiBold
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
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
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,

        ) {
            val density = LocalDensity.current
            var prefixPx by remember { mutableStateOf(0) }
            val firstLineIndent: TextUnit = with(density) { prefixPx.toSp() }

            Box(modifier = Modifier.weight(1f) ){
                Row(
                    modifier = Modifier
                        .onGloballyPositioned { prefixPx = it.size.width }
                        .wrapContentWidth()
                        .wrapContentHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Show creatorName first if available
                    if (creatorName != null) {
                        Text(
                            text = "$creatorName: ",
                            style = PreviewTitle2Medium,
                            color = colorResource(id = R.color.text_secondary),
                            textAlign = TextAlign.Left,
                        )
                    }

                    // Show attachment icons after creatorName
                    if (attachmentPreviews.isNotEmpty()) {
                        attachmentPreviews.take(3).forEach { preview ->
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
                                            modifier = Modifier.size(18.dp),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                                VaultSpaceView.AttachmentType.FILE -> {
                                    val iconResource = preview.mimeType.getMimeIcon(preview.fileExtension)
                                    Image(
                                        painter = painterResource(iconResource),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                VaultSpaceView.AttachmentType.LINK -> {
                                    val linkIcon =
                                        preview.objectIcon ?: ObjectIcon.TypeIcon.Default.DEFAULT
                                    ListWidgetObjectIcon(
                                        icon = linkIcon,
                                        modifier = Modifier,
                                        iconSize = 18.dp
                                    )
                                }
                            }
                        }
                    }

                    if (attachmentPreviews.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }

                // --- body --------------------
                val displayText = buildChatContentText(
                    messageText = messageText,
                    attachmentPreviews = attachmentPreviews,
                    fallbackSubtitle = subtitle.ifEmpty { stringResource(id = R.string.chat) }
                )

                val finalText = buildAnnotatedString {
                    withStyle(
                        ParagraphStyle(
                            textIndent = TextIndent(firstLine = firstLineIndent)
                        )
                    ) {
                        append(displayText)
                    }
                }

                Text(
                    text = finalText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = colorResource(id = R.color.text_secondary),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }

            Row(
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

@Composable
@DefaultPreviews
fun ChatWithManyAttachmentsNoText() {
    VaultChatCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp),
        title = "File Archive",
        icon = SpaceIconView.Placeholder(),
        messageTime = "09:30",
        // No message text, so should show "5 Images" 
        attachmentPreviews = listOf(
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.IMAGE,
                imageUrl = null
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.IMAGE,
                imageUrl = null
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.IMAGE,
                imageUrl = null
            ),
            // These extra attachments should trigger the count text
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.IMAGE,
                imageUrl = null
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.IMAGE,
                imageUrl = null
            )
        )
    )
}

@Composable
@DefaultPreviews
fun ChatWithManyMixedAttachmentsNoText() {
    VaultChatCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp),
        title = "Mixed Files",
        icon = SpaceIconView.Placeholder(),
        messageTime = "14:15",
        // No message text, should show "6 Attachments"
        attachmentPreviews = listOf(
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.IMAGE,
                imageUrl = null
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.FILE,
                mimeType = "application/pdf",
                fileExtension = "pdf"
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.FILE,
                mimeType = "application/vnd.ms-excel",
                fileExtension = "xlsx"
            ),
            // Additional attachments for count
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.IMAGE,
                imageUrl = null
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.FILE,
                mimeType = "text/plain",
                fileExtension = "txt"
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.LINK,
                mimeType = "text/html",
                fileExtension = "html",
                objectIcon = ObjectIcon.Basic.Emoji(unicode = "🌐"),
                title = "Web Resource"
            )
        )
    )
}

@Composable
@DefaultPreviews
fun ChatWithImageAttachments() {
    VaultChatCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp),
        title = "Design Team",
        icon = SpaceIconView.Placeholder(),
        previewText = "Alice: Check out these designs",
        creatorName = "Alice",
        messageText = "Check out these designs",
        messageTime = "10:45",
        attachmentPreviews = listOf(
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.IMAGE,
                imageUrl = null // Use null to show placeholder icon instead
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.IMAGE,
                imageUrl = null
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.IMAGE,
                imageUrl = null
            )
        )
    )
}

@Composable
@DefaultPreviews
fun ChatWithFileAttachments() {
    VaultChatCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp),
        title = "Project Discussion",
        icon = SpaceIconView.Placeholder(),
        previewText = "Bob: Here are the documents",
        creatorName = "Bob",
        messageText = "Here are the documents",
        messageTime = "14:22",
        attachmentPreviews = listOf(
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.FILE,
                mimeType = "application/pdf",
                fileExtension = "pdf"
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.FILE,
                mimeType = "application/vnd.ms-excel",
                fileExtension = "xlsx"
            )
        )
    )
}

@Composable
@DefaultPreviews
fun ChatWithLinkAttachments() {
    VaultChatCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp),
        title = "Resource Sharing",
        icon = SpaceIconView.Placeholder(),
        previewText = "Charlie: Found some useful links",
        creatorName = "Charlie",
        messageText = "Found some useful links",
        messageTime = "11:30",
        attachmentPreviews = listOf(
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.LINK,
                mimeType = "text/html",
                fileExtension = "html",
                objectIcon = ObjectIcon.Basic.Emoji(unicode = "🔗"),
                title = "Resource Link 1"
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.LINK,
                mimeType = "text/html",
                fileExtension = "html",
                objectIcon = ObjectIcon.Bookmark(
                    image = "",
                    fallback = ObjectIcon.TypeIcon.Fallback.DEFAULT
                ),
                title = "Documentation"
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.LINK,
                mimeType = "text/html",
                fileExtension = "html",
                objectIcon = ObjectIcon.Basic.Emoji(unicode = "📋"),
                title = "Project Board"
            )
        )
    )
}

@Composable
@DefaultPreviews
fun ChatWithMixedAttachments() {
    VaultChatCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp),
        title = "Development Updates",
        icon = SpaceIconView.Placeholder(),
        previewText = "Dana: Latest progress and resources",
        creatorName = "Dana",
        messageText = "Latest progress and resources",
        messageTime = "16:30",
        attachmentPreviews = listOf(
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.IMAGE,
                imageUrl = null
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.FILE,
                mimeType = "application/pdf",
                fileExtension = "pdf"
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.LINK,
                mimeType = "text/html",
                fileExtension = "html",
                objectIcon = ObjectIcon.Basic.Emoji(unicode = "🌐"),
                title = "External Link"
            )
        )
    )
}

@Composable
@DefaultPreviews
fun ChatWithAttachmentsNoText() {
    VaultChatCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp),
        title = "Media Sharing",
        icon = SpaceIconView.Placeholder(),
        messageTime = "12:15",
        attachmentPreviews = listOf(
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.IMAGE,
                imageUrl = null
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.LINK,
                mimeType = "text/html",
                fileExtension = "html",
                objectIcon = ObjectIcon.Bookmark(
                    image = "",
                    fallback = ObjectIcon.TypeIcon.Fallback.DEFAULT
                ),
                title = "Shared Resource"
            )
        )
    )
}

@Composable
@DefaultPreviews
fun ChatWithManyLinksNoText() {
    VaultChatCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp),
        title = "Reference Links",
        icon = SpaceIconView.Placeholder(),
        messageTime = "15:45",
        // No message text, should show "5 Attachments" for mixed link/file types
        attachmentPreviews = listOf(
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.LINK,
                objectIcon = ObjectIcon.Basic.Emoji(unicode = "📚"),
                title = "Documentation"
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.LINK,
                objectIcon = ObjectIcon.Basic.Emoji(unicode = "🔧"),
                title = "Tools"
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.LINK,
                objectIcon = ObjectIcon.Basic.Emoji(unicode = "📊"),
                title = "Analytics"
            ),
            // These extra attachments should trigger the count text
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.LINK,
                objectIcon = ObjectIcon.Basic.Emoji(unicode = "🔗"),
                title = "Reference"
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.FILE,
                mimeType = "application/pdf",
                fileExtension = "pdf"
            )
        )
    )
}

@Composable
@DefaultPreviews
fun ChatWithSingleLinkNoText() {
    VaultChatCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp),
        title = "Single Link",
        icon = SpaceIconView.Placeholder(),
        messageTime = "09:15",
        // Single link should show object name instead of "1 Object"
        attachmentPreviews = listOf(
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.LINK,
                objectIcon = ObjectIcon.Basic.Emoji(unicode = "📚"),
                title = "API Documentation"
            )
        )
    )
}

@Composable
@DefaultPreviews
fun ChatWithMultipleLinksOnlyNoText() {
    VaultChatCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp),
        title = "Multiple Links Only",
        icon = SpaceIconView.Placeholder(),
        messageTime = "10:30",
        // Multiple links only should show "3 Objects"
        attachmentPreviews = listOf(
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.LINK,
                objectIcon = ObjectIcon.Basic.Emoji(unicode = "📚"),
                title = "Documentation"
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.LINK,
                objectIcon = ObjectIcon.Basic.Emoji(unicode = "🔧"),
                title = "Tools"
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.LINK,
                objectIcon = ObjectIcon.Basic.Emoji(unicode = "📊"),
                title = "Analytics"
            )
        )
    )
}

@Composable
@DefaultPreviews
fun ChatWithSingleImageNoMessage() {
    VaultChatCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp),
        title = "Single Image Demo",
        icon = SpaceIconView.Placeholder(),
        creatorName = "Alice",
        messageTime = "09:00",
        // Single image, no message: "Alice: [] Image"
        attachmentPreviews = listOf(
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.IMAGE,
                imageUrl = null
            )
        )
    )
}

@Composable
@DefaultPreviews
fun ChatWithSingleLinkWithMessage() {
    VaultChatCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp),
        title = "Single Link With Text",
        icon = SpaceIconView.Placeholder(),
        creatorName = "Bob",
        messageText = "Check this out",
        messageTime = "10:00",
        // Single link with message: "Bob: [] API Documentation Check this out"
        attachmentPreviews = listOf(
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.LINK,
                objectIcon = ObjectIcon.Basic.Emoji(unicode = "📚"),
                title = "API Documentation"
            )
        )
    )
}

@Composable
@DefaultPreviews
fun ChatWithMultipleImagesNoMessage() {
    VaultChatCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp),
        title = "Multiple Images Demo",
        icon = SpaceIconView.Placeholder(),
        creatorName = "Charlie",
        messageTime = "11:00",
        // Multiple images, no message: "Charlie: [][][] 3 Images"
        attachmentPreviews = listOf(
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.IMAGE,
                imageUrl = null
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.IMAGE,
                imageUrl = null
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.IMAGE,
                imageUrl = null
            )
        )
    )
}

@Composable
@DefaultPreviews
fun ChatWithMultipleObjectsWithMessage() {
    VaultChatCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp),
        title = "Multiple Objects Demo",
        icon = SpaceIconView.Placeholder(),
        creatorName = "Dana",
        messageText = "Here are some resources",
        messageTime = "12:00",
        // Multiple objects with message: "Dana: [][][] 3 Objects Here are some resources"
        attachmentPreviews = listOf(
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.LINK,
                objectIcon = ObjectIcon.Basic.Emoji(unicode = "📚"),
                title = "Documentation"
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.LINK,
                objectIcon = ObjectIcon.Basic.Emoji(unicode = "🔧"),
                title = "Tools"
            ),
            VaultSpaceView.AttachmentPreview(
                type = VaultSpaceView.AttachmentType.LINK,
                objectIcon = ObjectIcon.Basic.Emoji(unicode = "📊"),
                title = "Analytics"
            )
        )
    )
}

@Composable
@DefaultPreviews
fun ChatEmpty() {
    VaultChatCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp),
        title = "Empty Chat",
        icon = SpaceIconView.Placeholder(),
        messageTime = "08:00"
    )
}

@Composable
fun ChatContentWithProperIndent(
    creatorName: String?,
    messageText: String?,
    attachmentPreviews: List<VaultSpaceView.AttachmentPreview>,
    fallbackSubtitle: String,
    modifier: Modifier = Modifier
) {

}

@Composable
private fun buildChatContentText(
    messageText: String?,
    attachmentPreviews: List<VaultSpaceView.AttachmentPreview>,
    fallbackSubtitle: String
): AnnotatedString {
    return buildAnnotatedString {
        val attachmentCount = attachmentPreviews.size
        val imageCount = attachmentPreviews.count { it.type == VaultSpaceView.AttachmentType.IMAGE }
        val fileCount = attachmentPreviews.count { it.type == VaultSpaceView.AttachmentType.FILE }
        val linkCount = attachmentPreviews.count { it.type == VaultSpaceView.AttachmentType.LINK }
        
        when {
            // A. attachments size <= 1
            attachmentCount <= 1 -> {
                when {
                    messageText.isNullOrEmpty() -> {
                        // Single attachment, no message text
                        when {
                            imageCount == 1 -> {
                                withStyle(style = SpanStyle(
                                    fontFamily = com.anytypeio.anytype.core_ui.views.fontInterMedium,
                                    fontWeight = FontWeight.W500,
                                    fontSize = 15.sp,
                                    letterSpacing = (-0.016).em
                                )) {
                                    append("Image")
                                }
                            }
                            fileCount == 1 -> {
                                withStyle(style = SpanStyle(
                                    fontFamily = com.anytypeio.anytype.core_ui.views.fontInterMedium,
                                    fontWeight = FontWeight.W500,
                                    fontSize = 15.sp,
                                    letterSpacing = (-0.016).em
                                )) {
                                    append("File")
                                }
                            }
                            linkCount == 1 -> {
                                val linkTitle = attachmentPreviews.find { it.type == VaultSpaceView.AttachmentType.LINK }?.title ?: "Object"
                                withStyle(style = SpanStyle(
                                    fontFamily = com.anytypeio.anytype.core_ui.views.fontInterMedium,
                                    fontWeight = FontWeight.W500,
                                    fontSize = 15.sp,
                                    letterSpacing = (-0.016).em
                                )) {
                                    append(linkTitle)
                                }
                            }
                            else -> {
                                // No attachments, no message, show fallback
                                withStyle(style = SpanStyle(
                                    fontFamily = com.anytypeio.anytype.core_ui.views.fontInterRegular,
                                    fontWeight = FontWeight.W400,
                                    fontSize = 17.sp,
                                    letterSpacing = (-0.024).em
                                )) {
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
                                val linkTitle = attachmentPreviews.find { it.type == VaultSpaceView.AttachmentType.LINK }?.title ?: "Object"
                                withStyle(style = SpanStyle(
                                    fontFamily = com.anytypeio.anytype.core_ui.views.fontInterMedium,
                                    fontWeight = FontWeight.W500,
                                    fontSize = 15.sp,
                                    letterSpacing = (-0.016).em
                                )) {
                                    append(linkTitle)
                                    append(" ")
                                }
                                withStyle(style = SpanStyle(
                                    fontFamily = com.anytypeio.anytype.core_ui.views.fontInterRegular,
                                    fontWeight = FontWeight.W400,
                                    fontSize = 17.sp,
                                    letterSpacing = (-0.024).em
                                )) {
                                    append(messageText)
                                }
                            }
                            else -> {
                                // For files/images: just show messageText
                                withStyle(style = SpanStyle(
                                    fontFamily = com.anytypeio.anytype.core_ui.views.fontInterRegular,
                                    fontWeight = FontWeight.W400,
                                    fontSize = 17.sp,
                                    letterSpacing = (-0.024).em
                                )) {
                                    append(messageText)
                                }
                            }
                        }
                    }
                }
            }
            
            // B. attachments size >= 2
            else -> {
                when {
                    messageText.isNullOrEmpty() -> {
                        // Multiple attachments, no message text
                        when {
                            imageCount > 0 && fileCount == 0 && linkCount == 0 -> {
                                // Images only
                                withStyle(style = SpanStyle(
                                    fontFamily = com.anytypeio.anytype.core_ui.views.fontInterMedium,
                                    fontWeight = FontWeight.W500,
                                    fontSize = 15.sp,
                                    letterSpacing = (-0.016).em
                                )) {
                                    append("$imageCount Images")
                                }
                            }
                            fileCount > 0 && imageCount == 0 && linkCount == 0 -> {
                                // Files only
                                withStyle(style = SpanStyle(
                                    fontFamily = com.anytypeio.anytype.core_ui.views.fontInterMedium,
                                    fontWeight = FontWeight.W500,
                                    fontSize = 15.sp,
                                    letterSpacing = (-0.016).em
                                )) {
                                    append("$fileCount Files")
                                }
                            }
                            linkCount > 0 && imageCount == 0 && fileCount == 0 -> {
                                // Objects only
                                withStyle(style = SpanStyle(
                                    fontFamily = com.anytypeio.anytype.core_ui.views.fontInterMedium,
                                    fontWeight = FontWeight.W500,
                                    fontSize = 15.sp,
                                    letterSpacing = (-0.016).em
                                )) {
                                    append("$linkCount Objects")
                                }
                            }
                            else -> {
                                // Mixed types
                                withStyle(style = SpanStyle(
                                    fontFamily = com.anytypeio.anytype.core_ui.views.fontInterMedium,
                                    fontWeight = FontWeight.W500,
                                    fontSize = 15.sp,
                                    letterSpacing = (-0.016).em
                                )) {
                                    append("$attachmentCount Attachments")
                                }
                            }
                        }
                    }
                    else -> {
                        // Multiple attachments, with message text
                        when {
                            imageCount > 0 && fileCount == 0 && linkCount == 0 -> {
                                // Images only
                                withStyle(style = SpanStyle(
                                    fontFamily = com.anytypeio.anytype.core_ui.views.fontInterMedium,
                                    fontWeight = FontWeight.W500,
                                    fontSize = 15.sp,
                                    letterSpacing = (-0.016).em
                                )) {
                                    append("$imageCount Images ")
                                }
                                withStyle(style = SpanStyle(
                                    fontFamily = com.anytypeio.anytype.core_ui.views.fontInterRegular,
                                    fontWeight = FontWeight.W400,
                                    fontSize = 17.sp,
                                    letterSpacing = (-0.024).em
                                )) {
                                    append(messageText)
                                }
                            }
                            fileCount > 0 && imageCount == 0 && linkCount == 0 -> {
                                // Files only
                                withStyle(style = SpanStyle(
                                    fontFamily = com.anytypeio.anytype.core_ui.views.fontInterMedium,
                                    fontWeight = FontWeight.W500,
                                    fontSize = 15.sp,
                                    letterSpacing = (-0.016).em
                                )) {
                                    append("$fileCount Files ")
                                }
                                withStyle(style = SpanStyle(
                                    fontFamily = com.anytypeio.anytype.core_ui.views.fontInterRegular,
                                    fontWeight = FontWeight.W400,
                                    fontSize = 17.sp,
                                    letterSpacing = (-0.024).em
                                )) {
                                    append(messageText)
                                }
                            }
                            linkCount > 0 && imageCount == 0 && fileCount == 0 -> {
                                // Objects only
                                withStyle(style = SpanStyle(
                                    fontFamily = com.anytypeio.anytype.core_ui.views.fontInterMedium,
                                    fontWeight = FontWeight.W500,
                                    fontSize = 15.sp,
                                    letterSpacing = (-0.016).em
                                )) {
                                    append("$linkCount Objects ")
                                }
                                withStyle(style = SpanStyle(
                                    fontFamily = com.anytypeio.anytype.core_ui.views.fontInterRegular,
                                    fontWeight = FontWeight.W400,
                                    fontSize = 17.sp,
                                    letterSpacing = (-0.024).em
                                )) {
                                    append(messageText)
                                }
                            }
                            else -> {
                                // Mixed types
                                withStyle(style = SpanStyle(
                                    fontFamily = com.anytypeio.anytype.core_ui.views.fontInterMedium,
                                    fontWeight = FontWeight.W500,
                                    fontSize = 15.sp,
                                    letterSpacing = (-0.016).em
                                )) {
                                    append("$attachmentCount Attachments ")
                                }
                                withStyle(style = SpanStyle(
                                    fontFamily = com.anytypeio.anytype.core_ui.views.fontInterRegular,
                                    fontWeight = FontWeight.W400,
                                    fontSize = 17.sp,
                                    letterSpacing = (-0.024).em
                                )) {
                                    append(messageText)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

const val MENTION_COUNT_THRESHOLD = 9