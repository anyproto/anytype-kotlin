package com.anytypeio.anytype.feature_vault.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Text
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
import com.anytypeio.anytype.core_models.chats.NotificationState
import com.anytypeio.anytype.core_models.ui.AttachmentPreview
import com.anytypeio.anytype.core_models.ui.AttachmentType
import com.anytypeio.anytype.core_ui.views.BodySemiBold
import com.anytypeio.anytype.core_ui.views.Caption2Regular
import com.anytypeio.anytype.core_ui.views.CodeChatPreviewMedium
import com.anytypeio.anytype.core_ui.views.CodeChatPreviewRegular
import com.anytypeio.anytype.core_ui.views.Relations2
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.core_ui.widgets.SpaceBackground
import com.anytypeio.anytype.feature_vault.R


/**
 * Determines the text color for chat preview based on notification state and read/unread status.
 *
 * Logic:
 * - Muted/disabled chats: Always show as secondary color (even if unread)
 * - Enabled chats with no unread messages: Show as secondary color (read state)
 * - Enabled chats with unread messages: Show as primary color (unread state)
 */
@Composable
fun getChatTextColor(
    notificationMode: NotificationState?,
    unreadMessageCount: Int,
    unreadMentionCount: Int
): androidx.compose.ui.graphics.Color {
    return when {
        // Muted/disabled chats: always show as secondary (even if unread)
        notificationMode == NotificationState.DISABLE ->
            colorResource(id = R.color.text_transparent_secondary)

        // Read messages: show as secondary
        unreadMessageCount == 0 && unreadMentionCount == 0 ->
            colorResource(id = R.color.text_transparent_secondary)

        // Unread messages (when notifications enabled): show as primary
        else ->
            colorResource(id = R.color.text_primary)
    }
}

@Composable
private fun getUnreadMentionCountBadgeColor(
    notificationMode: NotificationState?
): androidx.compose.ui.graphics.Color {
    return when (notificationMode) {
        NotificationState.ALL -> colorResource(id = R.color.control_accent)
        NotificationState.MENTIONS -> colorResource(id = R.color.control_accent)
        NotificationState.DISABLE -> colorResource(id = R.color.control_transparent_tetriary)
        else -> colorResource(id = R.color.control_accent)
    }
}

@Composable
private fun getUnreadMessageCountBadgeColor(
    notificationMode: NotificationState?
): androidx.compose.ui.graphics.Color {
    return when (notificationMode) {
        NotificationState.ALL -> colorResource(id = R.color.control_accent)
        NotificationState.MENTIONS -> colorResource(id = R.color.control_transparent_tetriary)
        NotificationState.DISABLE -> colorResource(id = R.color.control_transparent_tetriary)
        else -> colorResource(id = R.color.control_accent)
    }
}

