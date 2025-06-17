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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_ui.foundation.AlertConfig
import com.anytypeio.anytype.core_ui.foundation.BUTTON_SECONDARY
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.GRADIENT_TYPE_RED
import com.anytypeio.anytype.core_ui.foundation.GenericAlert
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Regular
import com.anytypeio.anytype.core_utils.common.DefaultFileInfo
import com.anytypeio.anytype.core_utils.ext.isVideo
import com.anytypeio.anytype.core_utils.ext.parseImagePath
import com.anytypeio.anytype.domain.chats.ChatContainer
import com.anytypeio.anytype.feature_chats.R
import com.anytypeio.anytype.feature_chats.presentation.ChatView
import com.anytypeio.anytype.feature_chats.presentation.ChatViewModel
import com.anytypeio.anytype.feature_chats.presentation.ChatViewModel.ChatBoxMode
import com.anytypeio.anytype.feature_chats.presentation.ChatViewModel.MentionPanelState
import com.anytypeio.anytype.feature_chats.presentation.ChatViewModel.UXCommand
import com.anytypeio.anytype.feature_chats.presentation.ChatViewState
import kotlinx.coroutines.Job
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
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
    onViewChatReaction: (Id, String) -> Unit,
    onRequestVideoPlayer: (ChatView.Message.Attachment.Video) -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var showSendRateLimitWarning by remember { mutableStateOf(false) }
    val context = LocalContext.current
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        val clipboard = LocalClipboardManager.current
        val lazyListState = rememberLazyListState()

        val messages by vm.uiState
            .map { it.messages }
            .collectAsStateWithLifecycle(emptyList())

        val counter by vm.uiState
            .map { it.counter }
            .collectAsStateWithLifecycle(ChatViewState.Counter())

        val intent by vm.uiState
            .map { it.intent }
            .collectAsStateWithLifecycle(ChatContainer.Intent.None)

        val mentionPanelState by vm.mentionPanelState.collectAsStateWithLifecycle()

        ChatScreen(
            chatBoxMode = vm.chatBoxMode.collectAsState().value,
            messages = messages,
            counter = counter,
            intent = intent,
            attachments = vm.chatBoxAttachments.collectAsState().value,
            onMessageSent = { text, spans ->
                vm.onMessageSent(
                    msg = text,
                    markup = spans.mapNotNull { span ->
                        when(span) {
                            is ChatBoxSpan.Mention -> {
                                Block.Content.Text.Mark(
                                    type = Block.Content.Text.Mark.Type.MENTION,
                                    param = span.param,
                                    range = span.start..span.end
                                )
                            }
                            is ChatBoxSpan.Markup -> {
                                val type = when(span.type) {
                                    ChatBoxSpan.Markup.BOLD -> Block.Content.Text.Mark.Type.BOLD
                                    ChatBoxSpan.Markup.ITALIC -> Block.Content.Text.Mark.Type.ITALIC
                                    ChatBoxSpan.Markup.STRIKETHROUGH -> Block.Content.Text.Mark.Type.STRIKETHROUGH
                                    ChatBoxSpan.Markup.CODE -> Block.Content.Text.Mark.Type.KEYBOARD
                                    ChatBoxSpan.Markup.UNDERLINE -> Block.Content.Text.Mark.Type.UNDERLINE
                                    else -> null
                                }
                                if (type != null) {
                                    Block.Content.Text.Mark(
                                        type = type,
                                        range = span.start..span.end
                                    )
                                } else {
                                    null
                                }
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
                vm.onChatBoxMediaPicked(
                    uris.map {
                        ChatViewModel.ChatBoxMediaUri(
                            uri = it.parseImagePath(context = context),
                            isVideo = isVideo(
                                uri = it,
                                context = context
                            )
                        )
                    }
                )
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
            mentionPanelState = mentionPanelState,
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
            onScrollToBottomClicked = vm::onScrollToBottomClicked,
            onVisibleRangeChanged = vm::onVisibleRangeChanged,
            onUrlInserted = vm::onUrlPasted,
            onGoToMentionClicked = vm::onGoToMentionClicked,
            onShareInviteClicked = vm::onShareInviteLinkClicked,
            canCreateInviteLink = vm.canCreateInviteLink.collectAsStateWithLifecycle().value,
            isReadOnly = vm.chatBoxMode
                .collectAsStateWithLifecycle()
                .value is ChatBoxMode.ReadOnly,
            onImageCaptured = {
                vm.onChatBoxMediaPicked(
                    uris = listOf(
                        ChatViewModel.ChatBoxMediaUri(
                            uri = it.toString(),
                            isVideo = false,
                            capturedByCamera = true
                        )
                    )
                )
            },
            onVideoCaptured = {
                vm.onChatBoxMediaPicked(
                    uris = listOf(
                        ChatViewModel.ChatBoxMediaUri(
                            uri = it.toString(),
                            isVideo = true,
                            capturedByCamera = true
                        )
                    )
                )
            },
            onRequestVideoPlayer = onRequestVideoPlayer
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
                    is UXCommand.ShowRateLimitWarning -> {
                        showSendRateLimitWarning = true
                    }
                }
            }
        }
    }

    if (showSendRateLimitWarning) {
        ModalBottomSheet(
            onDismissRequest = {
                showSendRateLimitWarning = false
            },
            sheetState = sheetState,
            containerColor = colorResource(id = R.color.background_secondary),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            dragHandle = null
        ) {
            GenericAlert(
                config = AlertConfig.WithOneButton(
                    title = stringResource(R.string.chat_send_message_rate_limit_title),
                    firstButtonText = stringResource(id = R.string.button_okay),
                    firstButtonType = BUTTON_SECONDARY,
                    description = stringResource(R.string.chat_send_message_rate_limit_desc),
                    icon = AlertConfig.Icon(
                        gradient = GRADIENT_TYPE_RED,
                        icon = R.drawable.ic_alert_message
                    )
                ),
                onFirstButtonClicked = {
                    showSendRateLimitWarning = false
                }
            )
        }
    }
}

@Composable
private fun LazyListState.OnTopReachedSafely(
    messages: List<ChatView>,
    thresholdItems: Int = 0,
    onTopReached: () -> Unit
) {
    LaunchedEffect(this, messages.size) {
        snapshotFlow {
            layoutInfo.visibleItemsInfo.lastOrNull()?.index
        }
            .filterNotNull()
            .distinctUntilChanged()
            .collect { lastVisibleIndex ->
                val isTop = lastVisibleIndex >= layoutInfo.totalItemsCount - 1 - thresholdItems
                if (isTop) {
                    onTopReached()
                }
            }
    }
}

@Composable
private fun LazyListState.OnBottomReachedSafely(
    messages: List<ChatView>,
    thresholdItems: Int = 0,
    onBottomReached: () -> Unit
) {
    LaunchedEffect(this, messages.size) {
        snapshotFlow {
            layoutInfo.visibleItemsInfo.firstOrNull()?.index
        }
            .filterNotNull()
            .distinctUntilChanged()
            .collect { index ->
                if (index <= thresholdItems) {
                    onBottomReached()
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
    messages: List<ChatView>,
    counter: ChatViewState.Counter,
    intent: ChatContainer.Intent,
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
    onImageCaptured: (Uri) -> Unit,
    onVideoCaptured: (Uri) -> Unit,
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
    onScrollToBottomClicked: (Id?) -> Unit,
    onVisibleRangeChanged: (Id, Id) -> Unit,
    onUrlInserted: (Url) -> Unit,
    onGoToMentionClicked: () -> Unit,
    onShareInviteClicked: () -> Unit,
    canCreateInviteLink: Boolean = false,
    isReadOnly: Boolean = false,
    onRequestVideoPlayer: (ChatView.Message.Attachment.Video) -> Unit = {}
) {

    Timber.d("DROID-2966 Render called with state, number of messages: ${messages.size}")

    val scope = rememberCoroutineScope()

    var text by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }

    var highlightedMessageId by remember { mutableStateOf<Id?>(null) }

    val triggerHighlight: (Id) -> Unit = { id ->
        highlightedMessageId = id
        scope.launch {
            delay(1000)
            highlightedMessageId = null
        }
    }

    // Floating date header tracking

    val isFloatingDateVisible = remember { mutableStateOf(false) }
    val floatingDateState = rememberFloatingDateHeaderState(lazyListState, messages)
    var scrollDebounceJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.isScrollInProgress }
            .distinctUntilChanged()
            .collect { isScrolling ->
                if (isScrolling) {
                    // Show header immediately on scroll start
                    isFloatingDateVisible.value = true

                    // Cancel existing debounce job if still running
                    scrollDebounceJob?.cancel()
                } else {
                    // Start debounce to hide after 1500ms of no scroll
                    scrollDebounceJob = scope.launch {
                        delay(FLOATING_DATE_DELAY)
                        isFloatingDateVisible.value = false
                    }
                }
            }
    }

    var spans by remember { mutableStateOf<List<ChatBoxSpan>>(emptyList()) }

    val chatBoxFocusRequester = remember { FocusRequester() }

    val keyboardController = LocalSoftwareKeyboardController.current

    val isPerformingScrollIntent = remember { mutableStateOf(false) }

    val offsetPx = with(LocalDensity.current) { 50.dp.toPx().toInt() }

    // Applying view model intents
    LaunchedEffect(intent) {
        Timber.d("DROID-2966 New intent: $intent")
        when (intent) {
            is ChatContainer.Intent.ScrollToMessage -> {
                isPerformingScrollIntent.value = true
                val index = messages.indexOfFirst {
                    it is ChatView.Message && it.id == intent.id
                }
                if (index >= 0) {
                    snapshotFlow { lazyListState.layoutInfo.totalItemsCount }
                        .first { it > index }
                    if (intent.smooth) {
                        lazyListState.animateScrollToItem(index)
                    } else {
                        if (intent.startOfUnreadMessageSection) {
                            lazyListState.scrollToItem(index, scrollOffset = -offsetPx)
                        } else {
                            lazyListState.scrollToItem(index)
                        }
                    }
                    awaitFrame()

                    if (intent.highlight) {
                        highlightedMessageId = intent.id
                        delay(500)
                        highlightedMessageId = null
                    }
                } else {
                    Timber.d("DROID-2966 COMPOSE Could not find the scrolling target for the intent")
                }
                onClearIntent()
                isPerformingScrollIntent.value = false
            }
            is ChatContainer.Intent.ScrollToBottom -> {
                Timber.d("DROID-2966 COMPOSE scroll to bottom")
                isPerformingScrollIntent.value = true
                smoothScrollToBottom(lazyListState)
                awaitFrame()
                isPerformingScrollIntent.value = false
                onClearIntent()
            }
            ChatContainer.Intent.None -> Unit
        }
    }

    // Tracking visible range
    LaunchedEffect(lazyListState, messages) {
        snapshotFlow { lazyListState.layoutInfo }
            .mapNotNull { layoutInfo ->
                // TODO optimise by only sending event when scrolling towards bottom
                val viewportHeight = layoutInfo.viewportSize.height
                val visibleMessages = layoutInfo.visibleItemsInfo
                    .filter { item ->
                        val itemBottom = item.offset + item.size
                        val isFullyVisible = item.offset >= 0 && itemBottom <= viewportHeight
                        isFullyVisible
                    }
                    .sortedBy { it.index } // still necessary
                    .mapNotNull { item -> messages.getOrNull(item.index) }
                    .filterIsInstance<ChatView.Message>()

                if (visibleMessages.isNotEmpty() && !isPerformingScrollIntent.value) {
                    // TODO could be optimised by passing order ID
                    visibleMessages.first().id to visibleMessages.last().id
                } else null
            }
            .distinctUntilChanged()
            .collect { (from, to) ->
                onVisibleRangeChanged(from, to)
            }
    }

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

    val isAtBottom by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex == 0 &&
                    lazyListState.firstVisibleItemScrollOffset == 0
        }
    }

    var wasAtBottom by remember { mutableStateOf(isAtBottom) }

    LaunchedEffect(lazyListState) {
        snapshotFlow {
            val layoutInfo = lazyListState.layoutInfo
            layoutInfo.visibleItemsInfo.firstOrNull()?.index == 0 &&
                    layoutInfo.visibleItemsInfo.firstOrNull()?.offset == 0
        }
            .distinctUntilChanged()
            .collect { atBottom ->
                wasAtBottom = atBottom
                Timber.d("DROID-2966 Updated wasAtBottom: $wasAtBottom")
            }
    }

    // Scrolling to bottom when list size changes and we are at the bottom of the list
    LaunchedEffect(messages.size) {
        if (wasAtBottom && !isPerformingScrollIntent.value) {
            lazyListState.animateScrollToItem(0)
        } else {
            Timber.d("DROID-2966 Skipping auto-scroll")
        }
    }

    lazyListState.OnBottomReachedSafely(messages) {
        if (!isPerformingScrollIntent.value && messages.isNotEmpty()) {
            Timber.d("DROID-2966 Safe onBottomReached dispatched from compose to VM")
            onChatScrolledToBottom()
        } else {
            Timber.d("DROID-2966 SKIPPED(!): Safe onBottomReached dispatched from compose to VM")
        }
    }

    lazyListState.OnTopReachedSafely(messages) {
        if (!isPerformingScrollIntent.value && messages.isNotEmpty()) {
            Timber.d("DROID-2966 Safe onTopReached dispatched from compose to VM")
            onChatScrolledToTop()
        } else {
            Timber.d("DROID-2966 SKIPPED(!): Safe onTopReached dispatched from compose to VM: is empty: ${messages.isEmpty()}, is performing: ${isPerformingScrollIntent.value}")
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.weight(1f)) {
            Messages(
                modifier = Modifier.fillMaxSize(),
                messages = messages,
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
                        scope.launch {
                            delay(100) // optionally delay to let layout settle
                            chatBoxFocusRequester.requestFocus()
                            delay(50) // small buffer
                            keyboardController?.show()
                        }
                    }
                },
                onReplyMessage = {
                    onReplyMessage(it)
                    scope.launch {
                        delay(100) // optionally delay to let layout settle
                        chatBoxFocusRequester.requestFocus()
                        delay(50) // small buffer
                        keyboardController?.show()
                    }
                },
                onMarkupLinkClicked = onMarkupLinkClicked,
                onAddReactionClicked = onAddReactionClicked,
                onViewChatReaction = onViewChatReaction,
                onMemberIconClicked = onMemberIconClicked,
                onMentionClicked = onMentionClicked,
                onScrollToReplyClicked = onScrollToReplyClicked,
                isReadOnly = isReadOnly,
                onShareInviteClicked = onShareInviteClicked,
                canCreateInviteLink = canCreateInviteLink,
                onRequestVideoPlayer = onRequestVideoPlayer,
                highlightedMessageId = highlightedMessageId,
                onHighlightMessage = triggerHighlight
            )

            GoToMentionButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = 12.dp,
                        bottom = if (jumpToBottomButtonEnabled) 60.dp else 0.dp
                    ),
                onClick = onGoToMentionClicked,
                enabled = counter.mentions > 0
            )

            if (counter.mentions > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
                        .padding(
                            bottom = if (jumpToBottomButtonEnabled) 106.dp else 46.dp,
                            end = 2.dp
                        )
                        .background(
                            color = colorResource(R.color.transparent_active),
                            shape = CircleShape
                        )
                ) {
                    Text(
                        text = counter.mentions.toString(),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(
                                horizontal = 5.dp,
                                vertical = 2.dp
                            ),
                        color = colorResource(R.color.glyph_white),
                        style = Caption1Regular
                    )
                }
            }

            GoToBottomButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 12.dp),
                onGoToBottomClicked = {
                    val lastVisibleIndex = lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                    val lastVisibleView = if (lastVisibleIndex != null) {
                        messages.getOrNull(lastVisibleIndex)
                    } else {
                        null
                    }
                    if (lastVisibleView is ChatView.Message) {
                        onScrollToBottomClicked(
                            lastVisibleView.id
                        )
                    } else {
                        onScrollToBottomClicked(null)
                    }
                },
                enabled = jumpToBottomButtonEnabled
            )

            if (counter.messages > 0) {
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
                        text = counter.messages.toString(),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(
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

                                                    is ChatBoxSpan.Markup -> {
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

            if (isFloatingDateVisible.value && floatingDateState.value != null) {
                FloatingDateHeader(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp),
                    text = floatingDateState.value.orEmpty()
                )
            }
        }

        if (isReadOnly) {
            ReaderChatBox(
                modifier = Modifier
                    .padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
                    .navigationBarsPadding()
            )
        } else {
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
                    if (!isPerformingScrollIntent.value) {
                        scope.launch {
                            lazyListState.scrollToItem(0)
                            awaitFrame()
                            while (!isAtBottom) {
                                val offset = lazyListState.firstVisibleItemScrollOffset
                                val delta = (-offset).coerceAtLeast(-80)
                                lazyListState.animateScrollBy(delta.toFloat())
                                awaitFrame()
                            }
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
                spans = spans,
                onUrlInserted = onUrlInserted,
                onImageCaptured = onImageCaptured,
                onVideoCaptured = onVideoCaptured
            )
        }
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
    onHighlightMessage: (Id) -> Unit,
    onShareInviteClicked: () -> Unit,
    canCreateInviteLink: Boolean = false,
    isReadOnly: Boolean = false,
    onRequestVideoPlayer: (ChatView.Message.Attachment.Video) -> Unit,
    highlightedMessageId: Id?
) {
    Timber.d("DROID-2966 Messages composition")
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

                val isHighlighted = msg.id == highlightedMessageId

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
                        shouldHideUsername = msg.shouldHideUsername,
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
                            val targetIndex = messages.indexOfFirst { it is ChatView.Message && it.id == reply.msg }
                            scope.launch {
                                if (targetIndex != -1 && targetIndex < scrollState.layoutInfo.totalItemsCount) {
                                    scrollState.animateScrollToItem(index = targetIndex)
                                    onHighlightMessage(reply.msg)
                                } else {
                                    // Defer to VM: message likely not yet in the list (e.g. paged)
                                    onScrollToReplyClicked(reply.msg)
                                }
                            }
                        },
                        onAddReactionClicked = {
                            onAddReactionClicked(msg.id)
                        },
                        onViewChatReaction = { emoji ->
                            onViewChatReaction(msg.id, emoji)
                        },
                        onMentionClicked = onMentionClicked,
                        isReadOnly = isReadOnly,
                        onRequestVideoPlayer = onRequestVideoPlayer,
                        isHighlighted = isHighlighted
                    )
                }
                if (idx == messages.lastIndex) {
                    Spacer(modifier = Modifier.height(36.dp))
                }

                if (msg.startOfUnreadMessageSection) {
                    Box(
                        modifier = Modifier
                            .height(50.dp)
                            .fillParentMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.chat_new_messages_section_text),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxWidth(),
                            style = Caption1Medium,
                            color = colorResource(R.color.transparent_active),
                            textAlign = TextAlign.Center
                        )
                    }
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
                            .padding(horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            modifier = Modifier.size(56.dp),
                            painter = painterResource(id = R.drawable.ic_vault_create_space),
                            contentDescription = "Empty state icon",
                            colorFilter = ColorFilter.tint(colorResource(id = R.color.transparent_inactive))
                        )
                        Text(
                            text = stringResource(R.string.chat_empty_state_title),
                            style = BodyRegular,
                            color = colorResource(id = R.color.text_primary),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                        )
                        Text(
                            text = stringResource(R.string.chat_empty_state_subtitle),
                            style = BodyRegular,
                            color = colorResource(id = R.color.text_secondary),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                        if (canCreateInviteLink) {
                            ButtonSecondary(
                                text = stringResource(R.string.chat_empty_state_share_invite_button),
                                onClick = { onShareInviteClicked() },
                                size = ButtonSize.SmallSecondary,
                                modifier = Modifier.padding(top = 10.dp)
                            )
                        }
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

@Composable
fun rememberFloatingDateHeaderState(
    lazyListState: LazyListState,
    messages: List<ChatView>
): State<String?> {
    val topDate = remember { mutableStateOf<String?>(null) }

    val topVisibleIndex by remember {
        derivedStateOf {
            lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
        }
    }

    LaunchedEffect(topVisibleIndex, messages) {
        val msg = messages.getOrNull(topVisibleIndex ?: return@LaunchedEffect) as? ChatView.Message
        val newDate = msg?.formattedDate
        if (newDate != null && newDate != topDate.value) {
            topDate.value = newDate
        }
    }

    return topDate
}

suspend fun smoothScrollToBottom(lazyListState: LazyListState) {
    Timber.d("DROID-2966 Performing scroll-to-bottom")
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

@Composable
fun TrackFloatingDate(
    lazyListState: LazyListState,
    messages: List<ChatView>,
    floatingDateState: MutableState<String?>
) {
    val topVisibleIndex by remember {
        derivedStateOf {
            lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
        }
    }

    LaunchedEffect(topVisibleIndex, messages) {
        val msg = messages.getOrNull(topVisibleIndex ?: return@LaunchedEffect) as? ChatView.Message
        val newDate = msg?.formattedDate
        if (newDate != null && newDate != floatingDateState.value) {
            floatingDateState.value = newDate
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Light Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Dark Mode")
@Composable
fun TopDiscussionToolbarPreview() {
    TopDiscussionToolbar()
}

private const val DATE_KEY_PREFIX = "date-"
private val JumpToBottomThreshold = 200.dp
private const val FLOATING_DATE_DELAY = 1500L