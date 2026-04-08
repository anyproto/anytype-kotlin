package com.anytypeio.anytype.feature_discussions.presentation

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SupportedLayouts
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.core_models.misc.OpenObjectNavigation
import com.anytypeio.anytype.core_models.misc.navigation
import com.anytypeio.anytype.core_models.primitives.Space
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.ui.SpaceMemberIconView
import com.anytypeio.anytype.core_models.ui.objectIcon
import com.anytypeio.anytype.core_ui.text.splitByMarks
import com.anytypeio.anytype.core_utils.common.DefaultFileInfo
import com.anytypeio.anytype.core_utils.ext.cancel
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.onFailure
import com.anytypeio.anytype.domain.base.onSuccess
import com.anytypeio.anytype.domain.chats.ChatContainer
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.library.StoreSearchByIdsParams
import com.anytypeio.anytype.domain.media.PreloadFile
import com.anytypeio.anytype.domain.media.UploadFile
import com.anytypeio.anytype.domain.multiplayer.ActiveSpaceMemberSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.ActiveSpaceMemberSubscriptionContainer.Store
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.getTypeOfObject
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.feature_discussions.ui.DiscussionLinkDetector
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.util.CopyFileToCacheDirectory
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private val deleteComment: DeleteComment,
    private val toggleCommentReaction: ToggleCommentReaction,
    private val dateProvider: DateProvider,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val fieldParser: FieldParser,
    private val subscription: StorelessSubscriptionContainer,
    private val uploadFile: UploadFile,
    private val preloadFile: PreloadFile,
    private val copyFileToCacheDirectory: CopyFileToCacheDirectory
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

    private val _commands = MutableSharedFlow<DiscussionCommand>()
    val commands: SharedFlow<DiscussionCommand> = _commands

    private val _navigation = MutableSharedFlow<OpenObjectNavigation>()
    val navigation: SharedFlow<OpenObjectNavigation> = _navigation

    val mentionPanelState = MutableStateFlow<MentionPanelState>(MentionPanelState.Hidden)

    private val blockLinkTargets = MutableStateFlow<Set<Id>>(emptySet())
    private var latestDependencies: Map<Id, ObjectWrapper.Basic> = emptyMap()

    private var account: Id = ""

    val commentAttachments = MutableStateFlow<List<CommentAttachment>>(emptyList())
    private val preloadingJobs = mutableListOf<Job>()

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

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun proceedWithObservingMessages() {
        val chatFlow = chatContainer
            .watch(chat = vmParams.ctx)
            .distinctUntilChanged()
            .onEach { result ->
                val linkIds = mutableSetOf<Id>()
                result.messages.forEach { msg ->
                    msg.blocks.forEach { block ->
                        if (block is Chat.Message.MessageBlock.Link) {
                            linkIds.add(block.targetObjectId)
                        }
                    }
                }
                blockLinkTargets.value = linkIds
            }

        val dependenciesFlow = blockLinkTargets
            .flatMapLatest { ids ->
                if (ids.isEmpty()) {
                    kotlinx.coroutines.flow.flowOf(emptyMap())
                } else {
                    subscription.subscribe(
                        searchParams = StoreSearchByIdsParams(
                            subscription = "${vmParams.ctx}/discussion-block-links",
                            space = SpaceId(vmParams.space.id),
                            targets = ids.toList(),
                            keys = BLOCK_LINK_KEYS
                        )
                    ).map { wrappers ->
                        wrappers.associateBy { it.id }
                    }
                }
            }
            .catch { e ->
                Timber.e(e, "Error subscribing to block link targets")
                emit(emptyMap())
            }

        combine(chatFlow, dependenciesFlow) { result, dependencies ->
            result to dependencies
        }
            .flowOn(dispatchers.io)
            .catch { e ->
                Timber.e(e, "Error observing discussion messages")
            }
            .collect { (result, dependencies) ->
                latestDependencies = dependencies
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
                                    hash = urlBuilder.thumbnail(iconImage),
                                    fallbackInitial = member.name?.firstOrNull()?.uppercase().orEmpty()
                                )
                            } else {
                                DiscussionView.Avatar.Initials(
                                    initial = member.name?.firstOrNull()?.uppercase().orEmpty()
                                )
                            }
                        } ?: DiscussionView.Avatar.Initials()

                        val isReply = !msg.replyToMessageId.isNullOrEmpty()

                        val mappedContent = if (msg.blocks.isNotEmpty()) {
                            flattenBlocksToContent(msg.blocks)
                        } else {
                            DiscussionView.Content(
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
                        }

                        val contentBlocks = if (msg.blocks.isNotEmpty()) {
                            mapBlocksToContentBlocks(msg.blocks, dependencies)
                        } else {
                            emptyList()
                        }

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
                                    contentBlocks = contentBlocks,
                                    author = member?.name.orEmpty(),
                                    creator = member?.id,
                                    timestamp = msg.createdAt * 1000,
                                    formattedDate = formattedMsgDate,
                                    reactions = reactions,
                                    avatar = avatar,
                                    isOwn = msg.creator == account
                                )
                            )
                        } else {
                            add(
                                DiscussionView.Comment(
                                    id = msg.id,
                                    content = mappedContent,
                                    contentBlocks = contentBlocks,
                                    author = member?.name.orEmpty(),
                                    creator = member?.id,
                                    timestamp = msg.createdAt * 1000,
                                    formattedDate = formattedMsgDate,
                                    reactions = reactions,
                                    replyCount = 0,
                                    avatar = avatar,
                                    isOwn = msg.creator == account
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
                    topLevelComments.forEachIndexed { commentIndex, comment ->
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
                        if (commentIndex < topLevelComments.lastIndex) {
                            add(DiscussionView.ThreadDivider(threadId = comment.id))
                        }
                    }
                }

                _messages.value = reordered
                _header.value = DiscussionHeader(
                    commentCount = topLevelComments.size
                )
                _isLoading.value = false
            }
    }

    // region Mention panel

    fun onInputChanged(selection: IntRange, text: String) {
        val query = resolveMentionQuery(
            text = text,
            selectionStart = selection.start
        )
        if (isMentionTriggered(text, selection.start)) {
            val results = getMentionedMembers(query)
            if (query != null && results.isNotEmpty()) {
                mentionPanelState.value = MentionPanelState.Visible(
                    results = results,
                    query = query
                )
            }
        } else if (shouldHideMention(text, selection.start)) {
            mentionPanelState.value = MentionPanelState.Hidden
        } else {
            val results = getMentionedMembers(query)
            if (results.isNotEmpty() && query != null) {
                mentionPanelState.value = MentionPanelState.Visible(
                    results = results,
                    query = query
                )
            } else {
                mentionPanelState.value = MentionPanelState.Hidden
            }
        }
    }

    private fun getMentionedMembers(
        query: MentionPanelState.Query?
    ): List<MentionPanelState.Member> {
        val results = members.get().let { store ->
            when (store) {
                is Store.Data -> {
                    store.members
                        .filter { member -> member.permissions?.isAtLeastReader() == true }
                        .map { member ->
                            MentionPanelState.Member(
                                id = member.id,
                                name = member.name.orEmpty(),
                                icon = SpaceMemberIconView.icon(
                                    obj = member,
                                    urlBuilder = urlBuilder
                                ),
                                isUser = member.identity == account
                            )
                        }.filter { m ->
                            if (query != null) {
                                m.name.contains(query.query, true)
                            } else {
                                true
                            }
                        }
                }
                Store.Empty -> emptyList()
            }
        }
        return results
    }

    private fun isMentionTriggered(text: String, selectionStart: Int): Boolean {
        if (selectionStart <= 0 || selectionStart > text.length) return false
        val previousChar = text[selectionStart - 1]
        return previousChar == '@'
                && (selectionStart == 1 || !text[selectionStart - 2].isLetterOrDigit())
    }

    private fun shouldHideMention(text: String, selectionStart: Int): Boolean {
        if (selectionStart > text.length) return false
        val query = resolveMentionQuery(text, selectionStart)
        return query == null
    }

    private fun resolveMentionQuery(
        text: String,
        selectionStart: Int
    ): MentionPanelState.Query? {
        val atIndex = text.lastIndexOf('@', selectionStart - 1)
        if (atIndex == -1) return null

        val beforeAt = text.getOrNull(atIndex - 1)
        if (beforeAt != null && beforeAt.isLetterOrDigit()) return null

        val endIndex = text.indexOfAny(charArrayOf(' ', '\n'), startIndex = atIndex)
            .takeIf { it != -1 } ?: text.length

        val query = text.substring(atIndex + 1, endIndex)
        return MentionPanelState.Query(query, atIndex until endIndex)
    }

    // endregion

    sealed class MentionPanelState {
        data object Hidden : MentionPanelState()
        data class Visible(
            val results: List<Member>,
            val query: Query
        ) : MentionPanelState()

        data class Member(
            val id: Id,
            val name: String,
            val icon: SpaceMemberIconView,
            val isUser: Boolean = false
        )

        data class Query(
            val query: String,
            val range: IntRange
        )
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

    fun onAddReaction(msg: Id) {
        viewModelScope.launch {
            _commands.emit(DiscussionCommand.SelectReaction(msg = msg))
        }
    }

    fun onToggleReaction(msg: Id, emoji: String) {
        viewModelScope.launch {
            toggleCommentReaction.async(
                Command.ChatCommand.ToggleMessageReaction(
                    chat = vmParams.ctx,
                    msg = msg,
                    emoji = emoji
                )
            ).onFailure { e ->
                Timber.e(e, "Failed to toggle comment reaction")
            }
        }
    }

    fun onDeleteComment(id: Id) {
        viewModelScope.launch {
            deleteComment.async(
                params = Command.ChatCommand.DeleteMessage(
                    chat = vmParams.ctx,
                    msg = id
                )
            ).onSuccess {
                val mode = _inputMode.value
                if (mode is DiscussionInputMode.Reply && mode.msg == id) {
                    _inputMode.value = DiscussionInputMode.Default
                }
            }.onFailure { e ->
                Timber.e(e, "Failed to delete comment")
            }
        }
    }

    fun onSendComment(text: String, marks: List<Block.Content.Text.Mark>) {
        if (text.isBlank() && commentAttachments.value.isEmpty()) return
        val mode = inputMode.value
        val leadingSpaces = text.length - text.trimStart().length
        val trimmedText = text.trim()
        val adjustedMarks = marks.mapNotNull { mark ->
            val newStart = (mark.range.first - leadingSpaces).coerceAtLeast(0)
            val newEnd = (mark.range.last - leadingSpaces).coerceAtMost(trimmedText.length)
            if (newStart < newEnd) {
                mark.copy(range = newStart..newEnd)
            } else {
                null
            }
        }
        val marksWithLinks = DiscussionLinkDetector.addLinkMarksToText(
            text = trimmedText,
            existingMarks = adjustedMarks
        )
        viewModelScope.launch {
            preloadingJobs.cancel()

            val additionalBlocks = buildList {
                val currAttachments = commentAttachments.value
                currAttachments.forEachIndexed { idx, attachment ->
                    when (attachment) {
                        is CommentAttachment.Media -> {
                            commentAttachments.value = commentAttachments.value.toMutableList().apply {
                                set(idx, attachment.copy(state = CommentAttachment.State.Uploading))
                            }
                            val state = attachment.state
                            var preloadedFileId: Id? = null
                            var path: String
                            if (state is CommentAttachment.State.Preloaded) {
                                preloadedFileId = state.preloadedFileId
                                path = state.path
                            } else {
                                path = if (attachment.capturedByCamera) {
                                    try {
                                        withContext(dispatchers.io) {
                                            copyFileToCacheDirectory.copy(attachment.uri)
                                        }
                                    } catch (e: Exception) {
                                        Timber.e(e, "Failed to copy media file to cache: ${attachment.uri}")
                                        commentAttachments.value = commentAttachments.value.toMutableList().apply {
                                            set(idx, attachment.copy(state = CommentAttachment.State.Failed))
                                        }
                                        return@forEachIndexed
                                    }
                                } else {
                                    attachment.uri
                                }
                            }
                            uploadFile.async(
                                UploadFile.Params(
                                    space = vmParams.space,
                                    path = path,
                                    type = if (attachment.isVideo)
                                        Block.Content.File.Type.VIDEO
                                    else
                                        Block.Content.File.Type.IMAGE,
                                    preloadFileId = preloadedFileId,
                                    createdInContext = vmParams.ctx
                                )
                            ).onSuccess { file ->
                                val linkType = if (attachment.isVideo)
                                    Chat.Message.MessageBlock.Link.LinkType.FILE
                                else
                                    Chat.Message.MessageBlock.Link.LinkType.IMAGE
                                add(
                                    Chat.Message.MessageBlock.Link(
                                        targetObjectId = file.id,
                                        type = linkType
                                    )
                                )
                            }.onFailure {
                                Timber.e(it, "Error while uploading media attachment")
                                commentAttachments.value = commentAttachments.value.toMutableList().apply {
                                    set(idx, attachment.copy(state = CommentAttachment.State.Failed))
                                }
                            }
                        }
                        is CommentAttachment.File -> {
                            commentAttachments.value = commentAttachments.value.toMutableList().apply {
                                set(idx, attachment.copy(state = CommentAttachment.State.Uploading))
                            }
                            val state = attachment.state
                            var preloadedFileId: Id? = null
                            var path: String?
                            if (state is CommentAttachment.State.Preloaded) {
                                preloadedFileId = state.preloadedFileId
                                path = state.path
                            } else {
                                path = try {
                                    withContext(dispatchers.io) {
                                        copyFileToCacheDirectory.copy(attachment.uri)
                                    }
                                } catch (e: Exception) {
                                    Timber.e(e, "Failed to copy file to cache: ${attachment.uri}")
                                    commentAttachments.value = commentAttachments.value.toMutableList().apply {
                                        set(idx, attachment.copy(state = CommentAttachment.State.Failed))
                                    }
                                    return@forEachIndexed
                                }
                            }
                            uploadFile.async(
                                UploadFile.Params(
                                    space = vmParams.space,
                                    path = path,
                                    type = Block.Content.File.Type.NONE,
                                    preloadFileId = preloadedFileId,
                                    createdInContext = vmParams.ctx
                                )
                            ).onSuccess { file ->
                                add(
                                    Chat.Message.MessageBlock.Link(
                                        targetObjectId = file.id,
                                        type = Chat.Message.MessageBlock.Link.LinkType.FILE
                                    )
                                )
                            }.onFailure {
                                Timber.e(it, "Error while uploading file attachment")
                                commentAttachments.value = commentAttachments.value.toMutableList().apply {
                                    set(idx, attachment.copy(state = CommentAttachment.State.Failed))
                                }
                            }
                        }
                    }
                }
            }

            if (trimmedText.isEmpty() && additionalBlocks.isEmpty()) {
                Timber.w("Aborting send: no text and all attachments failed")
                return@launch
            }

            addChatMessage.async(
                params = Command.ChatCommand.AddMessage(
                    chat = vmParams.ctx,
                    message = Chat.Message.newWithBlocks(
                        text = trimmedText,
                        marks = marksWithLinks,
                        replyToMessageId = if (mode is DiscussionInputMode.Reply) mode.msg else null,
                        additionalBlocks = additionalBlocks
                    )
                )
            ).onSuccess { (id, payload) ->
                chatContainer.onPayload(payload)
                _inputMode.value = DiscussionInputMode.Default
                mentionPanelState.value = MentionPanelState.Hidden
                commentAttachments.value = emptyList()
            }.onFailure { e ->
                Timber.e(e, "Failed to send comment")
            }
        }
    }

    fun onCommentMediaPicked(uris: List<MediaUri>) {
        commentAttachments.value += uris.map { uri ->
            CommentAttachment.Media(
                uri = uri.uri,
                isVideo = uri.isVideo,
                capturedByCamera = uri.capturedByCamera
            )
        }
        preloadingJobs += viewModelScope.launch {
            uris.forEach { info ->
                val path = if (info.capturedByCamera) {
                    withContext(dispatchers.io) {
                        copyFileToCacheDirectory.copy(info.uri)
                    }
                } else {
                    info.uri
                }
                preloadFile.async(
                    params = PreloadFile.Params(
                        space = vmParams.space,
                        path = path,
                        type = Block.Content.File.Type.NONE
                    )
                ).onSuccess { preloadedFileId: Id ->
                    commentAttachments.value = commentAttachments.value.map { a ->
                        if (a is CommentAttachment.Media && a.uri == info.uri) {
                            a.copy(
                                state = CommentAttachment.State.Preloaded(
                                    preloadedFileId = preloadedFileId,
                                    path = path
                                )
                            )
                        } else {
                            a
                        }
                    }
                }.onFailure {
                    commentAttachments.value = commentAttachments.value.map { a ->
                        if (a is CommentAttachment.Media && a.uri == info.uri) {
                            a.copy(state = CommentAttachment.State.Failed)
                        } else {
                            a
                        }
                    }
                }
            }
        }
    }

    fun onCommentFilePicked(infos: List<DefaultFileInfo>) {
        commentAttachments.value += infos.map { info ->
            CommentAttachment.File(
                uri = info.uri,
                name = info.name,
                size = info.size,
                state = CommentAttachment.State.Preloading
            )
        }
        preloadingJobs += viewModelScope.launch {
            infos.forEach { info ->
                val path = withContext(dispatchers.io) {
                    copyFileToCacheDirectory.copy(info.uri)
                }
                preloadFile.async(
                    params = PreloadFile.Params(
                        space = vmParams.space,
                        path = path,
                        type = Block.Content.File.Type.NONE
                    )
                ).onSuccess { preloadedFileId: Id ->
                    commentAttachments.value = commentAttachments.value.map { a ->
                        if (a is CommentAttachment.File && a.uri == info.uri) {
                            a.copy(
                                state = CommentAttachment.State.Preloaded(
                                    preloadedFileId = preloadedFileId,
                                    path = path
                                )
                            )
                        } else {
                            a
                        }
                    }
                }.onFailure {
                    commentAttachments.value = commentAttachments.value.map { a ->
                        if (a is CommentAttachment.File && a.uri == info.uri) {
                            a.copy(state = CommentAttachment.State.Failed)
                        } else {
                            a
                        }
                    }
                }
            }
        }
    }

    fun onClearAttachment(attachment: CommentAttachment) {
        commentAttachments.value = commentAttachments.value.filter { it != attachment }
    }

    data class MediaUri(
        val uri: String,
        val isVideo: Boolean = false,
        val capturedByCamera: Boolean = false
    )

    private suspend fun mapBlocksToContentBlocks(
        blocks: List<Chat.Message.MessageBlock>,
        dependencies: Map<Id, ObjectWrapper.Basic>
    ): List<DiscussionView.ContentBlock> = buildList {
        for (block in blocks) {
            when (block) {
                is Chat.Message.MessageBlock.Text -> {
                    val leadingSpaces = block.text.length - block.text.trimStart().length
                    val trimmedText = block.text.trim()
                    val adjustedMarks = block.marks.mapNotNull { mark ->
                        val newStart = (mark.range.first - leadingSpaces).coerceAtLeast(0)
                        val newEnd = (mark.range.last - leadingSpaces).coerceAtMost(trimmedText.length)
                        if (newStart < newEnd) {
                            mark.copy(range = newStart..newEnd)
                        } else {
                            null
                        }
                    }
                    val parts = trimmedText
                        .splitByMarks(marks = adjustedMarks)
                        .map { (part, styles) ->
                            DiscussionView.Content.Part(part = part, styles = styles)
                        }
                    add(
                        DiscussionView.ContentBlock.Text(
                            content = DiscussionView.Content(msg = trimmedText, parts = parts)
                        )
                    )
                }
                is Chat.Message.MessageBlock.Link -> {
                    add(mapLinkBlock(block, dependencies))
                }
                is Chat.Message.MessageBlock.Embed -> {
                    val parts = listOf(
                        DiscussionView.Content.Part(part = block.text)
                    )
                    add(
                        DiscussionView.ContentBlock.Text(
                            content = DiscussionView.Content(msg = block.text, parts = parts)
                        )
                    )
                }
            }
        }
    }

    private suspend fun mapLinkBlock(
        block: Chat.Message.MessageBlock.Link,
        dependencies: Map<Id, ObjectWrapper.Basic>
    ): DiscussionView.ContentBlock {
        val wrapper = dependencies[block.targetObjectId]

        // IMAGE type or IMAGE layout
        if (block.type == Chat.Message.MessageBlock.Link.LinkType.IMAGE
            || wrapper?.layout == ObjectType.Layout.IMAGE
        ) {
            return DiscussionView.ContentBlock.Image(
                targetObjectId = block.targetObjectId,
                url = urlBuilder.original(block.targetObjectId)
            )
        }

        // BOOKMARK type or BOOKMARK layout
        if (block.type == Chat.Message.MessageBlock.Link.LinkType.BOOKMARK
            || wrapper?.layout == ObjectType.Layout.BOOKMARK
        ) {
            val imageHash = wrapper?.getSingleValue<String?>(Relations.PICTURE)
            return DiscussionView.ContentBlock.Bookmark(
                targetObjectId = block.targetObjectId,
                url = wrapper?.getSingleValue<String>(Relations.SOURCE).orEmpty(),
                title = wrapper?.name.orEmpty(),
                description = wrapper?.description.orEmpty(),
                imageUrl = if (!imageHash.isNullOrEmpty()) urlBuilder.large(imageHash) else null
            )
        }

        // OBJECT / FILE / other — render as linked object card
        if (wrapper != null && wrapper.isValid) {
            val objType = storeOfObjectTypes.getTypeOfObject(wrapper)
            val (_, typeName) = fieldParser.getObjectTypeIdAndName(
                wrapper,
                storeOfObjectTypes.getAll()
            )
            val title = resolveObjectTitle(wrapper)
            return DiscussionView.ContentBlock.Link(
                targetObjectId = block.targetObjectId,
                title = title,
                typeName = typeName,
                icon = wrapper.objectIcon(
                    builder = urlBuilder,
                    objType = objType
                )
            )
        }

        // Fallback: render as mention text when wrapper is not available
        val placeholder = block.targetObjectId
        val mark = Block.Content.Text.Mark(
            range = IntRange(0, placeholder.length),
            type = Block.Content.Text.Mark.Type.MENTION,
            param = block.targetObjectId
        )
        val parts = placeholder
            .splitByMarks(marks = listOf(mark))
            .map { (part, styles) ->
                DiscussionView.Content.Part(part = part, styles = styles)
            }
        return DiscussionView.ContentBlock.Text(
            content = DiscussionView.Content(msg = placeholder, parts = parts)
        )
    }

    private fun resolveObjectTitle(wrapper: ObjectWrapper.Basic): String {
        return when (wrapper.layout) {
            ObjectType.Layout.NOTE -> wrapper.snippet.orEmpty()
            in SupportedLayouts.fileLayouts -> {
                val fileName = wrapper.name.orEmpty()
                val fileExt = wrapper.fileExt
                when {
                    fileExt.isNullOrBlank() -> fileName
                    fileName.endsWith(".$fileExt") -> fileName
                    else -> "$fileName.$fileExt"
                }
            }
            else -> wrapper.name.orEmpty()
        }
    }

    private fun flattenBlocksToContent(
        blocks: List<Chat.Message.MessageBlock>
    ): DiscussionView.Content {
        val segments = mutableListOf<Pair<String, List<Block.Content.Text.Mark>>>()
        for (block in blocks) {
            when (block) {
                is Chat.Message.MessageBlock.Text -> {
                    val leadingSpaces = block.text.length - block.text.trimStart().length
                    val trimmedText = block.text.trim()
                    val adjustedMarks = block.marks.mapNotNull { mark ->
                        val newStart = (mark.range.first - leadingSpaces).coerceAtLeast(0)
                        val newEnd = (mark.range.last - leadingSpaces).coerceAtMost(trimmedText.length)
                        if (newStart < newEnd) {
                            mark.copy(range = newStart..newEnd)
                        } else {
                            null
                        }
                    }
                    segments.add(trimmedText to adjustedMarks)
                }
                is Chat.Message.MessageBlock.Link -> {
                    val placeholder = block.targetObjectId
                    val mark = Block.Content.Text.Mark(
                        range = IntRange(0, placeholder.length),
                        type = Block.Content.Text.Mark.Type.MENTION,
                        param = block.targetObjectId
                    )
                    segments.add(placeholder to listOf(mark))
                }
                is Chat.Message.MessageBlock.Embed -> {
                    segments.add(block.text to emptyList())
                }
            }
        }

        val combinedText = StringBuilder()
        val allMarks = mutableListOf<Block.Content.Text.Mark>()
        for ((index, segment) in segments.withIndex()) {
            val (text, marks) = segment
            val offset = combinedText.length
            combinedText.append(text)
            for (mark in marks) {
                allMarks.add(
                    mark.copy(
                        range = IntRange(
                            start = mark.range.first + offset,
                            endInclusive = mark.range.last + offset
                        )
                    )
                )
            }
            if (index < segments.size - 1) {
                combinedText.append("\n")
            }
        }

        val resultText = combinedText.toString()
        return DiscussionView.Content(
            msg = resultText,
            parts = resultText
                .splitByMarks(marks = allMarks)
                .map { (part, styles) ->
                    DiscussionView.Content.Part(
                        part = part,
                        styles = styles
                    )
                }
        )
    }

    data class Params(
        val ctx: Id,
        val space: Space
    )

    fun onMentionClicked(id: Id) {
        viewModelScope.launch {
            _commands.emit(
                DiscussionCommand.ViewMemberCard(
                    member = id,
                    space = vmParams.space
                )
            )
        }
    }

    fun onContentBlockClicked(block: DiscussionView.ContentBlock) {
        viewModelScope.launch {
            when (block) {
                is DiscussionView.ContentBlock.Image -> {
                    _commands.emit(
                        DiscussionCommand.MediaPreview(
                            obj = block.targetObjectId
                        )
                    )
                }
                is DiscussionView.ContentBlock.Bookmark -> {
                    if (block.url.isNotEmpty()) {
                        _commands.emit(DiscussionCommand.Browse(block.url))
                    }
                }
                is DiscussionView.ContentBlock.Link -> {
                    val wrapper = latestDependencies[block.targetObjectId]
                    if (wrapper != null && wrapper.isValid) {
                        _navigation.emit(wrapper.navigation())
                    } else {
                        Timber.w("Wrapper not found for content block link: ${block.targetObjectId}")
                    }
                }
                is DiscussionView.ContentBlock.Text -> {
                    // Text blocks handle their own clicks (mentions, links)
                }
            }
        }
    }

    sealed class DiscussionCommand {
        data class SelectReaction(val msg: Id) : DiscussionCommand()
        data class ViewMemberCard(val member: Id, val space: Space) : DiscussionCommand()
        data class Browse(val url: String) : DiscussionCommand()
        data class MediaPreview(val obj: Id) : DiscussionCommand()
    }

    companion object {
        private val BLOCK_LINK_KEYS = listOf(
            Relations.ID,
            Relations.SPACE_ID,
            Relations.PICTURE,
            Relations.SOURCE,
            Relations.DESCRIPTION,
            Relations.NAME,
            Relations.ICON_IMAGE,
            Relations.ICON_EMOJI,
            Relations.ICON_NAME,
            Relations.ICON_OPTION,
            Relations.TYPE,
            Relations.LAYOUT,
            Relations.IS_ARCHIVED,
            Relations.IS_DELETED,
            Relations.DONE,
            Relations.SNIPPET,
            Relations.SIZE_IN_BYTES,
            Relations.FILE_MIME_TYPE,
            Relations.FILE_EXT
        )
    }
}
