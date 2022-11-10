package com.anytypeio.anytype.presentation.dashboard

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ext.amend
import com.anytypeio.anytype.core_models.ext.getChildrenIdsList
import com.anytypeio.anytype.core_models.ext.set
import com.anytypeio.anytype.core_models.ext.unset
import com.anytypeio.anytype.core_utils.tools.FeatureToggles
import com.anytypeio.anytype.core_utils.tools.toPrettyString
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.common.StateReducer
import com.anytypeio.anytype.presentation.dashboard.HomeDashboardStateMachine.Event
import com.anytypeio.anytype.presentation.dashboard.HomeDashboardStateMachine.Interactor
import com.anytypeio.anytype.presentation.dashboard.HomeDashboardStateMachine.Reducer
import com.anytypeio.anytype.presentation.dashboard.HomeDashboardStateMachine.State
import com.anytypeio.anytype.presentation.extension.addAndSortByIds
import com.anytypeio.anytype.presentation.extension.sortByIds
import com.anytypeio.anytype.presentation.extension.updateDetails
import com.anytypeio.anytype.presentation.mapper.toDashboardViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * State machine for this view model consisting of [Interactor], [State], [Event] and [Reducer]
 * It reduces [Event] to the immutable [State] by applying [Reducer] fuction.
 * This [State] then will be rendered.
 */
sealed class HomeDashboardStateMachine {

    class Interactor(
        private val scope: CoroutineScope,
        private val reducer: Reducer,
        private val channel: Channel<List<Event>> = Channel(),
        private val events: Flow<List<Event>> = channel.consumeAsFlow()
    ) {
        fun onEvents(events: List<Event>) = scope.launch { channel.send(events) }
        fun state(): Flow<State> = events.scan(State.init(), reducer.function)
    }

    /**
     * @property isInitialized whether this state is initialized
     * @property isLoading whether the data is being loaded to prepare a new state
     * @property error if present, represents an error occurred in this state machine
     * @property blocks current dashboard data state that should be rendered
     */
    data class State(
        val isInitialzed: Boolean,
        val isLoading: Boolean,
        val error: String?,
        val blocks: List<DashboardView> = emptyList(),
        val childrenIdsList: List<String> = emptyList(),
        val objectTypes: List<ObjectType> = emptyList(),
        val details: Block.Details = Block.Details()
    ) : HomeDashboardStateMachine() {
        companion object {
            fun init() = State(
                isInitialzed = false,
                isLoading = false,
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
            val objectTypes: List<ObjectType>
        ) : Event()

        data class OnDetailsUpdated(
            val context: String,
            val target: String,
            val details: Block.Fields,
            val builder: UrlBuilder
        ) : Event()

        data class OnDetailsAmended(
            val context: String,
            val target: String,
            val slice: Map<Id, Any?>,
            val builder: UrlBuilder
        ) : Event()

        data class OnDetailsUnset(
            val context: String,
            val target: String,
            val keys: List<Id>,
            val builder: UrlBuilder
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

        object OnStartedCreatingPage : Event()

        object OnFinishedCreatingPage : Event()
    }

    class Reducer(private val featureToggles: FeatureToggles) : StateReducer<State, List<Event>> {

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
            state: State, events: List<Event>
        ): State {
            var update: State = state
            events.forEach { event ->
                update = reduceEvent(update, event)
            }
            return update
        }

        private fun reduceEvent(
            state: State, event: Event
        ): State {
            return when (event) {
                is Event.OnDashboardLoadingStarted -> state.copy(
                    isInitialzed = true,
                    isLoading = true,
                    error = null
                )
                is Event.OnShowDashboard -> {

                    val new = event.blocks.toDashboardViews(
                        details = event.details,
                        builder = event.builder,
                        objectTypes = event.objectTypes
                    )

                    val childrenIdsList = event.blocks.getChildrenIdsList(parent = event.context)

                    state.copy(
                        isInitialzed = true,
                        isLoading = false,
                        error = null,
                        blocks = new,
                        childrenIdsList = childrenIdsList,
                        objectTypes = event.objectTypes,
                        details = event.details
                    )
                }
                is Event.OnStartedCreatingPage -> state.copy(
                    isLoading = true
                )
                is Event.OnFinishedCreatingPage -> state.copy(
                    isLoading = false
                )
                is Event.OnStructureUpdated -> state.copy(
                    isInitialzed = true,
                    isLoading = false,
                    blocks = state.blocks.sortByIds(event.children),
                    childrenIdsList = event.children
                )
                is Event.OnBlocksAdded -> {
                    val new = event.blocks.toDashboardViews(
                        details = event.details,
                        builder = event.builder
                    )
                    state.copy(
                        isInitialzed = true,
                        isLoading = false,
                        blocks = state.blocks.addAndSortByIds(state.childrenIdsList, new)
                    )
                }
                is Event.OnDetailsUpdated -> {
                    state.copy(
                        blocks = state.blocks.updateDetails(
                            event.target,
                            event.details,
                            event.builder,
                            objectTypes = state.objectTypes
                        ),
                        details = state.details.set(
                            target = event.target,
                            fields = event.details
                        )
                    )
                }
                is Event.OnDetailsAmended -> {
                    val updated = state.details.amend(
                        target = event.target,
                        slice = event.slice
                    )
                    state.copy(
                        details = updated,
                        blocks = state.blocks.updateDetails(
                            target = event.target,
                            details = updated.details[event.target] ?: Block.Fields.empty(),
                            builder = event.builder,
                            objectTypes = state.objectTypes
                        )
                    )
                }
                is Event.OnDetailsUnset -> {
                    val updated = state.details.unset(
                        target = event.target,
                        keys = event.keys
                    )
                    state.copy(
                        details = updated,
                        blocks = state.blocks.updateDetails(
                            target = event.target,
                            details = updated.details[event.target] ?: Block.Fields.empty(),
                            builder = event.builder,
                            objectTypes = state.objectTypes
                        )
                    )
                }
            }
        }
    }
}

fun State.findOTypeById(types: List<String>): ObjectType? {
    val target = types.firstOrNull()
    return objectTypes.find { oType -> oType.url == target }
}
