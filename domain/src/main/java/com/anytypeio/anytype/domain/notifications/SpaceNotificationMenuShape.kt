package com.anytypeio.anytype.domain.notifications

import com.anytypeio.anytype.core_models.chats.NotificationState
import com.anytypeio.anytype.core_models.multiplayer.SpaceType

/**
 * Per-space-type shape of the notification-setting surface.
 * Tripartite  — three options (Enable / Mentions only / Disabled or Mute and hide).
 * DmToggle    — binary Mute/Unmute; Mute maps to DISABLE.
 * ChannelToggle — binary Mute/Unmute; Mute maps to MENTIONS so @mentions still push.
 */
sealed class SpaceNotificationMenuShape {
    data object Tripartite : SpaceNotificationMenuShape()
    data object DmToggle : SpaceNotificationMenuShape()
    data object ChannelToggle : SpaceNotificationMenuShape()
}

fun SpaceType?.notificationMenuShape(): SpaceNotificationMenuShape = when (this) {
    SpaceType.REGULAR -> SpaceNotificationMenuShape.Tripartite
    SpaceType.ONE_TO_ONE -> SpaceNotificationMenuShape.DmToggle
    SpaceType.CHAT -> SpaceNotificationMenuShape.ChannelToggle
    else -> SpaceNotificationMenuShape.Tripartite
}

/** Backend state to dispatch when the binary toggle moves from Unmute → Mute. */
fun SpaceNotificationMenuShape.muteState(): NotificationState = when (this) {
    SpaceNotificationMenuShape.DmToggle -> NotificationState.DISABLE
    SpaceNotificationMenuShape.ChannelToggle -> NotificationState.MENTIONS
    SpaceNotificationMenuShape.Tripartite -> NotificationState.DISABLE
}
