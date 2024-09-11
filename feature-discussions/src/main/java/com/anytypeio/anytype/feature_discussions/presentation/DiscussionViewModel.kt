package com.anytypeio.anytype.feature_discussions.presentation

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.base.onFailure
import com.anytypeio.anytype.domain.base.onSuccess
import com.anytypeio.anytype.domain.chats.AddChatMessage
import com.anytypeio.anytype.domain.chats.ChatContainer
import com.anytypeio.anytype.domain.chats.ToggleChatMessageReaction
import com.anytypeio.anytype.domain.multiplayer.ActiveSpaceMemberSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.ActiveSpaceMemberSubscriptionContainer.Store
import com.anytypeio.anytype.domain.`object`.OpenObject
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.search.GlobalSearchItemView
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

class DiscussionViewModel(
    private val params: DefaultParams,
    private val setObjectDetails: SetObjectDetails,
    private val openObject: OpenObject,
    private val chatContainer: ChatContainer,
    private val addChatMessage: AddChatMessage,
    private val toggleChatMessageReaction: ToggleChatMessageReaction,
    private val members: ActiveSpaceMemberSubscriptionContainer,
    private val getAccount: GetAccount
) : BaseViewModel() {

    val name = MutableStateFlow<String?>(null)
    val messages = MutableStateFlow<List<DiscussionView.Message>>(emptyList())
    val attachments = MutableStateFlow<List<GlobalSearchItemView>>(emptyList())
    val commands = MutableSharedFlow<UXCommand>()

    // TODO naive implementation; switch to state
    private lateinit var chat: Id

    init {
        viewModelScope.launch {
            val account = requireNotNull(getAccount.async(Unit).getOrNull())
            openObject.async(
                OpenObject.Params(
                    spaceId = params.space,
                    obj = params.ctx,
                    saveAsLastOpened = false
                )
            ).fold(
                onSuccess = { obj ->
                    val root = ObjectWrapper.Basic(obj.details[params.ctx].orEmpty())
                    name.value = root.name
                    proceedWithObservingChatMessages(
                        account = account.id,
                        root = root
                    )
                },
                onFailure = {
                    Timber.e(it, "Error while opening chat object")
                }
            )
        }
    }

    private suspend fun proceedWithObservingChatMessages(
        account: Id,
        root: ObjectWrapper.Basic
    ) {
        val chat = root.getValue<Id>(Relations.CHAT_ID)
        if (chat != null) {
            this.chat = chat
            chatContainer
                .watch(chat)
                .onEach { Timber.d("Got new update: $it") }
                .collect {
                    messages.value = it.map { msg ->
                        DiscussionView.Message(
                            id = msg.id,
                            timestamp = msg.timestamp * 1000,
                            msg = msg.content?.text.orEmpty(),
                            author = members.get().let { store ->
                                when(store) {
                                    is Store.Data -> store.members.find { member ->
                                        member.identity == msg.creator
                                    }?.name ?: msg.creator.takeLast(5)
                                    is Store.Empty -> msg.creator.takeLast(5)
                                }
                            },
                            isUserAuthor = msg.creator == account,
                            reactions = msg.reactions.map{ (emoji, ids) ->
                                DiscussionView.Message.Reaction(
                                    emoji = emoji,
                                    count = ids.size,
                                    isSelected = ids.contains(account)
                                )
                            }
                        )
                    }.reversed()
                }
        } else {
            Timber.w("Chat ID was missing in chat smart-object details")
        }
    }

    fun onMessageSent(msg: String) {
        Timber.d("DROID-2635 OnMessageSent: $msg")
        viewModelScope.launch {
            addChatMessage.async(
                params = Command.ChatCommand.AddMessage(
                    chat = chat,
                    message = Chat.Message.new(msg)
                )
            ).onSuccess {
                delay(JUMP_TO_BOTTOM_DELAY)
                commands.emit(UXCommand.JumpToBottom)
            }.onFailure {
                Timber.e(it, "Error while adding message")
            }
        }
    }

    fun onTitleChanged(input: String) {
        Timber.d("DROID-2635 OnTitleChanged: $input")
        viewModelScope.launch {
            name.value = input
            setObjectDetails.async(
                params = SetObjectDetails.Params(
                    ctx = params.ctx,
                    details = mapOf(
                        Relations.NAME to input
                    )
                )
            )
        }
    }

    fun onAttachObject(obj: GlobalSearchItemView) {
        attachments.value = listOf(obj)
    }

    fun onClearAttachmentClicked() {
        attachments.value = emptyList()
    }

    fun onReacted(msg: Id, reaction: String) {
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

    sealed class UXCommand {
        data object JumpToBottom: UXCommand()
    }

    companion object {
        /**
         * Delay before jump-to-bottom after adding new message to the chat.
         */
        const val JUMP_TO_BOTTOM_DELAY = 150L
    }
}