package com.anytypeio.anytype.device.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.core.app.NotificationCompat
import com.anytypeio.anytype.R
import com.anytypeio.anytype.app.AndroidApplication
import com.anytypeio.anytype.di.main.ConfigModule.DEFAULT_APP_COROUTINE_SCOPE
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.notifications.RegisterDeviceTokenUseCase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AnytypeFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var decryptionPushContentService: DecryptionPushContentServiceProtocol

    @Inject
    lateinit var dispatchers: AppCoroutineDispatchers

    @Inject
    @Named(DEFAULT_APP_COROUTINE_SCOPE)
    lateinit var scope: CoroutineScope

    @Inject
    lateinit var registerDeviceToken: RegisterDeviceTokenUseCase

    @Inject
    lateinit var context: Context

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        scope.launch(dispatchers.io) {
            val params = RegisterDeviceTokenUseCase.Params(
                token = token
            )
            registerDeviceToken.async(params).fold(
                onSuccess = {
                    Log.d("Test1983", "Token registered successfully")
                },
                onFailure = { error ->
                    Log.e("Test1983", "Failed to register token: $error")
                }
            )
        }
    }

    init {
        Log.d("Test1983", "FirebaseMessagingService initialized")
    }

    override fun onCreate() {
        (application as AndroidApplication).componentManager.pushContentComponent.get().inject(this)
        super.onCreate()
        Log.d("Test1983", "FirebaseMessagingService onCreate")
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { t ->
                Log.d("Test1983", "Refreshed token: ${t.result}")
            }
        createNotificationChannel()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // 1) Попытка получить зашифрованный Base64-пэйлоад и keyId
        val data = remoteMessage.data
        val base64Payload = data["x-any-payload"]
        val keyId = data["x-any-key-id"]

        if (base64Payload != null && keyId != null) {
            // 2) Расшифровываем в фоне
            scope.launch(dispatchers.io) {
                val decrypted = decryptPayload(base64Payload, keyId)
                withContext(dispatchers.main) {
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

    private fun decryptPayload(base64: String, keyId: String): DecryptedPushContent? {
        return try {
            val encryptedData = Base64.decode(base64, Base64.DEFAULT)
            decryptionPushContentService.decrypt(encryptedData, keyId)
        } catch (e: Exception) {
            null
        }
    }

    private fun showDecryptedNotification(decrypted: DecryptedPushContent) {
        val title = decrypted.newMessage.spaceName
        val text = "${decrypted.newMessage.senderName}: ${decrypted.newMessage.text}"

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_relation_delete)
            .setAutoCancel(true)

        // Прокидываем данные в Intent (если нужно)
        builder.addExtras(
            Bundle().apply {
                putString("spaceId", decrypted.spaceId)
                putString("chatId", decrypted.newMessage.chatId)
            }
        )

//        NotificationManagerCompat.from(this)
//            .notify(System.currentTimeMillis().toInt(), builder.build())
    }

    private fun showFallbackNotification(remoteMessage: RemoteMessage) {
//        val title = remoteMessage.data["title"] ?: remoteMessage.notification?.title
//        val body  = remoteMessage.data["body"]  ?: remoteMessage.notification?.body
//
//        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
//            .setContentTitle(title)
//            .setContentText(body)
//            .setSmallIcon(R.drawable.ic_notification)
//            .setAutoCancel(true)
//
//        NotificationManagerCompat.from(this)
//            .notify(System.currentTimeMillis().toInt(), builder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Main Anytype Chat Channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = "Messages from Anytype Chat"
            val manager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "default"
    }
}