package com.anytypeio.anytype.feature_chats.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.DropdownMenu
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.common.DEFAULT_DISABLED_ALPHA
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.common.FULL_ALPHA
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.feature_chats.R
import com.anytypeio.anytype.feature_chats.presentation.ChatView
import com.anytypeio.anytype.feature_chats.presentation.ChatViewModel.ChatBoxMode
import com.anytypeio.anytype.presentation.confgs.ChatConfig
import kotlinx.coroutines.launch
import timber.log.Timber


@Composable
fun ChatBox(
    text: TextFieldValue,
    spans: List<ChatBoxSpan>,
    mode: ChatBoxMode = ChatBoxMode.Default(),
    modifier: Modifier = Modifier,
    chatBoxFocusRequester: FocusRequester,
    onMessageSent: (String, List<ChatBoxSpan>) -> Unit,
    resetScroll: () -> Unit = {},
    attachments: List<ChatView.Message.ChatBoxAttachment>,
    clearText: () -> Unit,
    onAttachObjectClicked: () -> Unit,
    onClearAttachmentClicked: (ChatView.Message.ChatBoxAttachment) -> Unit,
    onClearReplyClicked: () -> Unit,
    onChatBoxMediaPicked: (List<Uri>) -> Unit,
    onChatBoxFilePicked: (List<Uri>) -> Unit,
    onExitEditMessageMode: () -> Unit,
    onValueChange: (TextFieldValue, List<ChatBoxSpan>) -> Unit
) {

    val length = text.text.length

    val uploadMediaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = ChatConfig.MAX_ATTACHMENT_COUNT)
    ) {
        onChatBoxMediaPicked(it)
    }

    val uploadFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        onChatBoxFilePicked(uris.take(ChatConfig.MAX_ATTACHMENT_COUNT))
    }

    var showDropdownMenu by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val focus = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .padding(
                start = 12.dp,
                end = 12.dp,
                bottom = 20.dp
            )
            .background(
                color = colorResource(R.color.navigation_panel),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        if (mode is ChatBoxMode.EditMessage) {
            EditMessageToolbar(
                onExitClicked = onExitEditMessageMode
            )
        }
        ChatBoxAttachments(
            attachments = attachments,
            onClearAttachmentClicked = onClearAttachmentClicked
        )
        when(mode) {
            is ChatBoxMode.Default -> {

            }
            is ChatBoxMode.EditMessage -> {

            }
            is ChatBoxMode.Reply -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Text(
                        text = "Reply to ${mode.author}",
                        modifier = Modifier.padding(
                            start = 12.dp,
                            top = 8.dp,
                            end = 44.dp
                        ),
                        style = Caption1Medium,
                        color = colorResource(R.color.text_primary),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = mode.text,
                        modifier = Modifier.padding(
                            start = 12.dp,
                            top = 28.dp,
                            end = 44.dp
                        ),
                        style = Caption1Regular,
                        color = colorResource(R.color.text_primary),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Image(
                        painter = painterResource(R.drawable.ic_chat_close_chat_box_reply),
                        contentDescription = "Clear reply to icon",
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .align(Alignment.CenterEnd)
                            .clickable {
                                onClearReplyClicked()
                            }
                    )
                }
            }
        }
        Row {
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 8.dp)
                    .clip(CircleShape)
                    .align(Alignment.Bottom)
                    .clickable {
                        scope.launch {
                            focus.clearFocus(force = true)
                            showDropdownMenu = true
                        }
                    }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_chat_box_add_attachment),
                    contentDescription = "Plus button",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                )
                if (attachments.size < ChatConfig.MAX_ATTACHMENT_COUNT) {
                    MaterialTheme(
                        shapes = MaterialTheme.shapes.copy(
                            medium = RoundedCornerShape(
                                12.dp
                            )
                        ),
                        colors = MaterialTheme.colors.copy(
                            surface = colorResource(id = R.color.background_secondary)
                        )
                    ) {
                        DropdownMenu(
                            offset = DpOffset(8.dp, 40.dp),
                            expanded = showDropdownMenu,
                            onDismissRequest = {
                                showDropdownMenu = false
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .defaultMinSize(
                                    minWidth = 252.dp
                                )
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(R.string.chat_attachment_object),
                                        color = colorResource(id = R.color.text_primary)
                                    )
                                },
                                onClick = {
                                    showDropdownMenu = false
                                    onAttachObjectClicked()
                                }
                            )
                            Divider(
                                paddingStart = 0.dp,
                                paddingEnd = 0.dp
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(R.string.chat_attachment_media),
                                        color = colorResource(id = R.color.text_primary)
                                    )
                                },
                                onClick = {
                                    showDropdownMenu = false
                                    uploadMediaLauncher.launch(
                                        PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            )
                            Divider(
                                paddingStart = 0.dp,
                                paddingEnd = 0.dp
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(R.string.chat_attachment_file),
                                        color = colorResource(id = R.color.text_primary)
                                    )
                                },
                                onClick = {
                                    showDropdownMenu = false
                                    uploadFileLauncher.launch(
                                        arrayOf("*/*")
                                    )
                                }
                            )
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.Bottom)
            ) {
                ChatBoxUserInput(
                    text = text,
                    spans = spans,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(chatBoxFocusRequester)
                )
                if (length >= ChatConfig.MAX_MESSAGE_CHARACTER_OFFSET_LIMIT) {
                    Box(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .background(
                                color = colorResource(R.color.background_secondary),
                                shape = RoundedCornerShape(100.dp)
                            )
                            .clip(RoundedCornerShape(100.dp))
                            .border(
                                color = colorResource(R.color.shape_tertiary),
                                width = 1.dp,
                                shape = RoundedCornerShape(100.dp)
                            )
                            .align(Alignment.TopCenter)
                    ) {
                        Text(
                            text = "${text.text.length} / ${ChatConfig.MAX_MESSAGE_CHARACTER_LIMIT}",
                            modifier = Modifier.padding(
                                horizontal = 8.dp,
                                vertical = 3.dp
                            ).align(
                                Alignment.Center
                            ),
                            style = Caption1Regular,
                            color = if (length > ChatConfig.MAX_MESSAGE_CHARACTER_LIMIT)
                                colorResource(R.color.palette_system_red)
                            else
                                colorResource(R.color.text_primary)
                        )
                    }
                }
            }
            AnimatedVisibility(
                visible = attachments.isNotEmpty() || text.text.isNotEmpty(),
                exit = fadeOut() + scaleOut(),
                enter = fadeIn() + scaleIn(),
                modifier = Modifier.align(Alignment.Bottom)
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                        .clip(CircleShape)
                        .then(
                            if (mode.isSendingMessageBlocked || length > ChatConfig.MAX_MESSAGE_CHARACTER_LIMIT) {
                                Modifier
                            } else {
                                Modifier
                                    .clickable {
                                        onMessageSent(text.text, spans)
                                        clearText()
                                        resetScroll()
                                    }
                            }
                        )
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_send_message),
                        contentDescription = "Send message button",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                            .alpha(
                                if (mode.isSendingMessageBlocked || length > ChatConfig.MAX_MESSAGE_CHARACTER_LIMIT)
                                    0.3f
                                else
                                    FULL_ALPHA
                            )
                    )
                }
            }
        }
        // Markup panel
        ChatBoxMarkup(
            onMarkupEvent = { event ->
                scope.launch {
                    val selection = text.selection
                    if (selection.start == selection.end) return@launch // No selection, nothing to apply

                    val newSpan = when (event) {
                        ChatMarkupEvent.Bold -> ChatBoxSpan.Markup(
                            style = SpanStyle(fontWeight = FontWeight.Bold),
                            start = selection.start,
                            end = selection.end,
                            type = ChatBoxSpan.Markup.BOLD
                        )
                        ChatMarkupEvent.Italic -> ChatBoxSpan.Markup(
                            style = SpanStyle(fontStyle = FontStyle.Italic),
                            start = selection.start,
                            end = selection.end,
                            type = ChatBoxSpan.Markup.ITALIC
                        )
                        ChatMarkupEvent.Strike -> ChatBoxSpan.Markup(
                            style = SpanStyle(textDecoration = TextDecoration.LineThrough),
                            start = selection.start,
                            end = selection.end,
                            type = ChatBoxSpan.Markup.STRIKETHROUGH
                        )
                        ChatMarkupEvent.Underline -> ChatBoxSpan.Markup(
                            style = SpanStyle(textDecoration = TextDecoration.Underline),
                            start = selection.start,
                            end = selection.end,
                            type = ChatBoxSpan.Markup.UNDERLINE
                        )
                        ChatMarkupEvent.Code -> ChatBoxSpan.Markup(
                            style = SpanStyle(fontFamily = FontFamily.Monospace),
                            start = selection.start,
                            end = selection.end,
                            type = ChatBoxSpan.Markup.CODE
                        )
                    }

                    val updatedSpans = toggleSpan(text, spans, newSpan)
                    onValueChange(text, updatedSpans)
                }
            }
        )
    }
}

