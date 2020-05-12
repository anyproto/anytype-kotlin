package com.agileburo.anytype.presentation.page.picker

import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.core_utils.ui.ViewStateViewModel
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.icon.SetIconName
import com.agileburo.anytype.library_page_icon_picker_widget.model.PageIconPickerView
import com.agileburo.anytype.presentation.common.StateReducer
import com.agileburo.anytype.presentation.page.picker.PageIconPickerViewModel.Contract.Event
import com.agileburo.anytype.presentation.page.picker.PageIconPickerViewModel.Contract.State
import com.agileburo.anytype.presentation.page.picker.PageIconPickerViewModel.ViewState
import com.vdurmont.emoji.Emoji
import com.vdurmont.emoji.EmojiManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

class PageIconPickerViewModel(
    private val setIconName: SetIconName
) : ViewStateViewModel<ViewState>(), StateReducer<State, Event> {

    private val channel = ConflatedBroadcastChannel<Event>()
    private val actions = Channel<Contract.Action>()
    private val flow: Flow<State> = channel.asFlow().scan(State.init(), function)

    override val function: suspend (State, Event) -> State
        get() = { state, event -> reduce(state, event) }


    private val headers = listOf(
        PageIconPickerView.Action.UploadPhoto,
        PageIconPickerView.Action.PickRandomly,
        PageIconPickerView.Action.ChooseEmoji,
        PageIconPickerView.EmojiFilter
    )

    init {
        processViewState()
        processActions()
        initialize()
    }

    private fun initialize() {
        channel.offer(Event.Init)
    }

    private fun processViewState() {
        flow
            .map { state ->
                when {
                    state.error != null -> ViewState.Error(state.error)
                    state.isLoading -> ViewState.Loading
                    state.isCompleted -> ViewState.Exit
                    else -> ViewState.Success(
                        views = headers + map(state.selection)
                    )
                }
            }
            .onEach { stateData.postValue(it) }
            .launchIn(viewModelScope)
    }

    private fun processActions() {
        actions
            .consumeAsFlow()
            .map { action ->
                when (action) {
                    is Contract.Action.FetchEmojis -> {
                        val emojis = loadEmoji()
                        Event.OnEmojiLoaded(emojis)
                    }
                    is Contract.Action.SearchEmojis -> {
                        val emojis = findEmojis(action.query)
                        Event.OnSearchResult(emojis)
                    }
                    is Contract.Action.SetEmoji -> {
                        setIconEmojiName(action).let { result ->
                            when (result) {
                                is Either.Left -> Event.Failure(result.a)
                                is Either.Right -> Event.OnCompleted
                            }
                        }
                    }
                    is Contract.Action.ClearEmoji -> {
                        clearIconEmojiName(action).let { result ->
                            when (result) {
                                is Either.Left -> Event.Failure(result.a)
                                is Either.Right -> Event.OnCompleted
                            }
                        }
                    }
                    is Contract.Action.PickRandomEmoji -> {
                        val emoji = pickRandomEmoji(action.emojis)
                        Event.OnRandomEmojiSelected(
                            target = action.target,
                            context = action.context,
                            emoji = emoji
                        )
                    }
                }
            }
            .onEach(channel::send)
            .launchIn(viewModelScope)
    }

    private suspend fun setIconEmojiName(
        action: Contract.Action.SetEmoji
    ): Either<Throwable, Unit> = withContext(Dispatchers.IO) {
        setIconName.run(
            params = SetIconName.Params(
                target = action.target,
                name = ":${action.alias}:",
                context = action.context
            )
        )
    }

    private suspend fun clearIconEmojiName(
        action: Contract.Action.ClearEmoji
    ): Either<Throwable, Unit> = withContext(Dispatchers.IO) {
        setIconName.run(
            params = SetIconName.Params(
                target = action.target,
                name = "",
                context = action.context
            )
        )
    }

    private suspend fun loadEmoji(): List<Emoji> = withContext(Dispatchers.IO) {
        EmojiManager.getAll().toList()
    }

    private suspend fun findEmojis(query: String): List<Emoji> = withContext(Dispatchers.IO) {
        EmojiManager.getAll().filter { emoji ->
            emoji.aliases.any { alias ->
                alias.contains(query, ignoreCase = true) || query == alias
            }
        }
    }

    private suspend fun pickRandomEmoji(emojis: List<Emoji>): Emoji = withContext(Dispatchers.IO) {
        emojis.random()
    }

    private suspend fun map(emojis: List<Emoji>) = withContext(Dispatchers.IO) {
        emojis.map { emoji ->
            PageIconPickerView.Emoji(
                alias = emoji.aliases.first(),
                /**
                 * Fix pirate flag emoji render, after fixing
                 * in table https://github.com/vdurmont/emoji-java/blob/master/EMOJIS.md
                 * can be removed
                 */
                unicode = emoji.unicode.filterTextByChar(
                    value = '☠',
                    filterBy = '♾'
                )
            )
        }
    }

    private fun String.filterTextByChar(value: Char, filterBy: Char): String =
        if (contains(value)) {
            filterNot { it == filterBy }
        } else {
            this
        }

    fun onEvent(event: Event) {
        channel.offer(event)
    }

    sealed class ViewState {
        object Loading : ViewState()
        object Exit : ViewState()
        data class Success(val views: List<PageIconPickerView>) : ViewState()
        data class Error(val message: String) : ViewState()
    }

    sealed class Contract {

        sealed class Action {

            object FetchEmojis : Action()

            data class SearchEmojis(
                val query: String
            ) : Action()

            data class PickRandomEmoji(
                val emojis: List<Emoji>,
                val context: String,
                val target: String
            ) : Action()

            data class ClearEmoji(
                val target: String,
                val context: String
            ) : Action()

            data class SetEmoji(
                val unicode: String,
                val alias: String,
                val target: String,
                val context: String
            ) : Action()
        }

        data class State(
            val isLoading: Boolean,
            val isCompleted: Boolean = false,
            val error: String? = null,
            val emojis: List<Emoji>,
            val selection: List<Emoji>
        ) : Contract() {
            companion object {
                fun init() = State(
                    isLoading = false,
                    emojis = emptyList(),
                    selection = emptyList()
                )
            }
        }

        sealed class Event : Contract() {

            object Init : Event()

            data class OnEmojiClicked(
                val unicode: String,
                val alias: String,
                val target: String,
                val context: String
            ) : Event()

            data class OnFilterQueryChanged(
                val query: String
            ) : Event()

            data class OnSetRandomEmojiClicked(
                val target: String,
                val context: String
            ) : Event()

            data class OnEmojiLoaded(
                val emojis: List<Emoji>
            ) : Event()

            data class OnRandomEmojiSelected(
                val emoji: Emoji,
                val context: String,
                val target: String
            ) : Event()

            data class OnRemoveEmojiSelected(
                val context: String,
                val target: String
            ) : Event()

            data class OnSearchResult(
                val emojis: List<Emoji>
            ) : Event()

            object OnCompleted : Event()

            data class Failure(val error: Throwable) : Event()
        }
    }

    override suspend fun reduce(state: State, event: Event): State {
        return when (event) {
            is Event.Init -> state.copy(isLoading = true).also {
                actions.send(Contract.Action.FetchEmojis)
            }
            is Event.OnEmojiLoaded -> state.copy(
                isLoading = false,
                emojis = event.emojis,
                selection = event.emojis
            )
            is Event.OnSearchResult -> state.copy(
                isLoading = false,
                selection = event.emojis
            )
            is Event.OnRandomEmojiSelected -> state.copy(
                isLoading = true
            ).also {
                actions.send(
                    Contract.Action.SetEmoji(
                        target = event.target,
                        context = event.context,
                        unicode = event.emoji.unicode,
                        alias = event.emoji.aliases.first()
                    )
                )
            }
            is Event.OnFilterQueryChanged -> state.copy(
                isLoading = true
            ).also {
                actions.send(
                    Contract.Action.SearchEmojis(
                        query = event.query
                    )
                )
            }
            is Event.OnEmojiClicked -> {
                state.copy(isLoading = true).also {
                    actions.send(
                        Contract.Action.SetEmoji(
                            unicode = event.unicode,
                            target = event.target,
                            alias = event.alias,
                            context = event.context
                        )
                    )
                }
            }
            is Event.OnSetRandomEmojiClicked -> {
                state.copy(
                    isLoading = true
                ).also {
                    actions.send(
                        Contract.Action.PickRandomEmoji(
                            emojis = state.emojis,
                            target = event.target,
                            context = event.context
                        )
                    )
                }
            }
            is Event.OnRemoveEmojiSelected -> {
                state.copy(
                    isLoading = true
                ).also {
                    actions.send(
                        Contract.Action.ClearEmoji(
                            context = event.context,
                            target = event.target
                        )
                    )
                }
            }
            is Event.OnCompleted -> state.copy(
                isLoading = false,
                isCompleted = true,
                error = null
            )
            is Event.Failure -> state.copy(
                isLoading = false,
                isCompleted = false,
                error = event.error.toString()
            )
        }
    }
}