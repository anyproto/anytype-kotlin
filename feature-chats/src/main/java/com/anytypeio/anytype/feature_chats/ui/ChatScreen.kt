package com.anytypeio.anytype.feature_chats.ui

import android.content.res.Configuration
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.foundation.AlertConfig
import com.anytypeio.anytype.core_ui.foundation.AlertIcon
import com.anytypeio.anytype.core_ui.foundation.GRADIENT_TYPE_BLUE
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Regular
import com.anytypeio.anytype.core_ui.views.Relations2
import com.anytypeio.anytype.core_utils.common.DefaultFileInfo
import com.anytypeio.anytype.core_utils.ext.parseImagePath
import com.anytypeio.anytype.feature_chats.R
import com.anytypeio.anytype.feature_chats.presentation.ChatView
import com.anytypeio.anytype.feature_chats.presentation.ChatViewModel
import com.anytypeio.anytype.feature_chats.presentation.ChatViewModel.ChatBoxMode
import com.anytypeio.anytype.feature_chats.presentation.ChatViewModel.UXCommand
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreenWrapper(
    isSpaceLevelChat: Boolean = false,
    vm: ChatViewModel,
    // TODO move to view model
    onAttachObjectClicked: () -> Unit,
    onBackButtonClicked: () -> Unit,
    onMarkupLinkClicked: (String) -> Unit,
    onRequestOpenFullScreenImage: (String) -> Unit,
    onSelectChatReaction: (String) -> Unit,
    onViewChatReaction: (Id, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var showReactionSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current
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

                ChatScreen(
                    chatBoxMode = vm.chatBoxMode.collectAsState().value,
                    isSpaceLevelChat = isSpaceLevelChat,
                    title = vm.name.collectAsState().value,
                    messages = vm.messages.collectAsState().value,
                    attachments = vm.chatBoxAttachments.collectAsState().value,
                    onMessageSent = vm::onMessageSent,
                    onTitleChanged = vm::onTitleChanged,
                    onAttachClicked = onAttachObjectClicked,
                    onClearAttachmentClicked = vm::onClearAttachmentClicked,
                    lazyListState = lazyListState,
                    onReacted = vm::onReacted,
                    onCopyMessage = { msg ->
                        clipboard.setText(AnnotatedString(text = msg.content.msg))
                    },
                    onDeleteMessage = vm::onDeleteMessage,
                    onEditMessage = vm::onRequestEditMessageClicked,
                    onAttachmentClicked = vm::onAttachmentClicked,
                    isInEditMessageMode = vm.chatBoxMode.collectAsState().value is ChatBoxMode.EditMessage,
                    onExitEditMessageMode = vm::onExitEditMessageMode,
                    onBackButtonClicked = onBackButtonClicked,
                    onMarkupLinkClicked = onMarkupLinkClicked,
                    onAttachObjectClicked = onAttachObjectClicked,
                    onAttachMediaClicked = {

                    },
                    onAttachFileClicked = {

                    },
                    onUploadAttachmentClicked = {

                    },
                    onReplyMessage = vm::onReplyMessage,
                    onClearReplyClicked = vm::onClearReplyClicked,
                    onChatBoxMediaPicked = { uris ->
                        vm.onChatBoxMediaPicked(uris.map { it.parseImagePath(context = context) })
                    },
                    onChatBoxFilePicked = { uris ->
                        val infos = uris.mapNotNull { uri ->
                            val cursor = context.contentResolver.query(
                                uri,
                                null,
                                null,
                                null,
                                null
                            )
                            if (cursor != null) {
                                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                                cursor.moveToFirst()
                                DefaultFileInfo(
                                    uri = uri.toString(),
                                    name = cursor.getString(nameIndex),
                                    size = cursor.getLong(sizeIndex).toInt()
                                )
                            } else {
                                null
                            }
                        }
                        vm.onChatBoxFilePicked(infos)
                    },
                    onAddReactionClicked = onSelectChatReaction,
                    onViewChatReaction = onViewChatReaction
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
                            is UXCommand.OpenFullScreenImage -> {
                                onRequestOpenFullScreenImage(command.url)
                            }
                        }
                    }
                }
            }
        }
    }
    if (showReactionSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showReactionSheet = false
            },
            sheetState = sheetState,
            containerColor = colorResource(id = R.color.background_secondary),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            dragHandle = null
        ) {
            SelectChatReactionScreen(
                onEmojiClicked = {}
            )
        }
    }
}

/**
 * TODO: do date formating before rendering?
 */
