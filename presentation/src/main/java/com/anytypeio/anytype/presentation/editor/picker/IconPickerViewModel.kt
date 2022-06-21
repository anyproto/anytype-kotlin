package com.anytypeio.anytype.presentation.editor.picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.icon.RemoveIcon
import com.anytypeio.anytype.domain.icon.SetEmojiIcon
import com.anytypeio.anytype.domain.icon.SetImageIcon
import com.anytypeio.anytype.emojifier.data.Emoji
import com.anytypeio.anytype.emojifier.data.EmojiProvider
import com.anytypeio.anytype.emojifier.suggest.EmojiSuggester
import com.anytypeio.anytype.emojifier.suggest.model.EmojiSuggest
import com.anytypeio.anytype.presentation.editor.editor.Proxy
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class IconPickerViewModel<Iconable>(
    private val setEmojiIcon: SetEmojiIcon<Iconable>,
    private val setImageIcon: SetImageIcon<Iconable>,
    private val removeDocumentIcon: RemoveIcon<Iconable>,
    private val provider: EmojiProvider,
    private val suggester: EmojiSuggester,
    private val dispatcher: Dispatcher<Payload>,
    private val analytics: Analytics
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

    fun onEmojiClicked(unicode: String, iconable: Iconable) {
        setEmoji(iconable, unicode)
    }

    fun onRandomEmoji(iconable: Iconable) {
        setEmoji(iconable, provider.emojis.random().random())
    }

    private fun setEmoji(
        iconable: Iconable, emojiUnicode: String
    ) {
        viewModelScope.launch {
            setEmojiIcon(
                params = SetEmojiIcon.Params(
                    emoji = emojiUnicode,
                    target = iconable
                )
            ).process(
                failure = { Timber.e(it, "Error while setting emoji") },
                success = { payload ->
                    sendEvent(
                        analytics = analytics,
                        eventName = EventsDictionary.objectSetIcon
                    )
                    if (payload.events.isNotEmpty()) dispatcher.send(payload)
                    state.value = ViewState.Exit
                }
            )
        }
    }

    fun onRemoveClicked(iconable: Iconable) {
        viewModelScope.launch {
            removeDocumentIcon(iconable).process(
                failure = { Timber.e(it, "Error while removing icon") },
                success = { payload ->
                    sendEvent(
                        analytics = analytics,
                        eventName = EventsDictionary.objectRemoveIcon
                    )
                    if (payload.events.isNotEmpty()) dispatcher.send(payload)
                    state.value = ViewState.Exit
                }
            )
        }
    }

    fun onPickedFromDevice(iconable: Iconable, path: String) {
        viewModelScope.launch {
            state.value = ViewState.Loading
            setImageIcon(
                SetImageIcon.Params(
                    target = iconable,
                    path = path
                )
            ).process(
                failure = {
                    Timber.e("Error while setting image icon")
                    state.value = ViewState.Init
                },
                success = { (payload, _) ->
                    dispatcher.send(payload)
                    state.value = ViewState.Exit
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