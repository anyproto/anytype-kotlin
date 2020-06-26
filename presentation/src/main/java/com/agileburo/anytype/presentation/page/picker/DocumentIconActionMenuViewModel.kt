package com.agileburo.anytype.presentation.page.picker

import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.core_utils.ui.ViewStateViewModel
import com.agileburo.anytype.domain.icon.SetDocumentEmojiIcon
import com.agileburo.anytype.domain.icon.SetDocumentImageIcon
import com.agileburo.anytype.emojifier.data.Emoji
import com.agileburo.anytype.presentation.common.StateReducer
import com.agileburo.anytype.presentation.page.picker.DocumentIconActionMenuViewModel.Contract.*
import com.agileburo.anytype.presentation.page.picker.DocumentIconActionMenuViewModel.ViewState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DocumentIconActionMenuViewModel(
    private val setEmojiIcon: SetDocumentEmojiIcon,
    private val setImageIcon: SetDocumentImageIcon
) : ViewStateViewModel<ViewState>(), StateReducer<State, Event> {

    private val events = ConflatedBroadcastChannel<Event>()
    private val actions = Channel<Action>()
    private val flow: Flow<State> = events.asFlow().scan(State.init(), function)

    override val function: suspend (State, Event) -> State
        get() = { state, event -> reduce(state, event) }

    init {
        flow
            .map { state ->
                when {
                    state.error != null -> ViewState.Error(state.error)
                    state.isCompleted -> ViewState.Exit
                    else -> ViewState.Idle
                }
            }
            .onEach { stateData.postValue(it) }
            .launchIn(viewModelScope)

        actions
            .consumeAsFlow()
            .onEach { action ->
                when (action) {
                    is Action.SetEmojiIcon -> setEmojiIcon(
                        params = SetDocumentEmojiIcon.Params(
                            target = action.target,
                            emoji = action.unicode,
                            context = action.context
                        )
                    ).proceed(
                        success = { events.send(Event.OnCompleted) },
                        failure = { events.send(Event.Failure(it)) }
                    )
                    is Action.ClearEmoji -> setEmojiIcon(
                        params = SetDocumentEmojiIcon.Params(
                            target = action.target,
                            emoji = "",
                            context = action.context
                        )
                    ).proceed(
                        success = { events.send(Event.OnCompleted) },
                        failure = { events.send(Event.Failure(it)) }
                    )
                    is Action.SetImageIcon -> setImageIcon(
                        SetDocumentImageIcon.Params(
                            context = action.context,
                            path = action.path
                        )
                    ).proceed(
                        failure = { events.send(Event.Failure(it)) },
                        success = { events.send(Event.OnCompleted) }
                    )
                    is Action.PickRandomEmoji -> {
                        val random = Emoji.DATA.random().random()
                        events.send(
                            Event.OnRandomEmojiSelected(
                                target = action.target,
                                context = action.context,
                                unicode = random
                            )
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: Event) {
        viewModelScope.launch { events.send(event) }
    }

    sealed class ViewState {
        object Loading : ViewState()
        object Exit : ViewState()
        object Idle : ViewState()
        data class Error(val message: String) : ViewState()
    }

    sealed class Contract {

        sealed class Action {

            class PickRandomEmoji(
                val context: String,
                val target: String
            ) : Action()

            class ClearEmoji(
                val target: String,
                val context: String
            ) : Action()

            class SetEmojiIcon(
                val unicode: String,
                val target: String,
                val context: String
            ) : Action()

            class SetImageIcon(
                val context: String,
                val path: String
            ) : Action()
        }

        data class State(
            val isLoading: Boolean,
            val isCompleted: Boolean = false,
            val error: String? = null
        ) : Contract() {
            companion object {
                fun init() = State(isLoading = false)
            }
        }

        sealed class Event : Contract() {

            class OnImagePickedFromGallery(
                val context: String,
                val path: String
            ) : Event()

            class OnSetRandomEmojiClicked(
                val target: String,
                val context: String
            ) : Event()

            class OnRandomEmojiSelected(
                val unicode: String,
                val context: String,
                val target: String
            ) : Event()

            class OnRemoveEmojiSelected(
                val context: String,
                val target: String
            ) : Event()

            object OnCompleted : Event()

            class Failure(val error: Throwable) : Event()
        }
    }

    override suspend fun reduce(state: State, event: Event): State {
        return when (event) {
            is Event.OnRandomEmojiSelected -> state.copy(
                isLoading = true
            ).also {
                actions.send(
                    Action.SetEmojiIcon(
                        target = event.target,
                        context = event.context,
                        unicode = event.unicode
                    )
                )
            }
            is Event.OnSetRandomEmojiClicked -> {
                state.copy(
                    isLoading = true
                ).also {
                    actions.send(
                        Action.PickRandomEmoji(
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
                        Action.ClearEmoji(
                            context = event.context,
                            target = event.target
                        )
                    )
                }
            }
            is Event.OnImagePickedFromGallery -> {
                state.copy(
                    isLoading = true
                ).also {
                    actions.send(
                        Action.SetImageIcon(
                            context = event.context,
                            path = event.path
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