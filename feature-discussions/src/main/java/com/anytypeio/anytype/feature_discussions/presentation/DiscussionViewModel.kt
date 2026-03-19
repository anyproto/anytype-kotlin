package com.anytypeio.anytype.feature_discussions.presentation

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.UrlBuilder
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
import java.text.SimpleDateFormat
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import timber.log.Timber

class DiscussionViewModel @Inject constructor(
    private val vmParams: Params,
    private val chatContainer: ChatContainer,
    private val members: ActiveSpaceMemberSubscriptionContainer,
    private val getAccount: GetAccount,
    private val urlBuilder: UrlBuilder,
    private val dispatchers: AppCoroutineDispatchers
) : BaseViewModel() {

    private val _header = MutableStateFlow(DiscussionHeader())
    val header: StateFlow<DiscussionHeader> = _header

    private val _messages = MutableStateFlow<List<DiscussionView>>(emptyList())
    val messages: StateFlow<List<DiscussionView>> = _messages

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val dateFormatter = SimpleDateFormat("d MMMM yyyy")

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
                        val formattedMsgDate = dateFormatter.format(msg.createdAt * 1000)

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
                                DiscussionView.Avatar.Image(iconImage)
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

                _messages.value = commentViews
                _header.value = DiscussionHeader(
                    commentCount = commentViews.size
                )
                _isLoading.value = false
            }
    }

    data class Params(
        val ctx: Id,
        val space: Space
    )
}
