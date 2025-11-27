package com.anytypeio.anytype.domain.notifications

import com.anytypeio.anytype.core_models.DecryptedPushContent
import com.anytypeio.anytype.core_models.Id

interface NotificationBuilder {
    suspend fun buildAndNotify(message: DecryptedPushContent.Message, spaceId: Id, groupId: String)
    fun clearNotificationChannel(spaceId: String, chatId: String)
    fun clearNotificationsByGroupId(groupId: String)
}