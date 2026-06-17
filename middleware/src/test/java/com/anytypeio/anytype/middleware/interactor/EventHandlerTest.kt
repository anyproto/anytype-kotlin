package com.anytypeio.anytype.middleware.interactor

import anytype.Event
import app.cash.turbine.test
import com.anytypeio.anytype.data.auth.status.SyncAndP2PStatusEventsStore
import java.util.concurrent.CopyOnWriteArrayList
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import kotlin.test.assertEquals

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

    private fun event(id: String) = Event(contextId = id)
    private fun bytes(id: String): ByteArray = Event.ADAPTER.encode(event(id))

    @Test
    fun `preserves FIFO order of events`() = runTest {
        val channel = EventHandlerChannelImpl()
        val eventHandler = handler(channel)

        eventHandler.flow().test {
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
    fun `preserves order under real multi-threaded dispatch`() = runBlocking {
        // The runTest cases above run on a single deterministic test dispatcher, where a reordering
        // seam (e.g. a reintroduced inner launch / withContext in the consumer) cannot manifest — so
        // they verify the structure but not the ordering property. This case drives the consumer on a
        // real multi-threaded dispatcher: a single producer feeds in order (matching the sequential
        // gomobile callback) and the single consumer must emit in that exact order, every repetition.
        // The old per-event `scope.launch { handle() }` on Dispatchers.Default would flake here.
        repeat(STRESS_REPEATS) {
            val scope = CoroutineScope(Dispatchers.Default + Job())
            try {
                val channel = EventHandlerChannelImpl()
                val eventHandler = EventHandler(
                    logger = mock(),
                    scope = scope,
                    channel = channel,
                    syncP2PStore = mock(),
                    registrar = MiddlewareEventRegistrar { }
                )

                val received = CopyOnWriteArrayList<Int>()
                val subscribed = CompletableDeferred<Unit>()
                val done = CompletableDeferred<Unit>()

                scope.launch {
                    // flow() is backed by a MutableSharedFlow; onSubscription lets us feed only
                    // once the collector is registered (replay = 0 would otherwise drop a prefix).
                    (channel.flow() as SharedFlow<Event>)
                        .onSubscription { subscribed.complete(Unit) }
                        .collect { event ->
                            received.add(event.contextId.toInt())
                            if (received.size == STRESS_EVENTS) done.complete(Unit)
                        }
                }

                // onSubscription guarantees the collector is registered before we feed, so with
                // replay = 0 no event is missed — any reordering shows up as an order violation below.
                subscribed.await()
                repeat(STRESS_EVENTS) { eventHandler.onRawEvent(bytes(it.toString())) }

                withTimeout(STRESS_TIMEOUT_MS) { done.await() }
                assertEquals((0 until STRESS_EVENTS).toList(), received.toList())
            } finally {
                scope.cancel()
            }
        }
    }

    @Test
    fun `buffers a burst without dropping events`() = runTest {
        val channel = EventHandlerChannelImpl()
        val eventHandler = handler(channel)
        val n = 50

        eventHandler.flow().test {
            repeat(n) { eventHandler.onRawEvent(bytes(it.toString())) }
            val received = (0 until n).map { awaitItem().contextId }
            assertEquals((0 until n).map { it.toString() }, received)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `skips an undecodable event and keeps processing`() = runTest {
        val channel = EventHandlerChannelImpl()
        val eventHandler = handler(channel)

        eventHandler.flow().test {
            eventHandler.onRawEvent(bytes("1"))
            eventHandler.onRawEvent(byteArrayOf(0x08)) // varint tag with no value -> decode throws IOException
            eventHandler.onRawEvent(bytes("2"))

            assertEquals(event("1"), awaitItem())
            assertEquals(event("2"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `survives a non-IOException thrown while handling one event`() = runTest {
        val channel = EventHandlerChannelImpl()
        val logger = mock<MiddlewareProtobufLogger> {
            on { logEvent(any()) } doAnswer { invocation ->
                val arg = invocation.arguments[0] as Event
                if (arg.contextId == "poison") throw RuntimeException("boom")
            }
        }
        val eventHandler = handler(channel, logger = logger)

        eventHandler.flow().test {
            eventHandler.onRawEvent(bytes("1"))
            eventHandler.onRawEvent(bytes("poison"))
            eventHandler.onRawEvent(bytes("2"))

            assertEquals(event("1"), awaitItem())
            assertEquals(event("2"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onRawEvent never throws (null, and after scope cancellation)`() = runTest {
        val channel = EventHandlerChannelImpl()
        val ownScope = CoroutineScope(coroutineContext + Job())
        val eventHandler = handler(channel, scope = ownScope)

        eventHandler.onRawEvent(null) // ignored, no throw

        ownScope.cancel()
        // A late JNI callback after the consumer is gone must not throw across the JNI boundary.
        eventHandler.onRawEvent(bytes("late"))
    }

    companion object {
        private const val STRESS_REPEATS = 25
        private const val STRESS_EVENTS = 200
        private const val STRESS_TIMEOUT_MS = 10_000L
    }
}
