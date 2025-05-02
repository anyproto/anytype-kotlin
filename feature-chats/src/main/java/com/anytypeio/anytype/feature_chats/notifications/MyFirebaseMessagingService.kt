package com.anytypeio.anytype.feature_chats.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL
import android.util.Base64

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // Получаем ваш сервис расшифровки из контейнера (например, Dagger/Hilt или Service Locator)
    private val decryptionPushContentService: DecryptionPushContentService =
        AppContainer.decryptionPushContentService()

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO: отправить token на ваш сервер
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // 1) Попытка получить зашифрованный Base64-пэйлоад и keyId
        val data = remoteMessage.data
        val base64Payload = data["x-any-payload"]
        val keyId = data["x-any-key-id"]

        if (base64Payload != null && keyId != null) {
            // 2) Расшифровываем в фоне
            GlobalScope.launch(Dispatchers.IO) {
                val decrypted = decryptPayload(base64Payload, keyId)
                withContext(Dispatchers.Main) {
                    if (decrypted != null) {
                        showDecryptedNotification(decrypted)
                    } else {
                        // Если не удалось расшифровать — показать оригинал
                        showFallbackNotification(remoteMessage)
                    }
                }
            }
        } else {
            // Нет зашифрованного блока — показываем как есть
            showFallbackNotification(remoteMessage)
        }
    }

    private fun decryptPayload(base64: String, keyId: String): DecryptedMessage? {
        return try {
            val encryptedData = Base64.decode(base64, Base64.DEFAULT)
            decryptionPushContentService.decrypt(encryptedData, keyId)
        } catch (e: Exception) {
            null
        }
    }

    private fun showDecryptedNotification(decrypted: DecryptedMessage) {
        val title = decrypted.newMessage.spaceName
        val text  = "${decrypted.newMessage.senderName}: ${decrypted.newMessage.text}"

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)

        // Прокидываем данные в Intent (если нужно)
        builder.addExtras(
            Bundle().apply {
                putString("spaceId", decrypted.spaceId)
                putString("chatId", decrypted.newMessage.chatId)
            }
        )

        NotificationManagerCompat.from(this)
            .notify(System.currentTimeMillis().toInt(), builder.build())
    }

    private fun showFallbackNotification(remoteMessage: RemoteMessage) {
        val title = remoteMessage.data["title"] ?: remoteMessage.notification?.title
        val body  = remoteMessage.data["body"]  ?: remoteMessage.notification?.body

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)

        NotificationManagerCompat.from(this)
            .notify(System.currentTimeMillis().toInt(), builder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Основной канал"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = "Канал для основных уведомлений"
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "default"
    }
}