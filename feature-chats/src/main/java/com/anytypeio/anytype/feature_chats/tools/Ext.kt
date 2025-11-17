package com.anytypeio.anytype.feature_chats.tools

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SyncStatus
import com.anytypeio.anytype.core_models.chats.NotificationState
import com.anytypeio.anytype.feature_chats.presentation.ChatView
import com.anytypeio.anytype.feature_chats.ui.NotificationSetting

fun ObjectWrapper.Basic.syncStatus(): ChatView.Message.Attachment.SyncStatus {
    val code = getSingleValue<Double>(Relations.SYNC_STATUS)
    return if (code != null) {
        val status = SyncStatus.fromCode(code.toInt())
        when(status) {
            SyncStatus.Syncing -> ChatView.Message.Attachment.SyncStatus.Syncing
            SyncStatus.Synced -> ChatView.Message.Attachment.SyncStatus.Synced
            SyncStatus.Error -> ChatView.Message.Attachment.SyncStatus.Failed
            SyncStatus.Queued -> ChatView.Message.Attachment.SyncStatus.Syncing
            else -> ChatView.Message.Attachment.SyncStatus.Unknown
        }
    } else {
        ChatView.Message.Attachment.SyncStatus.Unknown
    }
}


/**
 * Maps UI notification setting to domain notification state
 */
fun NotificationSetting.toNotificationState(): NotificationState {
    return when (this) {
        NotificationSetting.ALL -> NotificationState.ALL
        NotificationSetting.MENTIONS -> NotificationState.MENTIONS
        NotificationSetting.MUTE -> NotificationState.DISABLE
    }
}

/**
 * Maps domain notification state to UI notification setting
 */
fun NotificationState.toNotificationSetting(): NotificationSetting {
    return when (this) {
        NotificationState.ALL -> NotificationSetting.ALL
        NotificationState.MENTIONS -> NotificationSetting.MENTIONS
        NotificationState.DISABLE -> NotificationSetting.MUTE
    }
}