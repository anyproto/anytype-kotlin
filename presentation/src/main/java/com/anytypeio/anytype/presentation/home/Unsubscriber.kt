package com.anytypeio.anytype.presentation.home

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

interface Unsubscriber {

    suspend fun start()
    suspend fun unsubscribe(subscriptions: List<Id>)

    class Impl @Inject constructor(
        private val manager: StorelessSubscriptionContainer,
        private val dispatchers: AppCoroutineDispatchers
    ) : Unsubscriber {

        private val queue = MutableSharedFlow<List<Id>>()

        override suspend fun start() {
            queue
                .onEach { Timber.d("Unsubscribing: $it") }
                .map { subscriptions -> manager.unsubscribe(subscriptions) }
                .catch { Timber.e(it, "Error on unsubscribing") }
                .flowOn(dispatchers.io)
                .collect()
        }

        override suspend fun unsubscribe(subscriptions: List<Id>) {
            queue.emit(subscriptions)
        }
    }
}