@Composable
private fun ChatBoxUserInput(
    modifier: Modifier,
    text: TextFieldValue,
    spans: List<ChatBoxSpan>,
    onValueChange: (TextFieldValue, List<ChatBoxSpan>) -> Unit
) {
    BasicTextField(
        value = text,
        onValueChange = { newValue ->
            val newText = newValue.text
            val oldText = text.text // Keep a reference to the current text before updating
            val textLengthDifference = newText.length - oldText.length

            val updatedSpans = spans.mapNotNull { span ->
                // Detect the common prefix length
                val commonPrefixLength = newText.commonPrefixWith(oldText).length

                // Adjust span ranges based on text changes
                val newStart = when {
                    // Insertion shifts spans after the insertion point
                    textLengthDifference > 0 && commonPrefixLength <= span.start -> span.start + textLengthDifference
                    // Deletion shifts spans after the deletion point
                    textLengthDifference < 0 && commonPrefixLength <= span.start -> span.start + textLengthDifference
                    else -> span.start
                }.coerceAtLeast(0) // Ensure bounds are valid

                val newEnd = when {
                    // Insertion shifts spans after the insertion point
                    textLengthDifference > 0 && commonPrefixLength < span.end -> span.end + textLengthDifference
                    // Deletion shifts spans after the deletion point
                    textLengthDifference < 0 && commonPrefixLength < span.end -> span.end + textLengthDifference
                    else -> span.end
                }.coerceAtLeast(newStart).coerceAtMost(newText.length) // Ensure bounds are valid

                // Log changes for debugging
                Timber.d("Text length: ${newText.length}, Old interval: ${span.start}, ${span.end}, New interval: $newStart, $newEnd")

                // Remove span if the entire range is deleted or invalid
                if (newStart < newEnd && newText.substring(newStart, newEnd).isNotBlank()) {
                    when(span) {
                        is ChatBoxSpan.Mention -> {
                            span.copy(start = newStart, end = newEnd)
                        }
                        is ChatBoxSpan.Markup -> {
                            span.copy(start = newStart, end = newEnd)
                        }
                    }
                } else {
                    Timber.d("Removing span: $span")
                    null // Remove invalid or deleted spans
                }
            }

            // Notify parent with the updated text and spans
            onValueChange(newValue, updatedSpans)

        },
        textStyle = BodyRegular.copy(
            color = colorResource(id = R.color.text_primary)
        ),
        modifier = modifier
            .padding(
                start = 4.dp,
                end = 4.dp,
                top = 16.dp,
                bottom = 16.dp
            )
        ,
        cursorBrush = SolidColor(colorResource(id = R.color.palette_system_blue)),
        maxLines = 5,
        decorationBox = @Composable { innerTextField ->
            DefaultHintDecorationBox(
                text = text.text,
                hint = stringResource(R.string.write_a_message),
                innerTextField = innerTextField,
                textStyle = BodyRegular.copy(color = colorResource(R.color.text_tertiary))
            )
        },
        visualTransformation = AnnotatedTextTransformation(spans)
    )
}

