package com.anytypeio.anytype.presentation.notifications

import com.anytypeio.anytype.core_models.Notification
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.workspace.NotificationsChannel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber

interface NotificationsProvider {
    fun start()
    fun stop()
    fun get(): Notification?
    fun observe(): Flow<Notification?>

    class Default @Inject constructor(
        private val dispatchers: AppCoroutineDispatchers,
        private val scope: CoroutineScope,
        private val notificationsChannel: NotificationsChannel,
        private val awaitAccountStartManager: AwaitAccountStartManager
    ) : NotificationsProvider {

        private val jobs = mutableListOf<Job>()
        private val notifications = MutableStateFlow<Notification?>(null)

        override fun start() {
            clear()
            jobs += scope.launch(dispatchers.io) {
                combine(
                    awaitAccountStartManager.isStarted(),
                    notificationsChannel.observe()
                ) { isStarted, events ->
                    isStarted to events
                }.collect { (isStarted, events) ->
                    if (isStarted) {
                        events.forEach { event ->
                            when (event) {
                                is Notification.Event.Send -> {
                                    Timber.d("Notification sent: ${event.notification}")
                                    notifications.value = event.notification
                                }
                                is Notification.Event.Update -> {
                                    Timber.d("Notification updated: ${event.notification}")
                                    notifications.value = event.notification
                                }
                            }
                        }
                    } else {
                        notifications.value = null
                    }
                }
            }
        }

        override fun stop() {
            clear()
        }

        override fun get(): Notification? {
            return notifications.value
        }

        override fun observe(): Flow<Notification?> {
            return notifications
        }

        private fun clear() {
            jobs.forEach { it.cancel() }
        }
    }
}