package com.anytypeio.anytype.feature_chats.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.views.Caption1Medium
import com.anytypeio.anytype.feature_chats.R
import com.anytypeio.anytype.feature_chats.presentation.ChatView
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun Messages(
    modifier: Modifier = Modifier,
    messages: List<ChatView>,
    isLoading: Boolean = false,
    scrollState: LazyListState,
    onReacted: (Id, String) -> Unit,
    onDeleteMessage: (ChatView.Message) -> Unit,
    onDeleteMessageWarningTriggered: () -> Unit,
    onCopyMessage: (ChatView.Message) -> Unit,
    onAttachmentClicked: (ChatView.Message, ChatView.Message.Attachment) -> Unit,
    onEditMessage: (ChatView.Message) -> Unit,
    onReplyMessage: (ChatView.Message) -> Unit,
    onMarkupLinkClicked: (String) -> Unit,
    onAddReactionClicked: (String) -> Unit,
    onViewChatReaction: (Id, String) -> Unit,
    onMemberIconClicked: (Id?) -> Unit,
    onMentionClicked: (Id) -> Unit,
    onScrollToReplyClicked: (Id) -> Unit,
    onHighlightMessage: (Id) -> Unit,
    onAddMembersClick: () -> Unit,
    onShowQRCodeClick: () -> Unit,
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
                        .then(
                            if (isHighlighted)
                                Modifier.background(
                                    color = colorResource(R.color.transparent_active).copy(alpha = 0.1f)
                                )
                            else
                                Modifier
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .animateItem()
                        .horizontalSwipeToReply(
                            swipeThreshold = with(LocalDensity.current) { SWIPE_THRESHOLD_DP.toPx() },
                            onReplyTriggered = { onReplyMessage(msg) }
                        )
                    ,
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
                        isSynced = msg.isSynced,
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
                        onAttachmentClicked = {
                            onAttachmentClicked(msg, it)
                        },
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
                        onDeleteMessageWarningTriggered = onDeleteMessageWarningTriggered
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
            if (isLoading) {
                item {
                    LoadingState(modifier = Modifier.fillParentMaxSize())
                }
            } else {
                item {
                    EmptyState(
                        modifier = Modifier.fillParentMaxSize(),
                        onAddMembersClick = onAddMembersClick,
                        onShowQRCodeClick = onShowQRCodeClick
                    )
                }
            }
        }
    }
}