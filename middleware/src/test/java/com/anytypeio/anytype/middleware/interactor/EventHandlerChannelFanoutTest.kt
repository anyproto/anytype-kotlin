package com.anytypeio.anytype.middleware.interactor

import anytype.Event
import app.cash.turbine.testIn
import app.cash.turbine.turbineScope
import com.anytypeio.anytype.middleware.EventGroup
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class EventHandlerChannelFanoutTest {

    private fun editorEvent(id: String) =
        Event(contextId = id, messages = listOf(Event.Message(blockSetText = Event.Block.Set.Text())))

    private fun detailsEvent(id: String) =
        Event(contextId = id, messages = listOf(Event.Message(objectDetailsSet = Event.Object.Details.Set())))

    private fun p2pEvent(id: String) =
        Event(contextId = id, messages = listOf(Event.Message(p2pStatusUpdate = Event.P2PStatus.Update())))

    private fun chatEvent(id: String) =
        Event(contextId = id, messages = listOf(Event.Message(chatAdd = Event.Chat.Add())))

    @Test
    fun `dispatch delivers an event only to its group collector`() = runTest(UnconfinedTestDispatcher()) {
        val channel = EventHandlerChannelImpl()
        turbineScope {
            val editor = channel.flow(EventGroup.EDITOR).testIn(backgroundScope)
            val sync = channel.flow(EventGroup.SYNC_P2P).testIn(backgroundScope)

            channel.dispatch(p2pEvent("p1"))

            assertEquals("p1", sync.awaitItem().contextId)
            editor.expectNoEvents()
            editor.cancel()
            sync.cancel()
        }
    }

    @Test
    fun `objectDetails fans out to BOTH editor and subscription collectors`() = runTest(UnconfinedTestDispatcher()) {
        val channel = EventHandlerChannelImpl()
        turbineScope {
            val editor = channel.flow(EventGroup.EDITOR).testIn(backgroundScope)
            val sub = channel.flow(EventGroup.SUBSCRIPTION).testIn(backgroundScope)

            channel.dispatch(detailsEvent("d1"))

            assertEquals("d1", editor.awaitItem().contextId)
            assertEquals("d1", sub.awaitItem().contextId)
            editor.cancel()
            sub.cancel()
        }
    }

    @Test
    fun `two collectors of the same group both receive every event (no theft)`() = runTest(UnconfinedTestDispatcher()) {
        val channel = EventHandlerChannelImpl()
        turbineScope {
            val a = channel.flow(EventGroup.CHAT).testIn(backgroundScope)
            val b = channel.flow(EventGroup.CHAT).testIn(backgroundScope)

            channel.dispatch(chatEvent("c1"))

            assertEquals("c1", a.awaitItem().contextId)
            assertEquals("c1", b.awaitItem().contextId)
            a.cancel()
            b.cancel()
        }
    }

    @Test
    fun `a stuck collector of one group does not block another group`() = runTest(UnconfinedTestDispatcher()) {
        val channel = EventHandlerChannelImpl()
        turbineScope {
            // EDITOR collector registered but never drained (its inbox just grows).
            val stuckEditor = channel.flow(EventGroup.EDITOR).testIn(backgroundScope)
            val sync = channel.flow(EventGroup.SYNC_P2P).testIn(backgroundScope)

            // Pile up EDITOR events (non-blocking trySend into the unbounded inbox)...
            repeat(100) { channel.dispatch(editorEvent("e$it")) }
            // ...then a SYNC_P2P event must still be delivered, unaffected.
            channel.dispatch(p2pEvent("p1"))

            assertEquals("p1", sync.awaitItem().contextId)
            stuckEditor.cancelAndIgnoreRemainingEvents() // discard the piled-up EDITOR backlog
            sync.cancel()
        }
    }

    @Test
    fun `cancelling one collector does not affect others in the same group`() = runTest(UnconfinedTestDispatcher()) {
        val channel = EventHandlerChannelImpl()
        turbineScope {
            val a = channel.flow(EventGroup.CHAT).testIn(backgroundScope)
            val b = channel.flow(EventGroup.CHAT).testIn(backgroundScope)
            a.cancel() // a leaves

            channel.dispatch(chatEvent("c1"))

            assertEquals("c1", b.awaitItem().contextId)
            b.cancel()
        }
    }
}
