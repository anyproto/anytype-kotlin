package com.anytypeio.anytype.ui.vault

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.features.SpaceIconView
import com.anytypeio.anytype.core_ui.views.BodySemiBold
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Regular
import com.anytypeio.anytype.core_ui.views.Relations2
import com.anytypeio.anytype.core_ui.views.Title3
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.presentation.vault.VaultSpaceView

@Composable
fun VaultSpaceCard(
    modifier: Modifier,
    title: String,
    subtitle: String,
    icon: SpaceIconView,
    isPinned: Boolean = false,
    spaceView: VaultSpaceView? = null,
    expandedSpaceId: String? = null,
    onDismissMenu: () -> Unit = {},
    onMuteSpace: (Id) -> Unit = {},
    onUnmuteSpace: (Id) -> Unit = {},
    onPinSpace: (Id) -> Unit = {},
    onUnpinSpace: (Id) -> Unit = {},
    onSpaceSettings: (Id) -> Unit = {},
    currentPinnedCount: Int
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp)
    ) {
        SpaceIconView(
            icon = icon,
            mainSize = 56.dp,
            modifier = Modifier
                .align(Alignment.CenterStart)
        )
        ContentSpace(
            title = title,
            subtitle = subtitle,
            isPinned = isPinned
        )
        
        // Include dropdown menu inside the card
        spaceView?.let { space ->
            SpaceActionsDropdownMenu(
                expanded = expandedSpaceId == space.space.id,
                onDismiss = onDismissMenu,
                isMuted = spaceView.isMuted,
                isPinned = spaceView.isPinned,
                currentPinnedCount = currentPinnedCount,
                onMuteToggle = {
                    spaceView.space.targetSpaceId?.let {
                        if (spaceView.isMuted == true) onUnmuteSpace(it) else onMuteSpace(it)
                    }
                },
                onPinToggle = {
                    spaceView.space.id.let {
                        if (spaceView.isPinned) onUnpinSpace(it) else onPinSpace(it)
                    }
                },
                onSpaceSettings = {
                    spaceView.space.id.let { onSpaceSettings(it) }
                }
            )
        }
    }
}

@Composable
private fun ContentSpace(
    title: String,
    subtitle: String,
    isPinned: Boolean = false,
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
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = subtitle,
                style = Title3,
                color = colorResource(id = R.color.text_secondary),
                modifier = Modifier.weight(1f),
            )
            if (isPinned) {
                Image(
                    painter = painterResource(R.drawable.ic_pin_18),
                    contentDescription = stringResource(R.string.content_desc_pin),
                    modifier = Modifier
                        .size(18.dp),
                    colorFilter = ColorFilter.tint(colorResource(id = R.color.glyph_inactive))
                )
            }
        }
    }
}

@Composable
fun VaultChatCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: SpaceIconView,
    creatorName: String? = null,
    messageText: String? = null,
    messageTime: String? = null,
    chatPreview: Chat.Preview? = null,
    unreadMessageCount: Int = 0,
    unreadMentionCount: Int = 0,
    attachmentPreviews: List<VaultSpaceView.AttachmentPreview> = emptyList(),
    isMuted: Boolean? = null,
    isPinned: Boolean = false,
    maxPinnedSpaces: Int,
    spaceView: VaultSpaceView? = null,
    expandedSpaceId: String? = null,
    onDismissMenu: () -> Unit = {},
    onMuteSpace: (Id) -> Unit = {},
    onUnmuteSpace: (Id) -> Unit = {},
    onPinSpace: (Id) -> Unit = {},
    onUnpinSpace: (Id) -> Unit = {},
    onSpaceSettings: (Id) -> Unit = {},
    currentPinnedCount: Int
) {
    Box(
        modifier = modifier
    ) {
        SpaceIconView(
            icon = icon,
            mainSize = 56.dp,
            modifier = Modifier
                .align(Alignment.CenterStart)
        )
        ContentChat(
            title = title,
            subtitle = messageText ?: chatPreview?.message?.content?.text.orEmpty(),
            creatorName = creatorName,
            messageText = messageText,
            messageTime = messageTime,
            unreadMessageCount = unreadMessageCount,
            unreadMentionCount = unreadMentionCount,
            attachmentPreviews = attachmentPreviews,
            isMuted = isMuted,
            isPinned = isPinned
        )
        
        // Include dropdown menu inside the card
        spaceView?.let { space ->
            SpaceActionsDropdownMenu(
                expanded = expandedSpaceId == space.space.id,
                onDismiss = onDismissMenu,
                isMuted = spaceView.isMuted,
                isPinned = spaceView.isPinned,
                currentPinnedCount = currentPinnedCount,
                onMuteToggle = {
                    spaceView.space.targetSpaceId?.let {
                        if (spaceView.isMuted == true) onUnmuteSpace(it) else onMuteSpace(it)
                    }
                },
                onPinToggle = {
                    spaceView.space.id.let {
                        if (spaceView.isPinned) onUnpinSpace(it) else onPinSpace(it)
                    }
                },
                onSpaceSettings = {
                    spaceView.space.id.let { onSpaceSettings(it) }
                }
            )
        }
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
    attachmentPreviews: List<VaultSpaceView.AttachmentPreview> = emptyList(),
    isMuted: Boolean? = null,
    isPinned: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(start = 68.dp, top = 8.dp),
        verticalArrangement = Arrangement.Top
    ) {
        TitleRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp),
            message = title,
            messageTime = messageTime,
            mutedIcon = painterResource(R.drawable.ci_notifications_off),
            isMuted = isMuted
        )
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
                                color = if (isMuted == true) colorResource(R.color.glyph_active) else colorResource(
                                    R.color.color_accent
                                ),
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
                                color = if (isMuted == true) colorResource(R.color.glyph_active) else colorResource(
                                    R.color.color_accent
                                ),
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

                if (unreadMessageCount == 0 && unreadMentionCount == 0 && isPinned) {
                    Image(
                        painter = painterResource(R.drawable.ic_pin_18),
                        contentDescription = stringResource(R.string.content_desc_pin),
                        modifier = Modifier
                            .size(18.dp),
                        colorFilter = ColorFilter.tint(colorResource(id = R.color.glyph_inactive))
                    )
                }
            }
        }
    }
}

