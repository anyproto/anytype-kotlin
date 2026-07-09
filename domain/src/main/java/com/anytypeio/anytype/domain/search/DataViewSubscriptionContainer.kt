package com.anytypeio.anytype.domain.search

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.SearchResult
import com.anytypeio.anytype.core_models.SubscriptionEvent
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.`object`.move
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.runCatchingL
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.objects.ObjectStore
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Container for data view subscription responsible for:
 * fetching data for data view and storing it in the provided [ObjectStore]
 * reacting to subsequent [SubscriptionEvent] (objects added, removed, repositioned, etc.)
 * emitting Data View state [DataViewState] changes to its subscribers
 * keeping track for [SearchResult.Counter] changes needed for pagination logic and emitting it to its subscribers
 */
class DataViewSubscriptionContainer(
    private val repo: BlockRepository,
    private val channel: SubscriptionEventChannel,
    private val store: ObjectStore,
    private val dispatchers: AppCoroutineDispatchers
) {

    val counter = MutableSharedFlow<SearchResult.Counter>()

    /**
     * Monotonic revision stamped into [DataViewState.Loaded.lastModified]. A counter (not wall
     * clock) so that two states with identical id lists but different backing-store content can
     * never spuriously compare equal (System.currentTimeMillis() collides within one millisecond,
     * which would let downstream StateFlow equality dedup silently drop the second render).
     * Container-wide (not per-flow) so a re-subscription's first state also never equals the
     * previous incarnation's last one.
     */
    private val revision = AtomicLong(0L)

    fun observe(params: Params): Flow<DataViewState> {
        return flow {
            coroutineScope {
                // Attach to the event channel before the initial search RPC, buffering
                // events arriving while the request is in flight — otherwise they would
                // be dropped, leaving stale results until the next event shows up.
                // UNLIMITED: a stalled downstream collector grows heap instead of
                // back-pressuring the shared event emitter — deliberate, consistent with
                // the rest of the event pipeline (see MiddlewareSubscriptionEventChannel).
                val pending = Channel<List<SubscriptionEvent>>(capacity = Channel.UNLIMITED)
                // UNDISPATCHED: register the collector before this coroutine proceeds to the
                // search RPC. Note the guarantee is still probabilistic, not strict: the
                // channel's per-subscriber buffer (MiddlewareSubscriptionEventChannel) adds one
                // dispatch hop before the shared-flow slot is registered — a window of one
                // dispatch vs. a full middleware round-trip.
                launch(start = CoroutineStart.UNDISPATCHED) {
                    try {
                        subscribe(listOf(params.subscription)).collect { pending.send(it) }
                    } finally {
                        pending.close()
                    }
                }
                val initial = repo.searchObjectsWithSubscription(
                    space = params.space,
                    subscription = params.subscription,
                    sorts = params.sorts.distinct(),
                    filters = params.filters.distinct(),
                    offset = params.offset,
                    limit = params.limit,
                    keys = params.keys.distinct(),
                    afterId = null,
                    beforeId = null,
                    source = params.sources,
                    ignoreWorkspace = null,
                    noDepSubscription = null,
                    collection = params.collection
                )
                store.merge(
                    objects = initial.results,
                    dependencies = initial.dependencies,
                    subscriptions = listOf(params.subscription)
                )
                val sub = DataViewState.Loaded(
                    objects = initial.results.map { it.id },
                    dependencies = initial.dependencies.map { it.id },
                    lastModified = revision.incrementAndGet()
                )

                initial.counter?.let { counter.emit(it) }

                emitAll(
                    pending.consumeAsFlow().scan(sub) { s, payload ->
                        var result = s
                        // Whether this batch touched data that subscribers render. Counter-only
                        // (or empty) batches keep the previous state instance so downstream
                        // StateFlow equality dedup can skip a full re-render.
                        var changed = false
                        payload.forEach { event ->
                            when (event) {
                                is SubscriptionEvent.Group -> {
                                    // Group events are not delivered on the record subscription.
                                }
                                is SubscriptionEvent.Add -> {
                                    changed = true
                                    if (event.subscription == params.subscription) {
                                        // Main subscription. Skip targets already present —
                                        // e.g. an add buffered while the initial results were
                                        // in flight and already contained in them. If the
                                        // event's afterId implies a different position than the
                                        // snapshot, the snapshot order is kept; a subsequent
                                        // Position event corrects it.
                                        if (!result.objects.contains(event.target)) {
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
                                        }
                                    } else {
                                        // Dependent subscription
                                        if (!result.dependencies.contains(event.target)) {
                                            result = result.copy(
                                                dependencies = result.dependencies.toMutableList().apply {
                                                    add(0, event.target)
                                                }
                                            )
                                        }
                                    }
                                    store.subscribe(
                                        subscription = event.subscription,
                                        target = event.target
                                    )
                                }
                                is SubscriptionEvent.Amend -> {
                                    changed = true
                                    store.amend(
                                        target = event.target,
                                        diff = event.diff,
                                        subscriptions = event.subscriptions
                                    )
                                }
                                is SubscriptionEvent.Position -> {
                                    changed = true
                                    result = result.copy(
                                        objects = result.objects.move(
                                            target = event.target,
                                            afterId = event.afterId
                                        )
                                    )
                                }
                                is SubscriptionEvent.Remove -> {
                                    changed = true
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
                                    changed = true
                                    store.set(
                                        target = event.target,
                                        data = event.data,
                                        subscriptions = event.subscriptions
                                    )
                                }
                                is SubscriptionEvent.Unset -> {
                                    changed = true
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
                        if (changed) {
                            result.copy(
                                lastModified = revision.incrementAndGet()
                            )
                        } else {
                            result
                        }
                    }
                )
            }
        }.flowOn(dispatchers.io)
    }

    /**
     * Returns events for subscriptions and dependent subscriptions
     */
    private fun subscribe(subscriptions: List<Id>) = channel.subscribe(subscriptions)

    suspend fun unsubscribe(subscriptions: List<Id>) {
        withContext(dispatchers.io) {
            runCatchingL {
                repo.cancelObjectSearchSubscription(subscriptions)
            }
        }
    }

    data class Params(
        val space: SpaceId,
        val subscription: Id,
        val sorts: List<DVSort>,
        val filters: List<DVFilter>,
        val offset: Long,
        val limit: Int,
        val keys: List<Key>,
        val sources: List<String> = emptyList(),
        val collection: Id? = null
    )
}