package com.anytypeio.anytype.feature_discussions.presentation

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.core_models.primitives.Space
import com.anytypeio.anytype.core_ui.text.splitByMarks
import com.anytypeio.anytype.core_utils.common.DefaultFileInfo
import com.anytypeio.anytype.core_utils.ext.withLatestFrom
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.base.onFailure
import com.anytypeio.anytype.domain.base.onSuccess
import com.anytypeio.anytype.domain.chats.AddChatMessage
import com.anytypeio.anytype.domain.chats.ChatContainer
import com.anytypeio.anytype.domain.chats.DeleteChatMessage
import com.anytypeio.anytype.domain.chats.EditChatMessage
import com.anytypeio.anytype.domain.chats.ToggleChatMessageReaction
import com.anytypeio.anytype.domain.media.FileDrop
import com.anytypeio.anytype.domain.media.UploadFile
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.ActiveSpaceMemberSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.ActiveSpaceMemberSubscriptionContainer.Store
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.`object`.OpenObject
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.emojifier.data.Emoji
import com.anytypeio.anytype.emojifier.data.EmojiProvider
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.editor.picker.EmojiPickerView
import com.anytypeio.anytype.presentation.home.OpenObjectNavigation
import com.anytypeio.anytype.presentation.home.navigation
import com.anytypeio.anytype.presentation.mapper.objectIcon
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.search.GlobalSearchItemView
import com.anytypeio.anytype.presentation.util.CopyFileToCacheDirectory
import java.sql.Types
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.combineLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class DiscussionViewModel @Inject constructor(
    private val vmParams: Params,
    private val setObjectDetails: SetObjectDetails,
    private val openObject: OpenObject,
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
    private val emojiProvider: EmojiProvider
) : BaseViewModel() {

    val name = MutableStateFlow<String?>(null)
    val messages = MutableStateFlow<List<DiscussionView.Message>>(emptyList())
    val chatBoxAttachments = MutableStateFlow<List<DiscussionView.Message.ChatBoxAttachment>>(emptyList())
    val commands = MutableSharedFlow<UXCommand>()
    val navigation = MutableSharedFlow<OpenObjectNavigation>()
    val chatBoxMode = MutableStateFlow<ChatBoxMode>(ChatBoxMode.Default)

    val emojis = MutableStateFlow<List<EmojiPickerView>>(emptyList())

    var chat: Id = ""

    init {
        viewModelScope.launch {
            val account = requireNotNull(getAccount.async(Unit).getOrNull())
            when (vmParams) {
                is Params.Default -> {
                    chat = vmParams.ctx
                    openObject.async(
                        OpenObject.Params(
                            spaceId = vmParams.space,
                            obj = vmParams.ctx,
                            saveAsLastOpened = false
                        )
                    ).fold(
                        onSuccess = { obj ->
                            val root = ObjectWrapper.Basic(obj.details[vmParams.ctx].orEmpty())
                            name.value = root.name
                            proceedWithObservingChatMessages(
                                account = account.id,
                                chat = vmParams.ctx
                            )
                        },
                        onFailure = {
                            Timber.e(it, "Error while opening chat object")
                        }
                    )
                }
                is Params.SpaceLevelChat -> {
                    val targetSpaceView = spaceViews.get(vmParams.space)
                    val spaceLevelChat = targetSpaceView?.getValue<Id>(Relations.CHAT_ID)
                    if (spaceLevelChat != null) {
                        chat = spaceLevelChat
                        proceedWithObservingChatMessages(
                            account = account.id,
                            chat = spaceLevelChat
                        )
                    }
                }
            }
        }

        viewModelScope.launch {
            emojis.value = loadEmojiWithCategories()
        }
    }

    private suspend fun proceedWithObservingChatMessages(
        account: Id,
        chat: Id
    ) {
        combine(
            chatContainer
                .watchWhileTrackingAttachments(chat = chat),
            chatContainer.fetchAttachments(vmParams.space),
            chatContainer.fetchReplies(chat = chat)
        ) { result, dependencies, replies ->
            result.map { msg ->
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
                        DiscussionView.Message.Reply(
                            msg = msg.id,
                            text = msg.content?.text.orEmpty(),
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

                DiscussionView.Message(
                    id = msg.id,
                    timestamp = msg.createdAt * 1000,
                    content = DiscussionView.Message.Content(
                        msg = content?.text.orEmpty(),
                        parts = content?.text
                            .orEmpty()
                            .splitByMarks(marks = content?.marks.orEmpty())
                            .map { (part, styles) ->
                                DiscussionView.Message.Content.Part(
                                    part = part,
                                    styles = styles
                                )
                            }
                    ),
                    reply = reply,
                    author = member?.name ?: msg.creator.takeLast(5),
                    isUserAuthor = msg.creator == account,
                    isEdited = msg.modifiedAt > msg.createdAt,
                    reactions = msg.reactions.map { (emoji, ids) ->
                        DiscussionView.Message.Reaction(
                            emoji = emoji,
                            count = ids.size,
                            isSelected = ids.contains(account)
                        )
                    },
                    attachments = msg.attachments.map { attachment ->
                        when (attachment.type) {
                            Chat.Message.Attachment.Type.Image -> DiscussionView.Message.Attachment.Image(
                                target = attachment.target,
                                url = urlBuilder.medium(path = attachment.target)
                            )
                            else -> {
                                val wrapper = dependencies[attachment.target]
                                if (wrapper?.layout == ObjectType.Layout.IMAGE) {
                                    DiscussionView.Message.Attachment.Image(
                                        target = attachment.target,
                                        url = urlBuilder.large(path = attachment.target)
                                    )
                                } else {
                                    val type = wrapper?.type?.firstOrNull()
                                    DiscussionView.Message.Attachment.Link(
                                        target = attachment.target,
                                        wrapper = wrapper,
                                        icon = wrapper?.objectIcon(urlBuilder) ?: ObjectIcon.None,
                                        typeName = if (type != null)
                                            storeOfObjectTypes.get(type)?.name.orEmpty()
                                        else
                                            ""
                                    )
                                }
                            }
                        }
                    }.also {
                        if (it.isNotEmpty()) {
                            Timber.d("Chat attachments: $it")
                        }
                    },
                    avatar = if (member != null && !member.iconImage.isNullOrEmpty()) {
                        DiscussionView.Message.Avatar.Image(
                            urlBuilder.thumbnail(member.iconImage!!)
                        )
                    } else {
                        DiscussionView.Message.Avatar.Initials(member?.name.orEmpty())
                    }
                )
            }.reversed()
        }.flowOn(dispatchers.io).collect {
            messages.value = it
        }
    }

    fun onMessageSent(msg: String) {
        Timber.d("DROID-2635 OnMessageSent: $msg")
        viewModelScope.launch {
            val attachments = buildList {
                chatBoxAttachments.value.forEach { attachment ->
                    when(attachment) {
                        is DiscussionView.Message.ChatBoxAttachment.Link -> {
                            add(
                                Chat.Message.Attachment(
                                    target = attachment.target,
                                    type = Chat.Message.Attachment.Type.Link
                                )
                            )
                        }
                        is DiscussionView.Message.ChatBoxAttachment.Media -> {
                            uploadFile.async(
                                UploadFile.Params(
                                    space = vmParams.space,
                                    path = attachment.uri
                                )
                            ).onSuccess { file ->
                                add(
                                    Chat.Message.Attachment(
                                        target = file.id,
                                        type = Chat.Message.Attachment.Type.Image
                                    )
                                )
                            }
                        }
                        is DiscussionView.Message.ChatBoxAttachment.File -> {
                            val path = withContext(dispatchers.io) {
                                copyFileToCacheDirectory.copy(attachment.uri)
                            }
                            if (path != null) {
                                uploadFile.async(
                                    UploadFile.Params(
                                        space = vmParams.space,
                                        path = path
                                    )
                                ).onSuccess { file ->
                                    // TODO delete file.
                                    add(
                                        Chat.Message.Attachment(
                                            target = file.id,
                                            type = Chat.Message.Attachment.Type.File
                                        )
                                    )
                                }.onFailure {
                                    Timber.e(it, "Error while uploading file as attachment")
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
                            chat = chat,
                            message = Chat.Message.new(
                                text = msg,
                                attachments = attachments
                            )
                        )
                    ).onSuccess { (id, payload) ->
                        chatBoxAttachments.value = emptyList()
                        chatContainer.onPayload(payload)
                        delay(JUMP_TO_BOTTOM_DELAY)
                        commands.emit(UXCommand.JumpToBottom)
                    }.onFailure {
                        Timber.e(it, "Error while adding message")
                    }
                }
                is ChatBoxMode.EditMessage -> {
                    editChatMessage.async(
                        params = Command.ChatCommand.EditMessage(
                            chat = chat,
                            message = Chat.Message.updated(
                                id = mode.msg,
                                text = msg,
                                attachments = attachments
                            )
                        )
                    ).onSuccess {
                        delay(JUMP_TO_BOTTOM_DELAY)
                        commands.emit(UXCommand.JumpToBottom)
                        chatBoxAttachments.value = emptyList()
                    }.onFailure {
                        Timber.e(it, "Error while adding message")
                    }.onSuccess {
                        chatBoxMode.value = ChatBoxMode.Default
                    }
                }
                is ChatBoxMode.Reply -> {
                    addChatMessage.async(
                        params = Command.ChatCommand.AddMessage(
                            chat = chat,
                            message = Chat.Message.new(
                                text = msg,
                                replyToMessageId = mode.msg,
                                attachments = attachments
                            )
                        )
                    ).onSuccess { (id, payload) ->
                        chatBoxAttachments.value = emptyList()
                        chatContainer.onPayload(payload)
                        delay(JUMP_TO_BOTTOM_DELAY)
                        commands.emit(UXCommand.JumpToBottom)
                    }.onFailure {
                        Timber.e(it, "Error while adding message")
                    }
                    chatBoxMode.value = ChatBoxMode.Default
                }
            }
        }
    }

    fun onRequestEditMessageClicked(msg: DiscussionView.Message) {
        Timber.d("onRequestEditMessageClicked")
        viewModelScope.launch {
            chatBoxMode.value = ChatBoxMode.EditMessage(msg.id)
        }
    }

    fun onTitleChanged(input: String) {
        Timber.d("DROID-2635 OnTitleChanged: $input")
        viewModelScope.launch {
            name.value = input
            setObjectDetails.async(
                params = SetObjectDetails.Params(
                    ctx = chat,
                    details = mapOf(
                        Relations.NAME to input
                    )
                )
            ).onSuccess {
                Timber.d("Updated chat title successfully")
            }.onFailure {
                Timber.e(it, "Error while updating chat title")
            }
        }
    }

    fun onAttachObject(obj: GlobalSearchItemView) {
        chatBoxAttachments.value = chatBoxAttachments.value + listOf(
            DiscussionView.Message.ChatBoxAttachment.Link(
                target = obj.id,
                wrapper = obj
            )
        )
    }

    fun onClearAttachmentClicked(attachment: DiscussionView.Message.ChatBoxAttachment) {
        chatBoxAttachments.value = chatBoxAttachments.value.filter {
            it != attachment
        }
    }

    fun onClearReplyClicked() {
        viewModelScope.launch {
            chatBoxMode.value = ChatBoxMode.Default
        }
    }

    fun onReacted(msg: Id, reaction: String) {
        Timber.d("onReacted")
        viewModelScope.launch {
            val message = messages.value.find { it.id == msg }
            if (message != null) {
                toggleChatMessageReaction.async(
                    Command.ChatCommand.ToggleMessageReaction(
                        chat = chat,
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

    fun onReplyMessage(msg: DiscussionView.Message) {
        viewModelScope.launch {
            chatBoxMode.value = ChatBoxMode.Reply(
                msg = msg.id,
                text = msg.content.msg,
                author = msg.author
            )
        }
    }

    fun onDeleteMessage(msg: DiscussionView.Message) {
        Timber.d("onDeleteMessageClicked")
        viewModelScope.launch {
            deleteChatMessage.async(
                Command.ChatCommand.DeleteMessage(
                    chat = chat,
                    msg = msg.id
                )
            ).onFailure {
                Timber.e(it, "Error while deleting chat message")
            }
        }
    }

    fun onAttachmentClicked(attachment: DiscussionView.Message.Attachment) {
        Timber.d("onAttachmentClicked")
        viewModelScope.launch {
            when(attachment) {
                is DiscussionView.Message.Attachment.Image -> {
                    commands.emit(
                        UXCommand.OpenFullScreenImage(
                            url = urlBuilder.original(attachment.target)
                        )
                    )
                }
                is DiscussionView.Message.Attachment.Link -> {
                    val wrapper = attachment.wrapper
                    if (wrapper != null) {
                        navigation.emit(wrapper.navigation())
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
            DiscussionView.Message.ChatBoxAttachment.Media(
                uri = it
            )
        }
    }

    fun onChatBoxFilePicked(infos: List<DefaultFileInfo>) {
        Timber.d("onChatBoxFilePicked: $infos")
        chatBoxAttachments.value = chatBoxAttachments.value + infos.map { info ->
            DiscussionView.Message.ChatBoxAttachment.File(
                uri = info.uri,
                name = info.name,
                size = info.size
            )
        }
    }

    fun onExitEditMessageMode() {
        viewModelScope.launch {
            chatBoxMode.value = ChatBoxMode.Default
        }
    }

    private suspend fun loadEmojiWithCategories() : List<EmojiPickerView> = withContext(dispatchers.io) {
        buildList {
            emojiProvider.emojis.forEachIndexed { categoryIndex, emojis ->
                add(
                    EmojiPickerView.GroupHeader(
                        category = categoryIndex
                    )
                )
                emojis.forEachIndexed { emojiIndex, emoji ->
                    val skin = Emoji.COLORS.any { color -> emoji.contains(color) }
                    if (!skin)
                        add(
                            EmojiPickerView.Emoji(
                                unicode = emoji,
                                page = categoryIndex,
                                index = emojiIndex
                            )
                        )
                }
            }
        }
    }

    sealed class UXCommand {
        data object JumpToBottom : UXCommand()
        data class SetChatBoxInput(val input: String) : UXCommand()
        data class OpenFullScreenImage(val url: String) : UXCommand()
    }

    sealed class ChatBoxMode {
        data object Default : ChatBoxMode()
        data class EditMessage(val msg: Id) : ChatBoxMode()
        data class Reply(
            val msg: Id,
            val text: String,
            val author: String
        ): ChatBoxMode()
    }

    sealed class Params {

        abstract val space: Space

        data class Default(
            val ctx: Id,
            override val space: Space
        ) : Params()

        data class SpaceLevelChat(
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