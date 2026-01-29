package com.anytypeio.anytype.feature_chats.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.base.getOrDefault
import com.anytypeio.anytype.domain.base.onFailure
import com.anytypeio.anytype.domain.chats.GetChatMessagesByIds
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.ActiveSpaceMemberSubscriptionContainer
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.core_models.ui.SpaceMemberIconView
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

class ChatReactionViewModel @Inject constructor(
    private val vmParams: Params,
    private val getChatMessagesByIds: GetChatMessagesByIds,
    private val getAccount: GetAccount,
    private val members: ActiveSpaceMemberSubscriptionContainer,
    private val urlBuilder: UrlBuilder
) : BaseViewModel() {

    val viewState = MutableStateFlow<ViewState>(ViewState.Init(vmParams.emoji))

    init {
        viewModelScope.launch {
            val account = getAccount
                .async(Unit)
                .onFailure {
                    Timber.e(it, "Failed to get account for chat reaction screen")
                }
                .getOrNull()

            val result = getChatMessagesByIds
                .async(
                    Command.ChatCommand.GetMessagesByIds(
                        chat = vmParams.chat,
                        messages = listOf(vmParams.msg)
                    )
                )
            val msg = result.getOrDefault(emptyList()).firstOrNull()
            if (msg != null) {
                val identities = msg.reactions.getOrDefault(
                    key = vmParams.emoji,
                    defaultValue = emptyList()
                )
                if (identities.isNotEmpty()) {
                    members.observe().map { store ->
                        when(store) {
                            is ActiveSpaceMemberSubscriptionContainer.Store.Data -> {
                                identities.mapNotNull { identity ->
                                    val member = store.members.firstOrNull { it.identity == identity }
                                    if (member != null) {
                                        ViewState.Member(
                                            icon = SpaceMemberIconView.icon(
                                                obj = member,
                                                urlBuilder = urlBuilder
                                            ),
                                            name = member.name.orEmpty(),
                                            isUser = account?.id == member.identity
                                        )
                                    } else {
                                        null
                                    }
                                }
                            }
                            is ActiveSpaceMemberSubscriptionContainer.Store.Empty -> {
                                emptyList<ViewState.Member>()
                            }
                        }
                    }.collect {
                        viewState.value = ViewState.Success(
                            emoji = vmParams.emoji,
                            members = it
                        )
                    }
                } else {
                    viewState.value = ViewState.Empty(
                        emoji = vmParams.emoji
                    )
                }
            } else {
                viewState.value = ViewState.Error.MessageNotFound(
                    emoji = vmParams.emoji
                )
            }
        }
    }

    class Factory @Inject constructor(
        private val vmParams: Params,
        private val getChatMessagesByIds: GetChatMessagesByIds,
        private val getAccount: GetAccount,
        private val members: ActiveSpaceMemberSubscriptionContainer,
        private val urlBuilder: UrlBuilder
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ChatReactionViewModel(
            vmParams = vmParams,
            getChatMessagesByIds = getChatMessagesByIds,
            getAccount = getAccount,
            members = members,
            urlBuilder = urlBuilder
        ) as T
    }

    data class Params @Inject constructor(
        val chat: Id,
        val msg: Id,
        val emoji: String
    )

    sealed class ViewState {
        abstract val emoji: String

        data class Init(
            override val emoji: String
        ) : ViewState()

        sealed class Error : ViewState() {
            data class MessageNotFound(
                override val emoji: String
            ) : Error()
        }

        data class Loading(
            override val emoji: String
        ) : ViewState()

        data class Empty(
            override val emoji: String
        ): ViewState()

        data class Success(
            override val emoji: String,
            val members: List<Member>
        ) : ViewState()

        data class Member(
            val name: String,
            val icon: SpaceMemberIconView,
            val isUser: Boolean
        )
    }
}