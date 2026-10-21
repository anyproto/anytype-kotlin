package com.anytypeio.anytype.middleware

import anytype.Event
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Per-field routing is covered exhaustively by [EventGroupReflectionTest] (it iterates the generated
 * Event.Message bindings and checks each field against its family prefix, auto-covering new events).
 * This keeps a few explicit, readable assertions for the non-obvious behaviours.
 */
class EventGroupTest {

    private fun routed(message: Event.Message) =
        EventGroup.values().filter { (EventGroup.groupBits(message) and it.bit) != 0 }.toSet()

    @Test
    fun `objectDetails fans out to BOTH editor and subscription`() {
        assertEquals(
            setOf(EventGroup.EDITOR, EventGroup.SUBSCRIPTION),
            routed(Event.Message(objectDetailsSet = Event.Object.Details.Set()))
        )
    }

    @Test
    fun `a message in no routed family routes nowhere`() {
        assertEquals(emptySet(), routed(Event.Message()))
    }

    @Test
    fun `event mask is the OR over its messages`() {
        val event = Event(
            messages = listOf(
                Event.Message(p2pStatusUpdate = Event.P2PStatus.Update()),
                Event.Message(chatAdd = Event.Chat.Add())
            )
        )
        assertEquals(
            setOf(EventGroup.SYNC_P2P, EventGroup.CHAT),
            EventGroup.values().filter { (EventGroup.groupBits(event) and it.bit) != 0 }.toSet()
        )
    }
}
