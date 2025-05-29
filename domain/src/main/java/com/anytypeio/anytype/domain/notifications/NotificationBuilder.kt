package com.anytypeio.anytype.domain.notifications

import com.anytypeio.anytype.core_models.DecryptedPushContent
import com.anytypeio.anytype.core_models.Id

interface NotificationBuilder {
    fun buildAndNotify(message: DecryptedPushContent.Message, spaceId: Id)
    fun clearNotificationChannel(spaceId: String, chatId: String)
}