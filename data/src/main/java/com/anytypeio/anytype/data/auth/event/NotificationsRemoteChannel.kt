package com.anytypeio.anytype.data.auth.event

import com.anytypeio.anytype.core_models.Notification
import com.anytypeio.anytype.domain.workspace.NotificationsChannel
import kotlinx.coroutines.flow.Flow

interface NotificationsRemoteChannel {
    fun observe(): Flow<List<Notification.Event>>
}

class NotificationsDateChannel(
    private val channel: NotificationsRemoteChannel
) : NotificationsChannel {

    override fun observe(): Flow<List<Notification.Event>> {
        return channel.observe()
    }
}