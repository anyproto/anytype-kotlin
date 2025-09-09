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
import com.anytypeio.anytype.core_utils.ext.runSafely
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
    private val groupNotificationIds = mutableMapOf<String, MutableSet<Int>>()

    override fun buildAndNotify(message: DecryptedPushContent.Message, spaceId: Id, groupId: String) {
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

        // Generate unique notification ID based on message ID
        val notificationId = message.msgId.hashCode()

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
            .setGroup(groupId) // Group notifications by groupId
            .build()

        // Track notification ID for this group
        groupNotificationIds.getOrPut(groupId) { mutableSetOf() }.add(notificationId)

        // Show individual notification with groupId as tag
        notificationManager.notify(groupId, notificationId, notif)

        // Create or update summary notification for the group
        updateSummaryNotification(groupId, message.spaceName)
    }

    /**
     * Creates or updates the summary notification for a group of chat notifications.
     */
    private fun updateSummaryNotification(groupId: String, spaceName: String) {
        val groupNotifications = groupNotificationIds[groupId] ?: return
        if (groupNotifications.isEmpty()) return

        val messageCount = groupNotifications.size
        if (messageCount <= 1) return // Don't show summary for single notification

        val summaryNotif = NotificationCompat.Builder(context, CHAT_SUMMARY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_app_notification)
            .setContentTitle(spaceName.trim())
            .setContentText(resourceProvider.getMessagesCountText(messageCount))
            .setGroup(groupId)
            .setGroupSummary(true)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()

        // Use consistent ID for summary notification
        val summaryId = "${groupId}_summary".hashCode()
        notificationManager.notify(SUMMARY_TAG, summaryId, summaryNotif)
    }

    /**
     * Ensures the notification channel (and group) exist before notifying.
     */
    private fun ensureChannelExists(channelId: String, channelName: String) {
        createChannelGroupIfNeeded()
        ensureSummaryChannelExists()
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                group = CHANNEL_GROUP_ID
            }
        }
        runSafely("creating notification channel and adding to created channels") {
            notificationManager.createNotificationChannel(channel)
            val added = createdChannels.add(channelId)
            Timber.d("Notification channel created: $channelId, success: $added")
        }
    }

    /**
     * Ensures the summary notification channel exists.
     */
    private fun ensureSummaryChannelExists() {
        if (createdChannels.contains(CHAT_SUMMARY_CHANNEL_ID)) return
        val summaryChannel = NotificationChannel(
            CHAT_SUMMARY_CHANNEL_ID,
            "Chat Summary",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Summary notifications for chat groups"
            enableLights(true)
            enableVibration(true)
            setShowBadge(true)
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                group = CHANNEL_GROUP_ID
            }
        }
        runSafely("creating summary notification channel") {
            notificationManager.createNotificationChannel(summaryChannel)
            createdChannels.add(CHAT_SUMMARY_CHANNEL_ID)
            Timber.d("Summary notification channel created")
        }
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

    /**
     * Clears all notifications for a specific group ID (for silent push "read" events).
     */
    override fun clearNotificationsByGroupId(groupId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Use active notifications approach for reliable clearing
            notificationManager.activeNotifications
                .filter { sbn ->
                    sbn.tag == groupId || sbn.notification.group == groupId
                }
                .forEach { sbn ->
                    notificationManager.cancel(sbn.tag, sbn.id)
                }
        }
        
        // Also clear using tracked IDs as fallback
        groupNotificationIds[groupId]?.forEach { notificationId ->
            notificationManager.cancel(groupId, notificationId)
        }
        
        // Clear summary notification
        val summaryId = "${groupId}_summary".hashCode()
        notificationManager.cancel(SUMMARY_TAG, summaryId)
        
        // Clean up local tracking
        groupNotificationIds.remove(groupId)
        
        Timber.d("Cleared all notifications for groupId: $groupId")
    }

    private fun sanitizeChannelName(name: String): String {
        return name.trim().replace(Regex("[^a-zA-Z0-9 _-]"), "_")
    }

    companion object {
        private const val CHANNEL_GROUP_ID = "chats_group"
        private const val CHANNEL_GROUP_NAME = "Chats"
        private const val CHAT_SUMMARY_CHANNEL_ID = "chat_summary_channel"
        private const val SUMMARY_TAG = "chat_summary"
    }
}