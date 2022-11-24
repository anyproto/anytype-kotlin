package com.anytypeio.anytype

import anytype.model.Block
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.RelationLink
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.middleware.EventProxy
import com.anytypeio.anytype.middleware.interactor.MiddlewareEventChannel
import com.anytypeio.anytype.middleware.mappers.MRelationLink
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import kotlin.test.assertEquals

class MiddlewareEventChannelTest {

    @Mock
    lateinit var proxy: EventProxy

    private lateinit var channel: MiddlewareEventChannel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        channel = MiddlewareEventChannel(proxy)
    }

    @Test
    fun `should filter event by context and pass it downstream`() {

        val context = MockDataFactory.randomUuid()
        val id = MockDataFactory.randomUuid()

        val msg = anytype.Event.Block.Set.BackgroundColor(
            id = id,
            backgroundColor = ThemeColor.YELLOW.code
        )

        val message = anytype.Event.Message(
            blockSetBackgroundColor = msg
        )

        val event = anytype.Event(contextId = context, messages = listOf(message))

        proxy.stub {
            on { flow() } doReturn flowOf(event)
        }

        val expected = listOf(
            Event.Command.GranularChange(
                id = id,
                context = context,
                backgroundColor = ThemeColor.YELLOW.code
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

        val msg = anytype.Event.Block.Set.BackgroundColor(
            backgroundColor = ThemeColor.YELLOW.code
        )

        val message = anytype.Event.Message(
            blockSetBackgroundColor = msg
        )

        val event =
            anytype.Event(contextId = MockDataFactory.randomUuid(), messages = listOf(message))

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
        val id = MockDataFactory.randomUuid()

        val msg = anytype.Event.Block.Set.BackgroundColor(
            id = id,
            backgroundColor = ThemeColor.YELLOW.code
        )

        val message = anytype.Event.Message(
            blockSetBackgroundColor = msg
        )

        val event = anytype.Event(contextId = context, messages = listOf(message))

        proxy.stub {
            on { flow() } doReturn flowOf(event)
        }

        val expected = listOf(
            Event.Command.GranularChange(
                id = id,
                context = context,
                backgroundColor = ThemeColor.YELLOW.code
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

        val msg = anytype.Event.Block.Set.File(
            id = id,
            hash = anytype.Event.Block.Set.File.Hash(hash),
            mime = anytype.Event.Block.Set.File.Mime(mime),
            size = anytype.Event.Block.Set.File.Size(size),
            type = anytype.Event.Block.Set.File.Type(type),
            state = anytype.Event.Block.Set.File.State(state),
            name = anytype.Event.Block.Set.File.Name(name)
        )

        val message = anytype.Event.Message(blockSetFile = msg)

        val event = anytype.Event(contextId = context, messages = listOf(message))

        proxy.stub {
            on { flow() } doReturn flowOf(event)
        }

        val expected = listOf(
            Event.Command.UpdateFileBlock(
                context = context,
                id = id,
                hash = hash,
                mime = mime,
                size = size,
                type = com.anytypeio.anytype.core_models.Block.Content.File.Type.VIDEO,
                state = com.anytypeio.anytype.core_models.Block.Content.File.State.DONE,
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

        val msg = anytype.Event.Block.Set.File(id = id)

        val message = anytype.Event.Message(blockSetFile = msg)

        val event = anytype.Event(contextId = context, messages = listOf(message))

        proxy.stub {
            on { flow() } doReturn flowOf(event)
        }

        val expected = listOf(
            Event.Command.UpdateFileBlock(
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

        val msg = anytype.Event.Block.Set.BackgroundColor(id = id, backgroundColor = color)

        val message = anytype.Event.Message(blockSetBackgroundColor = msg)

        val event = anytype.Event(contextId = context, messages = listOf(message))

        proxy.stub {
            on { flow() } doReturn flowOf(event)
        }

        val expected = listOf(
            Event.Command.GranularChange(
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

        val details = com.anytypeio.anytype.core_models.Block.Fields(map = mutableMapOf(icon, name))

        val msg = anytype.Event.Object.Details.Set(
            id = id,
            details = mapOf(
                icon.first to icon.second,
                name.first to name.second
            )
        )

        val message = anytype.Event.Message(objectDetailsSet = msg)

        val event = anytype.Event(contextId = context, messages = listOf(message))

        proxy.stub {
            on { flow() } doReturn flowOf(event)
        }

        val expected = listOf(
            Event.Command.Details.Set(
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

    @Test
    fun `should return amend relation links event`() {

        val context = MockDataFactory.randomUuid()

        val id = MockDataFactory.randomUuid()

        val relationLink1 = MRelationLink(
            key = MockDataFactory.randomUuid(),
            format = anytype.model.RelationFormat.longtext
        )

        val relationLink2 = MRelationLink(
            key = MockDataFactory.randomUuid(),
            format = anytype.model.RelationFormat.longtext
        )

        val relationLink3 = MRelationLink(
            key = MockDataFactory.randomUuid(),
            format = anytype.model.RelationFormat.longtext
        )

        val msg = anytype.Event.Object.Relations.Amend(
            id = id,
            relationLinks = listOf(relationLink1, relationLink2, relationLink3)
        )

        val message = anytype.Event.Message(objectRelationsAmend = msg)

        val event = anytype.Event(contextId = context, messages = listOf(message))

        proxy.stub {
            on { flow() } doReturn flowOf(event)
        }

        val expected = listOf(
            Event.Command.ObjectRelationLinks.Amend(
                context = context,
                id = id,
                relationLinks = listOf(
                    RelationLink(key = relationLink1.key, format = RelationFormat.LONG_TEXT),
                    RelationLink(key = relationLink2.key, format = RelationFormat.LONG_TEXT),
                    RelationLink(key = relationLink3.key, format = RelationFormat.LONG_TEXT)
                )
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
    fun `should return remove relation links event`() {

        val context = MockDataFactory.randomUuid()

        val id = MockDataFactory.randomUuid()

        val id1 = MockDataFactory.randomUuid()
        val id2 = MockDataFactory.randomUuid()
        val id3 = MockDataFactory.randomUuid()

        val msg = anytype.Event.Object.Relations.Remove(
            id = id,
            relationKeys = listOf(id1, id2, id3)
        )

        val message = anytype.Event.Message(objectRelationsRemove = msg)

        val event = anytype.Event(contextId = context, messages = listOf(message))

        proxy.stub {
            on { flow() } doReturn flowOf(event)
        }

        val expected = listOf(
            Event.Command.ObjectRelationLinks.Remove(
                context = context,
                id = id,
                keys = listOf(id1, id2, id3)
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