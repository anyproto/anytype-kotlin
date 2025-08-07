package com.anytypeio.anytype.feature_chats.tools

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SyncStatus
import com.anytypeio.anytype.feature_chats.presentation.ChatView

fun ObjectWrapper.Basic.syncStatus(): ChatView.Message.Attachment.SyncStatus {
    val code = getSingleValue<Double>(Relations.SYNC_STATUS)
    if (code != null) {
        val status = SyncStatus.fromCode(code.toInt())
        return when(status) {
            SyncStatus.Syncing -> return ChatView.Message.Attachment.SyncStatus.Syncing
            SyncStatus.Synced -> return ChatView.Message.Attachment.SyncStatus.Synced
            SyncStatus.Error -> return ChatView.Message.Attachment.SyncStatus.Failed
            SyncStatus.Queued -> return ChatView.Message.Attachment.SyncStatus.Syncing
            else -> ChatView.Message.Attachment.SyncStatus.Unknown
        }
    } else {
        return ChatView.Message.Attachment.SyncStatus.Unknown
    }
}