package com.anytypeio.anytype.device

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import androidx.core.app.NotificationCompat
import coil3.imageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.toBitmap
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.DecryptedPushContent
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SystemColor
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.ui.SpaceIconView
import com.anytypeio.anytype.core_models.ui.spaceIcon
import com.anytypeio.anytype.core_ui.extensions.resInt
import com.anytypeio.anytype.core_utils.ext.runSafely
import com.anytypeio.anytype.domain.chats.ChatsDetailsSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.notifications.NotificationBuilder
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.ui.main.MainActivity
import kotlin.math.absoluteValue
import timber.log.Timber

class NotificationBuilderImpl(
    private val context: Context,
    private val notificationManager: NotificationManager,
    private val resourceProvider: StringResourceProvider,
    private val urlBuilder: UrlBuilder,
    private val spaceViewSubscriptionContainer: SpaceViewSubscriptionContainer,
    private val chatsDetailsSubscriptionContainer: ChatsDetailsSubscriptionContainer
) : NotificationBuilder {

    private val attachmentText get() = resourceProvider.getAttachmentText()
    private val createdChannels = mutableSetOf<String>()

    /** Notification large icon size in pixels (48dp converted to px for current density) */
    private val iconSizePx: Int
        get() = (ICON_SIZE_DP * context.resources.displayMetrics.density).toInt()
    private val groupNotificationIds = mutableMapOf<String, MutableSet<Int>>()

    /**
     * In-memory cache for notification icon bitmaps (space icons only) to avoid redundant generation
     * when multiple notifications arrive for the same space in quick succession.
     * Cache is cleared when notifications are dismissed to allow fresh icons if changed.
     */
    private val spaceIconCache = mutableMapOf<Id, Bitmap>()

    override suspend fun buildAndNotify(message: DecryptedPushContent.Message, spaceId: Id, groupId: String) {
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

        // Format the notification body text (message preview)
        val bodyText = message.formatNotificationBody(attachmentText)

        // Generate unique notification ID based on message ID
        val notificationId = message.msgId.hashCode()

        // Load space icon for all notifications
        val largeIcon = loadSpaceIconBitmap(spaceId)

        if (largeIcon == null) {
            Timber.w("Failed to load space icon for notification. spaceId=$spaceId")
        }

        // Determine Line 2 content:
        // - For Data spaces: chat name (fallback to space name if unavailable)
        // - For Chat spaces: space name (since space = chat in 1-to-1)
        val spaceView = spaceViewSubscriptionContainer.get(SpaceId(spaceId))
        val secondLine = if (spaceView?.spaceUxType == SpaceUxType.DATA) {
            chatsDetailsSubscriptionContainer.get(message.chatId)?.name
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: message.spaceName.trim()  // Fallback to space name
        } else {
            message.spaceName.trim()  // Chat spaces use space name
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_app_notification)
            .setContentTitle(message.senderName.trim())  // Title: Author name
            .setContentText("$secondLine: $bodyText")     // Collapsed view
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$secondLine\n$bodyText")    // Expanded: Chat/Space name + Message
            )
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

        // Set large icon if available
        largeIcon?.let { builder.setLargeIcon(it) }

        val notif = builder.build()

        // Track notification ID for this group
        groupNotificationIds.getOrPut(groupId) { mutableSetOf() }.add(notificationId)

        // Show individual notification with groupId as tag
        notificationManager.notify(groupId, notificationId, notif)

        // Create or update summary notification for the group
        updateSummaryNotification(
            groupId = groupId,
            chatOrSpaceName = secondLine,
            chatPendingIntent = pending,
            largeIcon = largeIcon
        )
    }

    /**
     * Attempts to load an image from Coil3's cache (memory or disk).
     * Returns null if the image is not cached (no network request is made).
     */
    private suspend fun loadImageFromCoilCache(url: String): Bitmap? {
        return runCatching {
            val imageLoader = context.imageLoader
            val request = ImageRequest.Builder(context)
                .data(url)
                .memoryCachePolicy(CachePolicy.READ_ONLY)  // Only read from memory cache
                .diskCachePolicy(CachePolicy.READ_ONLY)     // Only read from disk cache
                .build()

            when (val result = imageLoader.execute(request)) {
                is SuccessResult -> result.image.toBitmap()
                else -> null
            }
        }.onFailure { error ->
            Timber.e(error, "Failed to load image from Coil cache: $url")
        }.getOrNull()
    }

    /**
     * Loads a space icon as a Bitmap for use in notifications.
     * Returns null if the space is not found or bitmap generation fails.
     *
     * Bitmaps are cached to avoid redundant generation when multiple notifications
     * arrive for the same space. Note: Android's notification system does not always create
     * a deep copy of the bitmap when setLargeIcon() is called. If the cached bitmap is modified
     * or recycled after being set, it could affect notifications that are already displayed.
     * The current implementation is safe because bitmaps are never modified after creation,
     * but care should be taken if this changes in the future.
     */
    private suspend fun loadSpaceIconBitmap(spaceId: Id): Bitmap? {
        // Return cached bitmap if available
        spaceIconCache[spaceId]?.let { cached ->
            Timber.d("Using cached space icon for space: $spaceId")
            return cached
        }

        Timber.d("Loading space icon for space: $spaceId")
        return runCatching {
            // Get space view from subscription container
            val spaceView = spaceViewSubscriptionContainer.get(SpaceId(spaceId)) ?: return null

            // Generate bitmap based on icon type
            when (val iconView = spaceView.spaceIcon(urlBuilder)) {
                is SpaceIconView.ChatSpace.Placeholder -> generatePlaceholderBitmap(iconView.color, iconView.name)
                is SpaceIconView.DataSpace.Placeholder -> generatePlaceholderBitmap(iconView.color, iconView.name)
                is SpaceIconView.ChatSpace.Image -> loadImageOrPlaceholder(url = iconView.url, color = iconView.color, name = spaceView.name.orEmpty())
                is SpaceIconView.DataSpace.Image -> loadImageOrPlaceholder(url = iconView.url, color = iconView.color, name = spaceView.name.orEmpty())
                SpaceIconView.Loading -> null
            }
        }.onSuccess { bitmap ->
            // Cache the generated bitmap for future use
            bitmap?.let { spaceIconCache[spaceId] = it }
        }.onFailure { error ->
            Timber.w(error, "Failed to load space icon bitmap for space: $spaceId")
        }.getOrNull()
    }


    /**
     * Attempts to load an image from cache, falling back to a placeholder if not available.
     */
    private suspend fun loadImageOrPlaceholder(
        url: String,
        color: SystemColor,
        name: String
    ): Bitmap {
        return loadImageFromCoilCache(url) ?: run {
            Timber.d("Image not in cache, generating placeholder for: $url")
            generatePlaceholderBitmap(color, name)
        }
    }

    /**
     * Generates a circular placeholder bitmap with the first letter of the name.
     */
    private fun generatePlaceholderBitmap(
        color: SystemColor,
        name: String
    ): Bitmap {
        val size = iconSizePx
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw circular background
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = context.getColor(color.resInt())
            style = Paint.Style.FILL
        }
        val radius = size / 2f
        canvas.drawCircle(radius, radius, radius, paint)

        // Draw first letter
        val initial = name.firstOrNull()?.uppercase() ?: ""
        if (initial.isNotEmpty()) {
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = android.graphics.Color.WHITE
                textSize = size / 2f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }

            // Calculate text position (centered)
            val textY = (size / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)
            canvas.drawText(initial, radius, textY, textPaint)
        }

        return bitmap
    }


    /**
     * Creates or updates the summary notification for a group of chat notifications.
     * @param chatOrSpaceName The chat name (for Data spaces) or space name (for Chat spaces)
     */
    private fun updateSummaryNotification(
        groupId: String,
        chatOrSpaceName: String,
        chatPendingIntent: PendingIntent,
        largeIcon: Bitmap?
    ) {
        val groupNotifications = groupNotificationIds[groupId] ?: return
        if (groupNotifications.isEmpty()) return

        val messageCount = groupNotifications.size
        if (messageCount <= 1) return // Don't show summary for single notification

        val builder = NotificationCompat.Builder(context, CHAT_SUMMARY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_app_notification)
            .setContentTitle(chatOrSpaceName.trim())
            .setContentText(resourceProvider.getMessagesCountText(messageCount))
            .setGroup(groupId)
            .setGroupSummary(true)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(chatPendingIntent)

        // Set large icon if available
        largeIcon?.let { builder.setLargeIcon(it) }

        val summaryNotif = builder.build()

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

        // Clear cached bitmap for this space to allow fresh icon on next notification
        spaceIconCache.remove(spaceId)
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
        private const val ICON_SIZE_DP = 48  // Size in dp for notification large icon
    }
}