package com.anytypeio.anytype.domain.notifications

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.chats.NotificationState
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType

/**
 * Utility object for calculating notification states for spaces and chats
 */
object NotificationStateCalculator {

    /**
     * Calculate muted state for a space based on its notification settings.
     *
     * This function checks the space-level notification mode only, ignoring app-level notification
     * permissions. This ensures the mute state accurately reflects the space's notification configuration
     * regardless of system-level notification settings.
     *
     * A space is considered "muted" when its notification mode is either:
     * - DISABLE (all notifications off)
     * - MENTIONS (only mentions, treating non-mention notifications as muted)
     *
     * @param spaceView The space view to check. Can be null or any space type.
     * @return true if the space is muted (DISABLE or MENTIONS mode), false otherwise (including null)
     */
    fun calculateSpaceNotificationMutedState(
        spaceView: ObjectWrapper.SpaceView?
    ): Boolean {
        // Return false (unmuted) as safe default for null spaces
        if (spaceView == null) {
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

    /**
     * Returns true when a chat object should be hidden from the Unread section
     * and excluded from the app badge count ("Mute and hide" behavior).
     *
     * Scope: chat objects inside regular data Spaces only. DM spaces and
     * Chat Channel spaces always return false here — their notification
     * semantics are handled separately (see DROID-4361).
     *
     * Callers should also apply the @mention exception for the Unread section:
     * if the chat has unread mentions, it still surfaces with a gray `@`.
     * The badge caller should apply no exception — muted chats never contribute.
     */
    fun isMutedAndHidden(
        chatSpace: ObjectWrapper.SpaceView,
        chatId: Id
    ): Boolean {
        if (chatSpace.spaceUxType != SpaceUxType.DATA) return false
        return calculateChatNotificationState(chatSpace, chatId) == NotificationState.DISABLE
    }
}
