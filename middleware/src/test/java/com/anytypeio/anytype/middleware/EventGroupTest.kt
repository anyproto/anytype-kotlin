package com.anytypeio.anytype.middleware

import anytype.Event
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Contract test for the fail-open routing. Every message type a consumer filter accepts must set the
 * expected group bit — a missing arm here is a CI failure, not a silent production drop. Covers the
 * documented traps: objectDetails* fan-out to BOTH groups, subscription-only -> SUBSCRIPTION (no
 * EDITOR), SYNC_P2P's two prefix-disjoint types, and accountShow -> ACCOUNT (login).
 */
class EventGroupTest {

    private fun bitsOf(message: Event.Message) = EventGroup.groupBits(message)
    private fun Int.has(group: EventGroup) = (this and group.bit) != 0

    @Test
    fun `objectDetails routes to BOTH editor and subscription`() {
        val bits = bitsOf(Event.Message(objectDetailsSet = Event.Object.Details.Set()))
        assertTrue(bits.has(EventGroup.EDITOR))
        assertTrue(bits.has(EventGroup.SUBSCRIPTION))
    }

    @Test
    fun `subscription-only message routes to SUBSCRIPTION and NOT editor`() {
        val bits = bitsOf(Event.Message(subscriptionCounters = Event.Object.Subscription.Counters()))
        assertTrue(bits.has(EventGroup.SUBSCRIPTION))
        assertFalse(bits.has(EventGroup.EDITOR))
    }

    @Test
    fun `block message routes to EDITOR and NOT subscription`() {
        val bits = bitsOf(Event.Message(blockSetText = Event.Block.Set.Text()))
        assertTrue(bits.has(EventGroup.EDITOR))
        assertFalse(bits.has(EventGroup.SUBSCRIPTION))
    }

    @Test
    fun `accountShow routes to ACCOUNT (login path)`() {
        assertTrue(bitsOf(Event.Message(accountShow = Event.Account.Show())).has(EventGroup.ACCOUNT))
    }

    @Test
    fun `accountUpdate routes to ACCOUNT`() {
        assertTrue(bitsOf(Event.Message(accountUpdate = Event.Account.Update())).has(EventGroup.ACCOUNT))
    }

    @Test
    fun `both sync-p2p disjoint types route to SYNC_P2P`() {
        assertTrue(bitsOf(Event.Message(p2pStatusUpdate = Event.P2PStatus.Update())).has(EventGroup.SYNC_P2P))
        assertTrue(bitsOf(Event.Message(spaceSyncStatusUpdate = Event.Space.SyncStatus.Update())).has(EventGroup.SYNC_P2P))
    }

    @Test
    fun `process routes to PROCESS`() {
        assertTrue(bitsOf(Event.Message(processNew = Event.Process.New())).has(EventGroup.PROCESS))
    }

    @Test
    fun `chat routes to CHAT`() {
        assertTrue(bitsOf(Event.Message(chatAdd = Event.Chat.Add())).has(EventGroup.CHAT))
    }

    @Test
    fun `file routes to FILE`() {
        assertTrue(bitsOf(Event.Message(fileSpaceUsage = Event.File.SpaceUsage())).has(EventGroup.FILE))
    }

    @Test
    fun `membership routes to MEMBERSHIP`() {
        assertTrue(bitsOf(Event.Message(membershipUpdate = Event.Membership.Update())).has(EventGroup.MEMBERSHIP))
    }

    @Test
    fun `notification routes to NOTIFICATIONS`() {
        assertTrue(bitsOf(Event.Message(notificationSend = Event.Notification.Send())).has(EventGroup.NOTIFICATIONS))
    }

    @Test
    fun `empty message routes nowhere`() {
        assertEquals(0, bitsOf(Event.Message()))
    }

    @Test
    fun `event mask is the OR over its messages`() {
        val event = Event(
            messages = listOf(
                Event.Message(p2pStatusUpdate = Event.P2PStatus.Update()),
                Event.Message(chatAdd = Event.Chat.Add())
            )
        )
        val bits = EventGroup.groupBits(event)
        assertTrue(bits.has(EventGroup.SYNC_P2P))
        assertTrue(bits.has(EventGroup.CHAT))
        assertFalse(bits.has(EventGroup.EDITOR))
    }
}
