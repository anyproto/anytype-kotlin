package com.anytypeio.anytype.presentation.page.picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.domain.common.Id
import com.anytypeio.anytype.domain.event.model.Payload
import com.anytypeio.anytype.domain.icon.SetDocumentEmojiIcon
import com.anytypeio.anytype.emojifier.data.Emoji
import com.anytypeio.anytype.emojifier.data.EmojiProvider
import com.anytypeio.anytype.emojifier.suggest.EmojiSuggester
import com.anytypeio.anytype.emojifier.suggest.model.EmojiSuggest
import com.anytypeio.anytype.library_page_icon_picker_widget.model.EmojiPickerView
import com.anytypeio.anytype.presentation.page.editor.Proxy
import com.anytypeio.anytype.presentation.util.Bridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class DocumentEmojiIconPickerViewModel(
    private val setEmojiIcon: SetDocumentEmojiIcon,
    private val provider: EmojiProvider,
    private val suggester: EmojiSuggester,
    private val bridge: Bridge<Payload>
) : ViewModel() {

    /**
     * Default emoji list, including categories.
     */
    private val default = MutableStateFlow<List<EmojiPickerView>>(emptyList())

    /**
     * UI state stream.
     */
    private val state: MutableStateFlow<ViewState> = MutableStateFlow(ViewState.Init)

    /**
     * Stream of user-generated queries.
     */
    private val queries = Proxy.Subject<String>()

    init {
        viewModelScope.launch {
            state.value = ViewState.Loading
            val loaded = loadEmojiWithCategories()
            default.value = loaded
            state.value = ViewState.Success(views = default.value)
        }
        viewModelScope.launch {
            queries
                .stream()
                .debounce(DEBOUNCE_DURATION)
                .distinctUntilChanged()
                .onEach { state.value = ViewState.Loading }
                .mapLatest { query ->
                    if (query.isEmpty())
                        default.value
                    else
                        select(suggester.search(query))
                }
                .flowOn(Dispatchers.Default)
                .collect { state.value = ViewState.Success(it) }
        }
    }

    /**
     * Maps found search suggests to emoji data, then adapts the latter to UI.
     */
    private fun select(suggests: List<EmojiSuggest>): MutableList<EmojiPickerView.Emoji> {
        val result = mutableListOf<EmojiPickerView.Emoji>()
        suggests.forEach { suggest ->
            provider.emojis.forEachIndexed loop@{ categoryIndex, emojis ->
                emojis.forEachIndexed { emojiIndex, emoji ->
                    if (emoji == suggest.emoji) {
                        val skin = Emoji.COLORS.any { color -> emoji.contains(color) }
                        if (!skin) {
                            result.add(
                                EmojiPickerView.Emoji(
                                    unicode = emoji,
                                    index = emojiIndex,
                                    page = categoryIndex
                                )
                            )
                            return@loop
                        }
                    }
                }
            }
        }
        return result
    }

    private suspend fun loadEmojiWithCategories() = withContext(Dispatchers.IO) {

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

    fun state(): StateFlow<ViewState> = state

    fun onEmojiClicked(unicode: String, target: Id, context: Id) {
        viewModelScope.launch {
            setEmojiIcon(
                params = SetDocumentEmojiIcon.Params(
                    emoji = unicode,
                    target = target,
                    context = context
                )
            ).proceed(
                failure = { Timber.e(it, "Error while setting emoji") },
                success = {
                    bridge.send(it)
                    state.apply { value = ViewState.Exit }
                }
            )
        }
    }

    fun onQueryChanged(query: String) {
        viewModelScope.launch { queries.send(query) }
    }

    sealed class ViewState {
        object Init : ViewState()
        object Loading : ViewState()
        data class Success(val views: List<EmojiPickerView>) : ViewState()
        object Exit : ViewState()
    }

    companion object {
        const val DEBOUNCE_DURATION = 300L
    }
}