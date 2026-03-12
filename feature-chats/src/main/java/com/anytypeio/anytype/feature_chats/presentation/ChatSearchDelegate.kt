package com.anytypeio.anytype.feature_chats.presentation

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.chats.ChatMessageSearchResult
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.chats.SearchChatMessages
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

interface ChatSearchDelegate {

    val chatSearchState: StateFlow<ChatSearchState>

    fun onSearchTriggered()
    fun onSearchQueryChanged(query: String)
    fun onSearchResultSelected(index: Int)
    fun onSearchBarTapped()
    fun onSearchNextResult()
    fun onSearchPreviousResult()
    fun onSearchDismissed()
    fun initSearchDelegate(
        scope: CoroutineScope,
        space: SpaceId,
        chat: Id,
        onScrollToMessage: suspend (Id) -> Unit
    )

    class Default @Inject constructor(
        private val searchChatMessages: SearchChatMessages,
        private val dispatchers: AppCoroutineDispatchers
    ) : ChatSearchDelegate {

        private lateinit var scope: CoroutineScope
        private lateinit var onScrollToMessage: suspend (Id) -> Unit
        private var searchJob: Job? = null
        private var space: SpaceId = SpaceId("")
        private var chat: Id = ""

        private val _chatSearchState = MutableStateFlow<ChatSearchState>(ChatSearchState.Idle)
        override val chatSearchState: StateFlow<ChatSearchState> = _chatSearchState

        override fun initSearchDelegate(
            scope: CoroutineScope,
            space: SpaceId,
            chat: Id,
            onScrollToMessage: suspend (Id) -> Unit
        ) {
            this.scope = scope
            this.space = space
            this.chat = chat
            this.onScrollToMessage = onScrollToMessage
        }

        override fun onSearchTriggered() {
            Timber.d("ChatSearch: onSearchTriggered")
            _chatSearchState.value = ChatSearchState.Active(
                query = "",
                results = emptyList(),
                currentIndex = -1,
                isSearching = false,
                isResultsListVisible = true
            )
        }

        override fun onSearchQueryChanged(query: String) {
            val current = _chatSearchState.value
            if (current !is ChatSearchState.Active) return

            _chatSearchState.value = current.copy(
                query = query,
                isSearching = query.isNotEmpty()
            )

            searchJob?.cancel()

            if (query.isEmpty()) {
                _chatSearchState.value = current.copy(
                    query = "",
                    results = emptyList(),
                    currentIndex = -1,
                    isSearching = false
                )
                return
            }

            searchJob = scope.launch {
                delay(SEARCH_DEBOUNCE_MS)
                try {
                    val response = searchChatMessages.run(
                        Command.ChatCommand.SearchMessages(
                            space = space,
                            chat = chat,
                            query = query
                        )
                    )
                    val state = _chatSearchState.value
                    if (state is ChatSearchState.Active && state.query == query) {
                        _chatSearchState.value = state.copy(
                            results = response.results,
                            currentIndex = -1,
                            isSearching = false
                        )
                    }
                } catch (e: Exception) {
                    Timber.e(e, "ChatSearch: error searching messages")
                    val state = _chatSearchState.value
                    if (state is ChatSearchState.Active && state.query == query) {
                        _chatSearchState.value = state.copy(
                            results = emptyList(),
                            currentIndex = -1,
                            isSearching = false
                        )
                    }
                }
            }
        }

        override fun onSearchResultSelected(index: Int) {
            val current = _chatSearchState.value
            if (current !is ChatSearchState.Active) return
            val result = current.results.getOrNull(index) ?: return

            // Dismiss search and scroll to the selected message
            _chatSearchState.value = ChatSearchState.Idle

            scope.launch {
                onScrollToMessage(result.messageId)
            }
        }

        override fun onSearchBarTapped() {
            val current = _chatSearchState.value
            if (current !is ChatSearchState.Active) return
            _chatSearchState.value = current.copy(isResultsListVisible = true)
        }

        override fun onSearchNextResult() {
            val current = _chatSearchState.value
            if (current !is ChatSearchState.Active) return
            if (current.currentIndex >= current.results.size - 1) return

            val newIndex = current.currentIndex + 1
            val result = current.results[newIndex]

            _chatSearchState.value = current.copy(currentIndex = newIndex)

            scope.launch {
                onScrollToMessage(result.messageId)
            }
        }

        override fun onSearchPreviousResult() {
            val current = _chatSearchState.value
            if (current !is ChatSearchState.Active) return
            if (current.currentIndex <= 0) return

            val newIndex = current.currentIndex - 1
            val result = current.results[newIndex]

            _chatSearchState.value = current.copy(currentIndex = newIndex)

            scope.launch {
                onScrollToMessage(result.messageId)
            }
        }

        override fun onSearchDismissed() {
            Timber.d("ChatSearch: onSearchDismissed")
            searchJob?.cancel()
            _chatSearchState.value = ChatSearchState.Idle
        }

        companion object {
            private const val SEARCH_DEBOUNCE_MS = 300L
        }
    }
}

sealed class ChatSearchState {
    data object Idle : ChatSearchState()
    data class Active(
        val query: String,
        val results: List<ChatMessageSearchResult>,
        val currentIndex: Int,
        val isSearching: Boolean,
        val isResultsListVisible: Boolean
    ) : ChatSearchState()
}
