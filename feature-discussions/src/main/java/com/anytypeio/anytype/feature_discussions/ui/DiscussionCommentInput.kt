package com.anytypeio.anytype.feature_discussions.ui

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.DropdownMenu
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.text.AnnotatedTextTransformation
import com.anytypeio.anytype.core_ui.text.InputSpan
import com.anytypeio.anytype.core_ui.text.MarkupEvent
import com.anytypeio.anytype.core_ui.text.normalizeSpans
import com.anytypeio.anytype.core_ui.text.toggleSpan
import com.anytypeio.anytype.core_ui.views.PreviewTitle1Regular
import com.anytypeio.anytype.feature_discussions.presentation.CommentAttachment
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionViewModel
import com.anytypeio.anytype.feature_discussions.tools.launchCamera
import com.anytypeio.anytype.feature_discussions.tools.launchVideoRecorder
import com.anytypeio.anytype.presentation.confgs.ChatConfig
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun DiscussionCommentInput(
    text: TextFieldValue,
    spans: List<InputSpan>,
    attachments: List<CommentAttachment>,
    onValueChange: (TextFieldValue, List<InputSpan>) -> Unit,
    onSendClicked: (String, List<InputSpan>) -> Unit,
    onMediaPicked: (List<DiscussionViewModel.MediaUri>) -> Unit,
    onFilePicked: (List<Uri>) -> Unit,
    onClearAttachment: (CommentAttachment) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isFocused by remember { mutableStateOf(false) }
    var showMarkup by remember { mutableStateOf(false) }
    var showDropdownMenu by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // LAUNCHERS

    val uploadMediaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = ChatConfig.MAX_ATTACHMENT_COUNT)
    ) { uris ->
        onMediaPicked(uris.map { uri ->
            DiscussionViewModel.MediaUri(uri = uri.toString())
        })
    }

    val uploadFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        onFilePicked(uris.take(ChatConfig.MAX_ATTACHMENT_COUNT))
    }

    var capturedImageUri by rememberSaveable { mutableStateOf<String?>(null) }
    var capturedVideoUri by rememberSaveable { mutableStateOf<String?>(null) }

    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess && capturedImageUri != null) {
            onMediaPicked(
                listOf(
                    DiscussionViewModel.MediaUri(
                        uri = capturedImageUri!!,
                        capturedByCamera = true
                    )
                )
            )
            capturedImageUri = null
        } else {
            Timber.w("Failed to capture image in discussion")
        }
    }

    val recordVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { isSuccess ->
        if (isSuccess && capturedVideoUri != null) {
            onMediaPicked(
                listOf(
                    DiscussionViewModel.MediaUri(
                        uri = capturedVideoUri!!,
                        isVideo = true,
                        capturedByCamera = true
                    )
                )
            )
            capturedVideoUri = null
        } else {
            Timber.w("Failed to capture video in discussion")
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
        }
    }

    // END OF LAUNCHERS

    Column(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .padding(
                start = 12.dp,
                end = 12.dp,
                bottom = 10.dp
            )
            .border(
                width = 1.dp,
                color = colorResource(R.color.shape_tertiary),
                shape = RoundedCornerShape(16.dp)
            )
            .background(
                color = colorResource(R.color.background_primary),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        // Attachment preview
        if (attachments.isNotEmpty()) {
            DiscussionCommentAttachments(
                attachments = attachments,
                onClearAttachmentClicked = onClearAttachment
            )
        }

        // Main input row: [plus (if !focused)] + [text input] + [send (if !focused)]
        Row {
            if (!isFocused) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Bottom)
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                        .clip(CircleShape)
                        .clickable {
                            showDropdownMenu = true
                        }
                ) {
                    Image(
                        painter = painterResource(
                            id = com.anytypeio.anytype.feature_discussions.R.drawable.ic_discussion_plus
                        ),
                        contentDescription = "Plus button",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
                    )
                    if (attachments.size < ChatConfig.MAX_ATTACHMENT_COUNT) {
                        AttachmentDropdownMenu(
                            expanded = showDropdownMenu,
                            onDismiss = { showDropdownMenu = false },
                            onTakePhoto = {
                                showDropdownMenu = false
                                takePhotoPermissionLauncher.launch(Manifest.permission.CAMERA)
                            },
                            onRecordVideo = {
                                showDropdownMenu = false
                                recordVideoPermissionLauncher.launch(Manifest.permission.CAMERA)
                            },
                            onUploadMedia = {
                                showDropdownMenu = false
                                uploadMediaLauncher.launch(
                                    PickVisualMediaRequest(
                                        mediaType = ActivityResultContracts.PickVisualMedia.ImageAndVideo
                                    )
                                )
                            },
                            onUploadFile = {
                                showDropdownMenu = false
                                uploadFileLauncher.launch(arrayOf("*/*"))
                            }
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.Bottom)
            ) {
                CommentUserInput(
                    text = text,
                    spans = spans,
                    onValueChange = { newValue ->
                        val updatedSpans = normalizeSpans(
                            oldText = text.text,
                            newText = newValue.text,
                            spans = spans
                        )
                        onValueChange(newValue, updatedSpans)
                    },
                    onFocusChanged = {
                        isFocused = it
                        if (it) showDropdownMenu = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = if (!isFocused) 4.dp else 12.dp,
                            end = 4.dp,
                            top = 16.dp,
                            bottom = 16.dp
                        )
                )
            }
            AnimatedVisibility(
                visible = (text.text.isNotEmpty() || attachments.isNotEmpty()) && !isFocused,
                exit = fadeOut() + scaleOut(),
                enter = fadeIn() + scaleIn(),
                modifier = Modifier.align(Alignment.Bottom)
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                        .clip(CircleShape)
                        .clickable {
                            onSendClicked(text.text, spans)
                            showMarkup = false
                        }
                ) {
                    Image(
                        painter = painterResource(
                            id = com.anytypeio.anytype.feature_discussions.R.drawable.ic_send_message
                        ),
                        contentDescription = "Send comment",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        if (!isFocused) return@Column

        // Bottom toolbar row (when focused): [edit panel / markup panel] + [send button]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            AnimatedContent(
                targetState = showMarkup,
                label = "DISCUSSION_PANELS",
                modifier = Modifier.weight(1.0f)
            ) { isMarkup ->
                if (isMarkup) {
                    DiscussionMarkupPanel(
                        selectionStart = text.selection.start,
                        selectionEnd = text.selection.end,
                        spans = spans,
                        onBackClicked = { showMarkup = false },
                        onMarkupEvent = { event ->
                            scope.launch {
                                val selection = text.selection
                                if (selection.start == selection.end) return@launch

                                val newSpan = when (event) {
                                    MarkupEvent.Bold -> InputSpan.Markup(
                                        style = SpanStyle(fontWeight = FontWeight.Bold),
                                        start = selection.start,
                                        end = selection.end,
                                        type = InputSpan.Markup.BOLD
                                    )
                                    MarkupEvent.Italic -> InputSpan.Markup(
                                        style = SpanStyle(fontStyle = FontStyle.Italic),
                                        start = selection.start,
                                        end = selection.end,
                                        type = InputSpan.Markup.ITALIC
                                    )
                                    MarkupEvent.Strike -> InputSpan.Markup(
                                        style = SpanStyle(textDecoration = TextDecoration.LineThrough),
                                        start = selection.start,
                                        end = selection.end,
                                        type = InputSpan.Markup.STRIKETHROUGH
                                    )
                                    MarkupEvent.Underline -> InputSpan.Markup(
                                        style = SpanStyle(textDecoration = TextDecoration.Underline),
                                        start = selection.start,
                                        end = selection.end,
                                        type = InputSpan.Markup.UNDERLINE
                                    )
                                    MarkupEvent.Code -> InputSpan.Markup(
                                        style = SpanStyle(fontFamily = FontFamily.Monospace),
                                        start = selection.start,
                                        end = selection.end,
                                        type = InputSpan.Markup.CODE
                                    )
                                }

                                val updatedSpans = toggleSpan(
                                    selectionStart = selection.start,
                                    selectionEnd = selection.end,
                                    spans = spans,
                                    newSpan = newSpan
                                )
                                onValueChange(text, updatedSpans)
                            }
                        }
                    )
                } else {
                    Box {
                        DiscussionEditPanel(
                            onPlusClicked = { showDropdownMenu = true },
                            onStyleClicked = { showMarkup = true },
                            onMentionClicked = {
                                val selection = text.selection
                                val cursorPosition = selection.start
                                val updatedText = text.text.substring(0, cursorPosition) +
                                        "@" +
                                        text.text.substring(cursorPosition)
                                val newSelection = TextRange(cursorPosition + 1)
                                onValueChange(
                                    TextFieldValue(updatedText, selection = newSelection),
                                    spans
                                )
                            }
                        )
                        if (attachments.size < ChatConfig.MAX_ATTACHMENT_COUNT) {
                            AttachmentDropdownMenu(
                                expanded = showDropdownMenu,
                                onDismiss = { showDropdownMenu = false },
                                onTakePhoto = {
                                    showDropdownMenu = false
                                    takePhotoPermissionLauncher.launch(Manifest.permission.CAMERA)
                                },
                                onRecordVideo = {
                                    showDropdownMenu = false
                                    recordVideoPermissionLauncher.launch(Manifest.permission.CAMERA)
                                },
                                onUploadMedia = {
                                    showDropdownMenu = false
                                    uploadMediaLauncher.launch(
                                        PickVisualMediaRequest(
                                            mediaType = ActivityResultContracts.PickVisualMedia.ImageAndVideo
                                        )
                                    )
                                },
                                onUploadFile = {
                                    showDropdownMenu = false
                                    uploadFileLauncher.launch(arrayOf("*/*"))
                                }
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = text.text.isNotEmpty() || attachments.isNotEmpty(),
                exit = fadeOut() + scaleOut(),
                enter = fadeIn() + scaleIn()
            ) {
                Box(
                    modifier = Modifier
                        .padding(start = 12.dp, end = 12.dp)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .clickable {
                            onSendClicked(text.text, spans)
                            showMarkup = false
                        }
                ) {
                    Image(
                        painter = painterResource(
                            id = com.anytypeio.anytype.feature_discussions.R.drawable.ic_send_message
                        ),
                        contentDescription = "Send comment",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

    }
}

@Composable
private fun AttachmentDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onTakePhoto: () -> Unit,
    onRecordVideo: () -> Unit,
    onUploadMedia: () -> Unit,
    onUploadFile: () -> Unit
) {
    MaterialTheme(
        shapes = MaterialTheme.shapes.copy(
            medium = RoundedCornerShape(12.dp)
        ),
        colors = MaterialTheme.colors.copy(
            surface = colorResource(id = R.color.background_secondary)
        )
    ) {
        DropdownMenu(
            offset = DpOffset(8.dp, 0.dp),
            expanded = expanded,
            onDismissRequest = onDismiss,
            modifier = Modifier.defaultMinSize(minWidth = 252.dp),
            properties = PopupProperties(focusable = false)
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(com.anytypeio.anytype.localization.R.string.chat_box_take_photo),
                        color = colorResource(id = R.color.text_primary)
                    )
                },
                onClick = onTakePhoto
            )
            Divider(paddingStart = 0.dp, paddingEnd = 0.dp)
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(com.anytypeio.anytype.localization.R.string.chat_box_record_video),
                        color = colorResource(id = R.color.text_primary)
                    )
                },
                onClick = onRecordVideo
            )
            Divider(paddingStart = 0.dp, paddingEnd = 0.dp)
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(com.anytypeio.anytype.localization.R.string.chat_attachment_media),
                        color = colorResource(id = R.color.text_primary)
                    )
                },
                onClick = onUploadMedia
            )
            Divider(paddingStart = 0.dp, paddingEnd = 0.dp)
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(com.anytypeio.anytype.localization.R.string.chat_attachment_file),
                        color = colorResource(id = R.color.text_primary)
                    )
                },
                onClick = onUploadFile
            )
        }
    }
}

