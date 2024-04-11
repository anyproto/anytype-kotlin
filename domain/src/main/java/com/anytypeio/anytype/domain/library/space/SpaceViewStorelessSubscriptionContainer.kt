package com.anytypeio.anytype.domain.library.space

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper.SpaceView
import com.anytypeio.anytype.core_models.SubscriptionEvent
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.runCatchingL
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.processors.space.SpaceEventAddProcessor
import com.anytypeio.anytype.domain.library.processors.space.SpaceEventAmendProcessor
import com.anytypeio.anytype.domain.library.processors.space.SpaceEventPositionProcessor
import com.anytypeio.anytype.domain.library.processors.space.SpaceEventRemoveProcessor
import com.anytypeio.anytype.domain.library.processors.space.SpaceEventSetProcessor
import com.anytypeio.anytype.domain.library.processors.space.SpaceEventUnsetProcessor
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.withContext

interface SpaceViewSubscriptionContainer {

    fun subscribe(searchParams: StoreSearchParams) : Flow<List<SpaceView>>
    suspend fun unsubscribe(subscriptions: List<Id>)

    class Impl @Inject constructor(
        private val repo: BlockRepository,
        private val channel: SubscriptionEventChannel,
        private val dispatchers: AppCoroutineDispatchers,
        private val logger: Logger
    ) : SpaceViewSubscriptionContainer {

        private val addEventProcessor by lazy { SpaceEventAddProcessor() }
        private val unsetEventProcessor by lazy { SpaceEventUnsetProcessor() }
        private val removeEventProcessor by lazy { SpaceEventRemoveProcessor() }
        private val setEventProcessor by lazy { SpaceEventSetProcessor() }
        private val amendEventProcessor by lazy { SpaceEventAmendProcessor(logger = logger) }
        private val positionEventProcessor by lazy { SpaceEventPositionProcessor() }

        private fun subscribe(subscriptions: List<Id>) = channel.subscribe(subscriptions)

        override fun subscribe(searchParams: StoreSearchParams) = flow {
            with(searchParams) {
                val params = Command.SearchSpaceWithSubscription(
                    subscription = subscription,
                    filters = filters,
                    sorts = sorts,
                    limit = limit,
                    keys = keys
                )
                val initial = repo.searchSpaceWithSubscription(params).results.map { result ->
                    val spaceView = if (!result.isNullOrEmpty()) {
                        SpaceView(result)
                    } else {
                        null
                    }
                    SpaceSubscriptionObject(
                        id = subscription,
                        spaceView = spaceView
                    )
                }.toMutableList()
                emitAll(
                    buildObjectsFlow(
                        subscription = searchParams.subscription,
                        initial = initial
                    )
                )
            }
        }.flowOn(dispatchers.io)

        private fun buildObjectsFlow(
            subscription: Id,
            initial: MutableList<SpaceSubscriptionObject>
        ): Flow<List<SpaceView>> {
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
                    if (item.spaceView?.map.isNullOrEmpty()) {
                        null
                    } else {
                        item.spaceView
                    }
                }
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

data class SpaceSubscriptionObject(
    val id: Id,
    val spaceView: SpaceView? = null
)