/**
 * Toggles a text span (markup) on the selected text range.
 * If the range already has a conflicting span, it is removed or adjusted.
 *
 * @param text The current TextFieldValue.
 * @param spans The list of existing spans.
 * @param newSpan The new span to be applied (or toggled).
 * @return A new list of spans with the toggled result.
 */
fun toggleSpan(
    text: TextFieldValue,
    spans: List<ChatBoxSpan>,
    newSpan: ChatBoxSpan.Markup
): List<ChatBoxSpan> {
    val selectionStart = minOf(text.selection.start, text.selection.end)
    val selectionEnd = maxOf(text.selection.start, text.selection.end)
    if (selectionStart == selectionEnd) return spans // No selection, nothing to apply

    val updatedSpans = spans.toMutableList()
    val finalSpans = mutableListOf<ChatBoxSpan>()
    var spanToggled = false

    // Process existing spans and toggle where needed
    for (span in updatedSpans) {
        if (span !is ChatBoxSpan.Markup || span.type != newSpan.type) {
            finalSpans.add(span)
            continue
        }

        // Span completely outside the selection - keep it
        if (span.end <= selectionStart || span.start >= selectionEnd) {
            finalSpans.add(span)
            continue
        }

        // Toggle logic:
        spanToggled = true

        // Case 1: Selection fully covers the span - remove it (toggle off)
        if (selectionStart <= span.start && selectionEnd >= span.end) {
            continue // Skip adding this span
        }

        // Case 2: Partial overlap - split or trim
        if (span.start < selectionStart) {
            finalSpans.add(span.copy(end = selectionStart)) // Left part
        }
        if (span.end > selectionEnd) {
            finalSpans.add(span.copy(start = selectionEnd)) // Right part
        }
    }

    // If no span was toggled off, add the new span
    if (!spanToggled) {
        finalSpans.add(newSpan.copy(start = selectionStart, end = selectionEnd))
    }

    // Sort and merge contiguous spans
    return finalSpans
        .sortedBy { it.start }
        .fold(mutableListOf<ChatBoxSpan>()) { acc, span ->
            if (acc.isNotEmpty()) {
                val last = acc.last()
                if (last is ChatBoxSpan.Markup && span is ChatBoxSpan.Markup &&
                    last.type == span.type && last.end == span.start
                ) {
                    // Merge contiguous spans of the same type
                    acc[acc.lastIndex] = last.copy(end = span.end)
                } else {
                    acc.add(span)
                }
            } else {
                acc.add(span)
            }
            acc
        }
}

