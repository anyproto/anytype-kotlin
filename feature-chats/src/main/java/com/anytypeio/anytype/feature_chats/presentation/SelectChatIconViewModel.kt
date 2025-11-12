package com.anytypeio.anytype.feature_chats.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.emojifier.Emojifier
import com.anytypeio.anytype.emojifier.data.Emoji
import com.anytypeio.anytype.emojifier.data.EmojiProvider
import com.anytypeio.anytype.emojifier.suggest.EmojiSuggester
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.picker.EmojiPickerView
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SelectChatIconViewModel @Inject constructor(
    private val provider: EmojiProvider,
    private val suggester: EmojiSuggester,
    private val dispatchers: AppCoroutineDispatchers
) : BaseViewModel() {

    val isDismissed = MutableSharedFlow<Boolean>(replay = 0)
    val emojiSelected = MutableSharedFlow<String>(replay = 0)

    /**
     * Default emoji list, including categories.
     */
    private val default = MutableStateFlow<List<EmojiPickerView>>(emptyList())

    private val rawQuery = MutableStateFlow("")

    @OptIn(FlowPreview::class)
    private val debouncedQuery = rawQuery
        .debounce(DEBOUNCE_DURATION)
        .distinctUntilChanged()
        .onStart { emit(rawQuery.value) }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val queries = debouncedQuery.flatMapLatest { query ->
        flow {
            val emojis = if (query.isEmpty()) {
                emptyList()
            } else {
                suggester.search(query).map { result ->
                    EmojiPickerView.Emoji(
                        unicode = result.emoji,
                        page = -1,
                        index = -1,
                        emojified = Emojifier.safeUri(result.emoji)
                    )
                }
            }
            emit(query to emojis)
        }
    }.flowOn(dispatchers.io)

    val views = combine(default, queries) { default, (query, results) ->
        buildList {
            if (query.isEmpty()) {
                addAll(default)
            } else {
                addAll(results)
            }
        }
    }

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
                EmojiPickerView.Category(
                    index = categoryIndex
                )
            )
            emojis.forEachIndexed { emojiIndex, emoji ->
                val skin = Emoji.COLORS.any { color -> emoji.contains(color) }
                if (!skin)
                    views.add(
                        EmojiPickerView.Emoji(
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
            emojiSelected.emit(emoji)
            isDismissed.emit(true)
        }
    }

    fun onQueryChanged(input: String) {
        rawQuery.value = input
    }

    class Factory @Inject constructor(
        private val emojiProvider: EmojiProvider,
        private val emojiSuggester: EmojiSuggester,
        private val dispatchers: AppCoroutineDispatchers
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = SelectChatIconViewModel(
            provider = emojiProvider,
            suggester = emojiSuggester,
            dispatchers = dispatchers
        ) as T
    }

    companion object {
        const val DEBOUNCE_DURATION = 300L
    }
}
