package com.anytypeio.anytype.domain.chats

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.common.DefaultCoroutineTestRule
import com.anytypeio.anytype.domain.debugging.Logger
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify

class SpaceChatPreviewContainerTest {

    @get:Rule
    val rule = DefaultCoroutineTestRule()

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var channel: ChatEventChannel

    @Mock
    lateinit var logger: Logger

    val dispatchers = AppCoroutineDispatchers(
        io = rule.dispatcher,
        computation = rule.dispatcher,
        main = rule.dispatcher
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should create container with proper dependencies and not crash`() = runTest {
        // Given
        val spaceId = SpaceId("test-space")
        
        repo.stub {
            onBlocking { subscribeToMessagePreviews(any()) } doReturn emptyList()
        }

        channel.stub {
            on { subscribe(any()) } doReturn emptyFlow()
        }

        // When
        val container = SpaceChatPreviewContainer.Default(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = this,
            logger = logger
        )

        // Then
        assertNotNull(container)
        
        // Should be able to start and stop without issues
        container.start(spaceId)
        container.stop()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should return initial preview as null`() = runTest {
        // Given
        val spaceId = SpaceId("test-space")
        
        repo.stub {
            onBlocking { subscribeToMessagePreviews(any()) } doReturn emptyList()
        }

        channel.stub {
            on { subscribe(any()) } doReturn emptyFlow()
        }

        val container = SpaceChatPreviewContainer.Default(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = this,
            logger = logger
        )

        // When
        container.start(spaceId)
        delay(100)

        // Then
        val preview = container.observePreview(spaceId).first()
        assertEquals(null, preview)
        
        container.stop()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should return correct preview for initial previews`() = runTest {
        // Given
        val spaceId = SpaceId("test-space")
        
        val initialPreviews = listOf(
            Chat.Preview(
                space = spaceId,
                chat = "chat-1",
                message = null,
                dependencies = emptyList()
            )
        )
        
        repo.stub {
            onBlocking { subscribeToMessagePreviews(any()) } doReturn initialPreviews
        }

        channel.stub {
            on { subscribe(any()) } doReturn emptyFlow()
        }

        val container = SpaceChatPreviewContainer.Default(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = this,
            logger = logger
        )

        // When
        container.start(spaceId)
        delay(100)

        // Then
        val preview = container.observePreview(spaceId).first()
        assertNotNull(preview)
        assertEquals(spaceId, preview.space)
        assertEquals("chat-1", preview.chat)
        
        container.stop()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should handle Add events and update preview`() = runTest {
        // Given
        val spaceId = SpaceId("test-space")
        val chatId = "test-chat"
        
        val initialPreview = Chat.Preview(
            space = spaceId,
            chat = chatId,
            message = null,
            dependencies = emptyList()
        )
        
        val newMessage = Chat.Message(
            id = "msg-1",
            order = "order-1",
            creator = "creator-1",
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis(),
            content = Chat.Message.Content(
                text = "New message",
                style = Block.Content.Text.Style.P,
                marks = emptyList()
            ),
            attachments = emptyList()
        )
        
        val addEvent = Event.Command.Chats.Add(
            context = chatId,
            id = "msg-1",
            order = "order-1",
            message = newMessage,
            dependencies = emptyList()
        )
        
        repo.stub {
            onBlocking { subscribeToMessagePreviews(any()) } doReturn listOf(initialPreview)
        }

        channel.stub {
            on { subscribe(any()) } doReturn flowOf(listOf(addEvent))
        }

        val container = SpaceChatPreviewContainer.Default(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = this,
            logger = logger
        )

        // When
        container.start(spaceId)
        delay(100)

        // Then
        val preview = container.observePreview(spaceId).first()
        assertNotNull(preview)
        assertEquals(newMessage, preview.message)
        
        // Verify dependencies are not included (space-level container doesn't handle attachments)
        assertEquals(0, preview.dependencies.size)
        
        container.stop()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should handle Update events correctly`() = runTest {
        // Given
        val spaceId = SpaceId("test-space")
        val chatId = "test-chat"
        val messageId = "msg-1"
        
        val originalMessage = Chat.Message(
            id = messageId,
            order = "order-1",
            creator = "creator-1",
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis(),
            content = Chat.Message.Content(
                text = "Original message",
                style = Block.Content.Text.Style.P,
                marks = emptyList()
            ),
            attachments = emptyList()
        )
        
        val updatedMessage = Chat.Message(
            id = messageId,
            order = "order-1",
            creator = "creator-1",
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis(),
            content = Chat.Message.Content(
                text = "Updated message",
                style = Block.Content.Text.Style.P,
                marks = emptyList()
            ),
            attachments = emptyList()
        )
        
        val initialPreview = Chat.Preview(
            space = spaceId,
            chat = chatId,
            message = originalMessage,
            dependencies = emptyList()
        )
        
        val updateEvent = Event.Command.Chats.Update(
            context = chatId,
            id = messageId,
            message = updatedMessage
        )
        
        repo.stub {
            onBlocking { subscribeToMessagePreviews(any()) } doReturn listOf(initialPreview)
        }

        channel.stub {
            on { subscribe(any()) } doReturn flowOf(listOf(updateEvent))
        }

        val container = SpaceChatPreviewContainer.Default(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = this,
            logger = logger
        )

        // When
        container.start(spaceId)
        delay(100)

        // Then
        val preview = container.observePreview(spaceId).first()
        assertNotNull(preview)
        assertEquals(updatedMessage, preview.message)
        assertEquals("Updated message", preview.message?.content?.text)
        
        container.stop()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should handle Delete events correctly`() = runTest {
        // Given
        val spaceId = SpaceId("test-space")
        val chatId = "test-chat"
        val messageId = "msg-1"
        
        val originalMessage = Chat.Message(
            id = messageId,
            order = "order-1",
            creator = "creator-1",
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis(),
            content = Chat.Message.Content(
                text = "Message to delete",
                style = Block.Content.Text.Style.P,
                marks = emptyList()
            ),
            attachments = emptyList()
        )
        
        val initialPreview = Chat.Preview(
            space = spaceId,
            chat = chatId,
            message = originalMessage,
            dependencies = emptyList()
        )
        
        val deleteEvent = Event.Command.Chats.Delete(
            context = chatId,
            message = messageId
        )
        
        repo.stub {
            onBlocking { subscribeToMessagePreviews(any()) } doReturn listOf(initialPreview)
        }

        channel.stub {
            on { subscribe(any()) } doReturn flowOf(listOf(deleteEvent))
        }

        val container = SpaceChatPreviewContainer.Default(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = this,
            logger = logger
        )

        // When
        container.start(spaceId)
        delay(100)

        // Then
        val preview = container.observePreview(spaceId).first()
        assertNotNull(preview)
        assertEquals(null, preview.message) // Message should be deleted
        
        container.stop()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should handle UpdateState events with order validation`() = runTest {
        // Given
        val spaceId = SpaceId("test-space")
        val chatId = "test-chat"
        
        val initialPreview = Chat.Preview(
            space = spaceId,
            chat = chatId,
            message = null,
            dependencies = emptyList(),
            state = Chat.State(
                order = 5L,
                lastStateId = "msg-1"
            )
        )
        
        val higherOrderState = Chat.State(
            order = 10L,
            lastStateId = "msg-2"
        )
        
        val updateStateEvent = Event.Command.Chats.UpdateState(
            context = chatId,
            state = higherOrderState
        )
        
        repo.stub {
            onBlocking { subscribeToMessagePreviews(any()) } doReturn listOf(initialPreview)
        }

        channel.stub {
            on { subscribe(any()) } doReturn flowOf(listOf(updateStateEvent))
        }

        val container = SpaceChatPreviewContainer.Default(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = this,
            logger = logger
        )

        // When
        container.start(spaceId)
        delay(100)

        // Then
        val preview = container.observePreview(spaceId).first()
        assertNotNull(preview)
        assertEquals(higherOrderState, preview.state)
        
        container.stop()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should handle repository errors gracefully`() = runTest {
        // Given
        val spaceId = SpaceId("test-space")
        val exception = RuntimeException("Repository error")
        
        repo.stub {
            onBlocking { subscribeToMessagePreviews(any()) } doThrow exception
        }

        channel.stub {
            on { subscribe(any()) } doReturn emptyFlow()
        }

        val container = SpaceChatPreviewContainer.Default(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = this,
            logger = logger
        )

        // When
        container.start(spaceId)
        delay(100)

        // Then
        val preview = container.observePreview(spaceId).first()
        assertEquals(null, preview)
        
        // Verify logger was called with the error
        verify(logger).logWarning(any())
        
        container.stop()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should handle multiple start calls for different spaces`() = runTest {
        // Given
        val space1 = SpaceId("space-1")
        val space2 = SpaceId("space-2")
        
        val previews1 = listOf(
            Chat.Preview(
                space = space1,
                chat = "chat-1",
                message = null,
                dependencies = emptyList()
            )
        )
        
        val previews2 = listOf(
            Chat.Preview(
                space = space2,
                chat = "chat-2",
                message = null,
                dependencies = emptyList()
            ),
            Chat.Preview(
                space = space2,
                chat = "chat-3",
                message = null,
                dependencies = emptyList()
            )
        )
        
        repo.stub {
            onBlocking { subscribeToMessagePreviews("${space1.id}/space-chat-previews") } doReturn previews1
            onBlocking { subscribeToMessagePreviews("${space2.id}/space-chat-previews") } doReturn previews2
        }

        channel.stub {
            on { subscribe(any()) } doReturn emptyFlow()
        }

        val container = SpaceChatPreviewContainer.Default(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = this,
            logger = logger
        )

        // When - start with space1
        container.start(space1)
        delay(100)
        
        val preview1 = container.observePreview(space1).first()
        assertNotNull(preview1)
        assertEquals(space1, preview1.space)
        
        // Start with space2 (should cancel previous and start new)
        container.start(space2)
        delay(100)
        
        val preview2 = container.observePreview(space2).first()
        assertNotNull(preview2)
        assertEquals(space2, preview2.space)
        
        container.stop()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should clear state on stop`() = runTest {
        // Given
        val spaceId = SpaceId("test-space")
        
        val initialPreviews = listOf(
            Chat.Preview(
                space = spaceId,
                chat = "chat-1",
                message = null,
                dependencies = emptyList()
            )
        )
        
        repo.stub {
            onBlocking { subscribeToMessagePreviews(any()) } doReturn initialPreviews
            onBlocking { unsubscribeFromMessagePreviews(any()) } doReturn Unit
        }

        channel.stub {
            on { subscribe(any()) } doReturn emptyFlow()
        }

        val container = SpaceChatPreviewContainer.Default(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = this,
            logger = logger
        )

        // When
        container.start(spaceId)
        delay(100)
        
        val previewBeforeStop = container.observePreview(spaceId).first()
        assertNotNull(previewBeforeStop)
        
        container.stop()
        delay(100)
        
        // Then
        val previewAfterStop = container.observePreview(spaceId).first()
        assertEquals(null, previewAfterStop)
        
        verify(repo).unsubscribeFromMessagePreviews("${spaceId.id}/space-chat-previews")
    }
}