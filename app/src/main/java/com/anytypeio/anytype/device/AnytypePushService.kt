package com.anytypeio.anytype.device

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Base64
import androidx.core.app.NotificationCompat
import com.anytypeio.anytype.R
import com.anytypeio.anytype.app.AndroidApplication
import com.anytypeio.anytype.core_models.DecryptedPushContent
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_ui.views.Relations1
import com.anytypeio.anytype.domain.device.DeviceTokenStoringService
import com.anytypeio.anytype.presentation.notifications.DecryptionPushContentService
import com.anytypeio.anytype.ui.main.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import javax.inject.Inject
import timber.log.Timber

class AnytypePushService : FirebaseMessagingService() {

    @Inject
    lateinit var deviceTokenSavingService: DeviceTokenStoringService

    @Inject
    lateinit var decryptionService: DecryptionPushContentService

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    init {
        Timber.d("AnytypePushService initialized")
    }

    override fun onCreate() {
        super.onCreate()
        (application as AndroidApplication).componentManager.pushContentComponent.get().inject(this)
        createNotificationChannel()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("New token received: $token")
        deviceTokenSavingService.saveToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Timber.d("Received message: ${message}")

        try {
            // Extract encrypted data and keyId from the message
            val encryptedData = message.data[PAYLOAD_KEY]?.let { Base64.decode(it, Base64.DEFAULT) }
            val keyId = message.data[KEY_ID_KEY]

            if (encryptedData == null || keyId == null) {
                Timber.w("Missing required data in push message: encryptedData is null =${encryptedData == null}, keyId=$keyId")
                return
            }

            // Decrypt the message
            val decryptedContent = decryptionService.decrypt(encryptedData, keyId)
            if (decryptedContent == null) {
                Timber.w("Failed to decrypt push message")
                return
            }

            // Handle the decrypted content
            handleDecryptedContent(decryptedContent)
        } catch (e: Exception) {
            Timber.w(e, "Error processing push message")
        }
    }

    private fun handleDecryptedContent(content: DecryptedPushContent) {
        Timber.d("Decrypted content: $content")
        when (content.type) {
            1 -> handleNewMessage(content.newMessage)
            else -> Timber.w("Unknown message type: ${content.type}")
        }
    }

    private fun handleNewMessage(message: DecryptedPushContent.Message) {
        Timber.d("New message received: $message")
        
        // Create an intent to open the app when notification is tapped
        val intent = Intent(this, MainActivity::class.java).apply {
            action = ACTION_OPEN_CHAT
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(Relations.CHAT_ID, message.chatId)
            putExtra(Relations.SPACE_ID, message.spaceName)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            NOTIFICATION_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            //.setSmallIcon(R.drawable.ic_push_icon)
            .setContentTitle(message.spaceName.trim())
            .setSubText(message.senderName.trim())
            .setContentText(message.text.trim())
            .setStyle(NotificationCompat.BigTextStyle().bigText(message.text.trim()))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setFullScreenIntent(pendingIntent, true)
            .setLights(0xFF0000FF.toInt(), 300, 1000)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        // Show the notification
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "New messages notifications"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "messages_channel"
        private const val PAYLOAD_KEY = "x-any-payload"
        private const val KEY_ID_KEY = "x-any-key-id"
        private const val CHANNEL_NAME = "Chat Messages"
        private const val NOTIFICATION_REQUEST_CODE = 100
        const val ACTION_OPEN_CHAT = "com.anytype.ACTION_OPEN_CHAT"
    }
}