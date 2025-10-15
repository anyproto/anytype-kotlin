package com.anytypeio.anytype.presentation.notifications

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
} 