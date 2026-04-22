package com.anytypeio.anytype.domain.notifications

import com.anytypeio.anytype.core_models.chats.NotificationState
import com.anytypeio.anytype.core_models.multiplayer.SpaceType
import org.junit.Assert.assertEquals
import org.junit.Test

class SpaceNotificationMenuShapeTest {

    @Test
    fun `REGULAR maps to Tripartite`() {
        assertEquals(SpaceNotificationMenuShape.Tripartite, SpaceType.REGULAR.notificationMenuShape())
    }

    @Test
    fun `ONE_TO_ONE maps to DmToggle`() {
        assertEquals(SpaceNotificationMenuShape.DmToggle, SpaceType.ONE_TO_ONE.notificationMenuShape())
    }

    @Test
    fun `CHAT maps to ChannelToggle`() {
        assertEquals(SpaceNotificationMenuShape.ChannelToggle, SpaceType.CHAT.notificationMenuShape())
    }

    @Test
    fun `TECH falls back to Tripartite`() {
        assertEquals(SpaceNotificationMenuShape.Tripartite, SpaceType.TECH.notificationMenuShape())
    }

    @Test
    fun `UNKNOWN falls back to Tripartite`() {
        assertEquals(SpaceNotificationMenuShape.Tripartite, SpaceType.UNKNOWN.notificationMenuShape())
    }

    @Test
    fun `null falls back to Tripartite`() {
        val type: SpaceType? = null
        assertEquals(SpaceNotificationMenuShape.Tripartite, type.notificationMenuShape())
    }

    @Test
    fun `DmToggle muteState is DISABLE`() {
        assertEquals(NotificationState.DISABLE, SpaceNotificationMenuShape.DmToggle.muteState())
    }

    @Test
    fun `ChannelToggle muteState is MENTIONS`() {
        assertEquals(NotificationState.MENTIONS, SpaceNotificationMenuShape.ChannelToggle.muteState())
    }

    @Test
    fun `Tripartite muteState is DISABLE for safety`() {
        assertEquals(NotificationState.DISABLE, SpaceNotificationMenuShape.Tripartite.muteState())
    }
}
