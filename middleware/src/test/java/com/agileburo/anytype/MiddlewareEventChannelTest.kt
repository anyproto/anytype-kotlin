package com.agileburo.anytype

import anytype.Events.Event
import anytype.Events.Event.Message
import anytype.model.Models
import com.agileburo.anytype.common.MockDataFactory
import com.agileburo.anytype.data.auth.model.BlockEntity
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
                root = context,
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
                root = context,
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
    fun `should return UpdateBlockFile event`() {

        val hash = "785687346534hfjdbsjfbds"
        val name = "video1.mp4"
        val mime = "video/*"
        val size = 999111L
        val state = Models.Block.Content.File.State.Done
        val type = Models.Block.Content.File.Type.Video

        val context = MockDataFactory.randomUuid()
        val id = MockDataFactory.randomUuid()

        val msg = Message
            .newBuilder()
            .blockSetFileBuilder
            .setId(id)
            .setHash(Event.Block.Set.File.Hash.newBuilder().setValue(hash).build())
            .setMime(Event.Block.Set.File.Mime.newBuilder().setValue(mime).build())
            .setSize(Event.Block.Set.File.Size.newBuilder().setValue(size).build())
            .setType(Event.Block.Set.File.Type.newBuilder().setValue(type).build())
            .setState(Event.Block.Set.File.State.newBuilder().setValue(state).build())
            .setName(Event.Block.Set.File.Name.newBuilder().setValue(name).build())
            .build()

        val message = Message
            .newBuilder()
            .setBlockSetFile(msg)

        val event = Event
            .newBuilder()
            .setContextId(context)
            .addMessages(message)
            .build()

        proxy.stub {
            on { flow() } doReturn flowOf(event)
        }

        val expected = listOf(
            EventEntity.Command.UpdateBlockFile(
                context = context,
                id = id,
                hash = hash,
                mime = mime,
                size = size,
                type = BlockEntity.Content.File.Type.VIDEO,
                state = BlockEntity.Content.File.State.DONE,
                name = name
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
    fun `should return UpdateBlockFile event with nullable values`() {

        val context = MockDataFactory.randomUuid()
        val id = MockDataFactory.randomUuid()

        val msg = Message
            .newBuilder()
            .blockSetFileBuilder
            .setId(id)
            .build()

        val message = Message
            .newBuilder()
            .setBlockSetFile(msg)

        val event = Event
            .newBuilder()
            .setContextId(context)
            .addMessages(message)
            .build()

        proxy.stub {
            on { flow() } doReturn flowOf(event)
        }

        val expected = listOf(
            EventEntity.Command.UpdateBlockFile(
                context = context,
                id = id
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
    fun `should return granular change with background colour`() {

        val context = MockDataFactory.randomUuid()
        val id = MockDataFactory.randomUuid()
        val color = MockDataFactory.randomString()

        val msg = Message
            .newBuilder()
            .blockSetBackgroundColorBuilder
            .setId(id)
            .setBackgroundColor(color)
            .build()

        val message = Message
            .newBuilder()
            .setBlockSetBackgroundColor(msg)

        val event = Event
            .newBuilder()
            .setContextId(context)
            .addMessages(message)
            .build()

        proxy.stub {
            on { flow() } doReturn flowOf(event)
        }

        val expected = listOf(
            EventEntity.Command.GranularChange(
                context = context,
                id = id,
                backgroundColor = color
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