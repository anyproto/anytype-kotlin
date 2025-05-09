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
            val encryptedData = message.data["x-any-payload"]?.let { Base64.decode(it, Base64.DEFAULT) }
            val keyId = message.data["x-any-key-id"]

            if (encryptedData == null || keyId == null) {
                Timber.e("Missing required data in push message: encryptedData is null =${encryptedData == null}, keyId=$keyId")
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
        when (content.type) {
            1 -> handleNewMessage(content.newMessage)
            else -> Timber.w("Unknown message type: ${content.type}")
        }
    }

    private fun handleNewMessage(message: DecryptedPushContent.Message) {
        Timber.d("New message received: $message")
        
        // Create an intent to open the app when notification is tapped
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("chatId", message.chatId)
            putExtra("spaceId", message.spaceName)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_app_notification)
            .setContentTitle(message.senderName)
            .setContentText(message.text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message.text))
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
                "Chat Messages",
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
    }
}