package com.anytypeio.anytype.domain.workspace

import com.anytypeio.anytype.core_models.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationsChannel {
    fun observe(): Flow<List<Notification.Event>>
}