@Composable
fun ChatBoxMarkup(
    onMarkupEvent: (ChatMarkupEvent) -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(52.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.clickable {
                onMarkupEvent(ChatMarkupEvent.Bold)
            },
            text = "B",
            color = colorResource(R.color.text_primary)
        )
        Text(
            modifier = Modifier.clickable {
                onMarkupEvent(ChatMarkupEvent.Italic)
            },
            text = "I",
            color = colorResource(R.color.text_primary)
        )
        Text(
            modifier = Modifier.clickable {
                onMarkupEvent(ChatMarkupEvent.Strike)
            },
            text = "S",
            color = colorResource(R.color.text_primary)
        )
        Text(
            modifier = Modifier.clickable {
                onMarkupEvent(ChatMarkupEvent.Underline)
            },
            text = "U",
            color = colorResource(R.color.text_primary)
        )
        Text(
            modifier = Modifier.clickable {
                onMarkupEvent(ChatMarkupEvent.Code)
            },
            text = "<>",
            color = colorResource(R.color.text_primary)
        )
    }
}

sealed class ChatMarkupEvent {
    data object Bold : ChatMarkupEvent()
    data object Italic : ChatMarkupEvent()
    data object Strike : ChatMarkupEvent()
    data object Underline: ChatMarkupEvent()
    data object Code: ChatMarkupEvent()
}

@DefaultPreviews
@Composable
fun ChatBoxMarkupPreview() {
    ChatBoxMarkup()
}