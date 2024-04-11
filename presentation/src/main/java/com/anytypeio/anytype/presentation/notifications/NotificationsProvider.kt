package com.anytypeio.anytype.presentation.notifications

import com.anytypeio.anytype.core_models.Notification
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.workspace.NotificationsChannel
import com.anytypeio.anytype.presentation.BuildConfig
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import timber.log.Timber

interface NotificationsProvider {
    val events: StateFlow<List<Notification.Event>>

    class Default @Inject constructor(
        dispatchers: AppCoroutineDispatchers,
        scope: CoroutineScope,
        private val notificationsChannel: NotificationsChannel,
        private val awaitAccountStartManager: AwaitAccountStartManager
    ) : NotificationsProvider {

        private val _events = MutableStateFlow<List<Notification.Event>>(emptyList())
        override val events: StateFlow<List<Notification.Event>> = _events

        init {
            scope.launch(dispatchers.io) {
                observe().collect { events ->
                    if (BuildConfig.DEBUG) {
                        Timber.d("New notifications: $events")
                    }
                    _events.value = events
                }
            }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun observe(): Flow<List<Notification.Event>> {
            return awaitAccountStartManager.isStarted().flatMapLatest { isStarted ->
                if (isStarted) notificationsChannel.observe() else emptyFlow()
            }
        }
    }
}
