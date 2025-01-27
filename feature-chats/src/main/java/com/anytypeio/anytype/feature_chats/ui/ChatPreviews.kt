package com.anytypeio.anytype.feature_chats.ui

import android.content.res.Configuration
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.anytypeio.anytype.feature_chats.R
import com.anytypeio.anytype.feature_chats.presentation.ChatView
import com.anytypeio.anytype.feature_chats.presentation.ChatViewModel
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Light Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Dark Mode")
@Composable
fun ChatPreview() {
    Messages(
        messages = listOf(
            ChatView.Message(
                id = "1",
                content = ChatView.Message.Content(
                    msg = stringResource(id = R.string.default_text_placeholder),
                    parts = listOf(
                        ChatView.Message.Content.Part(
                            part = stringResource(id = R.string.default_text_placeholder)
                        )
                    )
                ),
                author = "Walter",
                timestamp = System.currentTimeMillis(),
                creator = ""
            ),
            ChatView.Message(
                id = "2",
                content = ChatView.Message.Content(
                    msg = stringResource(id = R.string.default_text_placeholder),
                    parts = listOf(
                        ChatView.Message.Content.Part(
                            part = stringResource(id = R.string.default_text_placeholder)
                        )
                    )
                ),
                author = "Leo",
                timestamp = System.currentTimeMillis(),
                creator = ""
            ),
            ChatView.Message(
                id = "3",
                content = ChatView.Message.Content(
                    msg = stringResource(id = R.string.default_text_placeholder),
                    parts = listOf(
                        ChatView.Message.Content.Part(
                            part = stringResource(id = R.string.default_text_placeholder)
                        )
                    )
                ),
                author = "Gilbert",
                timestamp = System.currentTimeMillis(),
                creator = ""
            )
        ),
        scrollState = LazyListState(),
        onReacted = { a, b -> },
        onDeleteMessage = {},
        onCopyMessage = {},
        onAttachmentClicked = {},
        onEditMessage = {},
        onMarkupLinkClicked = {},
        onReplyMessage = {},
        onAddReactionClicked = {},
        onViewChatReaction = { a, b -> },
        onMemberIconClicked = {},
        onMentionClicked = {}
    )
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Light Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Dark Mode")
@Composable
fun ChatScreenPreview() {
    ChatScreen(
        messages = buildList {
            repeat(30) { idx ->
                add(
                    ChatView.Message(
                        id = idx.toString(),
                        content = ChatView.Message.Content(
                            msg = stringResource(id = R.string.default_text_placeholder),
                            parts = listOf(
                                ChatView.Message.Content.Part(
                                    part = stringResource(id = R.string.default_text_placeholder)
                                )
                            )
                        ),
                        author = "User ${idx.inc()}",
                        timestamp =
                        System.currentTimeMillis()
                                - 30.toDuration(DurationUnit.DAYS).inWholeMilliseconds
                                + idx.toDuration(DurationUnit.DAYS).inWholeMilliseconds,
                        creator = "random id"
                    )
                )
            }
        }.reversed(),
        onMessageSent = {},
        attachments = emptyList(),
        onClearAttachmentClicked = {},
        lazyListState = LazyListState(),
        onReacted = { a, b -> },
        onCopyMessage = {},
        onDeleteMessage = {},
        onAttachmentClicked = {},
        onEditMessage = {},
        onExitEditMessageMode = {},
        onMarkupLinkClicked = {},
        onAttachObjectClicked = {},
        onReplyMessage = {},
        chatBoxMode = ChatViewModel.ChatBoxMode.Default,
        onClearReplyClicked = {},
        onChatBoxMediaPicked = {},
        onChatBoxFilePicked = {},
        onAddReactionClicked = {},
        onViewChatReaction = { a, b -> },
        onMemberIconClicked = {},
        onMentionClicked = {}
    )
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Light Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Dark Mode")
@Composable
fun BubblePreview() {
    Bubble(
        name = "Leo Marx",
        content = ChatView.Message.Content(
            msg = stringResource(id = R.string.default_text_placeholder),
            parts = listOf(
                ChatView.Message.Content.Part(
                    part = stringResource(id = R.string.default_text_placeholder)
                )
            )
        ),
        timestamp = System.currentTimeMillis(),
        onReacted = {},
        onDeleteMessage = {},
        onCopyMessage = {},
        onAttachmentClicked = {},
        onEditMessage = {},
        onMarkupLinkClicked = {},
        onReply = {},
        onScrollToReplyClicked = {},
        onAddReactionClicked = {},
        onViewChatReaction = {},
        onMentionClicked = {}
    )
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Light Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Dark Mode")
@Composable
fun BubbleEditedPreview() {
    Bubble(
        name = "Leo Marx",
        content = ChatView.Message.Content(
            msg = stringResource(id = R.string.default_text_placeholder),
            parts = listOf(
                ChatView.Message.Content.Part(
                    part = stringResource(id = R.string.default_text_placeholder)
                )
            )
        ),
        isEdited = true,
        timestamp = System.currentTimeMillis(),
        onReacted = {},
        onDeleteMessage = {},
        onCopyMessage = {},
        onAttachmentClicked = {},
        onEditMessage = {},
        onMarkupLinkClicked = {},
        onReply = {},
        onScrollToReplyClicked = {},
        onAddReactionClicked = {},
        onViewChatReaction = {},
        onMentionClicked = {}
    )
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Light Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Dark Mode")
@Composable
fun BubbleWithAttachmentPreview() {
    Bubble(
        name = "Leo Marx",
        content = ChatView.Message.Content(
            msg = stringResource(id = R.string.default_text_placeholder),
            parts = listOf(
                ChatView.Message.Content.Part(
                    part = stringResource(id = R.string.default_text_placeholder)
                )
            )
        ),
        timestamp = System.currentTimeMillis(),
        onReacted = {},
        onDeleteMessage = {},
        onCopyMessage = {},
        attachments = buildList {
            add(
                ChatView.Message.Attachment.Link(
                    target = "ID",
                    wrapper = null,
                    typeName = "Page"
                )
            )
        },
        onAttachmentClicked = {},
        onEditMessage = {},
        onMarkupLinkClicked = {},
        onReply = {},
        onScrollToReplyClicked = {},
        onAddReactionClicked = {},
        onViewChatReaction = {},
        onMentionClicked = {}
    )
}