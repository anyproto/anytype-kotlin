package com.anytypeio.anytype.feature_chats.presentation

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.LinkPreview
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.core_models.ext.EMPTY_STRING_VALUE
import com.anytypeio.anytype.core_models.primitives.Space
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_ui.text.splitByMarks
import com.anytypeio.anytype.core_utils.common.DefaultFileInfo
import com.anytypeio.anytype.core_utils.tools.DEFAULT_URL_REGEX
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.onFailure
import com.anytypeio.anytype.domain.base.onSuccess
import com.anytypeio.anytype.domain.chats.AddChatMessage
import com.anytypeio.anytype.domain.chats.ChatContainer
import com.anytypeio.anytype.domain.chats.DeleteChatMessage
import com.anytypeio.anytype.domain.chats.EditChatMessage
import com.anytypeio.anytype.domain.chats.ToggleChatMessageReaction
import com.anytypeio.anytype.domain.media.UploadFile
import com.anytypeio.anytype.domain.misc.GetLinkPreview
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.ActiveSpaceMemberSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.ActiveSpaceMemberSubscriptionContainer.Store
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.objects.CreateObjectFromUrl
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.getTypeOfObject
import com.anytypeio.anytype.feature_chats.BuildConfig
import com.anytypeio.anytype.feature_chats.tools.DummyMessageGenerator
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.confgs.ChatConfig
import com.anytypeio.anytype.presentation.home.OpenObjectNavigation
import com.anytypeio.anytype.presentation.home.navigation
import com.anytypeio.anytype.presentation.mapper.objectIcon
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.SpaceMemberIconView
import com.anytypeio.anytype.presentation.search.GlobalSearchItemView
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.presentation.spaces.spaceIcon
import com.anytypeio.anytype.presentation.util.CopyFileToCacheDirectory
import com.anytypeio.anytype.presentation.vault.ExitToVaultDelegate
import java.text.SimpleDateFormat
import javax.inject.Inject
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class ChatViewModel @Inject constructor(
    private val vmParams: Params.Default,
    private val chatContainer: ChatContainer,
    private val addChatMessage: AddChatMessage,
    private val editChatMessage: EditChatMessage,
    private val deleteChatMessage: DeleteChatMessage,
    private val toggleChatMessageReaction: ToggleChatMessageReaction,
    private val members: ActiveSpaceMemberSubscriptionContainer,
    private val getAccount: GetAccount,
    private val urlBuilder: UrlBuilder,
    private val spaceViews: SpaceViewSubscriptionContainer,
    private val dispatchers: AppCoroutineDispatchers,
    private val uploadFile: UploadFile,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val copyFileToCacheDirectory: CopyFileToCacheDirectory,
    private val exitToVaultDelegate: ExitToVaultDelegate,
    private val getLinkPreview: GetLinkPreview,
    private val createObjectFromUrl: CreateObjectFromUrl
) : BaseViewModel(), ExitToVaultDelegate by exitToVaultDelegate {

    private val visibleRangeUpdates = MutableSharedFlow<Pair<Id, Id>>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val header = MutableStateFlow<HeaderView>(HeaderView.Init)
    val uiState = MutableStateFlow<ChatViewState>(ChatViewState())
    val chatBoxAttachments = MutableStateFlow<List<ChatView.Message.ChatBoxAttachment>>(emptyList())
    val commands = MutableSharedFlow<ViewModelCommand>()
    val uXCommands = MutableSharedFlow<UXCommand>()
    val navigation = MutableSharedFlow<OpenObjectNavigation>()
    val chatBoxMode = MutableStateFlow<ChatBoxMode>(ChatBoxMode.Default())
    val mentionPanelState = MutableStateFlow<MentionPanelState>(MentionPanelState.Hidden)

    private val dateFormatter = SimpleDateFormat("d MMMM YYYY")

    private var account: Id = ""

    init {

//        generateDummyChatHistory()

        viewModelScope.launch {
            spaceViews
                .observe(
                    vmParams.space
                ).map { view ->
                    HeaderView.Default(
                        title = view.name.orEmpty(),
                        icon = view.spaceIcon(
                            builder = urlBuilder,
                            spaceGradientProvider = SpaceGradientProvider.Default
                        )
                    )
                }.collect {
                    header.value = it
                }
        }

        viewModelScope.launch {
            visibleRangeUpdates
                .debounce(300) // Delay to avoid spamming
                .distinctUntilChanged()
                .collect { (from, to) ->
                    chatContainer.onVisibleRangeChanged(from, to)
                }
        }

        viewModelScope.launch {
            getAccount
                .async(Unit)
                .onSuccess { acc ->
                    account = acc.id
                }
                .onFailure {
                    Timber.e("Failed to find account for space-level chat")
                }
            proceedWithObservingChatMessages(
                account = account,
                chat = vmParams.ctx
            )
        }
    }

    private suspend fun proceedWithObservingChatMessages(
        account: Id,
        chat: Id
    ) {
        combine(
            chatContainer
                .watchWhileTrackingAttachments(chat = chat).distinctUntilChanged()
            ,
            chatContainer.fetchAttachments(vmParams.space).distinctUntilChanged(),
            chatContainer.fetchReplies(chat = chat).distinctUntilChanged()
        ) { result, dependencies, replies ->
            Timber.d("DROID-2966 Chat counter state from container: ${result.state}")
            Timber.d("DROID-2966 Intent from container: ${result.intent}")
            Timber.d("DROID-2966 Message results size from container: ${result.messages.size}")
            var previousDate: ChatView.DateSection? = null
            val messageViews = buildList<ChatView> {
                result.messages.forEach { msg ->
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

                    val replyToId = msg.replyToMessageId

                    val reply = if (replyToId.isNullOrEmpty()) {
                        null
                    } else {
                        val msg = replies[replyToId]
                        if (msg != null) {
                            ChatView.Message.Reply(
                                msg = msg.id,
                                text = msg.content?.text.orEmpty().ifEmpty {
                                    // Fallback to attachment name if empty
                                    if (msg.attachments.isNotEmpty()) {
                                        val attachment = msg.attachments.last()
                                        val dependency = dependencies[attachment.target]
                                        val name = dependency?.name.orEmpty()
                                        val ext = dependency?.fileExt
                                        if (!ext.isNullOrEmpty()) {
                                            "$name.$ext"
                                        } else {
                                            name
                                        }
                                    } else {
                                        ""
                                    }
                                },
                                author = allMembers.let { type ->
                                    when (type) {
                                        is Store.Data -> type.members.find { member ->
                                            member.identity == msg.creator
                                        }?.name.orEmpty()
                                        is Store.Empty -> ""
                                    }
                                }
                            )
                        } else {
                            null
                        }
                    }

                    val view = ChatView.Message(
                        id = msg.id,
                        order = msg.order,
                        timestamp = msg.createdAt * 1000,
                        content = ChatView.Message.Content(
                            msg = content?.text.orEmpty(),
                            parts = content?.text
                                .orEmpty()
                                .splitByMarks(marks = content?.marks.orEmpty())
                                .map { (part, styles) ->
                                    ChatView.Message.Content.Part(
                                        part = part,
                                        styles = styles
                                    )
                                }
                        ),
                        reply = reply,
                        author = member?.name ?: msg.creator.takeLast(5),
                        creator = member?.id,
                        isUserAuthor = msg.creator == account,
                        isEdited = msg.modifiedAt > msg.createdAt,
                        reactions = msg.reactions
                            .map { (emoji, ids) ->
                                ChatView.Message.Reaction(
                                    emoji = emoji,
                                    count = ids.size,
                                    isSelected = ids.contains(account)
                                )
                            }
                            .take(ChatConfig.MAX_REACTION_COUNT)
                        ,
                        attachments = msg.attachments.map { attachment ->
                            when (attachment.type) {
                                Chat.Message.Attachment.Type.Image -> {
                                    val wrapper = dependencies[attachment.target]
                                    ChatView.Message.Attachment.Image(
                                        target = attachment.target,
                                        url = urlBuilder.medium(path = attachment.target),
                                        name =  wrapper?.name.orEmpty(),
                                        ext = wrapper?.fileExt.orEmpty()
                                    )
                                }
                                else -> {
                                    val wrapper = dependencies[attachment.target]
                                    if (wrapper?.layout == ObjectType.Layout.IMAGE) {
                                        ChatView.Message.Attachment.Image(
                                            target = attachment.target,
                                            url = urlBuilder.large(path = attachment.target),
                                            name = wrapper.name.orEmpty(),
                                            ext = wrapper.fileExt.orEmpty()
                                        )
                                    } else {
                                        val type = wrapper?.type?.firstOrNull()
                                        ChatView.Message.Attachment.Link(
                                            target = attachment.target,
                                            wrapper = wrapper,
                                            icon = wrapper?.objectIcon(
                                                builder = urlBuilder,
                                                objType = storeOfObjectTypes.getTypeOfObject(wrapper)
                                            ) ?: ObjectIcon.None,
                                            typeName = if (type != null)
                                                storeOfObjectTypes.get(type)?.name.orEmpty()
                                            else
                                                ""
                                        )
                                    }
                                }
                            }
                        }.let { results ->
                            if (results.size >= 2) {
                                val images = results.filterIsInstance<ChatView.Message.Attachment.Image>()
                                if (images.size == results.size) {
                                    listOf(
                                        ChatView.Message.Attachment.Gallery(
                                            images = images
                                        )
                                    )
                                } else {
                                    results
                                }
                            } else {
                                results
                            }
                        },
                        avatar = if (member != null && !member.iconImage.isNullOrEmpty()) {
                            ChatView.Message.Avatar.Image(
                                urlBuilder.thumbnail(member.iconImage!!)
                            )
                        } else {
                            ChatView.Message.Avatar.Initials(member?.name.orEmpty())
                        }
                    )
                    val currDate = ChatView.DateSection(
                        formattedDate = dateFormatter.format(msg.createdAt * 1000),
                        timeInMillis = msg.createdAt * 1000L
                    )
                    if (currDate.formattedDate != previousDate?.formattedDate) {
                        add(currDate)
                        previousDate = currDate
                    }
                    add(view)
                }
            }.reversed()
            ChatViewState(
                messages = messageViews,
                intent = result.intent,
                counter = ChatViewState.Counter(
                    count = result.state.unreadMessages?.counter ?: 0
                )
            )
        }.flowOn(dispatchers.io).distinctUntilChanged().collect {
            uiState.value = it
        }
    }
    
    fun onChatBoxInputChanged(
        selection: IntRange,
        text: String
    ) {
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
            } else {
                Timber.w("Query is empty or results are empty when mention is triggered")
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

    private fun getMentionedMembers(query: MentionPanelState.Query?): List<MentionPanelState.Member> {
        val results = members.get().let { store ->
            when (store) {
                is Store.Data -> {
                    store.members.map { member ->
                        MentionPanelState.Member(
                            member.id,
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

                Store.Empty -> {
                    emptyList()
                }
            }
        }
        return results
    }

    fun onMessageSent(msg: String, markup: List<Block.Content.Text.Mark>) {
        if (BuildConfig.DEBUG) {
            Timber.d("DROID-2635 OnMessageSent, markup: $markup}")
        }
        viewModelScope.launch {
            val urlRegex = Regex(DEFAULT_URL_REGEX)
            val parsedUrls = buildList {
                urlRegex.findAll(msg).forEach { match ->
                    val range = match.range
                    // Adjust the range to include the last character (inclusive end range)
                    val adjustedRange = range.first..range.last + 1
                    val url = match.value

                    // Check if a LINK markup already exists in the same range
                    if (markup.none { it.range == adjustedRange && it.type == Block.Content.Text.Mark.Type.LINK }) {
                        add(
                            Block.Content.Text.Mark(
                                range = adjustedRange,
                                type = Block.Content.Text.Mark.Type.LINK,
                                param = url
                            )
                        )
                    }
                }
            }

            val normalizedMarkup = (markup + parsedUrls).sortedBy { it.range.first }

            chatBoxMode.value = chatBoxMode.value.updateIsSendingBlocked(isBlocked = true)
            val attachments = buildList {
                val currAttachments = chatBoxAttachments.value
                currAttachments.forEachIndexed { idx, attachment ->
                    when(attachment) {
                        is ChatView.Message.ChatBoxAttachment.Link -> {
                            add(
                                Chat.Message.Attachment(
                                    target = attachment.target,
                                    type = Chat.Message.Attachment.Type.Link
                                )
                            )
                        }
                        is ChatView.Message.ChatBoxAttachment.Existing.Link -> {
                            add(
                                Chat.Message.Attachment(
                                    target = attachment.target,
                                    type = Chat.Message.Attachment.Type.Link
                                )
                            )
                        }
                        is ChatView.Message.ChatBoxAttachment.Existing.Image -> {
                            add(
                                Chat.Message.Attachment(
                                    target = attachment.target,
                                    type = Chat.Message.Attachment.Type.Image
                                )
                            )
                        }
                        is ChatView.Message.ChatBoxAttachment.Media -> {
                            chatBoxAttachments.value = currAttachments.toMutableList().apply {
                                set(
                                    index = idx,
                                    element = attachment.copy(
                                        state = ChatView.Message.ChatBoxAttachment.State.Uploading
                                    )
                                )
                            }
                            uploadFile.async(
                                UploadFile.Params(
                                    space = vmParams.space,
                                    path = attachment.uri,
                                    type = Block.Content.File.Type.IMAGE
                                )
                            ).onSuccess { file ->
                                add(
                                    Chat.Message.Attachment(
                                        target = file.id,
                                        type = Chat.Message.Attachment.Type.Image
                                    )
                                )
                                chatBoxAttachments.value = currAttachments.toMutableList().apply {
                                    set(
                                        index = idx,
                                        element = attachment.copy(
                                            state = ChatView.Message.ChatBoxAttachment.State.Uploaded
                                        )
                                    )
                                }
                            }.onFailure {
                                chatBoxAttachments.value = currAttachments.toMutableList().apply {
                                    set(
                                        index = idx,
                                        element = attachment.copy(
                                            state = ChatView.Message.ChatBoxAttachment.State.Uploading
                                        )
                                    )
                                }
                            }
                        }
                        is ChatView.Message.ChatBoxAttachment.Bookmark -> {
                            createObjectFromUrl.async(
                                params = attachment.preview.url
                            ).onSuccess { obj ->
                                if (obj.isValid) {
                                    add(
                                        Chat.Message.Attachment(
                                            target = obj.id,
                                            type = Chat.Message.Attachment.Type.Link
                                        )
                                    )
                                }
                            }.onFailure {

                            }
                        }
                        is ChatView.Message.ChatBoxAttachment.File -> {
                            val path = withContext(dispatchers.io) {
                                copyFileToCacheDirectory.copy(attachment.uri)
                            }
                            if (path != null) {
                                chatBoxAttachments.value = currAttachments.toMutableList().apply {
                                    set(
                                        index = idx,
                                        element = attachment.copy(
                                            state = ChatView.Message.ChatBoxAttachment.State.Uploading
                                        )
                                    )
                                }
                                uploadFile.async(
                                    UploadFile.Params(
                                        space = vmParams.space,
                                        path = path,
                                        type = Block.Content.File.Type.NONE
                                    )
                                ).onSuccess { file ->
                                    // TODO delete file.
                                    add(
                                        Chat.Message.Attachment(
                                            target = file.id,
                                            type = Chat.Message.Attachment.Type.File
                                        )
                                    )
                                    chatBoxAttachments.value = currAttachments.toMutableList().apply {
                                        set(
                                            index = idx,
                                            element = attachment.copy(
                                                state = ChatView.Message.ChatBoxAttachment.State.Uploaded
                                            )
                                        )
                                    }
                                }.onFailure {
                                    Timber.e(it, "DROID-2966 Error while uploading file as attachment")
                                    chatBoxAttachments.value = currAttachments.toMutableList().apply {
                                        set(
                                            index = idx,
                                            element = attachment.copy(
                                                state = ChatView.Message.ChatBoxAttachment.State.Failed
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            when (val mode = chatBoxMode.value) {
                is ChatBoxMode.Default -> {
                    // TODO consider moving this use-case inside chat container
                    addChatMessage.async(
                        params = Command.ChatCommand.AddMessage(
                            chat = vmParams.ctx,
                            message = Chat.Message.new(
                                text = msg.trim(),
                                attachments = attachments,
                                marks = normalizedMarkup
                            )
                        )
                    ).onSuccess { (id, payload) ->
                        chatBoxAttachments.value = emptyList()
                        chatContainer.onPayload(payload)
                        delay(JUMP_TO_BOTTOM_DELAY)
                        uXCommands.emit(UXCommand.JumpToBottom)
                    }.onFailure {
                        Timber.e(it, "Error while adding message")
                    }
                    chatBoxMode.value = ChatBoxMode.Default()
                }
                is ChatBoxMode.EditMessage -> {
                    editChatMessage.async(
                        params = Command.ChatCommand.EditMessage(
                            chat = vmParams.ctx,
                            message = Chat.Message.updated(
                                id = mode.msg,
                                text = msg.trim(),
                                attachments = attachments,
                                marks = normalizedMarkup
                            )
                        )
                    ).onSuccess {
                        delay(JUMP_TO_BOTTOM_DELAY)
                        uXCommands.emit(UXCommand.JumpToBottom)
                        chatBoxAttachments.value = emptyList()
                    }.onFailure {
                        Timber.e(it, "Error while adding message")
                    }.onSuccess {
                        chatBoxMode.value = ChatBoxMode.Default()
                    }
                }
                is ChatBoxMode.Reply -> {
                    addChatMessage.async(
                        params = Command.ChatCommand.AddMessage(
                            chat = vmParams.ctx,
                            message = Chat.Message.new(
                                text = msg.trim(),
                                replyToMessageId = mode.msg,
                                attachments = attachments,
                                marks = normalizedMarkup
                            )
                        )
                    ).onSuccess { (id, payload) ->
                        chatBoxAttachments.value = emptyList()
                        chatContainer.onPayload(payload)
                        delay(JUMP_TO_BOTTOM_DELAY)
                        uXCommands.emit(UXCommand.JumpToBottom)
                    }.onFailure {
                        Timber.e(it, "Error while adding message")
                    }
                    chatBoxMode.value = ChatBoxMode.Default()
                }
            }
        }
    }

    fun onRequestEditMessageClicked(msg: ChatView.Message) {
        Timber.d("onRequestEditMessageClicked")
        viewModelScope.launch {
            chatBoxAttachments.value = buildList {
                msg.attachments.forEach { a ->
                    when(a) {
                        is ChatView.Message.Attachment.Image -> {
                            add(
                                ChatView.Message.ChatBoxAttachment.Existing.Image(
                                    target = a.target,
                                    url = a.url
                                )
                            )
                        }
                        is ChatView.Message.Attachment.Gallery -> {
                            a.images.forEach { image ->
                                add(
                                    ChatView.Message.ChatBoxAttachment.Existing.Image(
                                        target = image.target,
                                        url = image.url
                                    )
                                )
                            }
                        }
                        is ChatView.Message.Attachment.Link -> {
                            val wrapper = a.wrapper
                            if (wrapper != null) {
                                val type = wrapper.type.firstOrNull()
                                add(
                                    ChatView.Message.ChatBoxAttachment.Existing.Link(
                                        target = wrapper.id,
                                        name = wrapper.name.orEmpty(),
                                        icon = wrapper.objectIcon(
                                            builder = urlBuilder,
                                            objType = storeOfObjectTypes.getTypeOfObject(wrapper)
                                        ),
                                        typeName = if (type != null)
                                            storeOfObjectTypes.get(type)?.name.orEmpty()
                                        else
                                            ""
                                    )
                                )
                            }
                        }
                    }
                }
            }
            chatBoxMode.value = ChatBoxMode.EditMessage(msg.id)
        }
    }

    fun onAttachObject(obj: GlobalSearchItemView) {
        chatBoxAttachments.value = chatBoxAttachments.value + listOf(
            ChatView.Message.ChatBoxAttachment.Link(
                target = obj.id,
                wrapper = obj
            )
        )
    }

    fun onClearAttachmentClicked(attachment: ChatView.Message.ChatBoxAttachment) {
        chatBoxAttachments.value = chatBoxAttachments.value.filter {
            it != attachment
        }
    }

    fun onClearReplyClicked() {
        viewModelScope.launch {
            chatBoxMode.value = ChatBoxMode.Default()
        }
    }

    fun onReacted(msg: Id, reaction: String) {
        Timber.d("onReacted")
        viewModelScope.launch {
            val message = uiState.value.messages.find { it is ChatView.Message && it.id == msg }
            if (message != null) {
                toggleChatMessageReaction.async(
                    Command.ChatCommand.ToggleMessageReaction(
                        chat = vmParams.ctx,
                        msg = msg,
                        emoji = reaction
                    )
                ).onFailure {
                    Timber.e(it, "Error while toggling chat message reaction")
                }
            } else {
                Timber.w("Target message not found for reaction")
            }
        }
    }

    fun onReplyMessage(msg: ChatView.Message) {
        viewModelScope.launch {
            chatBoxMode.value = ChatBoxMode.Reply(
                msg = msg.id,
                text = msg.content.msg.ifEmpty {
                    // Fallback to attachment name if empty
                    if (msg.attachments.isNotEmpty()) {
                        val attachment = msg.attachments.last()
                        when(attachment) {
                            is ChatView.Message.Attachment.Image -> {
                                if (attachment.ext.isNotEmpty()) {
                                    "${attachment.name}.${attachment.ext}"
                                } else {
                                    attachment.name
                                }
                            }
                            is ChatView.Message.Attachment.Gallery -> {
                                val first = attachment.images.firstOrNull()
                                if (first != null) {
                                    if (first.ext.isNotEmpty()) {
                                        "${first.name}.${first.ext}"
                                    } else {
                                        first.name
                                    }
                                } else {
                                    EMPTY_STRING_VALUE
                                }
                            }
                            is ChatView.Message.Attachment.Link -> {
                                attachment.wrapper?.name.orEmpty()
                            }
                        }
                    } else {
                        ""
                    }
                },
                author = msg.author,
                isSendingMessageBlocked = false
            )
        }
    }

    fun onDeleteMessage(msg: ChatView.Message) {
        Timber.d("onDeleteMessageClicked")
        viewModelScope.launch {
            deleteChatMessage.async(
                Command.ChatCommand.DeleteMessage(
                    chat = vmParams.ctx,
                    msg = msg.id
                )
            ).onFailure {
                Timber.e(it, "Error while deleting chat message")
            }
        }
    }

    fun onAttachmentClicked(attachment: ChatView.Message.Attachment) {
        Timber.d("onAttachmentClicked")
        viewModelScope.launch {
            when(attachment) {
                is ChatView.Message.Attachment.Image -> {
                    uXCommands.emit(
                        UXCommand.OpenFullScreenImage(
                            url = urlBuilder.original(attachment.target)
                        )
                    )
                }
                is ChatView.Message.Attachment.Gallery -> {
                    // TODO
                }
                is ChatView.Message.Attachment.Link -> {
                    val wrapper = attachment.wrapper
                    if (wrapper != null) {
                        if (wrapper.layout == ObjectType.Layout.BOOKMARK) {
                            val bookmark = ObjectWrapper.Bookmark(wrapper.map)
                            val url = bookmark.source
                            if (!url.isNullOrEmpty()) {
                                commands.emit(ViewModelCommand.Browse(url))
                            } else {
                                // If url not found, open bookmark object instead of browsing.
                                navigation.emit(wrapper.navigation())
                            }
                        } else {
                            navigation.emit(wrapper.navigation())
                        }
                    } else {
                        Timber.w("Wrapper is not found in attachment")
                    }
                }
            }
        }
    }

    fun onChatBoxMediaPicked(uris: List<String>) {
        Timber.d("onChatBoxMediaPicked: $uris")
        chatBoxAttachments.value = chatBoxAttachments.value + uris.map {
            ChatView.Message.ChatBoxAttachment.Media(
                uri = it
            )
        }
    }

    fun onChatBoxFilePicked(infos: List<DefaultFileInfo>) {
        Timber.d("onChatBoxFilePicked: $infos")
        chatBoxAttachments.value = chatBoxAttachments.value + infos.map { info ->
            ChatView.Message.ChatBoxAttachment.File(
                uri = info.uri,
                name = info.name,
                size = info.size
            )
        }
    }

    fun onExitEditMessageMode() {
        viewModelScope.launch {
            chatBoxMode.value = ChatBoxMode.Default()
        }
    }

    fun onBackButtonPressed(isSpaceRoot: Boolean) {
        viewModelScope.launch {
            if (isSpaceRoot) {
                Timber.d("Root space screen. Releasing resources...")
                proceedWithClearingSpaceBeforeExitingToVault()
            }
            commands.emit(ViewModelCommand.Exit)
        }
    }

    fun onSpaceIconClicked() {
        viewModelScope.launch {
            commands.emit(ViewModelCommand.OpenWidgets)
        }
    }

    fun onMediaPreview(url: String) {
        viewModelScope.launch {
            commands.emit(
                ViewModelCommand.MediaPreview(url = url)
            )
        }
    }

    fun onSelectChatReaction(msg: Id) {
        viewModelScope.launch {
            commands.emit(
                ViewModelCommand.SelectChatReaction(
                    msg = msg
                )
            )
        }
    }

    fun onViewChatReaction(
        msg: Id,
        emoji: String
    ) {
        viewModelScope.launch {
            commands.emit(
                ViewModelCommand.ViewChatReaction(
                    msg = msg,
                    emoji = emoji
                )
            )
        }
    }

    fun onMemberIconClicked(member: Id?) {
        viewModelScope.launch {
            if (member != null) {
                commands.emit(
                    ViewModelCommand.ViewMemberCard(
                        member = member,
                        space = vmParams.space
                    )
                )
            } else {
                Timber.e("Space member not found in space-level chat")
            }
        }
    }

    fun onMentionClicked(member: Id) {
        viewModelScope.launch {
            commands.emit(
                ViewModelCommand.ViewMemberCard(
                    member = member,
                    space = vmParams.space
                )
            )
        }
    }

    fun isMentionTriggered(text: String, selectionStart: Int): Boolean {
        if (selectionStart <= 0 || selectionStart > text.length) return false
        val previousChar = text[selectionStart - 1]
        return previousChar == '@'
                && (selectionStart == 1 || !text[selectionStart - 2].isLetterOrDigit())
    }

    fun shouldHideMention(text: String, selectionStart: Int): Boolean {
        if (selectionStart > text.length) return false
        // Check if the current character is a space
        val currentChar = if (selectionStart > 0) text[selectionStart - 1] else null
        // Hide mention when a space is typed, or '@' character has been deleted (even if it was the first character)
        val atCharExists = text.lastIndexOf('@', selectionStart - 1) != -1
        return currentChar == ' ' || !atCharExists
    }

    fun resolveMentionQuery(text: String, selectionStart: Int): MentionPanelState.Query? {
        val atIndex = text.lastIndexOf('@', selectionStart - 1)
        if (atIndex == -1 || (atIndex > 0 && text[atIndex - 1].isLetterOrDigit())) return null
        val endIndex = text.indexOf(' ', atIndex).takeIf { it != -1 } ?: text.length
        val query = text.substring(atIndex + 1, endIndex)
        // Allow empty queries if there's no space after '@'
        return MentionPanelState.Query(query, atIndex until endIndex)
    }

    fun onChatScrolledToTop() {
        Timber.d("DROID-2966 onChatScrolledToTop")
        viewModelScope.launch {
            chatContainer.onLoadPrevious()
        }
    }

    fun onChatScrolledToBottom() {
        Timber.d("DROID-2966 onChatScrolledToBottom")
        viewModelScope.launch {
            chatContainer.onLoadNext()
        }
    }

    fun onChatScrollToReply(replyMessage: Id) {
        Timber.d("DROID-2966 onScrollToReply: $replyMessage")
        viewModelScope.launch {
            chatContainer.onLoadToReply(replyMessage = replyMessage)
        }
    }

    fun onScrollToBottomClicked(lastVisibleMessage: Id?) {
        Timber.d("DROID-2966 onScrollToBottom")
        viewModelScope.launch {
            chatContainer.onLoadChatTail(lastVisibleMessage)
        }
    }

    fun onClearChatViewStateIntent() {
        Timber.d("DROID-2966 onClearChatViewStateIntent")
        viewModelScope.launch {
            chatContainer.onClearIntent()
        }
    }

    fun onVisibleRangeChanged(
        from: Id,
        to: Id
    ) {
        Timber.d("DROID-2966 onVisibleRangeChanged, from: $from, to: $to")
        visibleRangeUpdates.tryEmit(from to to)
    }

    fun onUrlPasted(url: Url) {
        viewModelScope.launch {
            getLinkPreview.async(
                params = url
            ).onSuccess { preview ->
                chatBoxAttachments.value = buildList {
                    addAll(chatBoxAttachments.value)
                    add(
                        ChatView.Message.ChatBoxAttachment.Bookmark(
                            preview = preview
                        )
                    )
                }
            }.onFailure {
                Timber.e(it, "Failed to get link preview")
            }
        }
    }

    /**
     * Used for testing. Will be deleted.
     */
    private fun generateDummyChatHistory() {
        viewModelScope.launch {
            var replyTo: Id? = null
            repeat(100) { idx ->

                addChatMessage.async(
                    Command.ChatCommand.AddMessage(
                        chat = vmParams.ctx,
                        message = DummyMessageGenerator.generateMessage(
                            text = idx.toString(),
                            replyTo = if (idx == 99) replyTo else null
                        )
                    )
                ).onSuccess { (msg, payload) ->
                    if (idx == 0) {
                       replyTo = msg
                    }
                }
            }
        }
    }

    sealed class ViewModelCommand {
        data object Exit : ViewModelCommand()
        data object OpenWidgets : ViewModelCommand()
        data class MediaPreview(val url: String) : ViewModelCommand()
        data class Browse(val url: String) : ViewModelCommand()
        data class SelectChatReaction(val msg: Id) : ViewModelCommand()
        data class ViewChatReaction(val msg: Id, val emoji: String) : ViewModelCommand()
        data class ViewMemberCard(val member: Id, val space: SpaceId) : ViewModelCommand()
    }

    sealed class UXCommand {
        data object JumpToBottom : UXCommand()
        data class SetChatBoxInput(val input: String) : UXCommand()
        data class OpenFullScreenImage(val url: String) : UXCommand()
    }

    sealed class ChatBoxMode {

        abstract val isSendingMessageBlocked: Boolean

        data class Default(
            override val isSendingMessageBlocked: Boolean = false
        ) : ChatBoxMode()
        data class EditMessage(
            val msg: Id,
            override val isSendingMessageBlocked: Boolean = false
        ) : ChatBoxMode()
        data class Reply(
            val msg: Id,
            val text: String,
            val author: String,
            override val isSendingMessageBlocked: Boolean = false
        ): ChatBoxMode()
    }

    private fun ChatBoxMode.updateIsSendingBlocked(isBlocked: Boolean): ChatBoxMode {
        return when (this) {
            is ChatBoxMode.Default -> copy(isSendingMessageBlocked = isBlocked)
            is ChatBoxMode.EditMessage -> copy(isSendingMessageBlocked = isBlocked)
            is ChatBoxMode.Reply -> copy(isSendingMessageBlocked = isBlocked)
        }
    }

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

    sealed class HeaderView {
        data object Init : HeaderView()
        data class Default(
            val icon: SpaceIconView,
            val title: String
        ) : HeaderView()
    }

    sealed class Params {
        abstract val space: Space
        data class Default(
            val ctx: Id,
            override val space: Space
        ) : Params()
    }

    companion object {
        /**
         * Delay before jump-to-bottom after adding new message to the chat.
         */
        const val JUMP_TO_BOTTOM_DELAY = 50L
    }
}