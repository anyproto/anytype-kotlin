package com.anytypeio.anytype.presentation.dashboard

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ext.amend
import com.anytypeio.anytype.core_models.ext.getChildrenIdsList
import com.anytypeio.anytype.core_models.ext.set
import com.anytypeio.anytype.core_models.ext.unset
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.common.StateReducer
import com.anytypeio.anytype.presentation.dashboard.HomeDashboardStateMachine.*
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
        private val reducer: Reducer = Reducer(),
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
            val objectTypes: List<ObjectType>,
            val isDataViewEnabled: Boolean
        ) : Event()

        data class OnShowProfile(
            val context: String,
            val blocks: List<Block>,
            val details: Block.Details,
            val builder: UrlBuilder
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

        data class OnLinkFieldsChanged(
            val id: String,
            val fields: Block.Fields,
            val builder: UrlBuilder
        ) : Event()

        object OnDashboardLoadingStarted : Event()

        object OnStartedCreatingPage : Event()

        object OnFinishedCreatingPage : Event()
    }

    class Reducer : StateReducer<State, List<Event>> {

        override val function: suspend (State, List<Event>) -> State
            get() = { state, events ->
                Timber.d("REDUCE, STATE:$state, EVENT:$events")
                val update = reduce(state, events)
                Timber.d("REDUCE, UPDATED STATE:$update")
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

                    val current = state.blocks.filterIsInstance<DashboardView.Profile>()

                    val new = event.blocks
                        .toDashboardViews(
                            details = event.details,
                            builder = event.builder,
                            objectTypes = event.objectTypes
                        )
                        .filterBySets(isDataViewEnabled = event.isDataViewEnabled)

                    val childrenIdsList = event.blocks.getChildrenIdsList(
                        parent = event.context
                    )

                    state.copy(
                        isInitialzed = true,
                        isLoading = false,
                        error = null,
                        blocks = current.addAndSortByIds(childrenIdsList, new),
                        childrenIdsList = childrenIdsList,
                        objectTypes = event.objectTypes,
                        details = event.details
                    )
                }
                is Event.OnShowProfile -> {

                    val current = state.blocks.filter { it !is DashboardView.Profile }

                    val new = event.blocks.toDashboardViews(
                        details = event.details,
                        builder = event.builder
                    ).filterIsInstance<DashboardView.Profile>()

                    state.copy(
                        isInitialzed = true,
                        isLoading = false,
                        error = null,
                        blocks = current.addAndSortByIds(state.childrenIdsList, new)
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
                is Event.OnLinkFieldsChanged -> {
                    state.copy(
                        blocks = state.blocks.updateDetails(
                            event.id,
                            event.fields,
                            event.builder,
                            objectTypes = state.objectTypes
                        )
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

        private fun List<DashboardView>.filterBySets(
            isDataViewEnabled: Boolean
        ): List<DashboardView> = if (isDataViewEnabled) {
            this
        } else {
            this.filter { it !is DashboardView.ObjectSet }
        }
    }
}

fun State.findOTypeById(types: List<String>): ObjectType? {
    val target = types.firstOrNull()
    return objectTypes.find { oType -> oType.url == target }
}
