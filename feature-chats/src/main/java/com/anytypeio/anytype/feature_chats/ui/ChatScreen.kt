package com.anytypeio.anytype.feature_chats.ui

import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_models.multiplayer.SpaceInviteLinkAccessLevel
import com.anytypeio.anytype.core_ui.common.ShimmerEffect
import com.anytypeio.anytype.core_ui.foundation.AlertConfig
import com.anytypeio.anytype.core_ui.foundation.BUTTON_SECONDARY
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.GenericAlert
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.Caption1Regular
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
    onRequestOpenFullScreenImageGallery: (List<Id>, Int) -> Unit,
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

        val inviteLinkAccessLevel by vm.inviteLinkAccessLevel.collectAsStateWithLifecycle()

        ChatScreen(
            isLoading = vm.uiState.collectAsStateWithLifecycle().value.isLoading,
            isSyncing = vm.isSyncing.collectAsStateWithLifecycle().value,
            chatBoxMode = vm.chatBoxMode.collectAsState().value,
            messages = messages,
            counter = counter,
            intent = intent,
            attachments = vm.chatBoxAttachments.collectAsState().value,
            inviteLinkAccessLevel = inviteLinkAccessLevel,
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
                vm.onCopyMessageTextActionTriggered()
            },
            onDeleteMessage = vm::onDeleteMessage,
            onDeleteMessageWarningTriggered = vm::onDeleteMessageWarningTriggered,
            onEditMessage = vm::onRequestEditMessageClicked,
            onAttachmentMenuTriggered = vm::onAttachmentMenuTriggered,
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
            onAddMembersClick = vm::onEmptyStateAction,
            onShowQRCodeClick = vm::onShowQRCode,
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
            onRequestVideoPlayer = onRequestVideoPlayer,
            onCreateAndAttachObject = vm::onCreateAndAttachObject,
            onCameraPermissionDenied = vm::onCameraPermissionDenied
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
                        onRequestOpenFullScreenImageGallery(
                            command.objects,
                            command.idx
                        )
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
                    icon = R.drawable.ic_popup_alert_56
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
    isLoading: Boolean = false,
    isSyncing: Boolean = false,
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
    onDeleteMessageWarningTriggered: () -> Unit,
    onCopyMessage: (ChatView.Message) -> Unit,
    onEditMessage: (ChatView.Message) -> Unit,
    onReplyMessage: (ChatView.Message) -> Unit,
    onAttachmentClicked: (ChatView.Message, ChatView.Message.Attachment) -> Unit,
    onAttachmentMenuTriggered: () -> Unit,
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
    onAddMembersClick: () -> Unit,
    onShowQRCodeClick: () -> Unit,
    isReadOnly: Boolean = false,
    onRequestVideoPlayer: (ChatView.Message.Attachment.Video) -> Unit = {},
    onCreateAndAttachObject: () -> Unit,
    onCameraPermissionDenied: () -> Unit = {},
    inviteLinkAccessLevel: SpaceInviteLinkAccessLevel = SpaceInviteLinkAccessLevel.LinkDisabled()
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
                    // Start debounce to hide after 1000ms of no scroll
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
    LaunchedEffect(lazyListState, messages, isPerformingScrollIntent.value) {
        snapshotFlow { lazyListState.layoutInfo }
            .mapNotNull { layoutInfo ->
                if (layoutInfo.totalItemsCount == 0) return@mapNotNull null

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
                isLoading = isLoading,
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
                onAddMembersClick = onAddMembersClick,
                onShowQRCodeClick = onShowQRCodeClick,
                onRequestVideoPlayer = onRequestVideoPlayer,
                highlightedMessageId = highlightedMessageId,
                onHighlightMessage = triggerHighlight,
                onDeleteMessageWarningTriggered = onDeleteMessageWarningTriggered,
                inviteLinkAccessLevel = inviteLinkAccessLevel
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

            DelayedSyncIndicator(
                isLoading = isLoading,
                isSyncing = isSyncing
            )
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
                    spans = emptyList()
                },
                onAttachObjectClicked = onAttachObjectClicked,
                onClearAttachmentClicked = onClearAttachmentClicked,
                onClearReplyClicked = onClearReplyClicked,
                onChatBoxMediaPicked = onChatBoxMediaPicked,
                onChatBoxFilePicked = onChatBoxFilePicked,
                onExitEditMessageMode = {
                    onExitEditMessageMode().also {
                        text = TextFieldValue()
                        spans = emptyList()
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
                onVideoCaptured = onVideoCaptured,
                onCreateAndAttachObject = onCreateAndAttachObject,
                onCameraPermissionDenied = onCameraPermissionDenied,
                onAttachmentMenuTriggered = onAttachmentMenuTriggered
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
fun DelayedSyncIndicator(
    isLoading: Boolean,
    isSyncing: Boolean,
    delayMillis: Long = SYNC_INDICATOR_DELAY
) {
    var shouldShowSyncing by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = isSyncing) {
        if (isSyncing) {
            delay(delayMillis)
            if (isSyncing) {
                shouldShowSyncing = true
            }
        } else {
            shouldShowSyncing = false
        }
    }

    if (isLoading || (isSyncing && shouldShowSyncing)) {
        ShimmerEffect(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            colorStart = colorResource(R.color.glyph_active).copy(0.1f),
            colorEnd = colorResource(R.color.glyph_active).copy(0.3f)
        )
    }
}

internal const val DATE_KEY_PREFIX = "date-"
private val JumpToBottomThreshold = 200.dp
private const val FLOATING_DATE_DELAY = 1000L
private const val SYNC_INDICATOR_DELAY = 2000L
