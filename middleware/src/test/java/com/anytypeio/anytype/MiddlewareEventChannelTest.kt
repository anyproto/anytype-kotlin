package com.anytypeio.anytype

import anytype.Event
import anytype.model.Block
import com.anytypeio.anytype.common.MockDataFactory
import com.anytypeio.anytype.data.auth.model.BlockEntity
import com.anytypeio.anytype.data.auth.model.EventEntity
import com.anytypeio.anytype.middleware.EventProxy
import com.anytypeio.anytype.middleware.interactor.MiddlewareEventChannel
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

        val msg = Event.Block.Show(
            rootId = context,
            blocks = emptyList()
        )

        val message = Event.Message(blockShow = msg)

        val event = Event(contextId = context, messages = listOf(message))

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

        val msg = Event.Block.Show(
            rootId = MockDataFactory.randomString(),
            blocks = emptyList()
        )

        val message = Event.Message(blockShow = msg)

        val event = Event(contextId = MockDataFactory.randomUuid(), messages = listOf(message))

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

        val msg = Event.Block.Show(
            rootId = context,
            blocks = emptyList()
        )

        val message = Event.Message(blockShow = msg)

        val event = Event(contextId = context, messages = listOf(message))

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
        val state = Block.Content.File.State.Done
        val type = Block.Content.File.Type.Video

        val context = MockDataFactory.randomUuid()
        val id = MockDataFactory.randomUuid()

        val msg = Event.Block.Set.File(
            id = id,
            hash = Event.Block.Set.File.Hash(hash),
            mime = Event.Block.Set.File.Mime(mime),
            size = Event.Block.Set.File.Size(size),
            type = Event.Block.Set.File.Type(type),
            state = Event.Block.Set.File.State(state),
            name = Event.Block.Set.File.Name(name)
        )

        val message = Event.Message(blockSetFile = msg)

        val event = Event(contextId = context, messages = listOf(message))

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

        val msg = Event.Block.Set.File(id = id)

        val message = Event.Message(blockSetFile = msg)

        val event = Event(contextId = context, messages = listOf(message))

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

        val msg = Event.Block.Set.BackgroundColor(id = id, backgroundColor = color)

        val message = Event.Message(blockSetBackgroundColor = msg)

        val event = Event(contextId = context, messages = listOf(message))

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

    @Test
    fun `should return update-details event`() {

        val context = MockDataFactory.randomUuid()

        val id = MockDataFactory.randomUuid()

        val icon = Pair("icon", ":package:")
        val name = Pair("name", "Document I")

        val details = BlockEntity.Fields(map = mutableMapOf(icon, name))

        val msg = Event.Block.Set.Details(
            id = id,
            details = mapOf(
                icon.first to icon.second,
                name.first to name.second
            )
        )

        val message = Event.Message(blockSetDetails = msg)

        val event = Event(contextId = context, messages = listOf(message))

        proxy.stub {
            on { flow() } doReturn flowOf(event)
        }

        val expected = listOf(
            EventEntity.Command.UpdateDetails(
                context = context,
                target = id,
                details = details
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