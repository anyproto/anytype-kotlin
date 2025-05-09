package com.anytypeio.anytype.device

import android.util.Base64
import com.anytypeio.anytype.app.AndroidApplication
import com.anytypeio.anytype.core_models.DecryptedPushContent
import com.anytypeio.anytype.domain.device.DeviceTokenStoringService
import com.anytypeio.anytype.presentation.notifications.DecryptionPushContentService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import javax.inject.Inject
import timber.log.Timber

class AnytypePushService : FirebaseMessagingService() {

    @Inject
    lateinit var deviceTokenSavingService: DeviceTokenStoringService

    @Inject
    lateinit var decryptionService: DecryptionPushContentService

    init {
        Timber.d("AnytypePushService initialized")
    }

    override fun onCreate() {
        (application as AndroidApplication).componentManager.pushContentComponent.get().inject(this)
        super.onCreate()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("New token received: $token")
        deviceTokenSavingService.saveToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Timber.d("Received message: ${message.data}")

        try {
            // Extract encrypted data and keyId from the message
            val encryptedData = message.data["data"]?.let { Base64.decode(it, Base64.DEFAULT) }
            val keyId = message.data["x-any-key-id"]

            if (encryptedData == null || keyId == null) {
                Timber.e("Missing required data in push message: data=${encryptedData != null}, keyId=$keyId")
                return
            }

            // Decrypt the message
            val decryptedContent = decryptionService.decrypt(encryptedData, keyId)
            if (decryptedContent == null) {
                Timber.e("Failed to decrypt push message")
                return
            }

            // Handle the decrypted content
            handleDecryptedContent(decryptedContent)
        } catch (e: Exception) {
            Timber.e(e, "Error processing push message")
        }
    }

    private fun handleDecryptedContent(content: DecryptedPushContent) {
        Timber.d("Decrypted content: $content")
        // TODO: Handle the decrypted content based on its type
        // For example, show a notification for new messages
        when (content.type) {
            1 -> handleNewMessage(content.newMessage)
            else -> Timber.w("Unknown message type: ${content.type}")
        }
    }

    private fun handleNewMessage(message: DecryptedPushContent.Message) {
        // TODO: Implement notification display logic
        Timber.d("New message received: $message")
    }
}