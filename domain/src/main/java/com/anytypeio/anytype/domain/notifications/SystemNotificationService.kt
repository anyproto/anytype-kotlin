package com.anytypeio.anytype.domain.notifications

import com.anytypeio.anytype.core_models.Notification

interface SystemNotificationService {
    fun notify(notification: Notification)
    fun cancel(id: String)
}