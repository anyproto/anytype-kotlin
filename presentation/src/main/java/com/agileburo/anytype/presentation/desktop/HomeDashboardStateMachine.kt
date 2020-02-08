package com.agileburo.anytype.presentation.desktop

import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.dashboard.model.HomeDashboard
import com.agileburo.anytype.presentation.common.StateReducer
import com.agileburo.anytype.presentation.desktop.HomeDashboardStateMachine.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch

/**
 * State machine for this view model consisting of [Interactor], [State], [Event] and [Reducer]
 * It reduces [Event] to the immutable [State] by applying [Reducer] fuction.
 * This [State] then will be rendered.
 */
sealed class HomeDashboardStateMachine {

    class Interactor(
        private val scope: CoroutineScope,
        private val reducer: Reducer = Reducer(),
        private val channel: Channel<Event> = Channel(),
        private val events: Flow<Event> = channel.consumeAsFlow()
    ) {
        fun onEvent(event: Event) = scope.launch { channel.send(event) }
        fun state(): Flow<State> = events.scan(State.init(), reducer.function)
    }

    /**
     * @property isInitialized whether this state is initialized
     * @property isLoading whether the data is being loaded to prepare a new state
     * @property error if present, represents an error occured in this state machine
     * @property dashboard current dashboard data state that should be rendered
     */
    data class State(
        val isInitialzed: Boolean,
        val isLoading: Boolean,
        val error: String?,
        val dashboard: HomeDashboard?
    ) : HomeDashboardStateMachine() {
        companion object {
            fun init() = State(
                isInitialzed = true,
                isLoading = false,
                error = null,
                dashboard = null
            )
        }
    }

    sealed class Event : HomeDashboardStateMachine() {

        data class OnDashboardLoaded(
            val dashboard: HomeDashboard
        ) : Event()

        data class OnBlocksAdded(
            val blocks: List<Block>
        ) : Event()

        data class OnStructureUpdated(
            val children: List<String>
        ) : Event()

        data class OnLinkFieldsChanged(
            val id: String,
            val fields: Block.Fields
        ) : Event()

        object OnDashboardLoadingStarted : Event()

        object OnStartedCreatingPage : Event()

        object OnFinishedCreatingPage : Event()
    }

    class Reducer : StateReducer<State, Event> {

        override val function: suspend (State, Event) -> State
            get() = { state, event -> reduce(state, event) }

        override suspend fun reduce(
            state: State, event: Event
        ) = when (event) {
            is Event.OnDashboardLoadingStarted -> state.copy(
                isInitialzed = true,
                isLoading = true,
                error = null,
                dashboard = null
            )
            is Event.OnDashboardLoaded -> state.copy(
                isInitialzed = true,
                isLoading = false,
                error = null,
                dashboard = event.dashboard
            )
            is Event.OnStartedCreatingPage -> state.copy(
                isLoading = true
            )
            is Event.OnFinishedCreatingPage -> state.copy(
                isLoading = false
            )
            is Event.OnStructureUpdated -> state.copy(
                isInitialzed = true,
                isLoading = false,
                dashboard = state.dashboard?.copy(
                    children = event.children
                )
            )
            is Event.OnBlocksAdded -> state.copy(
                isInitialzed = true,
                isLoading = false,
                dashboard = state.dashboard?.let { dashboard ->
                    dashboard.copy(blocks = dashboard.blocks + event.blocks)
                }
            )
            is Event.OnLinkFieldsChanged -> state.copy(
                dashboard = state.dashboard?.let { dashboard ->
                    dashboard.copy(
                        blocks = dashboard.blocks.map { block ->
                            if (block.id == event.id) {
                                val link = block.content.asLink()
                                block.copy(
                                    content = link.copy(
                                        fields = event.fields
                                    )
                                )
                            } else {
                                block
                            }
                        }
                    )
                }
            )
        }
    }
}