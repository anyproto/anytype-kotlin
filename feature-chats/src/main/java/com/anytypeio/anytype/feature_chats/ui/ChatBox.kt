package com.anytypeio.anytype.feature_chats.ui

import android.Manifest
import android.net.Uri
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.DropdownMenu
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.common.FULL_ALPHA
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.ContentMiscChat
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.feature_chats.R
import com.anytypeio.anytype.feature_chats.presentation.ChatView
import com.anytypeio.anytype.feature_chats.presentation.ChatViewModel.ChatBoxMode
import com.anytypeio.anytype.feature_chats.tools.launchCamera
import com.anytypeio.anytype.feature_chats.tools.launchVideoRecorder
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
    onCreateAndAttachObject: () -> Unit,
    onClearAttachmentClicked: (ChatView.Message.ChatBoxAttachment) -> Unit,
    onClearReplyClicked: () -> Unit,
    onChatBoxMediaPicked: (List<Uri>) -> Unit,
    onChatBoxFilePicked: (List<Uri>) -> Unit,
    onCameraPermissionDenied: () -> Unit = {},
    onExitEditMessageMode: () -> Unit,
    onValueChange: (TextFieldValue, List<ChatBoxSpan>) -> Unit,
    onUrlInserted: (Url) -> Unit,
    onImageCaptured: (Uri) -> Unit,
    onVideoCaptured: (Uri) -> Unit,
    onAttachmentMenuTriggered: () -> Unit,
    ) {

    val context = LocalContext.current

    // LAUNCHERS

    val uploadMediaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = ChatConfig.MAX_ATTACHMENT_COUNT)
    ) {
        onChatBoxMediaPicked(it)
    }

    val uploadFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.size > ChatConfig.MAX_ATTACHMENT_COUNT) {
            context.toast(context.getString(R.string.chats_warning_you_can_upload_only_10_files_at_a_time))
        }
        onChatBoxFilePicked(uris.take(ChatConfig.MAX_ATTACHMENT_COUNT))
    }

    var capturedImageUri by rememberSaveable { mutableStateOf<String?>(null) }
    var capturedVideoUri by rememberSaveable { mutableStateOf<String?>(null) }

    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess && capturedImageUri != null) {
            onImageCaptured(Uri.parse(capturedImageUri))
            capturedImageUri = null
        } else {
            Timber.w("DROID-2966 Failed to capture image")
        }
    }

    val recordVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { isSuccess ->
        if (isSuccess && capturedVideoUri != null) {
            onVideoCaptured(Uri.parse(capturedVideoUri))
            capturedVideoUri = null
        } else {
            Timber.w("DROID-2966 Failed to capture image")
        }
    }

    val takePhotoPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera(
                context = context,
                launcher = takePhotoLauncher,
                onUriReceived = { capturedImageUri = it.toString() }
            )
        } else {
            onCameraPermissionDenied()
        }
    }

    val recordVideoPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchVideoRecorder(
                context = context,
                launcher = recordVideoLauncher,
                onUriReceived = { capturedVideoUri = it.toString() }
            )
        } else {
            onCameraPermissionDenied()
        }
    }

    // END OF LAUNCHERS

    val length = text.text.length

    var showDropdownMenu by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    var isFocused by remember { mutableStateOf(false) }

    var showMarkup by remember { mutableStateOf(false) }

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
                // Do nothing
            }
            is ChatBoxMode.EditMessage -> {
                // Do nothing
            }
            is ChatBoxMode.ReadOnly -> {
                // Do nothing
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
            if (!isFocused) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Bottom)
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                        .clip(CircleShape)
                        .clickable {
                            scope.launch {
                                showDropdownMenu = true
                                onAttachmentMenuTriggered()
                            }
                        }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_chat_box_add_attachment),
                        contentDescription = "Plus button",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
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
                                    ),
                                properties = PopupProperties(focusable = false)
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = stringResource(R.string.chat_attachment_create_object),
                                            color = colorResource(id = R.color.text_primary)
                                        )
                                    },
                                    onClick = {
                                        showDropdownMenu = false
                                        onCreateAndAttachObject()
                                    }
                                )
                                Divider(
                                    paddingStart = 0.dp,
                                    paddingEnd = 0.dp
                                )
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
                                            text = stringResource(R.string.chat_box_take_photo),
                                            color = colorResource(id = R.color.text_primary)
                                        )
                                    },
                                    onClick = {
                                        showDropdownMenu = false
                                        takePhotoPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                )
                                Divider(
                                    paddingStart = 0.dp,
                                    paddingEnd = 0.dp
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = stringResource(R.string.chat_box_record_video),
                                            color = colorResource(id = R.color.text_primary)
                                        )
                                    },
                                    onClick = {
                                        showDropdownMenu = false
                                        recordVideoPermissionLauncher.launch(Manifest.permission.CAMERA)
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
                                            PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageAndVideo)
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
                    onFocusChanged = { isFocused = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(chatBoxFocusRequester)
                        .padding(
                            start = if (!isFocused) 4.dp else 12.dp,
                            end = 4.dp,
                            top = 16.dp,
                            bottom = 16.dp
                        ),
                    onUrlInserted = onUrlInserted
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
                            modifier = Modifier
                                .padding(
                                    horizontal = 8.dp,
                                    vertical = 3.dp
                                )
                                .align(
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
                visible = (attachments.isNotEmpty() || text.text.isNotEmpty()) && !isFocused,
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
                                        // Bypass resetScroll in edit mode because editing a message does not require
                                        // resetting the scroll position, unlike sending a new message.
                                        if (mode !is ChatBoxMode.EditMessage) {
                                            resetScroll()
                                        }
                                        showMarkup = false
                                    }
                            }
                        )
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_send_message),
                        contentDescription = "Send message button",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(start = 4.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
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

        if (!isFocused) return@Column

        // Markup panel

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .animateContentSize()
        ) {
            AnimatedContent(
                targetState = showMarkup,
                label = "PANELS",
                modifier = Modifier.weight(1.0f)
            ) { isMarkup ->
                if (isMarkup) {
                    ChatBoxMarkup(
                        selectionStart = text.selection.start,
                        selectionEnd = text.selection.end,
                        spans = spans,
                        onBackClicked = {
                            showMarkup = false
                        },
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
                } else {
                    ChatBoxEditPanel(
                        onAttachObjectClicked = onAttachObjectClicked,
                        onMentionClicked = {
                            val selection = text.selection
                            val cursorPosition = selection.start
                            val updatedText = text.text.substring(0, cursorPosition) +
                                    "@" +
                                    text.text.substring(cursorPosition)

                            // Update the cursor position after the inserted '@' character
                            val newSelection = TextRange(cursorPosition + 1)

                            // Notify parent with the updated text without any new spans
                            onValueChange(
                                TextFieldValue(updatedText, selection = newSelection),
                                spans // Keep existing spans without adding any
                            )
                        },
                        onStyleClicked = {
                            showMarkup = true
                        },
                        onUploadFileClicked = {
                            uploadFileLauncher.launch(
                                arrayOf("*/*")
                            )
                        },
                        onUploadMediaClicked = {
                            uploadMediaLauncher.launch(
                                PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onTakePhotoClicked = {
                            takePhotoPermissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        onRecordVideoClicked = {
                            recordVideoPermissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        onCreateAndAttachObject = onCreateAndAttachObject,
                        onAttachmentMenuTriggered = onAttachmentMenuTriggered
                    )
                }
            }

            AnimatedVisibility(
                visible = (attachments.isNotEmpty() || text.text.isNotEmpty()),
                exit = fadeOut() + scaleOut(),
                enter = fadeIn() + scaleIn(),
                modifier = Modifier
            ) {
                Box(
                    modifier = Modifier
                        .padding(start = 12.dp, end = 12.dp)
                        .fillMaxHeight()
                        .align(Alignment.CenterVertically)
                        .clip(CircleShape)
                        .then(
                            if (mode.isSendingMessageBlocked || length > ChatConfig.MAX_MESSAGE_CHARACTER_LIMIT) {
                                Modifier
                            } else {
                                Modifier
                                    .clickable {
                                        onMessageSent(text.text, spans)
                                        clearText()
                                        // Bypass resetScroll in edit mode because editing a message does not require
                                        // resetting the scroll position, unlike sending a new message.
                                        if (mode !is ChatBoxMode.EditMessage) {
                                            resetScroll()
                                        }
                                        showMarkup = false
                                    }
                            }
                        )
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_send_message),
                        contentDescription = "Send message button",
                        modifier = Modifier
                            .align(Alignment.Center)
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
    }
}

@Composable
private fun ChatBoxUserInput(
    modifier: Modifier,
    text: TextFieldValue,
    spans: List<ChatBoxSpan>,
    onValueChange: (TextFieldValue, List<ChatBoxSpan>) -> Unit,
    onUrlInserted: (Url) -> Unit,
    onFocusChanged: (Boolean) -> Unit
) {
    BasicTextField(
        value = text,
        onValueChange = { newValue ->

            val newText = newValue.text
            val oldText = text.text // Keep a reference to the current text before updating
            val textLengthDifference = newText.length - oldText.length

            // URL insert detection
            if (textLengthDifference > 0) {
                val prefixLen = newText.commonPrefixWith(oldText).length
                val inserted = newText.substring(prefixLen, prefixLen + textLengthDifference)
                val urlMatcher = Patterns.WEB_URL.matcher(inserted)
                if (urlMatcher.find()) {
                    val url = urlMatcher.group()
                    // Exclude email addresses from URL processing
                    if (!isEmailAddress(url)) {
                        onUrlInserted(url)
                    }
                }
            }

            // SPANS normalization
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
        textStyle = ContentMiscChat.copy(
            color = colorResource(id = R.color.text_primary)
        ),
        modifier = modifier
            .onFocusChanged { state ->
                onFocusChanged(state.isFocused)
            }
        ,
        cursorBrush = SolidColor(colorResource(id = R.color.palette_system_blue)),
        maxLines = 5,
        decorationBox = @Composable { innerTextField ->
            DefaultHintDecorationBox(
                text = text.text,
                hint = stringResource(R.string.write_a_message),
                innerTextField = innerTextField,
                textStyle = ContentMiscChat.copy(color = colorResource(R.color.text_tertiary))
            )
        },
        visualTransformation = AnnotatedTextTransformation(spans),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences
        )
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatBoxMarkup(
    modifier: Modifier = Modifier,
    selectionStart: Int,
    selectionEnd: Int,
    spans: List<ChatBoxSpan> = emptyList(),
    onMarkupEvent: (ChatMarkupEvent) -> Unit = {},
    onBackClicked: () -> Unit
) {
    // Compute which markup spans overlap the selection
    val activeTypes: Set<Int> = remember(spans, selectionStart, selectionEnd) {
        spans
            .filterIsInstance<ChatBoxSpan.Markup>()
            .filter { span ->
                // any overlap of [selectionStart, selectionEnd) with [span.start, span.end)
                span.start < selectionEnd && selectionStart < span.end
            }
            .map { it.type }
            .toSet()
    }

    val isBold = ChatBoxSpan.Markup.BOLD in activeTypes
    val isItalic = ChatBoxSpan.Markup.ITALIC in activeTypes
    val isStrike = ChatBoxSpan.Markup.STRIKETHROUGH in activeTypes
    val isUnderline = ChatBoxSpan.Markup.UNDERLINE in activeTypes
    val isCode = ChatBoxSpan.Markup.CODE in activeTypes

    CompositionLocalProvider(
        LocalOverscrollConfiguration provides null
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(52.dp)
            ,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Fixed back button
            MarkupIcon(
                onClick = onBackClicked,
                resId = R.drawable.ic_markup_panel_back,
                modifier = Modifier.padding(start = 12.dp, end = 12.dp)
            )

            // Scrollable toolbar
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .fillMaxWidth()
                    .weight(1.0f)
                    .height(52.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MarkupIcon(
                    onClick = { onMarkupEvent(ChatMarkupEvent.Bold) },
                    resId = if (isBold)
                        R.drawable.ic_toolbar_markup_bold_active
                    else
                        R.drawable.ic_toolbar_markup_bold
                )

                MarkupIcon(
                    onClick = { onMarkupEvent(ChatMarkupEvent.Italic) },
                    resId = if (isItalic)
                        R.drawable.ic_toolbar_markup_italic_active
                    else
                        R.drawable.ic_toolbar_markup_italic
                )

                MarkupIcon(
                    onClick = { onMarkupEvent(ChatMarkupEvent.Strike) },
                    resId = if (isStrike)
                        R.drawable.ic_toolbar_markup_strike_through_active
                    else
                        R.drawable.ic_toolbar_markup_strike_through
                )

                MarkupIcon(
                    onClick = { onMarkupEvent(ChatMarkupEvent.Underline) },
                    resId = if (isUnderline)
                        R.drawable.ic_toolbar_markup_underline_active
                    else
                        R.drawable.ic_toolbar_markup_underline
                )

                MarkupIcon(
                    onClick = { onMarkupEvent(ChatMarkupEvent.Code) },
                    resId = if (isCode)
                        R.drawable.ic_toolbar_markup_code_active
                    else
                        R.drawable.ic_toolbar_markup_code
                )
            }
        }
    }
}

@Composable
private fun MarkupIcon(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    @DrawableRes resId: Int
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .noRippleClickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(resId),
            contentDescription = null
        )
    }
}

@Composable
fun ChatBoxEditPanel(
    modifier: Modifier = Modifier,
    onAttachObjectClicked: () -> Unit,
    onStyleClicked: () -> Unit,
    onMentionClicked: () -> Unit,
    onUploadMediaClicked: () -> Unit,
    onUploadFileClicked: () -> Unit,
    onTakePhotoClicked: () -> Unit,
    onRecordVideoClicked: () -> Unit,
    onCreateAndAttachObject: () -> Unit,
    onAttachmentMenuTriggered: () -> Unit,
    ) {

    var showDropdownMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .padding(start = 12.dp)
                .clip(CircleShape)
                .clickable {
                    showDropdownMenu = true
                    onAttachmentMenuTriggered()
                }
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_chat_box_add_attachment),
                contentDescription = "Plus button"
            )
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
                        ),
                    properties = PopupProperties(focusable = false)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(R.string.chat_attachment_create_object),
                                color = colorResource(id = R.color.text_primary)
                            )
                        },
                        onClick = {
                            showDropdownMenu = false
                            onCreateAndAttachObject()
                        }
                    )
                    Divider(
                        paddingStart = 0.dp,
                        paddingEnd = 0.dp
                    )
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
                                text = stringResource(R.string.chat_box_take_photo),
                                color = colorResource(id = R.color.text_primary)
                            )
                        },
                        onClick = {
                            showDropdownMenu = false
                            onTakePhotoClicked()
                        }
                    )
                    Divider(
                        paddingStart = 0.dp,
                        paddingEnd = 0.dp
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(R.string.chat_box_record_video),
                                color = colorResource(id = R.color.text_primary)
                            )
                        },
                        onClick = {
                            showDropdownMenu = false
                            onRecordVideoClicked()
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
                            onUploadMediaClicked()
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
                            onUploadFileClicked()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(20.dp))

        Image(
            painter = painterResource(id = R.drawable.ic_style_32),
            contentDescription = "Plus button",
            modifier = Modifier
                .noRippleClickable {
                    onStyleClicked()
                }
        )

        Spacer(modifier = Modifier.width(20.dp))

        Image(
            painter = painterResource(id = R.drawable.ic_chat_mention),
            contentDescription = "Plus button",
            modifier = Modifier
                .noRippleClickable {
                    onMentionClicked()
                }
        )
    }
}

@Composable
fun ReaderChatBox(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = colorResource(R.color.navigation_panel),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.ic_chatbox_lock),
            contentDescription = "Lock icon"
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            modifier = Modifier.padding(start = 12.dp),
            text = "Only editors can send messages. Contact the owner to request access.",
            style = Caption1Regular,
            color = colorResource(R.color.text_primary)
        )
    }
}

/**
 * Checks if the given string is an email address.
 * This is used to exclude email addresses from URL processing in chat messages.
 */
private fun isEmailAddress(text: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(text).matches()
}

@DefaultPreviews
@Composable
fun ReaderChatBoxPreview() {
    ReaderChatBox()
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
    ChatBoxMarkup(
        spans = emptyList(),
        onBackClicked = {},
        selectionStart = 0,
        selectionEnd = 0
    )
}

@DefaultPreviews
@Composable
fun ChatBoxEditPanelPreview() {
    ChatBoxEditPanel(
        onMentionClicked = {},
        onStyleClicked = {},
        onAttachObjectClicked = {},
        onUploadFileClicked = {},
        onUploadMediaClicked = {},
        onTakePhotoClicked = {},
        onRecordVideoClicked = {},
        onCreateAndAttachObject = {},
        onAttachmentMenuTriggered = {}
    )
}