package com.anytypeio.anytype.presentation.dashboard

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.ext.getChildrenIdsList
import com.anytypeio.anytype.core_utils.tools.FeatureToggles
import com.anytypeio.anytype.core_utils.tools.toPrettyString
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.common.StateReducer
import com.anytypeio.anytype.presentation.dashboard.HomeDashboardStateMachine.Event
import com.anytypeio.anytype.presentation.dashboard.HomeDashboardStateMachine.Interactor
import com.anytypeio.anytype.presentation.dashboard.HomeDashboardStateMachine.Reducer
import com.anytypeio.anytype.presentation.dashboard.HomeDashboardStateMachine.State
import com.anytypeio.anytype.presentation.extension.sortByIds
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.scan
import timber.log.Timber

/**
 * State machine for this view model consisting of [Interactor], [State], [Event] and [Reducer]
 * It reduces [Event] to the immutable [State] by applying [Reducer] fuction.
 * This [State] then will be rendered.
 */
sealed class HomeDashboardStateMachine {

    class Interactor(
        private val featureToggles: FeatureToggles,
        private val reducer: Reducer = Reducer(featureToggles = featureToggles),
    ) {
        private val channel: Channel<List<Event>> = Channel()
        private val events: Flow<List<Event>> = channel.consumeAsFlow()
        suspend fun onEvents(events: List<Event>) = channel.send(events)
        fun state(): Flow<State> = events.scan(State.init(), reducer.function)
    }

    /**
     * @property isInitialized whether this state is initialized
     * @property error if present, represents an error occurred in this state machine
     * @property blocks current dashboard object blocks
     */
    data class State(
        val isInitialized: Boolean,
        val error: String?,
        val blocks: List<Block> = emptyList(),
        val childrenIdsList: List<Id> = emptyList()
    ) : HomeDashboardStateMachine() {
        companion object {
            fun init() = State(
                isInitialized = false,
                error = null,
                blocks = emptyList(),
                childrenIdsList = emptyList()
            )
        }
    }

    sealed class Event : HomeDashboardStateMachine() {

        data class OnShowDashboard(
            val context: String,
            val blocks: List<Block>,
            val details: Block.Details,
            val builder: UrlBuilder,
            val objectTypes: List<ObjectWrapper.Type>
        ) : Event()

        data class OnBlocksAdded(
            val blocks: List<Block>,
            val details: Block.Details,
            val builder: UrlBuilder
        ) : Event()

        data class OnStructureUpdated(
            val children: List<String>
        ) : Event()

        object OnDashboardLoadingStarted : Event()
    }

    class Reducer(
        private val featureToggles: FeatureToggles
    ) : StateReducer<State, List<Event>> {

        override val function: suspend (State, List<Event>) -> State
            get() = { state, events ->
                if (featureToggles.isLogDashboardReducer) {
                    Timber.d("REDUCE, STATE:$state, EVENT:${events.toPrettyString()}")
                }

                val update = reduce(state, events)

                if (featureToggles.isLogDashboardReducer) {
                    Timber.d("REDUCE, UPDATED STATE:${update.toPrettyString()}")
                }
                update
            }

        override suspend fun reduce(
            state: State, event: List<Event>
        ): State {
            var update: State = state
            event.forEach { update = reduceEvent(update, it) }
            return update
        }

        private fun reduceEvent(
            state: State, event: Event
        ): State {
            return when (event) {
                is Event.OnDashboardLoadingStarted -> state.copy(
                    isInitialized = true,
                    error = null
                )
                is Event.OnShowDashboard -> {
                    val childrenIdsList = event.blocks.getChildrenIdsList(parent = event.context)
                    state.copy(
                        isInitialized = true,
                        error = null,
                        blocks = event.blocks,
                        childrenIdsList = childrenIdsList
                    )
                }
                is Event.OnStructureUpdated -> state.copy(
                    isInitialized = true,
                    blocks = state.blocks.sortByIds(event.children),
                    childrenIdsList = event.children
                )
                is Event.OnBlocksAdded -> {
                    state.copy(blocks = state.blocks + event.blocks)
                }
            }
        }
    }
}