@Composable
fun ChatScreen(
    chatBoxMode: ChatBoxMode,
    isSpaceLevelChat: Boolean,
    isInEditMessageMode: Boolean = false,
    lazyListState: LazyListState,
    title: String?,
    messages: List<ChatView>,
    attachments: List<ChatView.Message.ChatBoxAttachment>,
    onMessageSent: (String) -> Unit,
    onTitleChanged: (String) -> Unit,
    onAttachClicked: () -> Unit,
    onBackButtonClicked: () -> Unit,
    onClearAttachmentClicked: (ChatView.Message.ChatBoxAttachment) -> Unit,
    onClearReplyClicked: () -> Unit,
    onReacted: (Id, String) -> Unit,
    onDeleteMessage: (ChatView.Message) -> Unit,
    onCopyMessage: (ChatView.Message) -> Unit,
    onEditMessage: (ChatView.Message) -> Unit,
    onReplyMessage: (ChatView.Message) -> Unit,
    onAttachmentClicked: (ChatView.Message.Attachment) -> Unit,
    onExitEditMessageMode: () -> Unit,
    onMarkupLinkClicked: (String) -> Unit,
    onAttachObjectClicked: () -> Unit,
    onAttachMediaClicked: () -> Unit,
    onAttachFileClicked: () -> Unit,
    onUploadAttachmentClicked: () -> Unit,
    onChatBoxMediaPicked: (List<Uri>) -> Unit,
    onChatBoxFilePicked: (List<Uri>) -> Unit,
    onAddReactionClicked: (String) -> Unit,
    onViewChatReaction: (Id, String) -> Unit
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


    // Scrolling to bottom when list size changes and we are at the bottom of the list
    LaunchedEffect(messages.size) {
        if (lazyListState.firstVisibleItemScrollOffset == 0) {
            scope.launch {
                lazyListState.animateScrollToItem(0)
            }
        }
    }

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
                            msg.content.msg,
                            selection = TextRange(msg.content.msg.length)
                        )
                        chatBoxFocusRequester.requestFocus()
                    }
                },
                onReplyMessage = {
                    onReplyMessage(it)
                    chatBoxFocusRequester.requestFocus()
                },
                onMarkupLinkClicked = onMarkupLinkClicked,
                onAddReactionClicked = onAddReactionClicked,
                onViewChatReaction = onViewChatReaction
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
        ChatBox(
            mode = chatBoxMode,
            modifier = Modifier
                .imePadding()
                .navigationBarsPadding(),
            chatBoxFocusRequester = chatBoxFocusRequester,
            textState = textState,
            onMessageSent = onMessageSent,
            onAttachClicked = onAttachClicked,
            resetScroll = {
                if (lazyListState.firstVisibleItemScrollOffset > 0) {
                    scope.launch {
                        lazyListState.animateScrollToItem(index = 0)
                    }
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
            onBackButtonClicked = onBackButtonClicked,
            onAttachFileClicked = onAttachFileClicked,
            onAttachMediaClicked = onAttachMediaClicked,
            onUploadAttachmentClicked = onUploadAttachmentClicked,
            onAttachObjectClicked = onAttachObjectClicked,
            onClearAttachmentClicked = onClearAttachmentClicked,
            onClearReplyClicked = onClearReplyClicked,
            onChatBoxMediaPicked = onChatBoxMediaPicked,
            onChatBoxFilePicked = onChatBoxFilePicked,
            onExitEditMessageMode = {
                onExitEditMessageMode().also {
                    textState = TextFieldValue()
                }
            }
        )
    }
}

@Composable
fun Messages(
    isSpaceLevelChat: Boolean = true,
    title: String?,
    onTitleChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    messages: List<ChatView>,
    scrollState: LazyListState,
    onTitleFocusChanged: (Boolean) -> Unit,
    onReacted: (Id, String) -> Unit,
    onDeleteMessage: (ChatView.Message) -> Unit,
    onCopyMessage: (ChatView.Message) -> Unit,
    onAttachmentClicked: (ChatView.Message.Attachment) -> Unit,
    onEditMessage: (ChatView.Message) -> Unit,
    onReplyMessage: (ChatView.Message) -> Unit,
    onMarkupLinkClicked: (String) -> Unit,
    onAddReactionClicked: (String) -> Unit,
    onViewChatReaction: (Id, String) -> Unit
) {
    val scope = rememberCoroutineScope()
    LazyColumn(
        modifier = modifier,
        reverseLayout = true,
        state = scrollState,
    ) {
        itemsIndexed(
            messages,
            key = { _, msg ->
                when(msg) {
                    is ChatView.DateSection -> msg.timeInMillis
                    is ChatView.Message -> msg.id
                }
            }
        ) { idx, msg ->
            if (msg is ChatView.Message) {
                if (idx == 0)
                    Spacer(modifier = Modifier.height(36.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .animateItem(),
                    horizontalArrangement = if (msg.isUserAuthor)
                        Arrangement.End
                    else
                        Arrangement.Start
                ) {
                    if (!msg.isUserAuthor) {
                        ChatUserAvatar(
                            msg = msg,
                            avatar = msg.avatar,
                            modifier = Modifier.align(Alignment.Bottom)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Bubble(
                        modifier = Modifier.padding(
                            start = if (msg.isUserAuthor) 32.dp else 0.dp,
                            end = if (msg.isUserAuthor) 0.dp else 32.dp
                        ),
                        name = msg.author,
                        content = msg.content,
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
                        },
                        onMarkupLinkClicked = onMarkupLinkClicked,
                        onReply = {
                            onReplyMessage(msg)
                        },
                        reply = msg.reply,
                        onScrollToReplyClicked = { reply ->
                            // Naive implementation
                            val idx = messages.indexOfFirst { it is ChatView.Message && it.id == reply.msg }
                            if (idx != -1) {
                                scope.launch {
                                    scrollState.animateScrollToItem(index = idx)
                                }
                            }
                        },
                        onAddReactionClicked = {
                            onAddReactionClicked(msg.id)
                        },
                        onViewChatReaction = { emoji ->
                            onViewChatReaction(msg.id, emoji)
                        }
                    )
                }
                if (idx == messages.lastIndex) {
                    Spacer(modifier = Modifier.height(36.dp))
                }
            } else if (msg is ChatView.DateSection) {
                Text(
                    text = msg.formattedDate,
                    style = Caption1Medium,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    textAlign = TextAlign.Center,
                    color = colorResource(R.color.transparent_active)
                )
            }
        }
        if (messages.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillParentMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
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

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Light Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Dark Mode")
@Composable
fun TopDiscussionToolbarPreview() {
    TopDiscussionToolbar()
}

private const val HEADER_KEY = "key.discussions.item.header"
private val JumpToBottomThreshold = 200.dp