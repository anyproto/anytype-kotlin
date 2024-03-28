package com.anytypeio.anytype.presentation.notifications

import com.anytypeio.anytype.core_models.Notification
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.workspace.NotificationsChannel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

interface NotificationsProvider {
    fun observe(): Flow<List<Notification.Event>>

    class Default @Inject constructor(
        private val dispatchers: AppCoroutineDispatchers,
        private val scope: CoroutineScope,
        private val notificationsChannel: NotificationsChannel,
        private val awaitAccountStartManager: AwaitAccountStartManager
    ) : NotificationsProvider {

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun observe(): Flow<List<Notification.Event>> {
            return combine(
                awaitAccountStartManager.isStarted(),
                notificationsChannel.observe()
            ) { isStarted, events ->
                isStarted to events
            }.flatMapLatest { (isStarted, events) ->
                if (isStarted) {
                    flowOf(events)
                } else {
                    emptyFlow()
                }
            }
        }
    }
}
