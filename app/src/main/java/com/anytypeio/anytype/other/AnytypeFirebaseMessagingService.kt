package com.anytypeio.anytype.other

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.anytypeio.anytype.R
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import service.Service
import timber.log.Timber

class AnytypeFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // This callback is fired whenever a new token is generated for the device.
        // Send this token to your app server for registration.
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Timber.d("onMessageReceived")

        // Check if message contains a data payload.
        remoteMessage.data.let { data ->
            Timber.d("onMessageReceivedData: $data")

            
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let { notification ->
            val title = notification.title
            val body = notification.body
            // Display the notification
            sendNotification(title, body)
        }
    }

    private fun sendNotification(title: String?, body: String?) {
        // Build and show a notification via NotificationManager
        val channelId = "fcm_default_channel"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_anytype_qr_code_logo)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Create channel for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, "FCM Channel", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}