package com.anytypeio.anytype.device

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.DecryptedPushContent
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.ui.main.MainActivity

class NotificationBuilder(
    private val context: Context,
    private val notificationManager: NotificationManager
) {

    fun buildAndNotify(message: DecryptedPushContent.Message, spaceId: Id) {

        // 1) Build the intent that’ll open your MainActivity in the right chat
        val pending = createChatPendingIntent(
            context = context,
            chatId = message.chatId,
            spaceId = spaceId
        )

        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_app_notification)
            .setContentTitle(message.spaceName.trim())
            .setSubText(message.senderName.trim())
            .setContentText(message.text.trim())
            .setStyle(NotificationCompat.BigTextStyle().bigText(message.text.trim()))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pending)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setFullScreenIntent(pending, true)
            .setLights(0xFF0000FF.toInt(), 300, 1000)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notif)
    }

    fun createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
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

    /**
     * Creates the tap-action intent and wraps it in a PendingIntent for notifications.
     */
    fun createChatPendingIntent(
        context: Context,
        chatId: String,
        spaceId: Id
    ): PendingIntent {
        // 1) Build the intent that’ll open your MainActivity in the right chat
        val intent = Intent(context, MainActivity::class.java).apply {
            action = AnytypePushService.ACTION_OPEN_CHAT
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(Relations.CHAT_ID, chatId)
            putExtra(Relations.SPACE_ID, spaceId)
        }

        // 2) Wrap it in a one-shot immutable PendingIntent
        return PendingIntent.getActivity(
            context,
            NOTIFICATION_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private const val NOTIFICATION_REQUEST_CODE = 100
        private const val CHANNEL_ID = "messages_channel"
        private const val CHANNEL_NAME = "Chat Messages"
    }
}