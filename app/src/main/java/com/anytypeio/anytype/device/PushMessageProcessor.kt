package com.anytypeio.anytype.device

import android.util.Base64
import com.anytypeio.anytype.presentation.notifications.DecryptionPushContentService

interface PushMessageProcessor {
    /**
     * Returns `true` if the message was handled (e.g. decrypted & showed a notification),
     * or `false` if it should be ignored (e.g. missing payload/key or decryption failed).
     */
    fun process(messageData: Map<String, String>): Boolean
}

class DefaultPushMessageProcessor(
    private val decryptionService: DecryptionPushContentService,
    private val notificationBuilder: NotificationBuilder
) : PushMessageProcessor {

    override fun process(messageData: Map<String, String>): Boolean {
        val base64 = messageData[PAYLOAD_KEY] ?: return false
        val keyId = messageData[KEY_ID_KEY] ?: return false

        val encrypted = Base64.decode(base64, Base64.DEFAULT)
        val content = decryptionService.decrypt(encrypted, keyId) ?: return false

        notificationBuilder.buildAndNotify(
            message = content.newMessage,
            spaceId = content.spaceId
        )

        return true
    }

    companion object {
        private const val PAYLOAD_KEY = "x-any-payload"
        private const val KEY_ID_KEY = "x-any-key-id"
    }
}