@Composable
private fun CommentUserInput(
    text: TextFieldValue,
    spans: List<InputSpan>,
    onValueChange: (TextFieldValue) -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = text,
        onValueChange = onValueChange,
        textStyle = PreviewTitle1Regular.copy(
            color = colorResource(id = R.color.text_primary)
        ),
        modifier = modifier
            .onFocusChanged { state ->
                onFocusChanged(state.isFocused)
            },
        cursorBrush = SolidColor(colorResource(id = R.color.palette_system_blue)),
        maxLines = 5,
        singleLine = false,
        decorationBox = @Composable { innerTextField ->
            CommentHintDecorationBox(
                text = text.text,
                hint = stringResource(
                    id = com.anytypeio.anytype.localization.R.string.discussion_leave_comment
                ),
                innerTextField = innerTextField,
                textStyle = PreviewTitle1Regular.copy(
                    color = colorResource(R.color.text_tertiary)
                )
            )
        },
        visualTransformation = AnnotatedTextTransformation(spans),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommentHintDecorationBox(
    text: String,
    hint: String,
    innerTextField: @Composable () -> Unit,
    textStyle: TextStyle
) {
    OutlinedTextFieldDefaults.DecorationBox(
        value = text,
        visualTransformation = VisualTransformation.None,
        innerTextField = innerTextField,
        singleLine = false,
        enabled = true,
        placeholder = {
            Text(
                text = hint,
                color = colorResource(id = R.color.text_tertiary),
                style = textStyle
            )
        },
        interactionSource = remember { MutableInteractionSource() },
        colors = OutlinedTextFieldDefaults.colors(
            disabledBorderColor = Color.Transparent,
            errorBorderColor = Color.Transparent,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        ),
        contentPadding = PaddingValues()
    )
}
