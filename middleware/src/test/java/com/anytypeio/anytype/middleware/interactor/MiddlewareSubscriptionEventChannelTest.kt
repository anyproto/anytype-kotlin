package com.anytypeio.anytype.middleware.interactor

import anytype.Event
import app.cash.turbine.test
import app.cash.turbine.turbineScope
import com.anytypeio.anytype.core_models.SearchResult
import com.anytypeio.anytype.core_models.SubscriptionEvent
import com.anytypeio.anytype.middleware.EventProxy
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import kotlin.test.assertEquals

class MiddlewareSubscriptionEventChannelTest {

    @Mock
    lateinit var proxy: EventProxy

    private lateinit var upstream: MutableSharedFlow<Event>

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        upstream = MutableSharedFlow(extraBufferCapacity = 16)
        proxy.stub { on { flow() } doReturn upstream }
    }

    private fun event(vararg messages: Event.Message) = Event(messages = messages.toList())

    @Test
    fun `Set matches by subId and maps to SubscriptionEvent_Set`() = runTest(UnconfinedTestDispatcher()) {
        val channel = MiddlewareSubscriptionEventChannel(events = proxy, scope = backgroundScope)
        val msg = Event.Message(
            objectDetailsSet = Event.Object.Details.Set(
                id = "obj-1",
                details = mapOf("name" to "Doc"),
                subIds = listOf("sub-1")
            )
        )
        channel.subscribe(listOf("sub-1")).test {
            upstream.emit(event(msg))
            assertEquals(
                listOf(
                    SubscriptionEvent.Set(
                        target = "obj-1",
                        data = mapOf("name" to "Doc"),
                        subscriptions = listOf("sub-1")
                    )
                ),
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Set matches dependent subscription via dep suffix`() = runTest(UnconfinedTestDispatcher()) {
        val channel = MiddlewareSubscriptionEventChannel(events = proxy, scope = backgroundScope)
        val msg = Event.Message(
            objectDetailsSet = Event.Object.Details.Set(
                id = "obj-1",
                details = mapOf("k" to "v"),
                subIds = listOf("sub-1/dep")
            )
        )
        channel.subscribe(listOf("sub-1")).test {
            upstream.emit(event(msg))
            assertEquals(
                listOf(
                    SubscriptionEvent.Set(
                        target = "obj-1",
                        data = mapOf("k" to "v"),
                        subscriptions = listOf("sub-1/dep")
                    )
                ),
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Position matches exact subId`() = runTest(UnconfinedTestDispatcher()) {
        val channel = MiddlewareSubscriptionEventChannel(events = proxy, scope = backgroundScope)
        val msg = Event.Message(
            subscriptionPosition = Event.Object.Subscription.Position(
                id = "obj-1",
                afterId = "obj-0",
                subId = "sub-1"
            )
        )
        channel.subscribe(listOf("sub-1")).test {
            upstream.emit(event(msg))
            assertEquals(
                listOf(SubscriptionEvent.Position(target = "obj-1", afterId = "obj-0")),
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Position does NOT honor dep suffix`() = runTest(UnconfinedTestDispatcher()) {
        val channel = MiddlewareSubscriptionEventChannel(events = proxy, scope = backgroundScope)
        val depOnly = Event.Message(
            subscriptionPosition = Event.Object.Subscription.Position(
                id = "obj-dep", afterId = "", subId = "sub-1/dep"
            )
        )
        val exact = Event.Message(
            subscriptionPosition = Event.Object.Subscription.Position(
                id = "obj-exact", afterId = "", subId = "sub-1"
            )
        )
        channel.subscribe(listOf("sub-1")).test {
            upstream.emit(event(depOnly, exact))
            assertEquals(
                listOf(SubscriptionEvent.Position(target = "obj-exact", afterId = "")),
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Counter is parsed and matched by exact subId`() = runTest(UnconfinedTestDispatcher()) {
        val channel = MiddlewareSubscriptionEventChannel(events = proxy, scope = backgroundScope)
        val msg = Event.Message(
            subscriptionCounters = Event.Object.Subscription.Counters(
                total = 10, nextCount = 3, prevCount = 2, subId = "sub-1"
            )
        )
        channel.subscribe(listOf("sub-1")).test {
            upstream.emit(event(msg))
            assertEquals(
                listOf(SubscriptionEvent.Counter(SearchResult.Counter(total = 10, prev = 2, next = 3))),
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Remove matches dependent subscription via dep suffix`() = runTest(UnconfinedTestDispatcher()) {
        val channel = MiddlewareSubscriptionEventChannel(events = proxy, scope = backgroundScope)
        val msg = Event.Message(
            subscriptionRemove = Event.Object.Subscription.Remove(
                id = "obj-1",
                subId = "sub-1/dep"
            )
        )
        channel.subscribe(listOf("sub-1")).test {
            upstream.emit(event(msg))
            assertEquals(
                listOf(SubscriptionEvent.Remove(target = "obj-1", subscription = "sub-1/dep")),
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `payload with no matching subscription does not emit`() = runTest(UnconfinedTestDispatcher()) {
        val channel = MiddlewareSubscriptionEventChannel(events = proxy, scope = backgroundScope)
        val other = Event.Message(
            subscriptionPosition = Event.Object.Subscription.Position(id = "x", afterId = "", subId = "sub-OTHER")
        )
        val mine = Event.Message(
            subscriptionPosition = Event.Object.Subscription.Position(id = "y", afterId = "", subId = "sub-1")
        )
        channel.subscribe(listOf("sub-1")).test {
            upstream.emit(event(other)) // no match -> suppressed
            upstream.emit(event(mine))  // match -> emitted
            assertEquals(
                listOf(SubscriptionEvent.Position(target = "y", afterId = "")),
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `two concurrent subscribers each receive only their own events`() = runTest(UnconfinedTestDispatcher()) {
        val channel = MiddlewareSubscriptionEventChannel(events = proxy, scope = backgroundScope)
        val payload = event(
            Event.Message(
                subscriptionPosition = Event.Object.Subscription.Position(id = "a", afterId = "", subId = "sub-A")
            ),
            Event.Message(
                subscriptionPosition = Event.Object.Subscription.Position(id = "b", afterId = "", subId = "sub-B")
            )
        )
        turbineScope {
            val a = channel.subscribe(listOf("sub-A")).testIn(backgroundScope)
            val b = channel.subscribe(listOf("sub-B")).testIn(backgroundScope)
            upstream.emit(payload)
            assertEquals(listOf(SubscriptionEvent.Position(target = "a", afterId = "")), a.awaitItem())
            assertEquals(listOf(SubscriptionEvent.Position(target = "b", afterId = "")), b.awaitItem())
            a.cancel()
            b.cancel()
        }
    }
}
