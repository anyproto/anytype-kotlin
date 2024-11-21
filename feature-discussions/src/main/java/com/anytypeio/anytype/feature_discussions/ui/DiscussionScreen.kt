package com.anytypeio.anytype.feature_discussions.ui

import android.content.res.Configuration
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.DropdownMenu
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.core_ui.foundation.AlertConfig
import com.anytypeio.anytype.core_ui.foundation.AlertIcon
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.GRADIENT_TYPE_BLUE
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyCalloutMedium
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.HeadlineTitle
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Medium
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Regular
import com.anytypeio.anytype.core_ui.views.Relations2
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.core_utils.const.DateConst.TIME_H24
import com.anytypeio.anytype.core_utils.ext.formatTimeInMillis
import com.anytypeio.anytype.feature_discussions.R
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionView
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionViewModel
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionViewModel.ChatBoxMode
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionViewModel.UXCommand
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.search.GlobalSearchItemView
import kotlinx.coroutines.launch


@Composable
fun DiscussionScreenWrapper(
    isSpaceLevelChat: Boolean = false,
    vm: DiscussionViewModel,
    // TODO move to view model
    onAttachClicked: () -> Unit,
    onBackButtonClicked: () -> Unit
) {
    NavHost(
        navController = rememberNavController(),
        startDestination = "discussions"
    ) {
        composable(
            route = "discussions"
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (!isSpaceLevelChat) {
                            Modifier.background(
                                color = colorResource(id = R.color.background_primary)
                            )
                        } else {
                            Modifier
                        }
                    )
            ) {
                val clipboard = LocalClipboardManager.current
                val lazyListState = rememberLazyListState()
                DiscussionScreen(
                    isSpaceLevelChat = isSpaceLevelChat,
                    title = vm.name.collectAsState().value,
                    messages = vm.messages.collectAsState().value,
                    attachments = vm.attachments.collectAsState().value,
                    onMessageSent = vm::onMessageSent,
                    onTitleChanged = vm::onTitleChanged,
                    onAttachClicked = onAttachClicked,
                    onClearAttachmentClicked = vm::onClearAttachmentClicked,
                    lazyListState = lazyListState,
                    onReacted = vm::onReacted,
                    onCopyMessage = { msg ->
                        clipboard.setText(
                            AnnotatedString(text = msg.content)
                        )
                    },
                    onDeleteMessage = vm::onDeleteMessage,
                    onEditMessage = vm::onRequestEditMessageClicked,
                    onAttachmentClicked = vm::onAttachmentClicked,
                    isInEditMessageMode = vm.chatBoxMode.collectAsState().value is ChatBoxMode.EditMessage,
                    onExitEditMessageMode = vm::onExitEditMessageMode,
                    onBackButtonClicked = onBackButtonClicked
                )
                LaunchedEffect(Unit) {
                    vm.commands.collect { command ->
                        when(command) {
                            is UXCommand.JumpToBottom -> {
                                lazyListState.animateScrollToItem(0)
                            }
                            is UXCommand.SetChatBoxInput -> {
                                // TODO
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * TODO: do date formating before rendering?
 */
@Composable
fun DiscussionScreen(
    isSpaceLevelChat: Boolean,
    isInEditMessageMode: Boolean = false,
    lazyListState: LazyListState,
    title: String?,
    messages: List<DiscussionView.Message>,
    attachments: List<GlobalSearchItemView>,
    onMessageSent: (String) -> Unit,
    onTitleChanged: (String) -> Unit,
    onAttachClicked: () -> Unit,
    onBackButtonClicked: () -> Unit,
    onClearAttachmentClicked: () -> Unit,
    onReacted: (Id, String) -> Unit,
    onDeleteMessage: (DiscussionView.Message) -> Unit,
    onCopyMessage: (DiscussionView.Message) -> Unit,
    onEditMessage: (DiscussionView.Message) -> Unit,
    onAttachmentClicked: (Chat.Message.Attachment) -> Unit,
    onExitEditMessageMode: () -> Unit
) {
    var textState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    var isTitleFocused by remember { mutableStateOf(false) }
    val chatBoxFocusRequester = FocusRequester()
    val isHeaderVisible by remember {
        derivedStateOf {
            val layoutInfo = lazyListState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) {
                false
            } else {
                visibleItems.last().key == HEADER_KEY
            }
        }
    }
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (!isSpaceLevelChat) {
            TopDiscussionToolbar(
                title = title,
                isHeaderVisible = isHeaderVisible
            )
        }
        Box(modifier = Modifier.weight(1f)) {
            Messages(
                isSpaceLevelChat = isSpaceLevelChat,
                modifier = Modifier.fillMaxSize(),
                messages = messages,
                scrollState = lazyListState,
                onTitleChanged = onTitleChanged,
                title = title,
                onTitleFocusChanged = {
                    isTitleFocused = it
                },
                onReacted = onReacted,
                onCopyMessage = onCopyMessage,
                onDeleteMessage = onDeleteMessage,
                onAttachmentClicked = onAttachmentClicked,
                onEditMessage = { msg ->
                    onEditMessage(msg).also {
                        textState = TextFieldValue(
                            msg.content,
                            selection = TextRange(msg.content.length)
                        )
                        chatBoxFocusRequester.requestFocus()
                    }
                }
            )
            // Jump to bottom button shows up when user scrolls past a threshold.
            // Convert to pixels:
            val jumpThreshold = with(LocalDensity.current) {
                JumpToBottomThreshold.toPx()
            }

            // Show the button if the first visible item is not the first one or if the offset is
            // greater than the threshold.
            val jumpToBottomButtonEnabled by remember {
                derivedStateOf {
                    lazyListState.firstVisibleItemIndex != 0 ||
                            lazyListState.firstVisibleItemScrollOffset > jumpThreshold
                }
            }

            GoToBottomButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 12.dp),
                onGoToBottomClicked = {
                    scope.launch {
                        lazyListState.animateScrollToItem(index = 0)
                    }
                },
                enabled = jumpToBottomButtonEnabled
            )
        }
        attachments.forEach {
            Box {
                Attachment(
                    modifier = Modifier.padding(
                        top = 12.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                    title = it.title,
                    type = it.type,
                    icon = it.icon,
                    onAttachmentClicked = {
                        // TODO
                    }
                )
                Image(
                    painter = painterResource(id = R.drawable.ic_clear_18),
                    contentDescription = "Close icon",
                    modifier = Modifier
                        .align(
                            Alignment.TopEnd
                        )
                        .padding(
                            top = 6.dp,
                            end = 10.dp
                        )
                        .noRippleClickable {
                            onClearAttachmentClicked()
                        }
                )
            }
        }
        if (isInEditMessageMode) {
            EditMessageToolbar(
                onExitClicked = {
                    onExitEditMessageMode().also {
                        textState = TextFieldValue()
                    }
                }
            )
        }

        ChatBox(
            modifier = Modifier.imePadding().navigationBarsPadding(),
            chatBoxFocusRequester = chatBoxFocusRequester,
            textState = textState,
            onMessageSent = onMessageSent,
            onAttachClicked = onAttachClicked,
            resetScroll = {
                scope.launch {
                    lazyListState.animateScrollToItem(index = 0)
                }
            },
            isTitleFocused = isTitleFocused,
            attachments = attachments,
            updateValue = {
                textState = it
            },
            clearText = {
                textState = TextFieldValue()
            },
            onBackButtonClicked = onBackButtonClicked
        )
    }
}

@Composable
private fun DiscussionTitle(
    title: String?,
    onTitleChanged: (String) -> Unit = {},
    onFocusChanged: (Boolean) -> Unit = {}
) {
    var lastFocusState by remember { mutableStateOf(false) }
    BasicTextField(
        textStyle = HeadlineTitle.copy(
            color = colorResource(id = R.color.text_primary)
        ),
        value = title.orEmpty(),
        onValueChange = {
            onTitleChanged(it)
        },
        modifier = Modifier
            .padding(
                top = 24.dp,
                start = 20.dp,
                end = 20.dp,
                bottom = 8.dp
            )
            .onFocusChanged { state ->
                if (lastFocusState != state.isFocused) {
                    onFocusChanged(state.isFocused)
                }
                lastFocusState = state.isFocused
            }
        ,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done
        ),
        decorationBox = @Composable { innerTextField ->
            DefaultHintDecorationBox(
                hint = stringResource(id = R.string.untitled),
                text = title.orEmpty(),
                innerTextField = innerTextField,
                textStyle = HeadlineTitle
            )
        }
    )
}

@Composable
private fun OldChatBox(
    chatBoxFocusRequester: FocusRequester,
    textState: TextFieldValue,
    onMessageSent: (String) -> Unit = {},
    onAttachClicked: () -> Unit = {},
    resetScroll: () -> Unit = {},
    isTitleFocused: Boolean,
    attachments: List<GlobalSearchItemView>,
    clearText: () -> Unit,
    updateValue: (TextFieldValue) -> Unit
) {

    val scope = rememberCoroutineScope()

    val focus = LocalFocusManager.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 4.dp, vertical = 8.dp)
                .clip(CircleShape)
                .align(Alignment.Bottom)
                .clickable {
                    scope.launch {
                        focus.clearFocus(force = true)
                        onAttachClicked()
                    }
                }
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_plus_32),
                contentDescription = "Plus button",
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            )
        }
        ChatBoxUserInput(
            textState = textState,
            onMessageSent = {
                onMessageSent(it)
                clearText()
                resetScroll()
            },
            onTextChanged = { value ->
                updateValue(value)
            },
            modifier = Modifier
                .weight(1f)
                .align(Alignment.Bottom)
                .focusRequester(chatBoxFocusRequester)
        )
        Box(
            modifier = Modifier
                .padding(horizontal = 4.dp, vertical = 8.dp)
                .clip(CircleShape)
                .align(Alignment.Bottom)
                .clickable {
                    if (textState.text.isNotBlank()) {
                        onMessageSent(textState.text)
                        clearText()
                        resetScroll()
                    }
                }
        ) {
            if (textState.text.isNotBlank()) {
                Image(
                    painter = painterResource(id = R.drawable.ic_send_message),
                    contentDescription = "Send message button",
                    modifier = Modifier
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                        .align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
private fun ChatBox(
    modifier: Modifier = Modifier,
    onBackButtonClicked: () -> Unit,
    chatBoxFocusRequester: FocusRequester,
    textState: TextFieldValue,
    onMessageSent: (String) -> Unit = {},
    onAttachClicked: () -> Unit = {},
    resetScroll: () -> Unit = {},
    isTitleFocused: Boolean,
    attachments: List<GlobalSearchItemView>,
    clearText: () -> Unit,
    updateValue: (TextFieldValue) -> Unit
) {

    val scope = rememberCoroutineScope()

    val focus = LocalFocusManager.current

    Row(
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
        Box(
            modifier = Modifier
                .padding(horizontal = 4.dp, vertical = 8.dp)
                .clip(CircleShape)
                .align(Alignment.Bottom)
                .clickable {
                    scope.launch {
                        focus.clearFocus(force = true)
                        onBackButtonClicked()
                    }
                }
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_nav_panel_back),
                contentDescription = "Back button",
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            )
        }
        ChatBoxUserInput(
            textState = textState,
            onMessageSent = {
                onMessageSent(it)
                clearText()
                resetScroll()
            },
            onTextChanged = { value ->
                updateValue(value)
            },
            modifier = Modifier
                .weight(1f)
                .align(Alignment.Bottom)
                .focusRequester(chatBoxFocusRequester)
        )
        Box(
            modifier = Modifier
                .padding(horizontal = 4.dp, vertical = 8.dp)
                .clip(CircleShape)
                .align(Alignment.Bottom)
                .clickable {
                    scope.launch {
                        focus.clearFocus(force = true)
                        onAttachClicked()
                    }
                }
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_nav_panel_plus),
                contentDescription = "Plus button",
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun EditMessageToolbar(
    onExitClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(40.dp)
            .fillMaxWidth()
            .background(
                color = colorResource(id = R.color.background_highlighted_light)
            )
    ) {
        Text(
            modifier = Modifier
                .padding(
                    start = 12.dp
                )
                .align(
                    Alignment.CenterStart
                ),
            text = stringResource(R.string.chats_edit_message),
            style = Caption1Medium,
            color = colorResource(id = R.color.text_primary)
        )
        Image(
            modifier = Modifier
                .padding(
                    end = 12.dp
                )
                .align(
                    Alignment.CenterEnd
                )
                .noRippleClickable {
                    onExitClicked()
                }
            ,
            painter = painterResource(id = R.drawable.ic_edit_message_close),
            contentDescription = "Close edit-message mode"
        )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable

private fun DefaultHintDecorationBox(
    text: String,
    hint: String,
    innerTextField: @Composable () -> Unit,
    textStyle: TextStyle
) {
    OutlinedTextFieldDefaults.DecorationBox(
        value = text,
        visualTransformation = VisualTransformation.None,
        innerTextField = innerTextField,
        singleLine = true,
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


@Composable
fun Messages(
    isSpaceLevelChat: Boolean = true,
    title: String?,
    onTitleChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    messages: List<DiscussionView.Message>,
    scrollState: LazyListState,
    onTitleFocusChanged: (Boolean) -> Unit,
    onReacted: (Id, String) -> Unit,
    onDeleteMessage: (DiscussionView.Message) -> Unit,
    onCopyMessage: (DiscussionView.Message) -> Unit,
    onAttachmentClicked: (Chat.Message.Attachment) -> Unit,
    onEditMessage: (DiscussionView.Message) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        reverseLayout = true,
        state = scrollState,
    ) {
        itemsIndexed(
            messages,
            key = { _, msg -> msg.id }
        ) { idx, msg ->
            if (idx == 0)
                Spacer(modifier = Modifier.height(36.dp))
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .animateItem()
            ) {
                if (!msg.isUserAuthor) {
                    ChatUserAvatar(
                        msg = msg,
                        avatar = msg.avatar,
                        modifier = Modifier.align(Alignment.Bottom)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                } else {
                    Spacer(modifier = Modifier.width(40.dp))
                }
                Bubble(
                    modifier = Modifier.weight(1.0f),
                    name = msg.author,
                    msg = msg.content,
                    timestamp = msg.timestamp,
                    attachments = msg.attachments,
                    isUserAuthor = msg.isUserAuthor,
                    isEdited = msg.isEdited,
                    onReacted = { emoji ->
                        onReacted(msg.id, emoji)
                    },
                    reactions = msg.reactions,
                    onDeleteMessage = {
                        onDeleteMessage(msg)
                    },
                    onCopyMessage = {
                        onCopyMessage(msg)
                    },
                    onAttachmentClicked = onAttachmentClicked,
                    onEditMessage = {
                        onEditMessage(msg)
                    }
                )
                if (msg.isUserAuthor) {
                    Spacer(modifier = Modifier.width(8.dp))
                    ChatUserAvatar(
                        msg = msg,
                        avatar = msg.avatar,
                        modifier = Modifier.align(Alignment.Bottom)
                    )
                } else {
                    Spacer(modifier = Modifier.width(40.dp))
                }
            }
            if (idx == messages.lastIndex) {
                Spacer(modifier = Modifier.height(36.dp))
            }
        }
        if (messages.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 170.dp)
                ) {
                    AlertIcon(
                        icon = AlertConfig.Icon(
                            gradient = GRADIENT_TYPE_BLUE,
                            icon = R.drawable.ic_alert_message
                        )
                    )
                    Text(
                        text = stringResource(R.string.chat_empty_state_message),
                        style = Caption1Regular,
                        color = colorResource(id = R.color.text_secondary),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 20.dp,
                                end = 20.dp,
                                top = 12.dp
                            )
                    )
                }
            }
        }
        if (!isSpaceLevelChat) {
            item(key = HEADER_KEY) {
                Column {
                    DiscussionTitle(
                        title = title,
                        onTitleChanged = onTitleChanged,
                        onFocusChanged = onTitleFocusChanged
                    )
                    Text(
                        style = Relations2,
                        text = stringResource(R.string.chat),
                        color = colorResource(id = R.color.text_secondary),
                        modifier = Modifier.padding(
                            start = 20.dp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatUserAvatar(
    msg: DiscussionView.Message,
    avatar: DiscussionView.Message.Avatar,
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .background(
                color = colorResource(id = R.color.text_tertiary),
                shape = CircleShape
            )
    ) {
        Text(
            text = msg.author.take(1).uppercase().ifEmpty { stringResource(id = R.string.u) },
            modifier = Modifier.align(Alignment.Center),
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorResource(id = R.color.text_white)
            )
        )
        if (avatar is DiscussionView.Message.Avatar.Image) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(avatar.hash)
                    .crossfade(true)
                    .build(),
                contentDescription = "Space member profile icon",
                modifier = modifier
                    .size(32.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}

val defaultBubbleColor = Color(0x99FFFFFF)
val userMessageBubbleColor = Color(0x66000000)

@Composable
fun Bubble(
    modifier: Modifier = Modifier,
    name: String,
    msg: String,
    timestamp: Long,
    attachments: List<Chat.Message.Attachment> = emptyList(),
    isUserAuthor: Boolean = false,
    isEdited: Boolean = false,
    reactions: List<DiscussionView.Message.Reaction> = emptyList(),
    onReacted: (String) -> Unit,
    onDeleteMessage: () -> Unit,
    onCopyMessage: () -> Unit,
    onEditMessage: () -> Unit,
    onAttachmentClicked: (Chat.Message.Attachment) -> Unit
) {
    var showDropdownMenu by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = if (isUserAuthor)
                    userMessageBubbleColor
                else
                    defaultBubbleColor,
                shape = RoundedCornerShape(24.dp)
            )
            .clip(RoundedCornerShape(24.dp))
            .clickable {
                showDropdownMenu = !showDropdownMenu
            }
    ) {
        Row(
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp
            )
        ) {
            Text(
                text = name,
                style = PreviewTitle2Medium,
                color = if (isUserAuthor)
                    colorResource(id = R.color.text_white)
                else
                    colorResource(id = R.color.text_primary),
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = timestamp.formatTimeInMillis(
                    TIME_H24
                ),
                style = Caption1Regular,
                color = if (isUserAuthor)
                    colorResource(id = R.color.text_white)
                else
                    colorResource(id = R.color.text_secondary),
                maxLines = 1
            )
        }
        if (isEdited) {
            Text(
                modifier = Modifier.padding(
                    top = 0.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 0.dp
                ),
                text = buildAnnotatedString {
                    append(msg)
                    withStyle(
                        style = SpanStyle(
                            color = if (isUserAuthor)
                                colorResource(id = R.color.text_white)
                            else
                                colorResource(id = R.color.text_primary),
                        )
                    ) {
                        append(
                            " (${stringResource(R.string.chats_message_edited)})"
                        )
                    }
                },
                style = BodyRegular,
                color = colorResource(id = R.color.text_primary)
            )
        } else {
            Text(
                modifier = Modifier.padding(
                    top = 0.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 0.dp
                ),
                text = msg,
                style = BodyRegular,
                color = if (isUserAuthor)
                    colorResource(id = R.color.text_white)
                else
                    colorResource(id = R.color.text_primary),
            )
        }
        attachments.forEach { attachment ->
            Attachment(
                modifier = Modifier.padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp
                ),
                title = attachment.target,
                type = attachment.type.toString(),
                icon = ObjectIcon.None,
                onAttachmentClicked = {
                    onAttachmentClicked(attachment)
                }
            )
        }
        if (reactions.isNotEmpty()) {
            ReactionList(
                reactions = reactions,
                onReacted = onReacted
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        MaterialTheme(
            shapes = MaterialTheme.shapes.copy(
                medium = RoundedCornerShape(
                    16.dp
                )
            ),
            colors = MaterialTheme.colors.copy(
                surface = colorResource(id = R.color.background_secondary)
            )
        ) {
            DropdownMenu(
                offset = DpOffset(0.dp, 8.dp),
                expanded = showDropdownMenu,
                onDismissRequest = {
                    showDropdownMenu = false
                }
            ) {
                DropdownMenuItem(
                    text = {
                        Row {
                            Text(
                                text = "\uD83D\uDC4D",
                                modifier = Modifier.noRippleClickable {
                                    onReacted("\uD83D\uDC4D")
                                    showDropdownMenu = false
                                }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "❤\uFE0F",
                                modifier = Modifier.noRippleClickable {
                                    onReacted("❤\uFE0F")
                                    showDropdownMenu = false
                                }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "\uD83D\uDE02",
                                modifier = Modifier.noRippleClickable {
                                    onReacted("\uD83D\uDE02")
                                    showDropdownMenu = false
                                }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "\uD83D\uDE2E",
                                modifier = Modifier.noRippleClickable {
                                    onReacted("\uD83D\uDE2E")
                                    showDropdownMenu = false
                                }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "\uD83D\uDE22",
                                modifier = Modifier.noRippleClickable {
                                    onReacted("\uD83D\uDE22")
                                    showDropdownMenu = false
                                }
                            )
                        }
                    },
                    onClick = {
                        // Do nothing.
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(R.string.copy),
                            color = colorResource(id = R.color.text_primary)
                        )
                    },
                    onClick = {
                        onCopyMessage()
                        showDropdownMenu = false
                    }
                )
                if (isUserAuthor) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(R.string.edit),
                                color = colorResource(id = R.color.text_primary)
                            )
                        },
                        onClick = {
                            onEditMessage()
                            showDropdownMenu = false
                        }
                    )
                }
                if (isUserAuthor) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(id = R.string.delete),
                                color = colorResource(id = R.color.palette_system_red)
                            )
                        },
                        onClick = {
                            onDeleteMessage()
                            showDropdownMenu = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TopDiscussionToolbar(
    title: String? = null,
    isHeaderVisible: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(48.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .align(Alignment.Center)
                    .background(color = Color.Green, shape = CircleShape)
            )
        }
        Text(
            text = if (isHeaderVisible) "" else title ?: stringResource(id = R.string.untitled),
            style = PreviewTitle2Regular,
            color = colorResource(id = R.color.text_primary),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(48.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_toolbar_three_dots),
                contentDescription = "Three dots menu",
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
fun Attachment(
    modifier: Modifier,
    title: String,
    type: String,
    icon: ObjectIcon,
    onAttachmentClicked: () -> Unit
) {
    Box(
        modifier = modifier
            .height(72.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = colorResource(id = R.color.shape_tertiary),
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                color = colorResource(id = R.color.background_secondary)
            )
            .clickable {
                onAttachmentClicked()
            }
    ) {
        ListWidgetObjectIcon(
            icon = icon,
            iconSize = 48.dp,
            modifier = Modifier
                .padding(
                    start = 12.dp
                )
                .align(alignment = Alignment.CenterStart),
            onTaskIconClicked = {
                // Do nothing
            }
        )
        Text(
            text = title,
            modifier = Modifier.padding(
                start = if (icon != ObjectIcon.None)
                    72.dp
                else
                    12.dp,
                top = 17.5.dp,
                end = 12.dp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = PreviewTitle2Medium,
            color = colorResource(id = R.color.text_primary)
        )
        Text(
            text = type,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(
                    start = if (icon != ObjectIcon.None)
                        72.dp
                    else
                        12.dp,
                    bottom = 17.5.dp
                ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = Relations3,
            color = colorResource(id = R.color.text_secondary)
        )
    }
}

@Composable
fun GoToBottomButton(
    enabled: Boolean,
    modifier: Modifier,
    onGoToBottomClicked: () -> Unit
) {
    val transition = updateTransition(
        enabled,
        label = "JumpToBottom visibility animation"
    )
    val bottomOffset by transition.animateDp(label = "JumpToBottom offset animation") {
        if (it) {
            (12).dp
        } else {
            (-12).dp
        }
    }
    if (bottomOffset > 0.dp) {
        Box(
            modifier = modifier
                .offset(x = 0.dp, y = -bottomOffset)
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 1.dp,
                    color = colorResource(id = R.color.shape_primary),
                    shape = RoundedCornerShape(12.dp)
                )
                .background(color = colorResource(id = R.color.background_primary))
                .clickable {
                    onGoToBottomClicked()
                }

        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_go_to_bottom_arrow),
                contentDescription = "Arrow icon",
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReactionList(
    reactions: List<DiscussionView.Message.Reaction>,
    onReacted: (String) -> Unit
) {
    FlowRow(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        reactions.forEach { reaction ->
            Box(
                modifier = Modifier
                    .height(32.dp)
                    .width(60.dp)
                    .background(
                        color = if (reaction.isSelected)
                            colorResource(id = R.color.palette_very_light_orange)
                        else
                            colorResource(id = R.color.background_highlighted),
                        shape = RoundedCornerShape(100.dp)
                    )
                    .clip(RoundedCornerShape(100.dp))
                    .then(
                        if (reaction.isSelected)
                            Modifier.border(
                                width = 1.dp,
                                color = colorResource(id = R.color.palette_system_amber_50),
                                shape = RoundedCornerShape(100.dp)
                            )
                        else
                            Modifier
                    )
                    .clickable {
                        onReacted(reaction.emoji)
                    }
            ) {
                Text(
                    text = reaction.emoji,
                    style = BodyCalloutMedium,
                    modifier = Modifier
                        .align(
                            alignment = Alignment.CenterStart
                        )
                        .padding(
                            start = 8.dp
                        )
                )
                Text(
                    text = reaction.count.toString(),
                    style = BodyCalloutMedium,
                    modifier = Modifier
                        .align(
                            alignment = Alignment.CenterEnd
                        )
                        .padding(
                            end = 12.dp
                        ),
                    color = colorResource(id = R.color.text_primary)
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Light Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Dark Mode")
@Composable
fun ReactionListPreview() {
    ReactionList(
        reactions = listOf(
            DiscussionView.Message.Reaction(
                emoji =  "❤\uFE0F",
                count = 1,
                isSelected = false
            ),
            DiscussionView.Message.Reaction(
                emoji =  "❤\uFE0F",
                count = 1,
                isSelected = true
            ),
            DiscussionView.Message.Reaction(
                emoji =  "❤\uFE0F",
                count = 1,
                isSelected = false
            ),
            DiscussionView.Message.Reaction(
                emoji =  "❤\uFE0F",
                count = 1,
                isSelected = false
            ),
            DiscussionView.Message.Reaction(
                emoji =  "❤\uFE0F",
                count = 1,
                isSelected = false
            ),
            DiscussionView.Message.Reaction(
                emoji =  "❤\uFE0F",
                count = 1,
                isSelected = false
            )
        ),
        onReacted = {}
    )
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Light Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Dark Mode")
@Composable
fun TopDiscussionToolbarPreview() {
    TopDiscussionToolbar()
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Light Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Dark Mode")
@Composable
fun AttachmentPreview() {
    Attachment(
        modifier = Modifier,
        icon = ObjectIcon.None,
        type = "Project",
        title = "Travel to Switzerland",
        onAttachmentClicked = {}
    )
}

private const val HEADER_KEY = "key.discussions.item.header"
private val JumpToBottomThreshold = 200.dp