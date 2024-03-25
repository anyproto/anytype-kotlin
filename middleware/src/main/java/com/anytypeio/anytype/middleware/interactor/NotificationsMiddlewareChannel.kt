package com.anytypeio.anytype.middleware.interactor

import com.anytypeio.anytype.core_models.Notification
import com.anytypeio.anytype.data.auth.event.NotificationsRemoteChannel
import com.anytypeio.anytype.middleware.EventProxy
import com.anytypeio.anytype.middleware.mappers.toCoreModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

class NotificationsMiddlewareChannel(
    private val eventsProxy: EventProxy
) : NotificationsRemoteChannel {

    override fun observe(): Flow<List<Notification.Event>> {
        return eventsProxy.flow()
            .mapNotNull { emission ->
                emission.messages.mapNotNull { message ->
                    when {
                        message.notificationUpdate != null -> {
                            val event = message.notificationUpdate
                            checkNotNull(event)
                            Notification.Event.Update(
                                notification = event.notification?.toCoreModel()
                            )
                        }
                        message.notificationSend != null -> {
                            val event = message.notificationSend
                            checkNotNull(event)
                            Notification.Event.Send(
                                notification = event.notification?.toCoreModel()
                            )
                        }
                        else -> null
                    }
                }
            }
    }
}