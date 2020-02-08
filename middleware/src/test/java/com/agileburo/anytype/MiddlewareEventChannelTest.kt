package com.agileburo.anytype

import anytype.Events.Event
import anytype.Events.Event.Message
import com.agileburo.anytype.common.MockDataFactory
import com.agileburo.anytype.data.auth.model.EventEntity
import com.agileburo.anytype.middleware.EventProxy
import com.agileburo.anytype.middleware.interactor.MiddlewareEventChannel
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.stub
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals

class MiddlewareEventChannelTest {

    @Mock
    lateinit var proxy: EventProxy

    private lateinit var channel: MiddlewareEventChannel

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        channel = MiddlewareEventChannel(proxy)
    }

    @Test
    fun `should filter event by context and pass it downstream`() {

        val context = MockDataFactory.randomUuid()

        val msg = Event.Block.Show
            .newBuilder()
            .setRootId(context)
            .addAllBlocks(emptyList())
            .build()

        val message = Message
            .newBuilder()
            .setBlockShow(msg)

        val event = Event
            .newBuilder()
            .setContextId(context)
            .addMessages(message)
            .build()

        proxy.stub {
            on { flow() } doReturn flowOf(event)
        }

        val expected = listOf(
            EventEntity.Command.ShowBlock(
                rootId = context,
                blocks = emptyList(),
                context = context
            )
        )

        runBlocking {
            channel.observeEvents(context = context).collect { events ->
                assertEquals(
                    expected = expected,
                    actual = events
                )
            }
        }
    }

    @Test
    fun `should filter event by context and do not pass it downstream`() {

        val context = MockDataFactory.randomUuid()

        val msg = Event.Block.Show
            .newBuilder()
            .setRootId(MockDataFactory.randomString())
            .addAllBlocks(emptyList())
            .build()

        val message = Message
            .newBuilder()
            .setBlockShow(msg)

        val event = Event
            .newBuilder()
            .setContextId(MockDataFactory.randomUuid())
            .addMessages(message)
            .build()

        proxy.stub {
            on { flow() } doReturn flowOf(event)
        }

        runBlocking {
            channel.observeEvents(context = context).collect { events ->
                assertEquals(
                    expected = emptyList(),
                    actual = events
                )
            }
        }
    }

    @Test
    fun `should pass event downstream if context for filtering is not provided`() {

        val context = MockDataFactory.randomUuid()

        val msg = Event.Block.Show
            .newBuilder()
            .setRootId(context)
            .addAllBlocks(emptyList())
            .build()

        val message = Message
            .newBuilder()
            .setBlockShow(msg)

        val event = Event
            .newBuilder()
            .setContextId(context)
            .addMessages(message)
            .build()

        proxy.stub {
            on { flow() } doReturn flowOf(event)
        }

        val expected = listOf(
            EventEntity.Command.ShowBlock(
                rootId = context,
                blocks = emptyList(),
                context = context
            )
        )

        runBlocking {
            channel.observeEvents(context = context).collect { events ->
                assertEquals(
                    expected = expected,
                    actual = events
                )
            }
        }
    }
}