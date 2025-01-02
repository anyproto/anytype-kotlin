package com.anytypeio.anytype.feature_discussions.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.features.editor.holders.text.Toggle
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.chats.ToggleChatMessageReaction
import com.anytypeio.anytype.emojifier.Emojifier
import com.anytypeio.anytype.emojifier.data.Emoji
import com.anytypeio.anytype.emojifier.data.EmojiProvider
import com.anytypeio.anytype.emojifier.suggest.EmojiSuggester
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.editor.picker.EmojiPickerView
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatReactionViewModel @Inject constructor(
    private val vmParams: Params,
    private val provider: EmojiProvider,
    private val suggester: EmojiSuggester,
    private val dispatchers: AppCoroutineDispatchers,
    private val toggleChatMessageReaction: ToggleChatMessageReaction
) : BaseViewModel() {

    val isDismissed = MutableSharedFlow<Boolean>(replay = 0)

    /**
     * Default emoji list, including categories.
     */
    val default = MutableStateFlow<List<ReactionPickerView>>(emptyList())

    init {
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
            toggleChatMessageReaction.execute(
                params = Command.ChatCommand.ToggleMessageReaction(
                    msg = vmParams.msg,
                    chat = vmParams.chat,
                    emoji = emoji
                )
            )
            isDismissed.emit(true)
        }
    }

    class Factory @Inject constructor(
        private val params: Params,
        private val emojiProvider: EmojiProvider,
        private val emojiSuggester: EmojiSuggester,
        private val dispatchers: AppCoroutineDispatchers,
        private val toggleChatMessageReaction: ToggleChatMessageReaction
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ChatReactionViewModel(
            vmParams = params,
            provider = emojiProvider,
            suggester = emojiSuggester,
            dispatchers = dispatchers,
            toggleChatMessageReaction = toggleChatMessageReaction
        ) as T
    }

    data class Params @Inject constructor(
        val chat: Id,
        val msg: Id
    )

    sealed class ReactionPickerView {
        data class Category(val index: Int) : ReactionPickerView()
        data class Emoji(
            val unicode: String,
            val page: Int,
            val index: Int,
            val emojified: String = ""
        ) : ReactionPickerView()
    }
}