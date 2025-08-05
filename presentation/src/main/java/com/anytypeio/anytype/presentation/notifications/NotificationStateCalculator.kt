package com.anytypeio.anytype.presentation.notifications

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.chats.NotificationState

/**
 * Utility object for calculating muted state for spaces based on both space-level and app-level notification settings
 */
object NotificationStateCalculator {
    
    /**
     * Calculate muted state for a space based on both space-level and app-level notification settings
     */
    fun calculateMutedState(
        spaceView: ObjectWrapper.SpaceView?,
        notificationPermissionManager: NotificationPermissionManager
    ): Boolean {
        if (spaceView == null) return false
        
        // Check both space-level notification settings AND app-level notification permission
        val isSpaceMuted = spaceView.spacePushNotificationMode == NotificationState.DISABLE
                || spaceView.spacePushNotificationMode == NotificationState.MENTIONS
        val isAppNotificationsDisabled = !notificationPermissionManager.areNotificationsEnabled()
        
        return isSpaceMuted || isAppNotificationsDisabled
    }
} 