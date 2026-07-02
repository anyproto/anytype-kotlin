package com.anytypeio.anytype.domain.search

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SubscriptionEvent
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.`object`.move
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.runCatchingL
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.objects.ObjectStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

/**
 * Non-visual bookkeeping keys that don't change a card's rendered content *by default*. An amend
 * touching only these is suppressed (does NOT bump [BoardRecordsSubscriptionContainer.GroupPage
 * .revision]) — otherwise the board would re-render on every edit's bookkeeping fields (e.g. the
 * automatic `lastModifiedBy` amend that fires right after create, while the "name your object"
 * sheet is animating in).
 *
 * This is the DEFAULT suppression set only. The effective set is this minus the keys the current
 * view actually displays ([Params.displayedKeys]), so the gate is fail-safe: any unknown/new key
 * re-renders, and a bookkeeping key that the viewer shows as a visible card relation (e.g. "Last
 * modified date") also re-renders instead of going stale.
 */
private val BOOKKEEPING_AMEND_KEYS: Set<Key> = setOf(
    Relations.LAST_MODIFIED_DATE,
    Relations.LAST_OPENED_DATE,
    Relations.LAST_USED_DATE,
    "lastModifiedBy"
)

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

    /**
     * A column's loaded page: the [ids] currently held and the backend [total] for the group.
     * [revision] bumps on every record content change (Amend/Set/Unset) so a downstream
     * StateFlow — which dedups value-equal maps — still re-emits and the board re-renders a
     * card whose name/relations/icon changed (its id and the group total are unchanged).
     */
    data class GroupPage(val ids: List<Id>, val total: Int, val revision: Int = 0)

    /** Per-column subscription limit (column id -> limit), grown by [loadMore]. */
    private val limits = MutableStateFlow<Map<Id, Int>>(emptyMap())

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observe(params: Params): Flow<Map<Id, GroupPage>> {
        if (params.columns.isEmpty()) return flowOf(emptyMap())
        limits.value = params.columns.associate { it.columnId to params.limit }
        val flows = params.columns.map { column ->
            limits
                .map { it[column.columnId] ?: params.limit }
                .distinctUntilChanged()
                .flatMapLatest { limit -> observeColumn(params, column, limit) }
        }
        return combine(flows) { pages -> pages.toMap() }
            .catch { logger.logException(it, "Error in board records subscription container") }
            .flowOn(dispatchers.io)
    }

    /**
     * Grows a single column's subscription limit by [additional], so only that column re-fetches
     * (a larger page on the same subscription id); other columns are untouched.
     */
    fun loadMore(columnId: Id, additional: Int) {
        limits.update { current ->
            current + (columnId to ((current[columnId] ?: 0) + additional))
        }
    }

    private fun observeColumn(params: Params, column: Column, limit: Int): Flow<Pair<Id, GroupPage>> = flow {
        // Fail-safe: a bookkeeping key that this view actually displays must still re-render the
        // card, so drop the displayed keys from the effective suppression set.
        val silentKeys = BOOKKEEPING_AMEND_KEYS - params.displayedKeys
        val initial = repo.searchObjectsWithSubscription(
            space = params.space,
            subscription = column.subscription,
            sorts = params.sorts.distinct(),
            filters = (params.baseFilters + column.filter).distinct(),
            keys = params.keys.distinct(),
            source = params.source,
            offset = 0,
            limit = limit,
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
                .scan(seed) { page, payload -> reduce(page, payload, column.subscription, silentKeys) }
                .map { page -> column.columnId to page }
        )
    }

    private suspend fun reduce(
        page: GroupPage,
        payload: List<SubscriptionEvent>,
        subscription: Id,
        silentKeys: Set<Key>
    ): GroupPage {
        var ids = page.ids
        var total = page.total
        var revision = page.revision
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
                    // A rendered field (name/relations/icon) changed; ids/total are the same, so
                    // bump the revision to force the board to re-render this card from the updated
                    // store. Skip amends that only touch bookkeeping keys this view doesn't display.
                    if (event.diff.keys.any { it !in silentKeys }) revision++
                }
                is SubscriptionEvent.Set -> {
                    store.set(target = event.target, data = event.data, subscriptions = event.subscriptions)
                    revision++
                }
                is SubscriptionEvent.Unset -> {
                    store.unset(target = event.target, keys = event.keys, subscriptions = event.subscriptions)
                    revision++
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
        return GroupPage(ids = ids, total = total, revision = revision)
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
        // Keys the current viewer displays as visible card relations. Bookkeeping keys in this set
        // are excluded from amend suppression so a displayed value never goes stale (fail-safe).
        val displayedKeys: Set<Key> = emptySet(),
        val source: List<String> = emptyList(),
        val collection: Id? = null,
        val limit: Int
    )
}
