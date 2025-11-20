package com.anytypeio.anytype.domain.library

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.SubscriptionEvent
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.library.processors.EventAddProcessor
import com.anytypeio.anytype.domain.library.processors.EventAmendProcessor
import com.anytypeio.anytype.domain.library.processors.EventPositionProcessor
import com.anytypeio.anytype.domain.library.processors.EventRemoveProcessor
import com.anytypeio.anytype.domain.library.processors.EventSetProcessor
import com.anytypeio.anytype.domain.library.processors.EventUnsetProcessor
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan

/**
 * Storeless subscription container for cross-space searches.
 *
 * ## What "Storeless" Means
 *
 * This container does NOT maintain persistent state between subscriptions:
 * - **No instance-level caching** of subscription results
 * - **State exists only within the Flow scope** using the `scan` operator
 * - **Each subscribe() call creates a fresh, independent Flow** with isolated state
 * - **State is automatically garbage collected** when the Flow collection ends
 *
 * ## Benefits of Storeless Design
 *
 * - **Multiple concurrent subscriptions** without state conflicts
 * - **Automatic cleanup** when subscribers stop collecting
 * - **Memory efficiency** - no leaked state or stale data
 * - **Thread safety** - no shared mutable state to synchronize
 *
 * ## Cross-Space vs Single-Space
 *
 * Unlike [StorelessSubscriptionContainer] which is scoped to a single space,
 * this container searches and subscribes to objects **across all user spaces** using
 * the `crossSpaceSearchSubscribe` middleware API.
 *
 * ## Flow Lifecycle
 *
 * 1. Subscribe with [CrossSpaceSearchParams] (filters, sorts, keys)
 * 2. Initial results fetched via [BlockRepository.crossSpaceSearchSubscribe]
 * 3. Results mapped to internal state ([SubscriptionObject] list)
 * 4. State accumulated via `scan` operator processing [SubscriptionEvent]s from [SubscriptionEventChannel]
 * 5. Final results emitted as [ObjectWrapper.Basic] list (only valid objects)
 * 6. **State discarded** when Flow collection ends
 *
 * ## Event Processing Order
 *
 * Events are processed in priority order to ensure consistency:
 * 1. **Add** - New objects added to subscription
 * 2. **Remove** - Objects removed from subscription
 * 3. **Set** - Object field values set
 * 4. **Amend** - Object field values updated
 * 5. **Unset** - Object field values cleared
 * 6. **Position** - Object positions reordered
 * 7. **Counter** - Counters updated (ignored by this container)
 * @see StorelessSubscriptionContainer for single-space subscriptions
 * @see SubscriptionObject internal state representation
 */
interface CrossSpaceSubscriptionContainer {

    fun subscribe(searchParams: CrossSpaceSearchParams): Flow<List<ObjectWrapper.Basic>>
    suspend fun unsubscribe(subscription: Id)

    class Impl @Inject constructor(
        private val repo: BlockRepository,
        private val channel: SubscriptionEventChannel,
        private val dispatchers: AppCoroutineDispatchers,
        private val logger: Logger
    ) : CrossSpaceSubscriptionContainer {

        private val addEventProcessor by lazy { EventAddProcessor() }
        private val unsetEventProcessor by lazy { EventUnsetProcessor() }
        private val removeEventProcessor by lazy { EventRemoveProcessor() }
        private val setEventProcessor by lazy { EventSetProcessor() }
        private val amendEventProcessor by lazy { EventAmendProcessor(logger = logger) }
        private val positionEventProcessor by lazy { EventPositionProcessor() }

        private fun subscribe(subscriptions: List<Id>) =
            channel.subscribe(subscriptions).map { payload ->
                payload.sortedBy { event ->
                    when (event) {
                        is SubscriptionEvent.Add -> 1
                        is SubscriptionEvent.Remove -> 2
                        is SubscriptionEvent.Set -> 3
                        is SubscriptionEvent.Amend -> 4
                        is SubscriptionEvent.Unset -> 5
                        is SubscriptionEvent.Position -> 6
                        is SubscriptionEvent.Counter -> 7
                    }
                }
            }

        override fun subscribe(searchParams: CrossSpaceSearchParams): Flow<List<ObjectWrapper.Basic>> =
            flow {
                with(searchParams) {
                    val command = Command.CrossSpaceSearchSubscribe(
                        subscription = subscription,
                        filters = filters,
                        sorts = sorts,
                        keys = keys,
                        source = source,
                        noDepSubscription = true,
                        collectionId = collection
                    )
                    val initial = repo.crossSpaceSearchSubscribe(command).results.map {
                        SubscriptionObject(
                            id = it.id, objectWrapper = it
                        )
                    }.toMutableList()
                    emitAll(
                        buildObjectsFlow(
                            subscription = searchParams.subscription,
                            initial = initial
                        )
                    )
                }
            }.catch {
                logger.logException(
                    it,
                    "Error in cross-space subscription container :[${searchParams.subscription}]"
                )
            }.flowOn(dispatchers.io)

        private fun buildObjectsFlow(
            subscription: Id,
            initial: MutableList<SubscriptionObject>
        ): Flow<List<ObjectWrapper.Basic>> {
            val objectsFlow = subscribe(listOf(subscription)).scan(initial) { dataItems, payload ->
                var result = dataItems
                payload.forEach { event ->
                    when (event) {
                        is SubscriptionEvent.Add -> {
                            if (event.subscription == subscription) {
                                result = addEventProcessor.process(event, result)
                            }
                        }

                        is SubscriptionEvent.Amend -> {
                            if (event.subscriptions.contains(subscription)) {
                                result = amendEventProcessor.process(event, result)
                            }
                        }

                        is SubscriptionEvent.Position -> {
                            result = positionEventProcessor.process(event, result)
                        }

                        is SubscriptionEvent.Remove -> {
                            if (event.subscription == subscription) {
                                result = removeEventProcessor.process(event, result)
                            }
                        }

                        is SubscriptionEvent.Set -> {
                            if (event.subscriptions.contains(subscription)) {
                                result = setEventProcessor.process(event, result)
                            }
                        }

                        is SubscriptionEvent.Unset -> {
                            if (event.subscriptions.contains(subscription)) {
                                result = unsetEventProcessor.process(event, result)
                            }
                        }

                        else -> {
                            logger.logWarning("Ignoring subscription event")
                        }
                    }
                }
                result
            }.map { result ->
                result.mapNotNull { item ->
                    if (item.objectWrapper?.isValid == true) {
                        item.objectWrapper
                    } else {
                        null
                    }
                }
            }
            return objectsFlow
        }

        override suspend fun unsubscribe(subscription: Id) {
            repo.objectCrossSpaceUnsubscribe(subscription)
        }
    }
}
