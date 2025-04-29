package com.anytypeio.anytype.feature_chats.ui

import android.content.res.Configuration
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.foundation.AlertConfig
import com.anytypeio.anytype.core_ui.foundation.AlertIcon
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.GRADIENT_TYPE_BLUE
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Regular
import com.anytypeio.anytype.core_utils.common.DefaultFileInfo
import com.anytypeio.anytype.core_utils.ext.parseImagePath
import com.anytypeio.anytype.domain.chats.ChatContainer
import com.anytypeio.anytype.feature_chats.R
import com.anytypeio.anytype.feature_chats.presentation.ChatView
import com.anytypeio.anytype.feature_chats.presentation.ChatViewModel
import com.anytypeio.anytype.feature_chats.presentation.ChatViewModel.ChatBoxMode
import com.anytypeio.anytype.feature_chats.presentation.ChatViewModel.MentionPanelState
import com.anytypeio.anytype.feature_chats.presentation.ChatViewModel.UXCommand
import com.anytypeio.anytype.feature_chats.presentation.ChatViewState
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreenWrapper(
    modifier: Modifier = Modifier,
    vm: ChatViewModel,
    onAttachObjectClicked: () -> Unit,
    onMarkupLinkClicked: (String) -> Unit,
    onRequestOpenFullScreenImage: (String) -> Unit,
    onSelectChatReaction: (String) -> Unit,
    onViewChatReaction: (Id, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var showReactionSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current
    NavHost(
        modifier = modifier,
        navController = rememberNavController(),
        startDestination = "discussions"
    ) {
        composable(
            route = "discussions"
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                val clipboard = LocalClipboardManager.current
                val lazyListState = rememberLazyListState()

                ChatScreen(
                    chatBoxMode = vm.chatBoxMode.collectAsState().value,
                    uiMessageState = vm.uiState.collectAsState().value,
                    attachments = vm.chatBoxAttachments.collectAsState().value,
                    onMessageSent = { text, spans ->
                        vm.onMessageSent(
                            msg = text,
                            markup = spans.map { span ->
                                when(span) {
                                    is ChatBoxSpan.Mention -> {
                                        Block.Content.Text.Mark(
                                            type = Block.Content.Text.Mark.Type.MENTION,
                                            param = span.param,
                                            range = span.start..span.end
                                        )
                                    }
                                }
                            }
                        )
                    },
                    onClearAttachmentClicked = vm::onClearAttachmentClicked,
                    lazyListState = lazyListState,
                    onReacted = vm::onReacted,
                    onCopyMessage = { msg ->
                        clipboard.setText(AnnotatedString(text = msg.content.msg))
                    },
                    onDeleteMessage = vm::onDeleteMessage,
                    onEditMessage = vm::onRequestEditMessageClicked,
                    onAttachmentClicked = vm::onAttachmentClicked,
                    onExitEditMessageMode = vm::onExitEditMessageMode,
                    onMarkupLinkClicked = onMarkupLinkClicked,
                    onAttachObjectClicked = onAttachObjectClicked,
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
                    onViewChatReaction = onViewChatReaction,
                    onMemberIconClicked = vm::onMemberIconClicked,
                    onMentionClicked = vm::onMentionClicked,
                    mentionPanelState = vm.mentionPanelState.collectAsStateWithLifecycle().value,
                    onTextChanged = { value ->
                        vm.onChatBoxInputChanged(
                            selection = value.selection.start..value.selection.end,
                            text = value.text
                        )
                    },
                    onChatScrolledToTop = vm::onChatScrolledToTop,
                    onChatScrolledToBottom = vm::onChatScrolledToBottom,
                    onScrollToReplyClicked = vm::onChatScrollToReply,
                    onClearIntent = vm::onClearChatViewStateIntent,
                    onScrollToBottomClicked = vm::onScrollToBottomClicked
                )
                LaunchedEffect(Unit) {
                    vm.uXCommands.collect { command ->
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

@Composable
fun LazyListState.OnBottomReached(
    thresholdItems: Int = 0,
    onBottomReached: () -> Unit
) {
    LaunchedEffect(this) {
        var prevIndex = firstVisibleItemIndex
        snapshotFlow { firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { index ->
                Timber.d("DROID-2966 OnBottomReached scroll index: $index")
                val isDragging = isScrollInProgress

                // Are we scrolling *toward* the bottom edge?
                val scrollingDown = isDragging && prevIndex > index

                // Have we crossed into the threshold zone?
                val atBottom = index <= thresholdItems

                if (scrollingDown && atBottom) {
                    onBottomReached()
                }
                prevIndex = index
            }
    }
}

@Composable
private fun LazyListState.OnTopReached(
    thresholdItems: Int = 0,
    onTopReached: () -> Unit
) {
    val isReached = remember {
        derivedStateOf {
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
            if (lastVisibleItem != null) {
                lastVisibleItem.index >= layoutInfo.totalItemsCount - 1 - thresholdItems
            } else {
                false
            }
        }
    }

    LaunchedEffect(isReached) {
        snapshotFlow { isReached.value }
            .distinctUntilChanged()
            .collect { if (it) onTopReached() }
    }
}

@Composable
private fun LazyListState.OnTopReachedSafely(
    thresholdItems: Int = 0,
    onTopReached: () -> Unit
) {
    LaunchedEffect(this) {
        snapshotFlow { layoutInfo }
            .filter { it.totalItemsCount > 0 && it.visibleItemsInfo.isNotEmpty() }
            .distinctUntilChanged()
            .collect { info ->
                val lastVisibleItem = info.visibleItemsInfo.lastOrNull()
                if (lastVisibleItem != null) {
                    val isTop = lastVisibleItem.index >= info.totalItemsCount - 1 - thresholdItems
                    if (isTop) {
                        Timber.d("DROID-2966 Safe onTopReached triggered")
                        onTopReached()
                    }
                }
            }
    }
}

@Composable
private fun LazyListState.OnBottomReachedSafely(
    thresholdItems: Int = 0,
    onBottomReached: () -> Unit
) {
    LaunchedEffect(this) {
        snapshotFlow { layoutInfo }
            .filter { it.totalItemsCount > 0 && it.visibleItemsInfo.isNotEmpty() }
            .distinctUntilChanged()
            .collect { info ->
                val firstVisibleItem = info.visibleItemsInfo.firstOrNull()
                if (firstVisibleItem != null) {
                    val isBottom = firstVisibleItem.index <= thresholdItems
                    if (isBottom) {
                        Timber.d("DROID-2966 Safe onBottomReached triggered")
                        onBottomReached()
                    }
                }
            }
    }
}

/**
 * TODO: do date formating before rendering?
 */
@Composable
fun ChatScreen(
    mentionPanelState: MentionPanelState,
    chatBoxMode: ChatBoxMode,
    lazyListState: LazyListState,
    uiMessageState: ChatViewState,
    attachments: List<ChatView.Message.ChatBoxAttachment>,
    onMessageSent: (String, List<ChatBoxSpan>) -> Unit,
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
    onChatBoxMediaPicked: (List<Uri>) -> Unit,
    onChatBoxFilePicked: (List<Uri>) -> Unit,
    onAddReactionClicked: (String) -> Unit,
    onViewChatReaction: (Id, String) -> Unit,
    onMemberIconClicked: (Id?) -> Unit,
    onMentionClicked: (Id) -> Unit,
    onTextChanged: (TextFieldValue) -> Unit,
    onChatScrolledToTop: () -> Unit,
    onChatScrolledToBottom: () -> Unit,
    onScrollToReplyClicked: (Id) -> Unit,
    onClearIntent: () -> Unit,
    onScrollToBottomClicked: () -> Unit
) {

    Timber.d("DROID-2966 Render called with state")

    var text by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }

    var spans by remember { mutableStateOf<List<ChatBoxSpan>>(emptyList()) }

    val chatBoxFocusRequester = FocusRequester()

    val scope = rememberCoroutineScope()

    LaunchedEffect(uiMessageState.intent) {
        when (val intent = uiMessageState.intent) {
            is ChatContainer.Intent.ScrollToMessage -> {
                val index = uiMessageState.messages.indexOfFirst {
                    it is ChatView.Message && it.id == intent.id
                }

                if (index >= 0) {
                    Timber.d("DROID-2966 Waiting for layout to stabilize...")

                    snapshotFlow { lazyListState.layoutInfo.totalItemsCount }
                        .first { it > index }

                    lazyListState.scrollToItem(index)

                    awaitFrame()

                    val itemInfo = lazyListState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
                    if (itemInfo != null) {
                        val viewportHeight = lazyListState.layoutInfo.viewportSize.height

                        // Centering calculation:
                        // itemCenter relative to viewport top
                        val itemCenterFromTop = itemInfo.offset + (itemInfo.size / 2)
                        val viewportCenter = viewportHeight / 2

                        val delta = itemCenterFromTop - viewportCenter

                        Timber.d("DROID-2966 Calculated delta for centering (reverseLayout-aware): $delta")

                        // move negatively because reverseLayout flips
                        lazyListState.animateScrollBy(delta.toFloat())

                        Timber.d("DROID-2966 Scroll complete. Now clearing intent.")

                        onClearIntent()
                    } else {
                        Timber.w("DROID-2966 Target item not found after scroll!")
                    }
                }
            }
            is ChatContainer.Intent.ScrollToBottom -> {
                smoothScrollToBottom2(lazyListState)
                onClearIntent()
            }
            is ChatContainer.Intent.Highlight -> {
                // maybe flash background, etc.
            }
            ChatContainer.Intent.None -> Unit
        }
    }

    val latestMessages by rememberUpdatedState(uiMessageState.messages)

    lazyListState.OnBottomReachedSafely(
        thresholdItems = 3
    ) {
        if (latestMessages.isNotEmpty()) {
            onChatScrolledToBottom()
        }
    }

    lazyListState.OnTopReachedSafely(
        thresholdItems = 3
    ) {
        if (latestMessages.isNotEmpty()) {
            onChatScrolledToTop()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.weight(1f)) {
            Messages(
                modifier = Modifier.fillMaxSize(),
                messages = uiMessageState.messages,
                scrollState = lazyListState,
                onReacted = onReacted,
                onCopyMessage = onCopyMessage,
                onDeleteMessage = onDeleteMessage,
                onAttachmentClicked = onAttachmentClicked,
                onEditMessage = { msg ->
                    onEditMessage(msg).also {
                        text = TextFieldValue(
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
                onViewChatReaction = onViewChatReaction,
                onMemberIconClicked = onMemberIconClicked,
                onMentionClicked = onMentionClicked,
                onScrollToReplyClicked = onScrollToReplyClicked
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
                    onScrollToBottomClicked()
                },
                enabled = jumpToBottomButtonEnabled
            )

            if (uiMessageState.counter.count > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
                        .padding(bottom = 46.dp, end = 2.dp)
                        .background(
                            color = colorResource(R.color.transparent_active),
                            shape = CircleShape
                        )
                ) {
                    Text(
                        text = uiMessageState.counter.count.toString(),
                        modifier = Modifier.align(Alignment.Center).padding(
                            horizontal = 5.dp,
                            vertical = 2.dp
                        ),
                        color = colorResource(R.color.glyph_white),
                        style = Caption1Regular
                    )
                }
            }

            when(mentionPanelState) {
                MentionPanelState.Hidden -> {
                    // Draw nothing.
                }
                is MentionPanelState.Visible -> {
                    LazyColumn(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth()
                            .height(168.dp)
                            .background(
                                color = colorResource(R.color.background_primary),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .align(Alignment.BottomCenter)
                    ) {
                        items(
                            items = mentionPanelState.results,
                            key = { member -> member.id }
                        ) { member ->
                            ChatMemberItem(
                                name = member.name,
                                icon = member.icon,
                                isUser = member.isUser,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .noRippleClickable {

                                        val query = mentionPanelState.query
                                        val input = text.text

                                        val replacementText = member.name + " "

                                        val lengthDifference =
                                            replacementText.length - (query.range.last - query.range.first + 1)

                                        val updatedText = input.replaceRange(
                                            query.range,
                                            replacementText
                                        )

                                        // After inserting a mention, all existing spans after the insertion point are shifted based on the text length difference.

                                        val updatedSpans = spans.map { span ->
                                            if (span.start > query.range.last) {
                                                when (span) {
                                                    is ChatBoxSpan.Mention -> {
                                                        span.copy(
                                                            start = span.start + lengthDifference,
                                                            end = span.end + lengthDifference
                                                        )
                                                    }
                                                }
                                            } else {
                                                span
                                            }
                                        }

                                        text = text.copy(
                                            text = updatedText,
                                            selection = TextRange(
                                                index = (query.range.start + replacementText.length)
                                            )
                                        )

                                        val mentionSpan = ChatBoxSpan.Mention(
                                            start = query.range.start,
                                            end = query.range.start + member.name.length,
                                            style = SpanStyle(
                                                textDecoration = TextDecoration.Underline
                                            ),
                                            param = member.id
                                        )

                                        spans = updatedSpans + mentionSpan

                                        onTextChanged(text)
                                    }
                            )
                            Divider()
                        }
                    }
                }
            }
        }
        ChatBox(
            mode = chatBoxMode,
            modifier = Modifier
                .imePadding()
                .navigationBarsPadding(),
            chatBoxFocusRequester = chatBoxFocusRequester,
            onMessageSent = { text, markup ->
                onMessageSent(text, markup)
            },
            resetScroll = {
                if (lazyListState.firstVisibleItemScrollOffset > 0) {
                    scope.launch {
                        lazyListState.animateScrollToItem(index = 0)
                    }
                }
            },
            attachments = attachments,
            clearText = {
                text = TextFieldValue()
            },
            onAttachObjectClicked = onAttachObjectClicked,
            onClearAttachmentClicked = onClearAttachmentClicked,
            onClearReplyClicked = onClearReplyClicked,
            onChatBoxMediaPicked = onChatBoxMediaPicked,
            onChatBoxFilePicked = onChatBoxFilePicked,
            onExitEditMessageMode = {
                onExitEditMessageMode().also {
                    text = TextFieldValue()
                }
            },
            onValueChange = { t, s ->
                text = t
                spans = s
                onTextChanged(t)
            },
            text = text,
            spans = spans
        )
    }
}

@Composable
fun Messages(
    modifier: Modifier = Modifier,
    messages: List<ChatView>,
    scrollState: LazyListState,
    onReacted: (Id, String) -> Unit,
    onDeleteMessage: (ChatView.Message) -> Unit,
    onCopyMessage: (ChatView.Message) -> Unit,
    onAttachmentClicked: (ChatView.Message.Attachment) -> Unit,
    onEditMessage: (ChatView.Message) -> Unit,
    onReplyMessage: (ChatView.Message) -> Unit,
    onMarkupLinkClicked: (String) -> Unit,
    onAddReactionClicked: (String) -> Unit,
    onViewChatReaction: (Id, String) -> Unit,
    onMemberIconClicked: (Id?) -> Unit,
    onMentionClicked: (Id) -> Unit,
    onScrollToReplyClicked: (Id) -> Unit,
) {
    Timber.d("DROID-2966 Messages composition: ${messages.map { if (it is ChatView.Message) it.content.msg else it }}")
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
                    is ChatView.DateSection -> "$DATE_KEY_PREFIX${msg.timeInMillis}"
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
                            modifier = Modifier
                                .align(Alignment.Bottom)
                                .clickable { onMemberIconClicked(msg.creator) }
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
                        isMaxReactionCountReached = msg.isMaxReactionCountReached,
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
                            val idx = messages.indexOfFirst { it is ChatView.Message && it.id == reply.msg }
                            if (idx != -1) {
                                scope.launch { scrollState.animateScrollToItem(index = idx) }
                            } else {
                                onScrollToReplyClicked(reply.msg)
                            }
                        },
                        onAddReactionClicked = {
                            onAddReactionClicked(msg.id)
                        },
                        onViewChatReaction = { emoji ->
                            onViewChatReaction(msg.id, emoji)
                        },
                        onMentionClicked = onMentionClicked
                    )
                }
                if (idx == messages.lastIndex) {
                    Spacer(modifier = Modifier.height(36.dp))
                }
            } else if (msg is ChatView.DateSection) {
                Text(
                    text = msg.formattedDate,
                    style = Caption1Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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

suspend fun smoothScrollToBottom(lazyListState: LazyListState) {
    if (lazyListState.firstVisibleItemIndex > 0) {
        lazyListState.scrollToItem(0)
        return
    }
    while (lazyListState.firstVisibleItemScrollOffset > 0) {
        val delta = (-lazyListState.firstVisibleItemScrollOffset).coerceAtLeast(-40)
        lazyListState.animateScrollBy(delta.toFloat())
    }
}

suspend fun smoothScrollToBottom2(lazyListState: LazyListState) {
    lazyListState.scrollToItem(0)

    // Wait for the layout to settle after scrolling
    awaitFrame()

    while (lazyListState.firstVisibleItemIndex == 0 && lazyListState.firstVisibleItemScrollOffset > 0) {
        val offset = lazyListState.firstVisibleItemScrollOffset
        val delta = (-offset).coerceAtLeast(-80)
        lazyListState.animateScrollBy(delta.toFloat())
        awaitFrame() // Yield to UI again
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Light Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Dark Mode")
@Composable
fun TopDiscussionToolbarPreview() {
    TopDiscussionToolbar()
}

private const val DATE_KEY_PREFIX = "date-"
private const val HEADER_KEY = "key.discussions.item.header"
private val JumpToBottomThreshold = 200.dp