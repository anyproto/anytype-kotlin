package com.anytypeio.anytype.device

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
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
import com.anytypeio.anytype.domain.notifications.NotificationBuilder
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.ui.main.MainActivity
import kotlin.math.absoluteValue
import timber.log.Timber

class NotificationBuilderImpl(
    private val context: Context,
    private val notificationManager: NotificationManager,
    private val resourceProvider: StringResourceProvider
) : NotificationBuilder {

    private val attachmentText get() = resourceProvider.getAttachmentText()
    private val createdChannels = mutableSetOf<String>()

    override fun buildAndNotify(message: DecryptedPushContent.Message, spaceId: Id) {
        val channelId = "${spaceId}_${message.chatId}"

        ensureChannelExists(
            channelId = channelId,
            channelName = sanitizeChannelName(message.spaceName)
        )

        // Create pending intent to open chat
        val pending = createChatPendingIntent(
            context = context,
            chatId = message.chatId,
            spaceId = spaceId
        )

        // Format the notification body text
        val bodyText = message.formatNotificationBody(attachmentText)
        val singleLine = "${message.senderName.trim()}: $bodyText"

        val notif = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_app_notification)
            .setContentTitle(message.spaceName.trim())
            .setContentText(singleLine)
            .setStyle(NotificationCompat.BigTextStyle().bigText(singleLine))
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

        // TODO maybe use message ID as notification ID?
        notificationManager.notify(System.currentTimeMillis().toInt(), notif)
    }

    /**
     * Ensures the notification channel (and group) exist before notifying.
     */
    private fun ensureChannelExists(channelId: String, channelName: String) {
        createChannelGroupIfNeeded()
        if (createdChannels.contains(channelId)) return
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "New messages notifications"
            enableLights(true)
            enableVibration(true)
            setShowBadge(true)
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            group = CHANNEL_GROUP_ID
        }
        notificationManager.createNotificationChannel(channel)
        createdChannels.add(channelId)
    }

    /**
     * Creates the tap-action intent and wraps it in a PendingIntent for notifications.
     */
    private fun createChatPendingIntent(
        context: Context,
        chatId: String,
        spaceId: Id
    ): PendingIntent {
        // 1) Build the intent that'll open your MainActivity in the right chat
        val intent = Intent(context, MainActivity::class.java).apply {
            action = AnytypePushService.ACTION_OPEN_CHAT
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(Relations.CHAT_ID, chatId)
            putExtra(Relations.SPACE_ID, spaceId)
        }

        // A unique PendingIntent per chat target.
        val requestCode = (chatId + spaceId).hashCode().absoluteValue

        // 2) Wrap it in a one-shot immutable PendingIntent
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun createChannelGroupIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val existingGroup =
                    notificationManager.getNotificationChannelGroup(CHANNEL_GROUP_ID)
                if (existingGroup == null) {
                    val group = NotificationChannelGroup(CHANNEL_GROUP_ID, CHANNEL_GROUP_NAME)
                    notificationManager.createNotificationChannelGroup(group)
                }
            } catch (e: NoSuchMethodError) {
                Timber.e(e, "Error while creating or getting notification group")
                // Some devices might not support getNotificationChannelGroup even on Android O
                // Just create the group without checking if it exists
                val group = NotificationChannelGroup(CHANNEL_GROUP_ID, CHANNEL_GROUP_NAME)
                notificationManager.createNotificationChannelGroup(group)
            } catch (e: Exception) {
                Timber.e(e, "Error while creating or getting notification group")
                val group = NotificationChannelGroup(CHANNEL_GROUP_ID, CHANNEL_GROUP_NAME)
                notificationManager.createNotificationChannelGroup(group)
            }
        }
    }

    /**
     * Deletes notifications and the channel for a specific chat in a space, so that
     * when the user opens that chat, old notifications are cleared.
     */
    override fun clearNotificationChannel(spaceId: String, chatId: String) {
        val channelId = "${spaceId}_${chatId}"

        // Remove posted notifications for this specific chat channel
        notificationManager.activeNotifications
            .filter { it.notification.channelId == channelId }
            .forEach { notificationManager.cancel(it.id) }

        // Delete the specific chat channel
        notificationManager.deleteNotificationChannel(channelId)
        createdChannels.remove(channelId)
    }

    private fun sanitizeChannelName(name: String): String {
        return name.trim().replace(Regex("[^a-zA-Z0-9 _-]"), "_")
    }

    companion object {
        private const val CHANNEL_GROUP_ID = "chats_group"
        private const val CHANNEL_GROUP_NAME = "Chats"
    }
}