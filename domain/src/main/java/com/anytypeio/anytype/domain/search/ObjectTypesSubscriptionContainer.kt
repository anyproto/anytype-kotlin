package com.anytypeio.anytype.domain.search

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.SubscriptionEvent
import com.anytypeio.anytype.domain.`object`.move
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.scan

class ObjectTypesSubscriptionContainer(
    private val repo: BlockRepository,
    private val channel: SubscriptionEventChannel,
    private val store: StoreOfObjectTypes,
    private val dispatchers: AppCoroutineDispatchers
) {

    fun observe(params: Params): Flow<Index> {
        return flow {
            val initial = repo.searchObjectsWithSubscription(
                subscription = params.subscription,
                sorts = params.sorts,
                filters = params.filters,
                offset = params.offset,
                limit = params.limit,
                keys = params.keys,
                source = params.sources,
                noDepSubscription = true,
                ignoreWorkspace = params.ignoreWorkspace,
                afterId = null,
                beforeId = null
            )
            store.merge(
                types = initial.results.map { ObjectWrapper.Type(it.map) }
            )
            val sub = Index(
                objects = initial.results.map { it.id },
                dependencies = initial.dependencies.map { it.id }
            )

            emitAll(
                subscribe(listOf(params.subscription)).scan(sub) { s, payload ->
                    var result = s
                    payload.forEach { event ->
                        when (event) {
                            is SubscriptionEvent.Add -> {
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
                            is SubscriptionEvent.Amend -> {
                                store.amend(
                                    target = event.target,
                                    diff = event.diff
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
                                result = result.copy(
                                    objects = result.objects.filter { id ->
                                        id != event.target
                                    }
                                )
                                store.remove(target = event.target)
                            }
                            is SubscriptionEvent.Set -> {
                                store.set(
                                    target = event.target,
                                    data = event.data
                                )
                            }
                            is SubscriptionEvent.Unset -> {
                                store.unset(
                                    target = event.target,
                                    keys = event.keys
                                )
                            }
                            else -> {
                                // Do nothing.
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
        val keys: List<String>,
        val ignoreWorkspace: Boolean
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

    companion object {
        const val SUBSCRIPTION_ID = "object-type-store-subscription"
    }
}