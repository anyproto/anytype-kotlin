package com.anytypeio.anytype.domain.library

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.SubscriptionEvent
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.runCatchingL
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface StorelessSubscriptionContainer {

    fun subscribe(searchParams: StoreSearchParams): Flow<List<ObjectWrapper.Basic>>
    fun subscribeWithDependencies(searchParams: StoreSearchParams): Flow<SubscriptionWithDependencies>
    fun subscribe(searchParams: StoreSearchByIdsParams) : Flow<List<ObjectWrapper.Basic>>
    suspend fun unsubscribe(subscriptions: List<Id>)

    class Impl @Inject constructor(
        private val repo: BlockRepository,
        private val channel: SubscriptionEventChannel,
        private val dispatchers: AppCoroutineDispatchers,
        private val logger: Logger
    ) : StorelessSubscriptionContainer {

        private val addEventProcessor by lazy { EventAddProcessor() }
        private val unsetEventProcessor by lazy { EventUnsetProcessor() }
        private val removeEventProcessor by lazy { EventRemoveProcessor() }
        private val setEventProcessor by lazy { EventSetProcessor() }
        private val amendEventProcessor by lazy { EventAmendProcessor(logger = logger) }
        private val positionEventProcessor by lazy { EventPositionProcessor() }

        private fun subscribe(subscriptions: List<Id>) = channel.subscribe(subscriptions).map { payload ->
            if (payload.size <= 1) {
                payload
            } else {
                payload.sortedBy { event ->
                    when (event) {
                        is SubscriptionEvent.Add -> 1
                        is SubscriptionEvent.Remove -> 2
                        is SubscriptionEvent.Set -> 3
                        is SubscriptionEvent.Amend -> 4
                        is SubscriptionEvent.Unset -> 5
                        is SubscriptionEvent.Position -> 6
                        is SubscriptionEvent.Counter -> 7
                        is SubscriptionEvent.Group -> 8
                    }
                }
            }
        }

        /**
         * Attaches to the subscription event stream and buffers payloads into an unlimited inbox.
         * Must be called BEFORE the initial search request is sent, so events emitted by the
         * middleware while that request is in flight are replayed on top of the initial results
         * instead of being dropped (the shared event stream has no replay).
         *
         * The inbox is deliberately UNBOUNDED: a stalled downstream collector grows heap
         * instead of back-pressuring the shared event emitter — consistent with the
         * per-subscriber buffer in MiddlewareSubscriptionEventChannel. There is no backlog
         * telemetry at this layer; the pipeline's earlier stages warn on high backlog.
         */
        private fun CoroutineScope.subscribeToEvents(
            subscription: Id
        ): ReceiveChannel<List<SubscriptionEvent>> {
            val inbox = Channel<List<SubscriptionEvent>>(capacity = Channel.UNLIMITED)
            launch(start = CoroutineStart.UNDISPATCHED) {
                try {
                    subscribe(listOf(subscription)).collect { inbox.send(it) }
                } finally {
                    inbox.close()
                }
            }
            return inbox
        }

        override fun subscribe(searchParams: StoreSearchParams): Flow<List<ObjectWrapper.Basic>> =
            flow {
                coroutineScope {
                    val events = subscribeToEvents(subscription = searchParams.subscription)
                    val initial = with(searchParams) {
                        repo.searchObjectsWithSubscription(
                            space = space,
                            subscription = subscription,
                            sorts = sorts,
                            filters = filters,
                            offset = offset,
                            limit = limit,
                            keys = keys,
                            afterId = null,
                            beforeId = null,
                            source = source,
                            ignoreWorkspace = null,
                            noDepSubscription = true,
                            collection = collection
                        )
                    }.results.map { SubscriptionObject(it.id, it) }.toMutableList()
                    emitAll(
                        buildObjectsFlow(
                            subscription = searchParams.subscription,
                            initial = initial,
                            events = events.consumeAsFlow()
                        )
                    )
                }
            }.catch {
                logger.logException(it, "Error in storeless subscription container")
            }.flowOn(dispatchers.io)

        override fun subscribe(searchParams: StoreSearchByIdsParams): Flow<List<ObjectWrapper.Basic>> = flow {
            if (searchParams.targets.isEmpty()) {
                emit(emptyList())
            } else {
                coroutineScope {
                    val events = subscribeToEvents(subscription = searchParams.subscription)
                    val initial = with(searchParams) {
                        repo.searchObjectsByIdWithSubscription(
                            space = space,
                            subscription = subscription,
                            ids = targets,
                            keys = keys
                        )
                    }.results.map {
                        SubscriptionObject(it.id, it)
                    }.toMutableList()
                    emitAll(
                        buildObjectsFlow(
                            subscription = searchParams.subscription,
                            initial = initial,
                            events = events.consumeAsFlow()
                        )
                    )
                }
            }
        }.catch {
            logger.logException(it, "Error in storeless subscription container")
        }.flowOn(
            context = dispatchers.io
        )

        private fun buildObjectsFlow(
            subscription: Id,
            initial: MutableList<SubscriptionObject>,
            events: Flow<List<SubscriptionEvent>>
        ): Flow<List<ObjectWrapper.Basic>> {
            val objectsFlow = events.scan(SubscriptionFold(dataItems = initial)) { fold, payload ->
                var result = fold.dataItems
                var changed = false
                payload.forEach { event ->
                    when (event) {
                        is SubscriptionEvent.Add -> {
                            if (event.subscription == subscription) {
                                result = addEventProcessor.process(event, result)
                                changed = true
                            }
                        }
                        is SubscriptionEvent.Amend -> {
                            if (event.subscriptions.contains(subscription)) {
                                result = amendEventProcessor.process(event, result)
                                changed = true
                            }
                        }
                        is SubscriptionEvent.Position -> {
                            result = positionEventProcessor.process(event, result)
                            changed = true
                        }
                        is SubscriptionEvent.Remove -> {
                            if (event.subscription == subscription) {
                                result = removeEventProcessor.process(event, result)
                                changed = true
                            }
                        }
                        is SubscriptionEvent.Set -> {
                            if (event.subscriptions.contains(subscription)) {
                                result = setEventProcessor.process(event, result)
                                changed = true
                            }
                        }
                        is SubscriptionEvent.Unset -> {
                            if (event.subscriptions.contains(subscription)) {
                                result = unsetEventProcessor.process(event, result)
                                changed = true
                            }
                        }
                        else -> {
                            logger.logWarning("Ignoring subscription event")
                        }
                    }
                }
                SubscriptionFold(dataItems = result, changed = changed)
            }.filter { fold ->
                // Payloads that did not modify the data set (e.g. counter-only payloads)
                // are skipped before the O(n) rebuild below — no duplicate emission downstream.
                fold.changed
            }.map { fold ->
                fold.dataItems.mapNotNull { item ->
                    if (item.objectWrapper?.isValid == true) {
                        item.objectWrapper
                    } else {
                        null
                    }
                }
            }
            return objectsFlow
        }

        override fun subscribeWithDependencies(searchParams: StoreSearchParams): Flow<SubscriptionWithDependencies> {
            return flow {
                coroutineScope {
                    val events = subscribeToEvents(subscription = searchParams.subscription)
                    val initial = with(searchParams) {
                        repo.searchObjectsWithSubscription(
                            space = space,
                            subscription = subscription,
                            sorts = sorts,
                            filters = filters,
                            offset = offset,
                            limit = limit,
                            keys = keys,
                            afterId = null,
                            beforeId = null,
                            source = source,
                            ignoreWorkspace = null,
                            noDepSubscription = false,
                            collection = collection
                        )
                    }
                    emitAll(
                        buildObjectsFlow(
                            subscription = searchParams.subscription,
                            initial = initial.results.map { SubscriptionObject(it.id, it) }
                                .toMutableList(),
                            dependencies = initial.dependencies,
                            events = events.consumeAsFlow()
                        )
                    )
                }
            }.catch {
                logger.logException(it, "Error in storeless subscription container")
            }.flowOn(
                context = dispatchers.io
            )
        }

        private fun buildObjectsFlow(
            subscription: Id,
            initial: MutableList<SubscriptionObject>,
            dependencies: List<ObjectWrapper.Basic>,
            events: Flow<List<SubscriptionEvent>>
        ): Flow<SubscriptionWithDependencies> {
            val objectsFlow = events.scan(SubscriptionFold(dataItems = initial)) { fold, payload ->
                var result = fold.dataItems
                var changed = false
                payload.forEach { event ->
                    when (event) {
                        is SubscriptionEvent.Add -> {
                            if (event.subscription == subscription) {
                                result = addEventProcessor.process(event, result)
                                changed = true
                            }
                        }
                        is SubscriptionEvent.Amend -> {
                            if (event.subscriptions.contains(subscription)) {
                                result = amendEventProcessor.process(event, result)
                                changed = true
                            }
                        }
                        is SubscriptionEvent.Position -> {
                            result = positionEventProcessor.process(event, result)
                            changed = true
                        }
                        is SubscriptionEvent.Remove -> {
                            if (event.subscription == subscription) {
                                result = removeEventProcessor.process(event, result)
                                changed = true
                            }
                        }
                        is SubscriptionEvent.Set -> {
                            if (event.subscriptions.contains(subscription)) {
                                result = setEventProcessor.process(event, result)
                                changed = true
                            }
                        }
                        is SubscriptionEvent.Unset -> {
                            if (event.subscriptions.contains(subscription)) {
                                result = unsetEventProcessor.process(event, result)
                                changed = true
                            }
                        }
                        else -> {
                            logger.logWarning("Ignoring subscription event")
                        }
                    }
                }
                SubscriptionFold(dataItems = result, changed = changed)
            }.filter { fold ->
                fold.changed
            }.map { fold ->
                val objects = fold.dataItems.mapNotNull { item ->
                    if (item.objectWrapper?.isValid == true) {
                        item.objectWrapper
                    } else {
                        null
                    }
                }
                return@map SubscriptionWithDependencies(
                    results = objects,
                    dependencies = dependencies
                )
            }
            return objectsFlow
        }

        override suspend fun unsubscribe(subscriptions: List<Id>) {
            withContext(dispatchers.io) {
                runCatchingL {
                    repo.cancelObjectSearchSubscription(subscriptions)
                }
            }
        }
    }
}

data class SubscriptionObject(
    val id: Id,
    val objectWrapper: ObjectWrapper.Basic? = null
)

data class SubscriptionWithDependencies(
    val results: List<ObjectWrapper.Basic>,
    val dependencies: List<ObjectWrapper.Basic>
)

/**
 * Accumulator for the subscription event fold: the (mutable) data set plus a flag telling
 * whether the last payload actually modified it. The initial state is flagged as changed
 * so the seed emission always reaches downstream collectors.
 */
internal class SubscriptionFold(
    val dataItems: MutableList<SubscriptionObject>,
    val changed: Boolean = true
)
