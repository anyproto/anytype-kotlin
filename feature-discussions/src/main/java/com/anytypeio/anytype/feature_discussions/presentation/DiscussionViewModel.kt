package com.anytypeio.anytype.feature_discussions.presentation

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.chats.AddChatMessage
import com.anytypeio.anytype.domain.chats.ChatContainer
import com.anytypeio.anytype.domain.`object`.OpenObject
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.search.GlobalSearchItemView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class DiscussionViewModel(
    private val params: DefaultParams,
    private val setObjectDetails: SetObjectDetails,
    private val openObject: OpenObject,
    private val chatContainer: ChatContainer,
    private val addChatMessage: AddChatMessage
) : BaseViewModel() {

    val name = MutableStateFlow<String?>(null)
    val messages = MutableStateFlow<List<DiscussionView.Message>>(emptyList())
    val attachments = MutableStateFlow<List<GlobalSearchItemView>>(emptyList())

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
                    Timber.d("DROID-2635 Opened object: $root")
                    name.value = root.name
                }
            )
            chatContainer.watch(
                params.ctx
            ).collect {
                messages.value = it.map { msg ->
                    DiscussionView.Message(
                        id = msg.id,
                        timestamp = msg.timestamp,
                        author = msg.creator,
                        msg = msg.content?.text.orEmpty()
                    )
                }
            }
        }
    }

    fun onMessageSent(msg: String) {
        Timber.d("DROID-2635 OnMessageSent: $msg")
        viewModelScope.launch {
            addChatMessage.async(
                params = Command.ChatCommand.AddMessage(
                    chat = params.ctx,
                    message = Chat.Message(
                        id = "",
                        timestamp = 0L,
                        attachments = emptyList(),
                        reactions = emptyMap(),
                        creator = "",
                        replyToMessageId = "",
                        content = Chat.Message.Content(
                            text = msg,
                            marks = emptyList(),
                            style = Block.Content.Text.Style.P
                        ),
                        order = ""
                    )
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