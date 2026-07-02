package com.anytypeio.anytype.domain.search

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DataViewGroup
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.SubscriptionEvent
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.debugging.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.withContext

/**
 * Live subscription to a data-view's groups (Kanban columns). Returns the initial
 * groups from the backend and keeps them in sync with `subscriptionGroups` events
 * (group add / update / remove) over the same subscription id.
 */
class BoardGroupSubscriptionContainer(
    private val repo: BlockRepository,
    private val channel: SubscriptionEventChannel,
    private val dispatchers: AppCoroutineDispatchers,
    private val logger: Logger
) {

    fun observe(params: Params): Flow<List<DataViewGroup>> {
        return flow {
            val initial = repo.objectGroupsSubscribe(
                space = params.space,
                subscription = params.subscription,
                relationKey = params.relationKey,
                filters = params.filters,
                source = params.sources,
                collection = params.collection
            )
            emitAll(
                channel.subscribe(listOf(params.subscription)).scan(initial) { groups, payload ->
                    var result = groups
                    payload.forEach { event ->
                        if (event is SubscriptionEvent.Group && event.subscription == params.subscription) {
                            result = if (event.remove) {
                                result.filterNot { it.id == event.group.id }
                            } else {
                                val index = result.indexOfFirst { it.id == event.group.id }
                                if (index >= 0) {
                                    // Update in place — keep the backend column order stable.
                                    result.toMutableList().also { it[index] = event.group }
                                } else {
                                    // New group — append (the backend prepends "empty").
                                    result + event.group
                                }
                            }
                        }
                    }
                    result
                }
            )
        }.catch {
            logger.logException(it, "Error in board group subscription container")
        }.flowOn(dispatchers.io)
    }

    suspend fun unsubscribe(subscription: Id) = withContext(dispatchers.io) {
        runCatching { repo.cancelObjectSearchSubscription(listOf(subscription)) }
    }

    data class Params(
        val space: SpaceId,
        val subscription: Id,
        val relationKey: Key,
        val filters: List<DVFilter>,
        val sources: List<String>,
        val collection: Id?
    )

    companion object {
        const val SUBSCRIPTION_POSTFIX = "-board-groups"
    }
}
