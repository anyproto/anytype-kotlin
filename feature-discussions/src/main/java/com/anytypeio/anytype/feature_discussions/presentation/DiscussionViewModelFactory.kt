package com.anytypeio.anytype.feature_discussions.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.chats.AddChatMessage
import com.anytypeio.anytype.domain.chats.ChatContainer
import com.anytypeio.anytype.domain.chats.DeleteChatMessage
import com.anytypeio.anytype.domain.chats.ToggleChatMessageReaction
import com.anytypeio.anytype.domain.multiplayer.ActiveSpaceMemberSubscriptionContainer
import com.anytypeio.anytype.domain.`object`.OpenObject
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.presentation.common.BaseViewModel
import javax.inject.Inject

class DiscussionViewModelFactory @Inject constructor(
    private val params: BaseViewModel.DefaultParams,
    private val setObjectDetails: SetObjectDetails,
    private val openObject: OpenObject,
    private val chatContainer: ChatContainer,
    private val addChatMessage: AddChatMessage,
    private val deleteChatMessage: DeleteChatMessage,
    private val toggleChatMessageReaction: ToggleChatMessageReaction,
    private val members: ActiveSpaceMemberSubscriptionContainer,
    private val getAccount: GetAccount
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = DiscussionViewModel(
        params = params,
        setObjectDetails = setObjectDetails,
        openObject = openObject,
        chatContainer = chatContainer,
        addChatMessage = addChatMessage,
        toggleChatMessageReaction = toggleChatMessageReaction,
        members = members,
        getAccount = getAccount,
        deleteChatMessage = deleteChatMessage
    ) as T
}