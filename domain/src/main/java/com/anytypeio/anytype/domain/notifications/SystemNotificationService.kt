package com.anytypeio.anytype.domain.notifications

import com.anytypeio.anytype.core_models.Notification

interface SystemNotificationService {
    val areNotificationsEnabled: Boolean
    fun notify(notification: Notification)
    fun cancel(id: String)

    suspend fun setPendingNotification(notification: Notification)
    suspend fun clearPendingNotification()
    suspend fun notifyIfPending()
}