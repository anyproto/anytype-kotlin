package com.anytypeio.anytype.device

import android.util.Base64
import com.anytypeio.anytype.domain.notifications.NotificationBuilder
import com.anytypeio.anytype.presentation.notifications.DecryptionPushContentService
import timber.log.Timber

interface PushMessageProcessor {
    /**
     * Returns `true` if the message was handled (e.g. decrypted & showed a notification),
     * or `false` if it should be ignored (e.g. missing payload/key or decryption failed).
     */
    fun process(messageData: Map<String, String>, isAppInForeground: Boolean = false): Boolean
}

class DefaultPushMessageProcessor(
    private val decryptionService: DecryptionPushContentService,
    private val notificationBuilder: NotificationBuilder
) : PushMessageProcessor {

    override fun process(messageData: Map<String, String>, isAppInForeground: Boolean): Boolean {

        val type = messageData[PUSH_TYPE_KEY] ?: return false
        val groupId = messageData[GROUP_ID_KEY] ?: return false

        Timber.d("Push message received: type=$type, groupId=$groupId")

        // Handle silent push for clearing notifications (when messages are read on another device)
        if (type == PUSH_TYPE_SILENT) {
            Timber.d("Silent push received, clearing notifications for groupId=$groupId")
            notificationBuilder.clearNotificationsByGroupId(groupId)
            return true
        }

        // Skip showing notification if app is in foreground
        if (isAppInForeground) {
            Timber.d("App is in foreground, skipping notification")
            return true
        }

        // Handle normal push with new message notification
        if (type == PUSH_TYPE_NORMAL) {
            val base64 = messageData[PAYLOAD_KEY] ?: return false
            val keyId = messageData[KEY_ID_KEY] ?: return false

            val encrypted = Base64.decode(base64, Base64.DEFAULT)

            // Use signature verification by default
            val content = decryptionService.decryptAndVerifySignature(
                encryptedData = encrypted,
                keyId = keyId,
                signature = messageData[SIGNATURE]
            ) ?: return false

            notificationBuilder.buildAndNotify(
                message = content.newMessage,
                spaceId = content.spaceId,
                groupId = groupId
            )
            return true
        }

        // Unknown push type
        Timber.w("Unknown push type: $type, ignoring")
        return false
    }

    companion object {
        private const val PAYLOAD_KEY = "x-any-payload"
        private const val KEY_ID_KEY = "x-any-key-id"
        private const val SIGNATURE = "x-any-signature"
        private const val GROUP_ID_KEY = "x-any-group-id"
        private const val PUSH_TYPE_KEY = "x-any-type"
        private const val PUSH_TYPE_NORMAL = "normal"
        private const val PUSH_TYPE_SILENT = "silent"
    }
}