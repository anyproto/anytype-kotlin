package com.anytypeio.anytype.middleware.interactor

import anytype.Event
import app.cash.turbine.test
import com.anytypeio.anytype.data.auth.status.SyncAndP2PStatusEventsStore
import com.anytypeio.anytype.middleware.EventGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import kotlin.test.assertEquals

/**
 * Pump tests (FIFO order / burst no-loss / decode-failure isolation / poison survival) for
 * EventHandler. Events carry a SYNC_P2P message so they route through the demux to flow(SYNC_P2P);
 * the assertions are about the pump (decode + serial ordering + error isolation), independent of the
 * group. UnconfinedTestDispatcher ensures the flow(SYNC_P2P) collector registers before the pump
 * dispatches.
 */
class EventHandlerTest {

    private fun TestScope.handler(
        channel: EventHandlerChannel,
        logger: MiddlewareProtobufLogger = mock(),
        syncStore: SyncAndP2PStatusEventsStore = mock(),
        registrar: MiddlewareEventRegistrar = MiddlewareEventRegistrar { },
        scope: CoroutineScope = backgroundScope
    ) = EventHandler(
        logger = logger,
        scope = scope,
        channel = channel,
        syncP2PStore = syncStore,
        registrar = registrar
    )

    // Carries a SYNC_P2P message so groupBits routes it to flow(SYNC_P2P).
    private fun event(id: String) = Event(
        contextId = id,
        messages = listOf(Event.Message(p2pStatusUpdate = Event.P2PStatus.Update()))
    )

    private fun bytes(id: String): ByteArray = Event.ADAPTER.encode(event(id))

    @Test
    fun `preserves FIFO order of events`() = runTest(UnconfinedTestDispatcher()) {
        val channel = EventHandlerChannelImpl()
        val eventHandler = handler(channel)

        eventHandler.flow(EventGroup.SYNC_P2P).test {
            eventHandler.onRawEvent(bytes("1"))
            eventHandler.onRawEvent(bytes("2"))
            eventHandler.onRawEvent(bytes("3"))

            assertEquals(event("1"), awaitItem())
            assertEquals(event("2"), awaitItem())
            assertEquals(event("3"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `buffers a burst without dropping events`() = runTest(UnconfinedTestDispatcher()) {
        val channel = EventHandlerChannelImpl()
        val eventHandler = handler(channel)
        val n = 50

        eventHandler.flow(EventGroup.SYNC_P2P).test {
            repeat(n) { eventHandler.onRawEvent(bytes(it.toString())) }
            val received = (0 until n).map { awaitItem().contextId }
            assertEquals((0 until n).map { it.toString() }, received)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `skips an undecodable event and keeps processing`() = runTest(UnconfinedTestDispatcher()) {
        val channel = EventHandlerChannelImpl()
        val eventHandler = handler(channel)

        eventHandler.flow(EventGroup.SYNC_P2P).test {
            eventHandler.onRawEvent(bytes("1"))
            eventHandler.onRawEvent(byteArrayOf(0x08)) // varint tag with no value -> decode throws IOException
            eventHandler.onRawEvent(bytes("2"))

            assertEquals(event("1"), awaitItem())
            assertEquals(event("2"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `survives a non-IOException thrown while handling one event`() = runTest(UnconfinedTestDispatcher()) {
        val channel = EventHandlerChannelImpl()
        val logger = mock<MiddlewareProtobufLogger> {
            on { logEvent(any()) } doAnswer { invocation ->
                val arg = invocation.arguments[0] as Event
                if (arg.contextId == "poison") throw RuntimeException("boom")
            }
        }
        val eventHandler = handler(channel, logger = logger)

        eventHandler.flow(EventGroup.SYNC_P2P).test {
            eventHandler.onRawEvent(bytes("1"))
            eventHandler.onRawEvent(bytes("poison"))
            eventHandler.onRawEvent(bytes("2"))

            assertEquals(event("1"), awaitItem())
            assertEquals(event("2"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onRawEvent never throws (null, and after scope cancellation)`() = runTest(UnconfinedTestDispatcher()) {
        val channel = EventHandlerChannelImpl()
        val ownScope = CoroutineScope(coroutineContext + Job())
        val eventHandler = handler(channel, scope = ownScope)

        eventHandler.onRawEvent(null) // ignored, no throw

        ownScope.cancel()
        // A late JNI callback after the consumer is gone must not throw across the JNI boundary.
        eventHandler.onRawEvent(bytes("late"))
    }
}