@Composable
fun vaultCardBackgroundModifier(
    modifier: Modifier,
    spaceBackground: SpaceBackground,
    fixedHeight: Boolean
): Modifier {
    val sizeModifier = if (fixedHeight) {
        modifier.fillMaxWidth().height(96.dp)
    } else {
        modifier.fillMaxWidth().heightIn(min = 56.dp)
    }
    return when (spaceBackground) {
        is SpaceBackground.SolidColor -> sizeModifier
            .padding(horizontal = 16.dp)
            .background(
                color = spaceBackground.color.copy(alpha = 0.3f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 16.dp, vertical = if (fixedHeight) 0.dp else 16.dp)

        is SpaceBackground.Gradient -> sizeModifier
            .padding(horizontal = 16.dp)
            .background(
                brush = spaceBackground.brush,
                shape = RoundedCornerShape(20.dp),
                alpha = 0.3f
            )
            .padding(horizontal = 16.dp, vertical = if (fixedHeight) 0.dp else 16.dp)

        SpaceBackground.None -> sizeModifier
            .padding(horizontal = 16.dp)
            .background(
                color = colorResource(id = R.color.background_secondary),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 16.dp, vertical = if (fixedHeight) 0.dp else 16.dp)
    }
}

const val COMPACT_CHAT_NAMES_VISIBLE_COUNT = 3

@Composable
fun UnreadIndicatorsRow(
    unreadMessageCount: Int,
    unreadMentionCount: Int,
    notificationMode: NotificationState?,
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
                        color = getUnreadMentionCountBadgeColor(
                            notificationMode = notificationMode
                        ),
                        shape = CircleShape
                    )
                    .size(18.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_chat_widget_mention),
                    contentDescription = "Mentions icon"
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
        }

        if (unreadMessageCount > 0) {
            val (shape, boxModifier) = if (unreadMessageCount <= MENTION_COUNT_THRESHOLD) {
                CircleShape to Modifier.size(18.dp)
            } else {
                RoundedCornerShape(100.dp) to Modifier
                    .padding(horizontal = 4.dp, vertical = 2.2.dp)
            }
            Box(
                modifier = Modifier
                    .background(
                        color = getUnreadMessageCountBadgeColor(
                            notificationMode = notificationMode
                        ),
                        shape = shape
                    )
                    .then(boxModifier),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = unreadMessageCount.toString(),
                    style = Caption2Regular,
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
    messageTime: String?,
    isMuted: Boolean?,
    showPendingIndicator: Boolean = false
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
                // 1: optional muted icon (only if isMuted == true)
                if (isMuted == true) {
                    Image(
                        painter = painterResource(R.drawable.ic_chat_muted_18),
                        contentDescription = stringResource(R.string.content_desc_muted),
                        modifier = Modifier.size(18.dp),
                        colorFilter = ColorFilter.tint(colorResource(R.color.control_transparent_secondary))
                    )
                }
                // 2: optional pending indicator (only if showPendingIndicator == true)
                if (showPendingIndicator) {
                    Image(
                        painter = painterResource(R.drawable.ic_chat_msg_not_synced),
                        contentDescription = stringResource(R.string.content_desc_pending_sync),
                        modifier = Modifier.size(12.dp),
                        colorFilter = ColorFilter.tint(colorResource(R.color.text_primary))
                    )
                }
                // 3: optional time (only if messageTime != null)
                messageTime?.let {
                    Text(
                        text = it,
                        style = Relations2,
                        color = colorResource(id = R.color.text_transparent_secondary),
                    )
                }
            }
        ) { measurables, constraints ->
            // spacing constants in px
            val iconTextGap = with(density) { 4.dp.roundToPx() }
            val textTimeGap = with(density) { 8.dp.roundToPx() }
            val pendingTimeGap = with(density) { 4.dp.roundToPx() }

            // measurables indices depend on what's present:
            // 0 = text (always)
            // 1 = muted icon (if isMuted == true)
            // next = pending indicator (if showPendingIndicator == true)
            // last = time (if messageTime != null)

            // Track current index for optional elements
            var currentIndex = 1

            // 1) Measure time first (if any) - always at the end
            val timePlaceable = if (messageTime != null) {
                measurables.last().measure(
                    constraints.copy(minWidth = 0, minHeight = 0)
                )
            } else null

            // 2) Measure muted icon (if present)
            val mutedIconPlaceable = if (isMuted == true) {
                val placeable = measurables[currentIndex].measure(
                    constraints.copy(minWidth = 0, minHeight = 0)
                )
                currentIndex++
                placeable
            } else null

            // 3) Measure pending indicator (if present)
            val pendingPlaceable = if (showPendingIndicator) {
                val placeable = measurables[currentIndex].measure(
                    constraints.copy(minWidth = 0, minHeight = 0)
                )
                currentIndex++
                placeable
            } else null

            // 4) Compute reserved width:
            //    time width + gap before time (if time exists)
            //  + pending width + gap before pending (if pending exists)
            //  + muted icon width + gap before icon (if icon exists)
            val reserved = listOfNotNull(
                mutedIconPlaceable?.width,
                pendingPlaceable?.width,
                timePlaceable?.width
            ).sum() +
            (if (mutedIconPlaceable != null) iconTextGap else 0) +
            (if (pendingPlaceable != null && timePlaceable != null) pendingTimeGap else 0) +
            (if (timePlaceable != null || pendingPlaceable != null) textTimeGap else 0)

            // 5) Measure text with remaining width
            val maxTextWidth = (constraints.maxWidth - reserved).coerceAtLeast(0)
            val textPlaceable = measurables.getOrNull(0)?.measure(
                constraints.copy(
                    maxWidth = maxTextWidth,
                    minWidth = 0, minHeight = 0
                )
            )

            // 6) Determine row height
            val rowHeight = listOfNotNull(
                textPlaceable?.height,
                mutedIconPlaceable?.height,
                pendingPlaceable?.height,
                timePlaceable?.height
            ).maxOrNull() ?: 0

            // 7) Layout & place children
            layout(constraints.maxWidth, rowHeight) {
                var xOffset = 0

                // Place text
                val textY = (rowHeight - (textPlaceable?.height ?: 0)) / 2
                textPlaceable?.placeRelative(x = xOffset, y = textY)
                xOffset += (textPlaceable?.width ?: 0)

                // Place muted icon after text (if present)
                mutedIconPlaceable?.let { icon ->
                    xOffset += iconTextGap
                    val iconY = (rowHeight - icon.height) / 2
                    icon.placeRelative(x = xOffset, y = iconY)
                    xOffset += icon.width
                }

                // Place pending indicator and time at the end (right-aligned)
                var endX = constraints.maxWidth

                // Place time at the far end
                timePlaceable?.let { time ->
                    endX -= time.width
                    val timeY = (rowHeight - time.height) / 2
                    time.placeRelative(x = endX, y = timeY)
                }

                // Place pending indicator left of time
                pendingPlaceable?.let { pending ->
                    if (timePlaceable != null) {
                        endX -= pendingTimeGap
                    }
                    endX -= pending.width
                    val pendingY = (rowHeight - pending.height) / 2
                    pending.placeRelative(x = endX, y = pendingY)
                }
            }
        }
    }
}

