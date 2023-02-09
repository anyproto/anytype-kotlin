package com.anytypeio.anytype.domain.library

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.SubscriptionEvent
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.library.processors.EventAddProcessor
import com.anytypeio.anytype.domain.library.processors.EventAmendProcessor
import com.anytypeio.anytype.domain.library.processors.EventPositionProcessor
import com.anytypeio.anytype.domain.library.processors.EventRemoveProcessor
import com.anytypeio.anytype.domain.library.processors.EventSetProcessor
import com.anytypeio.anytype.domain.library.processors.EventUnsetProcessor
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan

interface StorelessSubscriptionContainer {

    fun subscribe(searchParams: StoreSearchParams): Flow<List<ObjectWrapper.Basic>>

    class Impl @Inject constructor(
        private val repo: BlockRepository,
        private val channel: SubscriptionEventChannel,
        private val dispatchers: AppCoroutineDispatchers
    ) : StorelessSubscriptionContainer {

        private val addEventProcessor by lazy { EventAddProcessor() }
        private val unsetEventProcessor by lazy { EventUnsetProcessor() }
        private val removeEventProcessor by lazy { EventRemoveProcessor() }
        private val setEventProcessor by lazy { EventSetProcessor() }
        private val amendEventProcessor by lazy { EventAmendProcessor() }
        private val positionEventProcessor by lazy { EventPositionProcessor() }

        private fun subscribe(subscriptions: List<Id>) = channel.subscribe(subscriptions)

        override fun subscribe(searchParams: StoreSearchParams): Flow<List<ObjectWrapper.Basic>> =
            flow {
                with(searchParams) {

                    val initial = repo.searchObjectsWithSubscription(
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
                        noDepSubscription = true
                    ).results.map { SubscriptionObject(it.id, it) }.toMutableList()

                    val objectsFlow =
                        subscribe(
                            listOf(searchParams.subscription)
                        ).scan(initial) { dataItems, payload ->
                            var result = dataItems
                            payload.forEach { event ->
                                when (event) {
                                    is SubscriptionEvent.Add -> {
                                        result = addEventProcessor.process(event, result)
                                    }
                                    is SubscriptionEvent.Amend -> {
                                        result = amendEventProcessor.process(event, result)
                                    }
                                    is SubscriptionEvent.Position -> {
                                        result = positionEventProcessor.process(event, result)
                                    }
                                    is SubscriptionEvent.Remove -> {
                                        result = removeEventProcessor.process(event, result)
                                    }
                                    is SubscriptionEvent.Set -> {
                                        result = setEventProcessor.process(event, result)
                                    }
                                    is SubscriptionEvent.Unset -> {
                                        result = unsetEventProcessor.process(event, result)
                                    }
                                    else -> {
                                        // do nothing
                                    }
                                }
                            }
                            result
                        }.map {
                            it.mapNotNull { item -> item.objectWrapper }
                        }

                    emitAll(objectsFlow)
                }
            }.flowOn(dispatchers.io)
    }

}

data class SubscriptionObject(
    val id: Id,
    val objectWrapper: ObjectWrapper.Basic? = null
)