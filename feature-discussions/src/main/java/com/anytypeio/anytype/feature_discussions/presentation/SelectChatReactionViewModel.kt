package com.anytypeio.anytype.feature_discussions.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.onFailure
import com.anytypeio.anytype.domain.chats.ObserveRecentlyUsedChatReactions
import com.anytypeio.anytype.domain.chats.SetRecentlyUsedChatReactions
import com.anytypeio.anytype.domain.chats.ToggleChatMessageReaction
import com.anytypeio.anytype.emojifier.Emojifier
import com.anytypeio.anytype.emojifier.data.Emoji
import com.anytypeio.anytype.emojifier.data.EmojiProvider
import com.anytypeio.anytype.emojifier.suggest.EmojiSuggester
import com.anytypeio.anytype.presentation.common.BaseViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class SelectChatReactionViewModel @Inject constructor(
    private val vmParams: Params,
    private val provider: EmojiProvider,
    private val suggester: EmojiSuggester,
    private val dispatchers: AppCoroutineDispatchers,
    private val toggleChatMessageReaction: ToggleChatMessageReaction,
    private val setRecentlyUsedChatReactions: SetRecentlyUsedChatReactions,
    private val observeRecentlyUsedChatReactions: ObserveRecentlyUsedChatReactions
) : BaseViewModel() {

    val isDismissed = MutableSharedFlow<Boolean>(replay = 0)

    /**
     * Default emoji list, including categories.
     */
    private val default = MutableStateFlow<List<ReactionPickerView>>(emptyList())

    private val recentlyUsed = MutableStateFlow<List<String>>(emptyList())

    val views = combine(default, recentlyUsed) { default, recentlyUsed ->
        buildList<ReactionPickerView> {
            if (recentlyUsed.isNotEmpty()) {
                add(ReactionPickerView.RecentUsedSection)
                addAll(
                    recentlyUsed.map { unicode ->
                        ReactionPickerView.Emoji(
                            unicode = unicode,
                            page = -1,
                            index = -1,
                            emojified = Emojifier.safeUri(unicode)
                        )
                    }
                )
            }
            addAll(default)
        }
    }

    init {
        viewModelScope.launch {
            observeRecentlyUsedChatReactions
                .flow()
                .collect {
                    recentlyUsed.value = it
                }
        }
        viewModelScope.launch {
            val loaded = loadEmojiWithCategories()
            default.value = loaded
        }
    }


    private suspend fun loadEmojiWithCategories() = withContext(dispatchers.io) {

        val views = mutableListOf<ReactionPickerView>()

        provider.emojis.forEachIndexed { categoryIndex, emojis ->
            views.add(
                ReactionPickerView.Category(
                    index = categoryIndex
                )
            )
            emojis.forEachIndexed { emojiIndex, emoji ->
                val skin = Emoji.COLORS.any { color -> emoji.contains(color) }
                if (!skin)
                    views.add(
                        ReactionPickerView.Emoji(
                            unicode = emoji,
                            page = categoryIndex,
                            index = emojiIndex,
                            emojified = Emojifier.safeUri(emoji)
                        )
                    )
            }
        }

        views
    }

    fun onEmojiClicked(emoji: String) {
        viewModelScope.launch {
            setRecentlyUsedChatReactions.async(
                params = (listOf(emoji) + recentlyUsed.value)
                    .toSet()
                    .take(MAX_RECENTLY_USED_COUNT)
                    .toSet()
            ).onFailure {
                Timber.e(it, "Error while saving recently used reactions")
            }
            toggleChatMessageReaction.async(
                params = Command.ChatCommand.ToggleMessageReaction(
                    msg = vmParams.msg,
                    chat = vmParams.chat,
                    emoji = emoji
                )
            ).onFailure {
                Timber.e(it, "Error while toggling chat message reaction")
            }
            isDismissed.emit(true)
        }
    }

    class Factory @Inject constructor(
        private val params: Params,
        private val emojiProvider: EmojiProvider,
        private val emojiSuggester: EmojiSuggester,
        private val dispatchers: AppCoroutineDispatchers,
        private val toggleChatMessageReaction: ToggleChatMessageReaction,
        private val setRecentlyUsedChatReactions: SetRecentlyUsedChatReactions,
        private val observeRecentlyUsedChatReactions: ObserveRecentlyUsedChatReactions
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = SelectChatReactionViewModel(
            vmParams = params,
            provider = emojiProvider,
            suggester = emojiSuggester,
            dispatchers = dispatchers,
            toggleChatMessageReaction = toggleChatMessageReaction,
            setRecentlyUsedChatReactions = setRecentlyUsedChatReactions,
            observeRecentlyUsedChatReactions = observeRecentlyUsedChatReactions
        ) as T
    }

    data class Params @Inject constructor(
        val chat: Id,
        val msg: Id
    )

    sealed class ReactionPickerView {
        data object RecentUsedSection: ReactionPickerView()
        data class Category(val index: Int) : ReactionPickerView()
        data class Emoji(
            val unicode: String,
            val page: Int,
            val index: Int,
            val emojified: String = ""
        ) : ReactionPickerView()
    }

    companion object {
        const val MAX_RECENTLY_USED_COUNT = 20
    }
}