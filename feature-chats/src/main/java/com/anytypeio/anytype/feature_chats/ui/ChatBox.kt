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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.feature_chats.R
import com.anytypeio.anytype.feature_chats.presentation.ChatView
import com.anytypeio.anytype.feature_chats.presentation.ChatViewModel.ChatBoxMode
import com.anytypeio.anytype.presentation.confgs.ChatConfig
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import kotlin.collections.forEach
import kotlinx.coroutines.launch

@Composable
fun ChatBox(
    mode: ChatBoxMode = ChatBoxMode.Default,
    modifier: Modifier = Modifier,
    chatBoxFocusRequester: FocusRequester,
    textState: TextFieldValue,
    onMessageSent: (String) -> Unit = {},
    resetScroll: () -> Unit = {},
    attachments: List<ChatView.Message.ChatBoxAttachment>,
    clearText: () -> Unit,
    updateValue: (TextFieldValue) -> Unit,
    onAttachObjectClicked: () -> Unit,
    onClearAttachmentClicked: (ChatView.Message.ChatBoxAttachment) -> Unit,
    onClearReplyClicked: () -> Unit,
    onChatBoxMediaPicked: (List<Uri>) -> Unit,
    onChatBoxFilePicked: (List<Uri>) -> Unit,
    onExitEditMessageMode: () -> Unit
) {

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
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            attachments.forEach { attachment ->
                when(attachment) {
                    is ChatView.Message.ChatBoxAttachment.Link -> {
                        item {
                            Box {
                                AttachedObject(
                                    modifier = Modifier
                                        .padding(
                                            top = 12.dp,
                                            end = 4.dp
                                        )
                                        .width(216.dp),
                                    title = attachment.wrapper.title,
                                    type = attachment.wrapper.type,
                                    icon = attachment.wrapper.icon,
                                    onAttachmentClicked = {
                                        // TODO
                                    }
                                )
                                Image(
                                    painter = painterResource(id = R.drawable.ic_clear_chatbox_attachment),
                                    contentDescription = "Close icon",
                                    modifier = Modifier
                                        .align(
                                            Alignment.TopEnd
                                        )
                                        .padding(top = 6.dp)
                                        .noRippleClickable {
                                            onClearAttachmentClicked(attachment)
                                        }
                                )
                            }
                        }
                    }
                    is ChatView.Message.ChatBoxAttachment.Media -> {
                        item {
                            Box(modifier = Modifier.padding()) {
                                Image(
                                    painter = rememberAsyncImagePainter(attachment.uri),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .padding(
                                            top = 12.dp,
                                            end = 4.dp
                                        )
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(8.dp))

                                    ,
                                    contentScale = ContentScale.Crop
                                )
                                Image(
                                    painter = painterResource(R.drawable.ic_clear_chatbox_attachment),
                                    contentDescription = "Clear attachment icon",
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(top = 6.dp)
                                        .noRippleClickable {
                                            onClearAttachmentClicked(attachment)
                                        }
                                )
                            }
                        }
                    }
                    is ChatView.Message.ChatBoxAttachment.File -> {
                        item {
                            Box {
                                AttachedObject(
                                    modifier = Modifier
                                        .padding(
                                            top = 12.dp,
                                            end = 4.dp
                                        )
                                        .width(216.dp),
                                    title = attachment.name,
                                    type = stringResource(R.string.file),
                                    icon = ObjectIcon.File(
                                        mime = null,
                                        fileName = null
                                    ),
                                    onAttachmentClicked = {
                                        // TODO
                                    }
                                )
                                Image(
                                    painter = painterResource(id = R.drawable.ic_clear_chatbox_attachment),
                                    contentDescription = "Close icon",
                                    modifier = Modifier
                                        .align(
                                            Alignment.TopEnd
                                        )
                                        .padding(top = 6.dp)
                                        .noRippleClickable {
                                            onClearAttachmentClicked(attachment)
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }
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
            ChatBoxUserInput(
                textState = textState,
                onMessageSent = {
                    onMessageSent(it)
                    clearText()
                    if (mode !is ChatBoxMode.EditMessage) {
                        resetScroll()
                    }
                },
                onTextChanged = { value ->
                    updateValue(value)
                },
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.Bottom)
                    .focusRequester(chatBoxFocusRequester)
            )
            AnimatedVisibility(
                visible = attachments.isNotEmpty() || textState.text.isNotEmpty(),
                exit = fadeOut() + scaleOut(),
                enter = fadeIn() + scaleIn(),
                modifier = Modifier.align(Alignment.Bottom)
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                        .clip(CircleShape)
                        .clickable {
                            onMessageSent(textState.text)
                            clearText()
                            resetScroll()
                        }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_send_message),
                        contentDescription = "Send message button",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBoxUserInput(
    modifier: Modifier,
    textState: TextFieldValue,
    onMessageSent: (String) -> Unit,
    onTextChanged: (TextFieldValue) -> Unit,
) {
    BasicTextField(
        value = textState,
        onValueChange = { onTextChanged(it) },
        textStyle = BodyRegular.copy(
            color = colorResource(id = R.color.text_primary)
        ),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Send
        ),
        keyboardActions = KeyboardActions {
            if (textState.text.isNotBlank()) {
                onMessageSent(textState.text)
            }
        },
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
                text = textState.text,
                hint = stringResource(R.string.write_a_message),
                innerTextField = innerTextField,
                textStyle = BodyRegular.copy(color = colorResource(R.color.text_tertiary))
            )
        }
    )
}