@Composable
fun TitleRow(
    modifier: Modifier,
    message: String,
    messageTime: String?,
    mutedIcon: Painter,
    isMuted: Boolean? = null
) {
    val density = LocalDensity.current

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Layout(
            content = {
                // 0: the message text
                Text(
                    text = message,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = BodySemiBold,
                    color = colorResource(id = R.color.text_primary),
                )
                // 1: optional muted icon
                if (isMuted == true) {
                    Image(
                        painter = mutedIcon,
                        contentDescription = stringResource(R.string.content_desc_muted),
                        modifier = Modifier.size(18.dp),
                        colorFilter = ColorFilter.tint(colorResource(R.color.glyph_active))
                    )
                }
                // 2: optional time (only if messageTime != null)
                messageTime?.let {
                    Text(
                        text = it,
                        style = Relations2,
                        color = colorResource(id = R.color.transparent_active),
                    )
                }
            }
        ) { measurables, constraints ->
            // spacing constants in px
            val iconTextGap = with(density) { 8.dp.roundToPx() }
            val textTimeGap = with(density) { 8.dp.roundToPx() }

            // measurables indices:
            // 0 = text
            // 1 = icon if isMuted else (possibly) time
            // last = time only if messageTime != null

            // 1) Measure time first (if any)
            val timePlaceable = if (messageTime != null) {
                measurables.last().measure(
                    constraints.copy(minWidth = 0, minHeight = 0)
                )
            } else null

            // 2) Measure icon next (if muted)
            val iconPlaceable = if (isMuted == true) {
                // if time exists, icon is at index 1; if no time, still index 1
                measurables.getOrNull(1)?.measure(
                    constraints.copy(minWidth = 0, minHeight = 0)
                )
            } else null

            // 3) Compute reserved width:
            //    time width + gap before time (if time exists)
            //  + icon width + gap before icon (if icon exists)
            val reserved = listOfNotNull(
                timePlaceable?.width,
                iconPlaceable?.width
            ).sum() +
                    (if (iconPlaceable != null) iconTextGap else 0) +
                    (if (timePlaceable != null) textTimeGap else 0)

            // 4) Measure text with remaining width
            val maxTextWidth = (constraints.maxWidth - reserved).coerceAtLeast(0)
            val textPlaceable = measurables.getOrNull(0)?.measure(
                constraints.copy(
                    maxWidth = maxTextWidth,
                    minWidth = 0, minHeight = 0
                )
            )

            // 5) Determine row height
            val rowHeight = listOfNotNull(
                textPlaceable?.height,
                iconPlaceable?.height,
                timePlaceable?.height
            ).maxOrNull() ?: 0

            // 6) Layout & place children
            layout(constraints.maxWidth, rowHeight) {
                val textY = (rowHeight - (textPlaceable?.height ?: 0)) / 2
                textPlaceable?.placeRelative(x = 0, y = textY)

                iconPlaceable?.let {
                    val iconY = (rowHeight - it.height) / 2
                    it.placeRelative(
                        x = (textPlaceable?.width ?: 0) + iconTextGap,
                        y = iconY
                    )
                }

                timePlaceable?.let {
                    val timeY = (rowHeight - it.height) / 2
                    val timeX = constraints.maxWidth - it.width
                    it.placeRelative(x = timeX, y = timeY)
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
        modifier = Modifier.fillMaxWidth(),
        title = "B&O Museum",
        subtitle = "Private space",
        icon = SpaceIconView.Placeholder(),
        currentPinnedCount = 3
    )
}

@Composable
@DefaultPreviews
fun ChatWithMentionAndMessage() {
    VaultChatCard(
        title = "B&O Museum",
        icon = SpaceIconView.Placeholder(),
        creatorName = "John Doe",
        messageText = "Hello, this is a preview message that might be long enough to show how it looks with multiple lines.",
        messageTime = "18:32",
        unreadMessageCount = 32,
        unreadMentionCount = 1,
        isMuted = false,
        maxPinnedSpaces = 6,
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
                order = "order-id",
                synced = false
            )
        ),
        currentPinnedCount = 3
    )
}

@Composable
@DefaultPreviews
fun ChatWithMention() {
    VaultChatCard(
        title = "B&O Museum",
        icon = SpaceIconView.Placeholder(),
        creatorName = "John Doe",
        messageText = "Hello, this is a preview message that might be long enough to show how it looks with multiple lines.",
        messageTime = "18:32",
        unreadMentionCount = 1,
        isMuted = true,
        maxPinnedSpaces = 6,
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
                order = "order-id",
                synced = false
            )
        ),
        currentPinnedCount = 3
    )
}

@Composable
@DefaultPreviews
fun ChatPreview() {
    VaultChatCard(
        title = "B&O Museum",
        icon = SpaceIconView.Placeholder(),
        creatorName = "John Doe",
        messageText = "Hello, this is a preview message that might be long enough to show how it looks with multiple lines.",
        messageTime = "18:32",
        isMuted = false,
        maxPinnedSpaces = 6,
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
                order = "order-id",
                synced = false
            )
        ),
        currentPinnedCount = 3
    )
}

const val MENTION_COUNT_THRESHOLD = 9