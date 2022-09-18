package com.anytypeio.anytype.domain.search

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.SearchResult
import com.anytypeio.anytype.core_models.SubscriptionEvent
import com.anytypeio.anytype.domain.`object`.move
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.search.DataViewSubscriptionContainer.Index
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.scan

/**
 * Container for data view subscription responsible for:
 * fetching data for data view and storing it in the provided [ObjectStore]
 * reacting to subsequent [SubscriptionEvent] (objects added, removed, repositioned, etc.)
 * emitting database [Index] changes to its subscribers
 * keeping track for [SearchResult.Counter] changes needed for pagination logic and emitting it to its subscribers
 */
class DataViewSubscriptionContainer(
    private val repo: BlockRepository,
    private val channel: SubscriptionEventChannel,
    val store: ObjectStore,
    private val dispatchers: AppCoroutineDispatchers
) {

    val counter = MutableSharedFlow<SearchResult.Counter>()

    fun observe(params: Params): Flow<Index> {
        return flow {
            val initial = repo.searchObjectsWithSubscription(
                subscription = params.subscription,
                sorts = params.sorts,
                filters = params.filters,
                offset = params.offset,
                limit = params.limit,
                keys = params.keys,
                afterId = null,
                beforeId = null,
                source = params.sources
            )
            store.merge(
                objects = initial.results,
                dependencies = initial.dependencies,
                subscriptions = listOf(params.subscription)
            )
            val sub = Index(
                objects = initial.results.map { it.id },
                dependencies = initial.dependencies.map { it.id }
            )

            initial.counter?.let { counter.emit(it) }

            emitAll(
                subscribe(listOf(params.subscription)).scan(sub) { s, payload ->
                    var result = s
                    payload.forEach { event ->
                        when (event) {
                            is SubscriptionEvent.Add -> {
                                if (event.subscription == params.subscription) {
                                    // Main subscription
                                    val afterId = event.afterId
                                    if (afterId != null) {
                                        val afterIdx = result.objects.indexOfFirst { id ->
                                            afterId == id
                                        }
                                        val updated = result.objects.toMutableList().apply {
                                            if (afterIdx != -1) {
                                                add(afterIdx.inc(), event.target)
                                            } else {
                                                add(0, event.target)
                                            }
                                        }
                                        result = result.copy(
                                            objects = updated
                                        )
                                    } else {
                                        result = result.copy(
                                            objects = result.objects.toMutableList().apply {
                                                add(0, event.target)
                                            }
                                        )
                                    }
                                } else {
                                    // Dependent subscription
                                    result = result.copy(
                                        dependencies = result.dependencies.toMutableList().apply {
                                            add(0, event.target)
                                        }
                                    )
                                }
                                store.subscribe(
                                    subscription = event.subscription,
                                    target = event.target
                                )
                            }
                            is SubscriptionEvent.Amend -> {
                                store.amend(
                                    target = event.target,
                                    diff = event.diff,
                                    subscriptions = event.subscriptions
                                )
                            }
                            is SubscriptionEvent.Position -> {
                                result = result.copy(
                                    objects = result.objects.move(
                                        target = event.target,
                                        afterId = event.afterId
                                    )
                                )
                            }
                            is SubscriptionEvent.Remove -> {
                                result = if (event.subscription == params.subscription) {
                                    // Main subscription
                                    result.copy(
                                        objects = result.objects.filter { id ->
                                            id != event.target
                                        }
                                    )
                                } else {
                                    // Dependent subscription
                                    result.copy(
                                        dependencies = result.dependencies.filter { id ->
                                            id != event.target
                                        }
                                    )
                                }
                                store.unsubscribe(
                                    target = event.target,
                                    subscription = event.subscription
                                )
                            }
                            is SubscriptionEvent.Set -> {
                                store.set(
                                    target = event.target,
                                    data = event.data,
                                    subscriptions = event.subscriptions
                                )
                            }
                            is SubscriptionEvent.Unset -> {
                                store.unset(
                                    target = event.target,
                                    keys = event.keys,
                                    subscriptions = event.subscriptions
                                )
                            }
                            is SubscriptionEvent.Counter -> {
                                counter.emit(event.counter)
                            }
                        }
                    }
                    result.copy(
                        lastModified = System.currentTimeMillis()
                    )
                }
            )
        }.flowOn(dispatchers.io)
    }

    /**
     * Returns events for subscriptions and dependent subscriptions
     */
    private fun subscribe(subscriptions: List<Id>) = channel.subscribe(subscriptions)

    data class Params(
        val subscription: Id,
        val sorts: List<DVSort>,
        val filters: List<DVFilter>,
        val sources: List<String>,
        val offset: Long,
        val limit: Int,
        val keys: List<Key>
    )

    /**
     * Index for keeping track of results and its dependencies.
     * @property [objects] data for this subscription
     * @property [dependencies] its dependencies
     * @property [lastModified] timestamp for data modification
     */
    data class Index(
        val objects: List<Id> = emptyList(),
        val dependencies: List<Id> = emptyList(),
        val lastModified: Long = 0L
    )
}