@Composable
fun buildChatContentWithInlineIcons(
    creatorName: String?,
    messageText: String?,
    attachmentPreviews: List<AttachmentPreview>,
    fallbackSubtitle: String,
    singleLineFormat: Boolean = false,
    textColor: androidx.compose.ui.graphics.Color = colorResource(id = R.color.text_transparent_secondary),
    mediumStyle: androidx.compose.ui.text.SpanStyle = CodeChatPreviewMedium.toSpanStyle().copy(color = textColor),
    regularStyle: androidx.compose.ui.text.SpanStyle = CodeChatPreviewRegular.toSpanStyle().copy(color = textColor)
): Pair<AnnotatedString, Map<String, InlineTextContent>> {

    val attachmentCount = attachmentPreviews.size
    val imageCount = attachmentPreviews.count { it.type == AttachmentType.IMAGE }
    val fileCount = attachmentPreviews.count { it.type == AttachmentType.FILE }
    val linkCount = attachmentPreviews.count { it.type == AttachmentType.LINK }

    val inlineContentMap = mutableMapOf<String, InlineTextContent>()

    val text = buildAnnotatedString {
        // Add creator name if available (truncate if too long)
        if (creatorName != null) {
            val truncatedCreatorName = if (creatorName.length > 30) {
                creatorName.take(27) + "..."
            } else {
                creatorName
            }
            withStyle(style = regularStyle) {
                if (singleLineFormat) {
                    append("$truncatedCreatorName: ")
                } else {
                    append("$truncatedCreatorName\n")
                }
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
                                withStyle(style = mediumStyle) {
                                    append(stringResource(R.string.image))
                                }
                            }

                            fileCount == 1 -> {
                                withStyle(style = mediumStyle) {
                                    append(stringResource(R.string.file))
                                }
                            }

                            linkCount == 1 -> {
                                val linkTitle =
                                    attachmentPreviews.find { it.type == AttachmentType.LINK }?.title
                                        ?: stringResource(R.string.objects)
                                withStyle(style = mediumStyle) {
                                    append(linkTitle)
                                }
                            }

                            else -> {
                                // No attachments, no message, show fallback
                                withStyle(style = regularStyle) {
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
                                    attachmentPreviews.find { it.type == AttachmentType.LINK }?.title
                                        ?: stringResource(R.string.object_1)
                                withStyle(style = mediumStyle) {
                                    append(linkTitle)
                                    append(" ")
                                }
                                withStyle(style = regularStyle) {
                                    append(messageText)
                                }
                            }

                            else -> {
                                // For files/images: just show messageText
                                withStyle(style = regularStyle) {
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
                                withStyle(style = mediumStyle) {
                                    append("$imageCount ${stringResource(R.string.images)}")
                                }
                            }

                            fileCount > 0 && imageCount == 0 && linkCount == 0 -> {
                                // Files only
                                withStyle(style = mediumStyle) {
                                    append("$fileCount ${stringResource(R.string.files)}")
                                }
                            }

                            linkCount > 0 && imageCount == 0 && fileCount == 0 -> {
                                // Objects only
                                withStyle(style = mediumStyle) {
                                    append("$linkCount ${stringResource(R.string.objects)}")
                                }
                            }

                            else -> {
                                // Mixed types
                                withStyle(style = mediumStyle) {
                                    append("$attachmentCount ${stringResource(R.string.attachments)}")
                                }
                            }
                        }
                    }

                    else -> {
                        // Multiple attachments, with message text - just show message text
                        withStyle(style = regularStyle) {
                            append(messageText)
                        }
                    }
                }
            }
        }
    }

    return Pair(text, inlineContentMap)
}
