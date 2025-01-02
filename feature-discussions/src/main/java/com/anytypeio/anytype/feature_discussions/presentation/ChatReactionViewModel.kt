package com.anytypeio.anytype.feature_discussions.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.primitives.Id
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.emojifier.data.Emoji
import com.anytypeio.anytype.emojifier.data.EmojiProvider
import com.anytypeio.anytype.emojifier.suggest.EmojiSuggester
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.editor.picker.EmojiPickerView
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatReactionViewModel @Inject constructor(
    private val provider: EmojiProvider,
    private val suggester: EmojiSuggester,
    private val dispatchers: AppCoroutineDispatchers
): BaseViewModel() {

    /**
     * Default emoji list, including categories.
     */
    val default = MutableStateFlow<List<EmojiPickerView>>(emptyList())

    init {
        viewModelScope.launch {
            val loaded = loadEmojiWithCategories()
            default.value = loaded
        }
    }


    private suspend fun loadEmojiWithCategories() = withContext(dispatchers.io) {

        val views = mutableListOf<EmojiPickerView>()

        provider.emojis.forEachIndexed { categoryIndex, emojis ->
            views.add(
                EmojiPickerView.GroupHeader(
                    category = categoryIndex
                )
            )
            emojis.forEachIndexed { emojiIndex, emoji ->
                val skin = Emoji.COLORS.any { color -> emoji.contains(color) }
                if (!skin)
                    views.add(
                        EmojiPickerView.Emoji(
                            unicode = emoji,
                            page = categoryIndex,
                            index = emojiIndex
                        )
                    )
            }
        }

        views
    }

    class Factory @Inject constructor(
        private val emojiProvider: EmojiProvider,
        private val emojiSuggester: EmojiSuggester,
        private val dispatchers: AppCoroutineDispatchers
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ChatReactionViewModel(
            provider = emojiProvider,
            suggester = emojiSuggester,
            dispatchers = dispatchers
        ) as T
    }

    data class Params @Inject constructor(
        val chat: Id,
        val msg: Id
    )
}