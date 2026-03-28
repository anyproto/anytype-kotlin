package com.anytypeio.anytype.feature_discussions.presentation

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.core_models.primitives.Space
import com.anytypeio.anytype.core_ui.text.splitByMarks
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.onFailure
import com.anytypeio.anytype.domain.base.onSuccess
import com.anytypeio.anytype.domain.chats.ChatContainer
import com.anytypeio.anytype.domain.multiplayer.ActiveSpaceMemberSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.ActiveSpaceMemberSubscriptionContainer.Store
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.domain.misc.DateProvider
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import timber.log.Timber

sealed class DiscussionInputMode {
    data object Default : DiscussionInputMode()
    data class Reply(
        val msg: Id,
        val text: String,
        val author: String
    ) : DiscussionInputMode()
}

class DiscussionViewModel @Inject constructor(
    private val vmParams: Params,
    private val chatContainer: ChatContainer,
    private val members: ActiveSpaceMemberSubscriptionContainer,
    private val getAccount: GetAccount,
    private val urlBuilder: UrlBuilder,
    private val dispatchers: AppCoroutineDispatchers,
    private val addChatMessage: AddComment,
    private val dateProvider: DateProvider
) : BaseViewModel() {

    private val _header = MutableStateFlow(DiscussionHeader())
    val header: StateFlow<DiscussionHeader> = _header

    private val _messages = MutableStateFlow<List<DiscussionView>>(emptyList())
    val messages: StateFlow<List<DiscussionView>> = _messages

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val datePattern = "d MMMM yyyy, HH:mm"

    private val _inputMode = MutableStateFlow<DiscussionInputMode>(DiscussionInputMode.Default)
    val inputMode: StateFlow<DiscussionInputMode> = _inputMode

    private var account: Id = ""

    init {
        viewModelScope.launch {
            getAccount
                .async(Unit)
                .onSuccess { acc ->
                    account = acc.id
                }
                .onFailure {
                    Timber.e(it, "Failed to find account for discussion")
                }

            proceedWithObservingMessages()
        }
    }

    private suspend fun proceedWithObservingMessages() {
        chatContainer
            .watch(chat = vmParams.ctx)
            .distinctUntilChanged()
            .flowOn(dispatchers.io)
            .catch { e ->
                Timber.e(e, "Error observing discussion messages")
            }
            .collect { result ->
                val commentViews = buildList {
                    result.messages.forEach { msg ->
                        val formattedMsgDate = dateProvider.formatToDateString(
                            timestamp = msg.createdAt * 1000,
                            pattern = datePattern
                        )

                        val allMembers = members.get()
                        val member = allMembers.let { type ->
                            when (type) {
                                is Store.Data -> type.members.find { member ->
                                    member.identity == msg.creator
                                }
                                is Store.Empty -> null
                            }
                        }

                        val content = msg.content

                        val avatar = member?.iconImage?.let { iconImage ->
                            if (iconImage.isNotEmpty()) {
                                DiscussionView.Avatar.Image(
                                    hash = iconImage,
                                    fallbackInitial = member.name?.firstOrNull()?.uppercase().orEmpty()
                                )
                            } else {
                                DiscussionView.Avatar.Initials(
                                    initial = member.name?.firstOrNull()?.uppercase().orEmpty()
                                )
                            }
                        } ?: DiscussionView.Avatar.Initials()

                        val isReply = !msg.replyToMessageId.isNullOrEmpty()

                        val mappedContent = DiscussionView.Content(
                            msg = content?.text.orEmpty(),
                            parts = content?.text
                                .orEmpty()
                                .let { text ->
                                    text.splitByMarks(marks = content?.marks.orEmpty())
                                }
                                .map { (part, styles) ->
                                    DiscussionView.Content.Part(
                                        part = part,
                                        styles = styles
                                    )
                                }
                        )

                        val reactions = msg.reactions
                            .toList()
                            .sortedByDescending { (_, ids) -> ids.size }
                            .map { (emoji, ids) ->
                                DiscussionView.Reaction(
                                    emoji = emoji,
                                    count = ids.size,
                                    isSelected = ids.contains(account)
                                )
                            }

                        if (isReply) {
                            add(
                                DiscussionView.Reply(
                                    id = msg.id,
                                    content = mappedContent,
                                    author = member?.name.orEmpty(),
                                    creator = member?.id,
                                    timestamp = msg.createdAt * 1000,
                                    formattedDate = formattedMsgDate,
                                    reactions = reactions,
                                    avatar = avatar
                                )
                            )
                        } else {
                            add(
                                DiscussionView.Comment(
                                    id = msg.id,
                                    content = mappedContent,
                                    author = member?.name.orEmpty(),
                                    creator = member?.id,
                                    timestamp = msg.createdAt * 1000,
                                    formattedDate = formattedMsgDate,
                                    reactions = reactions,
                                    replyCount = 0,
                                    avatar = avatar
                                )
                            )
                        }
                    }
                }

                // Thread-reordering: group replies under their parent comment

                val replyToMap: Map<Id, Id?> = result.messages.associate {
                    it.id to it.replyToMessageId
                }


                fun depthOf(messageId: Id): Int {
                    var current = messageId
                    var depth = 0
                    val visited = mutableSetOf<Id>()
                    while (true) {
                        val parentId = replyToMap[current]
                        if (parentId.isNullOrEmpty() || !visited.add(current)) return depth
                        depth++
                        current = parentId
                    }
                }

                val topLevelComments = mutableListOf<DiscussionView.Comment>()

                for (view in commentViews) {
                    when (view) {
                        is DiscussionView.Comment -> topLevelComments.add(view)
                        else -> { /* Reply — handled below */ }
                    }
                }

                // Build children lookup: parentId -> list of reply views sorted by timestamp
                val childrenMap = mutableMapOf<Id, MutableList<DiscussionView.Reply>>()
                for (view in commentViews) {
                    if (view is DiscussionView.Reply) {
                        val parentId = replyToMap[view.id]
                        if (!parentId.isNullOrEmpty()) {
                            childrenMap.getOrPut(parentId) { mutableListOf() }.add(view)
                        }
                    }
                }
                childrenMap.values.forEach { list -> list.sortBy { it.timestamp } }

                // DFS traversal to produce tree-ordered replies with depth
                fun collectReplies(parentId: Id): List<DiscussionView.Reply> = buildList {
                    for (child in childrenMap[parentId].orEmpty()) {
                        add(child.copy(depth = depthOf(child.id)))
                        addAll(collectReplies(child.id))
                    }
                }

                val reordered = buildList<DiscussionView> {
                    for (comment in topLevelComments) {
                        val replies = collectReplies(comment.id)
                        add(comment.copy(replyCount = replies.size))
                        replies.forEachIndexed { index, reply ->
                            if (index > 0) {
                                add(DiscussionView.ReplyDivider(
                                    replyId = reply.id,
                                    depth = reply.depth
                                ))
                            }
                            add(reply)
                        }
                        add(DiscussionView.ThreadDivider(threadId = comment.id))
                    }
                }

                _messages.value = reordered
                _header.value = DiscussionHeader(
                    commentCount = topLevelComments.size
                )
                _isLoading.value = false
            }
    }

    fun onReplyComment(comment: DiscussionView.Comment) {
        _inputMode.value = DiscussionInputMode.Reply(
            msg = comment.id,
            text = comment.content.msg,
            author = comment.author
        )
    }

    fun onReplyToReply(reply: DiscussionView.Reply) {
        _inputMode.value = DiscussionInputMode.Reply(
            msg = reply.id,
            text = reply.content.msg,
            author = reply.author
        )
    }

    fun onClearReply() {
        _inputMode.value = DiscussionInputMode.Default
    }

    fun onSendComment(text: String) {
        if (text.isBlank()) return
        val mode = inputMode.value
        viewModelScope.launch {
            addChatMessage.async(
                params = Command.ChatCommand.AddMessage(
                    chat = vmParams.ctx,
                    message = Chat.Message.new(
                        text = text.trim(),
                        marks = emptyList(),
                        replyToMessageId = if (mode is DiscussionInputMode.Reply) mode.msg else null
                    )
                )
            ).onSuccess { (id, payload) ->
                chatContainer.onPayload(payload)
                _inputMode.value = DiscussionInputMode.Default
            }.onFailure { e ->
                Timber.e(e, "Failed to send comment")
            }
        }
    }

    data class Params(
        val ctx: Id,
        val space: Space
    )
}
