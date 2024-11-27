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
import com.anytypeio.anytype.core_utils.ext.withLatestFrom
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.base.onFailure
import com.anytypeio.anytype.domain.base.onSuccess
import com.anytypeio.anytype.domain.chats.AddChatMessage
import com.anytypeio.anytype.domain.chats.ChatContainer
import com.anytypeio.anytype.domain.chats.DeleteChatMessage
import com.anytypeio.anytype.domain.chats.EditChatMessage
import com.anytypeio.anytype.domain.chats.ToggleChatMessageReaction
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.ActiveSpaceMemberSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.ActiveSpaceMemberSubscriptionContainer.Store
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.`object`.OpenObject
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.home.OpenObjectNavigation
import com.anytypeio.anytype.presentation.search.GlobalSearchItemView
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
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
    private val spaceViews: SpaceViewSubscriptionContainer
) : BaseViewModel() {

    val name = MutableStateFlow<String?>(null)
    val messages = MutableStateFlow<List<DiscussionView.Message>>(emptyList())
    val attachments = MutableStateFlow<List<GlobalSearchItemView>>(emptyList())
    val commands = MutableSharedFlow<UXCommand>()
    val navigation = MutableSharedFlow<OpenObjectNavigation>()
    val chatBoxMode = MutableStateFlow<ChatBoxMode>(ChatBoxMode.Default)

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
    }

    private suspend fun proceedWithObservingChatMessages(
        account: Id,
        chat: Id
    ) {
        chatContainer
            .watchWhileTrackingAttachments(chat = chat)
            .withLatestFrom(chatContainer.fetchAttachments(vmParams.space)) { result, dependencies ->
                result.map { msg ->
                    val member = members.get().let { type ->
                        when (type) {
                            is Store.Data -> type.members.find { member ->
                                member.identity == msg.creator
                            }
                            is Store.Empty -> null
                        }
                    }

                    val content = msg.content

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
                            when(attachment.type) {
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
                                        DiscussionView.Message.Attachment.Link(
                                            target = attachment.target,
                                            wrapper = wrapper
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
            }
            .collect { result ->
                messages.value = result
            }
    }

    fun onMessageSent(msg: String) {
        Timber.d("DROID-2635 OnMessageSent: $msg")
        viewModelScope.launch {
            when (val mode = chatBoxMode.value) {
                is ChatBoxMode.Default -> {
                    // TODO consider moving this use-case inside chat container
                    addChatMessage.async(
                        params = Command.ChatCommand.AddMessage(
                            chat = chat,
                            message = Chat.Message.new(
                                text = msg,
                                attachments = attachments.value.map { a ->
                                    Chat.Message.Attachment(
                                        target = a.id,
                                        type = Chat.Message.Attachment.Type.Link
                                    )
                                }
                            )
                        )
                    ).onSuccess { (id, payload) ->
                        attachments.value = emptyList()
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
                                text = msg
                            )
                        )
                    ).onSuccess {
                        delay(JUMP_TO_BOTTOM_DELAY)
                        commands.emit(UXCommand.JumpToBottom)
                    }.onFailure {
                        Timber.e(it, "Error while adding message")
                    }.onSuccess {
                        chatBoxMode.value = ChatBoxMode.Default
                    }
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
        attachments.value = listOf(obj)
    }

    fun onClearAttachmentClicked() {
        attachments.value = emptyList()
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
        viewModelScope.launch {
//            // TODO naive implementation. Currently used for debugging.
//            navigation.emit(
//                OpenObjectNavigation.OpenEditor(
//                    target = attachment.target,
//                    space = vmParams.space.id
//                )
//            )
        }
    }

    fun onExitEditMessageMode() {
        viewModelScope.launch {
            chatBoxMode.value = ChatBoxMode.Default
        }
    }

    sealed class UXCommand {
        data object JumpToBottom : UXCommand()
        data class SetChatBoxInput(val input: String) : UXCommand()
    }

    sealed class ChatBoxMode {
        data object Default : ChatBoxMode()
        data class EditMessage(val msg: Id) : ChatBoxMode()
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