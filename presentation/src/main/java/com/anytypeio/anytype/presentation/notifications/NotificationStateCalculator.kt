package com.anytypeio.anytype.presentation.notifications

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.chats.NotificationState
import com.anytypeio.anytype.core_models.ext.hasChatFunctionality

/**
 * Utility object for calculating muted state for spaces with chat functionality
 */
object NotificationStateCalculator {

    /**
     * Calculate muted state for a space with chat functionality.
     *
     * This should only be called for spaces that have chat functionality (i.e., hasChatFunctionality() returns true).
     * For spaces with chat, only considers space-level notification settings, ignoring app-level notification
     * permission. This ensures the mute/unmute state is always visible for chat-enabled spaces regardless of
     * app notification settings.
     *
     * If called for a space without chat functionality, returns false (unmuted) as a safe default.
     *
     * @param spaceView The space view to check. Should have chat functionality.
     * @return true if the space is muted (notifications disabled or mentions-only), false otherwise
     */
    fun calculateMutedState(
        spaceView: ObjectWrapper.SpaceView?
    ): Boolean {
        // Return false (unmuted) as safe default for null or non-chat spaces
        if (spaceView == null || !spaceView.hasChatFunctionality()) {
            return false
        }

        // For spaces with chat, only return space-level mute state
        return spaceView.spacePushNotificationMode == NotificationState.DISABLE
                || spaceView.spacePushNotificationMode == NotificationState.MENTIONS
    }

    /**
     * Calculates the effective notification state for a specific chat.
     *
     * The priority order is:
     * 1. Per-chat force-all list (highest priority)
     * 2. Per-chat force-mute
     * 3. Per-chat force-mentions list
     * 4. Space default notification mode (fallback)
     *
     * @param chatSpace The space containing the chat and notification settings
     * @param chatId The ID of the specific chat object
     * @return The effective notification state for the chat
     */
    fun calculateChatNotificationState(
        chatSpace: ObjectWrapper.SpaceView,
        chatId: Id
    ): NotificationState {
        return when (chatId) {
            in chatSpace.spacePushNotificationForceAllIds -> NotificationState.ALL
            in chatSpace.spacePushNotificationForceMuteIds -> NotificationState.DISABLE
            in chatSpace.spacePushNotificationForceMentionIds -> NotificationState.MENTIONS
            else -> chatSpace.spacePushNotificationMode
        }
    }
} 