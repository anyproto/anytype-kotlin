package com.anytypeio.anytype.domain.search

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.SubscriptionEvent
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.`object`.move
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.runCatchingL
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.objects.ObjectStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.withContext

/**
 * Runs one paged record subscription per Kanban column, so each column populates independently
 * (instead of distributing a single shared 50-record page across all columns). Records are
 * merged into the shared [ObjectStore]; each column exposes its current page of ids plus the
 * backend [total] for its group, kept live via that column's [SubscriptionEvent]s.
 */
class BoardRecordsSubscriptionContainer(
    private val repo: BlockRepository,
    private val channel: SubscriptionEventChannel,
    private val store: ObjectStore,
    private val dispatchers: AppCoroutineDispatchers,
    private val logger: Logger
) {

    /** A column's loaded page: the [ids] currently held and the backend [total] for the group. */
    data class GroupPage(val ids: List<Id>, val total: Int)

    fun observe(params: Params): Flow<Map<Id, GroupPage>> {
        if (params.columns.isEmpty()) return flowOf(emptyMap())
        val flows = params.columns.map { column -> observeColumn(params, column) }
        return combine(flows) { pages -> pages.toMap() }
            .catch { logger.logException(it, "Error in board records subscription container") }
            .flowOn(dispatchers.io)
    }

    private fun observeColumn(params: Params, column: Column): Flow<Pair<Id, GroupPage>> = flow {
        val initial = repo.searchObjectsWithSubscription(
            space = params.space,
            subscription = column.subscription,
            sorts = params.sorts.distinct(),
            filters = (params.baseFilters + column.filter).distinct(),
            keys = params.keys.distinct(),
            source = params.source,
            offset = 0,
            limit = params.limit,
            beforeId = null,
            afterId = null,
            ignoreWorkspace = null,
            noDepSubscription = null,
            collection = params.collection
        )
        store.merge(
            objects = initial.results,
            dependencies = initial.dependencies,
            subscriptions = listOf(column.subscription)
        )
        val seed = GroupPage(
            ids = initial.results.map { it.id },
            total = initial.counter?.total ?: initial.results.size
        )
        emitAll(
            channel.subscribe(listOf(column.subscription))
                .scan(seed) { page, payload -> reduce(page, payload, column.subscription) }
                .map { page -> column.columnId to page }
        )
    }

    private suspend fun reduce(
        page: GroupPage,
        payload: List<SubscriptionEvent>,
        subscription: Id
    ): GroupPage {
        var ids = page.ids
        var total = page.total
        payload.forEach { event ->
            when (event) {
                is SubscriptionEvent.Add -> {
                    if (event.subscription == subscription) {
                        ids = ids.toMutableList().apply {
                            val afterIdx = event.afterId?.let { indexOf(it) } ?: -1
                            if (afterIdx != -1) add(afterIdx.inc(), event.target) else add(0, event.target)
                        }
                        total += 1
                    }
                    store.subscribe(subscription = event.subscription, target = event.target)
                }
                is SubscriptionEvent.Remove -> {
                    if (event.subscription == subscription && ids.contains(event.target)) {
                        ids = ids.filter { it != event.target }
                        total = (total - 1).coerceAtLeast(0)
                    }
                    store.unsubscribe(target = event.target, subscription = event.subscription)
                }
                is SubscriptionEvent.Position -> {
                    ids = ids.move(target = event.target, afterId = event.afterId)
                }
                is SubscriptionEvent.Amend -> {
                    store.amend(target = event.target, diff = event.diff, subscriptions = event.subscriptions)
                }
                is SubscriptionEvent.Set -> {
                    store.set(target = event.target, data = event.data, subscriptions = event.subscriptions)
                }
                is SubscriptionEvent.Unset -> {
                    store.unset(target = event.target, keys = event.keys, subscriptions = event.subscriptions)
                }
                is SubscriptionEvent.Counter -> {
                    // Counter is the authoritative absolute total; it corrects any add/remove drift.
                    total = event.counter.total
                }
                is SubscriptionEvent.Group -> {
                    // Group events are not delivered on a record subscription.
                }
            }
        }
        return GroupPage(ids = ids, total = total)
    }

    suspend fun unsubscribe(subscriptions: List<Id>) {
        withContext(dispatchers.io) {
            runCatchingL { repo.cancelObjectSearchSubscription(subscriptions) }
        }
    }

    data class Column(
        val subscription: Id,
        val columnId: Id,
        val filter: DVFilter
    )

    data class Params(
        val space: SpaceId,
        val columns: List<Column>,
        val sorts: List<DVSort>,
        val baseFilters: List<DVFilter>,
        val keys: List<Key>,
        val source: List<String> = emptyList(),
        val collection: Id? = null,
        val limit: Int
    )
}
