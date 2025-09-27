package com.anytypeio.anytype.ui.vault

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
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
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.views.BodySemiBold
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Regular
import com.anytypeio.anytype.core_ui.views.Relations2
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.core_ui.widgets.SpaceBackground
import com.anytypeio.anytype.core_ui.widgets.objectIcon.SpaceIconView
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.presentation.vault.VaultSpaceView

@Composable
fun VaultChatCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: SpaceIconView,
    spaceBackground: SpaceBackground,
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
    spaceView: VaultSpaceView,
    expandedSpaceId: String? = null,
    onDismissMenu: () -> Unit = {},
    onMuteSpace: (Id) -> Unit = {},
    onUnmuteSpace: (Id) -> Unit = {},
    onPinSpace: (Id) -> Unit = {},
    onUnpinSpace: (Id) -> Unit = {},
    onSpaceSettings: (Id) -> Unit = {},
    currentPinnedCount: Int
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
        ContentChat(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp),
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
        SpaceActionsDropdownMenu(
            expanded = expandedSpaceId == spaceView.space.id,
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

@Composable
private fun RowScope.ContentChat(
    modifier: Modifier,
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
    Column(modifier = modifier, verticalArrangement = Arrangement.Center) {
        val hasContent = !creatorName.isNullOrEmpty() ||
                        !messageText.isNullOrEmpty() ||
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
                messageTime = messageTime
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
            ChatSubtitleRow(
                subtitle = subtitle,
                creatorName = creatorName,
                messageText = messageText,
                attachmentPreviews = attachmentPreviews,
                unreadMessageCount = unreadMessageCount,
                unreadMentionCount = unreadMentionCount,
                isMuted = isMuted,
                isPinned = isPinned
            )
        }
    }
}

@Composable
private fun ChatSubtitleRow(
    subtitle: String,
    creatorName: String?,
    messageText: String?,
    attachmentPreviews: List<VaultSpaceView.AttachmentPreview>,
    unreadMessageCount: Int,
    unreadMentionCount: Int,
    isMuted: Boolean?,
    isPinned: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
        val (chatText, inlineContent) = buildChatContentWithInlineIcons(
            creatorName = creatorName,
            messageText = messageText,
            attachmentPreviews = attachmentPreviews,
            fallbackSubtitle = subtitle
        )

        Text(
            text = chatText,
            inlineContent = inlineContent,
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = colorResource(id = R.color.text_primary),
        )

        UnreadIndicatorsRow(
            unreadMessageCount = unreadMessageCount,
            unreadMentionCount = unreadMentionCount,
            isMuted = isMuted,
            isPinned = isPinned
        )
    }
}

@Composable
private fun UnreadIndicatorsRow(
    unreadMessageCount: Int,
    unreadMentionCount: Int,
    isMuted: Boolean?,
    isPinned: Boolean
) {
    Row(
        modifier = Modifier.padding(start = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (unreadMentionCount > 0) {
            Box(
                modifier = Modifier
                    .background(
                        color = if (isMuted == true) colorResource(R.color.control_transparent_tetriary) else colorResource(
                            R.color.control_accent
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
                        color = if (isMuted == true) colorResource(R.color.control_transparent_tetriary) else colorResource(
                            R.color.control_accent
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
                modifier = Modifier.size(18.dp),
                colorFilter = ColorFilter.tint(colorResource(R.color.control_transparent_secondary))
            )
        }
    }
}

@Composable
fun TitleRow(
    modifier: Modifier,
    message: String,
    messageTime: String?
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
                // 2: optional time (only if messageTime != null)
                messageTime?.let {
                    Text(
                        text = it,
                        style = Relations2,
                        color = colorResource(id = R.color.control_transparent_secondary),
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

            // 3) Compute reserved width:
            //    time width + gap before time (if time exists)
            //  + icon width + gap before icon (if icon exists)
            val reserved = listOfNotNull(
                timePlaceable?.width
            ).sum() + (if (timePlaceable != null) textTimeGap else 0)

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
                timePlaceable?.height
            ).maxOrNull() ?: 0

            // 6) Layout & place children
            layout(constraints.maxWidth, rowHeight) {
                val textY = (rowHeight - (textPlaceable?.height ?: 0)) / 2
                textPlaceable?.placeRelative(x = 0, y = textY)

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
        // Add creator name if available (truncate if too long)
        if (creatorName != null) {
            val truncatedCreatorName = if (creatorName.length > 30) {
                creatorName.take(27) + "..."
            } else {
                creatorName
            }
            withStyle(style = spanTitle2Medium) {
                append("$truncatedCreatorName\n")
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
fun ChatWithMentionAndMessage() {
    Column {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
        )
        VaultChatCard(
            modifier = Modifier.fillMaxWidth(),
            title = "B&O Museum",
            icon = SpaceIconView.ChatSpace.Placeholder(),
            isPinned = true,
            creatorName = "John Doe",
            messageText = "Hello, this is a preview message that might be long enough to show how it looks with multiple lines.",
            messageTime = "18:32",
            unreadMessageCount = 32,
            unreadMentionCount = 1,
            isMuted = true,
            maxPinnedSpaces = 6,
            chatPreview =
                Chat.Preview(
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
            currentPinnedCount = 3,
            spaceBackground = SpaceBackground.SolidColor(color = androidx.compose.ui.graphics.Color(0xFFE0F7FA)),
            spaceView = VaultSpaceView.Space(
                space = ObjectWrapper.SpaceView(map = mapOf("name" to "Space 1", "id" to "spaceId1")),
                isMuted = false,
                icon = SpaceIconView.ChatSpace.Placeholder(),
                isOwner = true,
                accessType = "Owner"
            ),
        )
    }
}