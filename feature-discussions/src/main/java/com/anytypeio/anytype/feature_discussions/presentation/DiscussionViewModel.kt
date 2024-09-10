package com.anytypeio.anytype.feature_discussions.presentation

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.chats.AddChatMessage
import com.anytypeio.anytype.domain.chats.ChatContainer
import com.anytypeio.anytype.domain.multiplayer.ActiveSpaceMemberSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.ActiveSpaceMemberSubscriptionContainer.Store
import com.anytypeio.anytype.domain.`object`.OpenObject
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.search.GlobalSearchItemView
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
    private val members: ActiveSpaceMemberSubscriptionContainer
) : BaseViewModel() {

    val name = MutableStateFlow<String?>(null)
    val messages = MutableStateFlow<List<DiscussionView.Message>>(emptyList())
    val attachments = MutableStateFlow<List<GlobalSearchItemView>>(emptyList())

    lateinit var chat: Id

    init {
        viewModelScope.launch {
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
                    proceedWithObservingChatMessages(root)
                },
                onFailure = {
                    Timber.e(it, "Error while opening chat object")
                }
            )
        }
    }

    private suspend fun proceedWithObservingChatMessages(root: ObjectWrapper.Basic) {
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
            )
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
}