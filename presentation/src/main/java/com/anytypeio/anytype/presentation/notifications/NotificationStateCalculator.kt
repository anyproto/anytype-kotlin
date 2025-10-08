package com.anytypeio.anytype.presentation.notifications

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.chats.NotificationState
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType

/**
 * Utility object for calculating muted state for spaces based on both space-level and app-level notification settings
 */
object NotificationStateCalculator {

    /**
     * Calculate muted state for a space based on both space-level and app-level notification settings
     *
     * For spaces with chat functionality (SpaceUxType.CHAT or spaces with chatId), only considers
     * space-level notification settings, ignoring app-level notification permission. This ensures
     * the mute/unmute state is always visible for chat-enabled spaces regardless of app notification settings.
     *
     * For other spaces (without chat), considers both space-level and app-level notification settings.
     */
    fun calculateMutedState(
        spaceView: ObjectWrapper.SpaceView?,
        notificationPermissionManager: NotificationPermissionManager
    ): Boolean {
        if (spaceView == null) return false

        // Check space-level notification settings
        val isSpaceMuted = spaceView.spacePushNotificationMode == NotificationState.DISABLE
                || spaceView.spacePushNotificationMode == NotificationState.MENTIONS

        // Check if this space has chat functionality
        val hasChat = spaceView.spaceUxType == SpaceUxType.CHAT || !spaceView.chatId.isNullOrEmpty()

        // For spaces with chat, only return space-level mute state
        // For other spaces, also consider app-level notification permission
        return if (hasChat) {
            isSpaceMuted
        } else {
            val isAppNotificationsDisabled = !notificationPermissionManager.areNotificationsEnabled()
            isSpaceMuted || isAppNotificationsDisabled
        }
    }
} 