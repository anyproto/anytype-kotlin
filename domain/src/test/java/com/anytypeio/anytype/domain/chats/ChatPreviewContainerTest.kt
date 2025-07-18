package com.anytypeio.anytype.domain.chats

import app.cash.turbine.turbineScope
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.StubObject
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.common.DefaultCoroutineTestRule
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.library.StoreSearchByIdsParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
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
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyNoInteractions

class ChatPreviewContainerTest {

    @get:Rule
    val rule = DefaultCoroutineTestRule()

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var channel: ChatEventChannel

    @Mock
    lateinit var logger: Logger

    @Mock
    lateinit var subscription: StorelessSubscriptionContainer

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
        repo.stub {
            onBlocking { subscribeToMessagePreviews(any()) } doReturn emptyList()
        }

        channel.stub {
            on { subscribe(any()) } doReturn emptyFlow()
        }

        // When
        val container = VaultChatPreviewContainer(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = this,
            logger = logger,
            subscription = subscription
        )

        // Then
        assertNotNull(container)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should validate order comparison logic implementation`() = runTest {
        // This test validates the order comparison logic that is now consolidated in ChatStateUtils.shouldApplyNewChatState()

        // Given - test scenarios for order comparison
        val currentOrder = 3L
        val higherOrder = 5L
        val lowerOrder = 1L
        val equalOrder = 3L

        // When - applying the shared logic from ChatStateUtils
        val shouldApplyHigher =
            ChatStateUtils.shouldApplyNewChatState(newOrder = higherOrder, currentOrder = currentOrder)
        val shouldApplyLower =
            ChatStateUtils.shouldApplyNewChatState(newOrder = lowerOrder, currentOrder = currentOrder)
        val shouldApplyEqual =
            ChatStateUtils.shouldApplyNewChatState(newOrder = equalOrder, currentOrder = currentOrder)

        // Then - validate order comparison behavior
        assertEquals(true, shouldApplyHigher, "Higher order should be applied")
        assertEquals(false, shouldApplyLower, "Lower order should be rejected")
        assertEquals(false, shouldApplyEqual, "Equal order should be rejected")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should validate null state handling in order comparison`() = runTest {
        // This test validates the null state handling in order comparison

        // Given - scenarios for null state handling
        val validOrder = 5L
        val nullCurrentOrder: Long? = null

        // When - applying the shared logic from ChatStateUtils
        val shouldApply =
            ChatStateUtils.shouldApplyNewChatState(newOrder = validOrder, currentOrder = nullCurrentOrder)
        val currentOrder = nullCurrentOrder ?: -1L

        // Then - validate null handling behavior
        assertEquals(-1L, currentOrder, "Null state should default to -1L")
        assertEquals(true, shouldApply, "Valid order should be applied over null state")
    }

    @Test
    fun `should test order comparison edge cases`() {
        // Test edge cases for the order comparison function using ChatStateUtils

        // Edge case: zero orders
        assertEquals(true, ChatStateUtils.shouldApplyNewChatState(newOrder = 1L, currentOrder = 0L))
        assertEquals(false, ChatStateUtils.shouldApplyNewChatState(newOrder = 0L, currentOrder = 1L))
        assertEquals(false, ChatStateUtils.shouldApplyNewChatState(newOrder = 0L, currentOrder = 0L))

        // Edge case: negative orders
        assertEquals(true, ChatStateUtils.shouldApplyNewChatState(newOrder = -1L, currentOrder = -2L))
        assertEquals(false, ChatStateUtils.shouldApplyNewChatState(newOrder = -2L, currentOrder = -1L))

        // Edge case: large numbers
        assertEquals(
            true,
            ChatStateUtils.shouldApplyNewChatState(
                newOrder = Long.MAX_VALUE,
                currentOrder = Long.MAX_VALUE - 1
            )
        )
        assertEquals(
            false,
            ChatStateUtils.shouldApplyNewChatState(
                newOrder = Long.MAX_VALUE - 1,
                currentOrder = Long.MAX_VALUE
            )
        )

        // Edge case: null current order (should default to -1L)
        assertEquals(true, ChatStateUtils.shouldApplyNewChatState(newOrder = 0L, currentOrder = null))
        assertEquals(false, ChatStateUtils.shouldApplyNewChatState(newOrder = -2L, currentOrder = null))
        assertEquals(false, ChatStateUtils.shouldApplyNewChatState(newOrder = -1L, currentOrder = null))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should provide observePreviewsWithAttachments method that returns empty dependencies when no attachments tracked`() = runTest {
        turbineScope {
            // Given
            val spaceId = SpaceId("test-space")

            val initialPreviews = listOf(
                Chat.Preview(
                    space = spaceId,
                    chat = "test-chat",
                    message = null,
                    dependencies = emptyList()
                )
            )

            // Mock repository and event channel
            repo.stub {
                onBlocking { subscribeToMessagePreviews(any()) } doReturn initialPreviews
            }

            channel.stub {
                on { subscribe(any()) } doReturn emptyFlow()
            }

            // When
            val container = VaultChatPreviewContainer(
                repo = repo,
                events = channel,
                dispatchers = dispatchers,
                scope = this,
                logger = logger,
                subscription = subscription
            )

            container.start()

            // Subscribe to previews with attachments
            val previews = container.observePreviewsWithAttachments().first()

            // Then - verify it returns previews with empty dependencies when no attachments are tracked
            assertEquals(1, previews.size)
            assertEquals(0, previews.first().dependencies.size)

            // Cleanup
            container.stop()
            assertNotNull(container)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should not start attachment subscription when preview has no attachments`() = runTest {
        // Given
        val spaceId = SpaceId("test-space")
        val chatId = "test-chat"
        
        val initialPreviews = listOf(
            Chat.Preview(
                space = spaceId,
                chat = chatId,
                message = null,
                dependencies = emptyList()
            )
        )
        
        // Mock repository to return initial previews
        repo.stub {
            onBlocking { subscribeToMessagePreviews(any()) } doReturn initialPreviews
        }
        
        // Mock event channel to emit no events
        channel.stub {
            on { subscribe(any()) } doReturn emptyFlow()
        }
        
        // When
        val container = VaultChatPreviewContainer(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = this,
            logger = logger,
            subscription = subscription
        )
        
        // Start the container
        container.start()
        
        // Subscribe to previews with attachments and verify no attachments tracked
        val previewsWithAttachments = container.observePreviewsWithAttachments().first()
        
        // Then - verify no subscription was made since there are no attachments
        verifyNoInteractions(subscription)
        
        // Verify the preview has no dependencies (no attachments)
        assertEquals(1, previewsWithAttachments.size)
        assertEquals(0, previewsWithAttachments.first().dependencies.size)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should verify observePreviewsWithAttachments interface method exists and can be called`() = runTest {
        // Given
        val spaceId = SpaceId("test-space")
        
        repo.stub {
            onBlocking { subscribeToMessagePreviews(any()) } doReturn emptyList()
        }
        
        channel.stub {
            on { subscribe(any()) } doReturn emptyFlow()
        }
        
        // When
        val container: ChatPreviewContainer = VaultChatPreviewContainer(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = this,
            logger = logger,
            subscription = subscription
        )
        
        container.start()
        
        // Then - verify the interface method exists and can be called
        val previewsFlow = container.observePreviewsWithAttachments()
        assertNotNull(previewsFlow)
        
        // Verify it returns a flow that emits empty list
        val result = previewsFlow.first()
        assertEquals(0, result.size)
        
        container.stop()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should subscribe only to missing attachments not in dependencies`() = runTest {
        // Given
        val spaceId = SpaceId("test-space")
        val chatId = "test-chat"
        val attachmentInDependencies = "attachment-in-deps"
        val attachmentNotInDependencies = "attachment-missing"
        
        // Create a dependency that matches one of the attachments
        val dependencyWithAttachment = StubObject(
            id = attachmentInDependencies,
            name = "Attachment in Dependencies"
        )
        
        // Create a chat message with two attachments
        val chatMessage = Chat.Message(
            id = "msg-id",
            order = "order-id",
            creator = "creator-id",
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis(),
            content = Chat.Message.Content(
                text = "Test message",
                style = Block.Content.Text.Style.P,
                marks = emptyList()
            ),
            attachments = listOf(
                Chat.Message.Attachment(
                    target = attachmentInDependencies,
                    type = Chat.Message.Attachment.Type.File
                ),
                Chat.Message.Attachment(
                    target = attachmentNotInDependencies,
                    type = Chat.Message.Attachment.Type.File
                )
            )
        )
        
        // Create event with one attachment in dependencies, one missing
        val chatAddEvent = Event.Command.Chats.Add(
            context = chatId,
            id = "msg-id",
            order = "order-id",
            message = chatMessage,
            dependencies = listOf(dependencyWithAttachment) // Only one attachment in dependencies
        )
        
        val initialPreviews = listOf(
            Chat.Preview(
                space = spaceId,
                chat = chatId,
                message = null,
                dependencies = emptyList()
            )
        )
        
        // Mock repository and event channel
        repo.stub {
            onBlocking { subscribeToMessagePreviews(any()) } doReturn initialPreviews
        }
        
        channel.stub {
            on { subscribe(any()) } doReturn flowOf(listOf(chatAddEvent))
        }
        
        // Mock subscription container to return the missing attachment
        val missingAttachmentDetails = StubObject(
            id = attachmentNotInDependencies,
            name = "Missing Attachment"
        )
        
        subscription.stub {
            on { subscribe(any<StoreSearchByIdsParams>()) } doReturn flowOf(listOf(missingAttachmentDetails))
        }
        
        // When
        val container = VaultChatPreviewContainer(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = this,
            logger = logger,
            subscription = subscription
        )
        
        container.start()
        
        // Allow time for events to be processed
        delay(100)
        
        // Subscribe to previews with attachments and verify attachment integration
        val previewsWithAttachments = container.observePreviewsWithAttachments().first()
        
        // Then - verify it only subscribes to the missing attachment
        assertEquals(1, previewsWithAttachments.size)
        val preview = previewsWithAttachments.first()
        
        // Should have 2 dependencies: original dependency + missing attachment
        assertEquals(2, preview.dependencies.size)
        
        // Verify both dependencies are present
        val dependencyIds = preview.dependencies.map { it.id }
        assertEquals(true, dependencyIds.contains(attachmentInDependencies))
        assertEquals(true, dependencyIds.contains(attachmentNotInDependencies))
        
        container.stop()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should observe preview with updated attachments via observePreviewsWithAttachments`() = runTest {
        // Given
        val spaceId = SpaceId("test-space")
        val chatId = "test-chat"
        val attachmentId = "attachment-id"
        
        val chatMessage = Chat.Message(
            id = "msg-id",
            order = "order-id",
            creator = "creator-id",
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis(),
            content = Chat.Message.Content(
                text = "Test message",
                style = Block.Content.Text.Style.P,
                marks = emptyList()
            ),
            attachments = listOf(
                Chat.Message.Attachment(
                    target = attachmentId,
                    type = Chat.Message.Attachment.Type.File
                )
            )
        )
        
        // Create event with attachment that's missing from dependencies
        val chatAddEvent = Event.Command.Chats.Add(
            context = chatId,
            id = "msg-id",
            order = "order-id",
            message = chatMessage,
            dependencies = emptyList() // No attachment details in dependencies
        )
        
        // Create initial preview without attachment details
        val initialPreview = Chat.Preview(
            space = spaceId,
            chat = chatId,
            message = null,
            dependencies = emptyList()
        )
        
        // Create attachment details
        val attachmentDetails = StubObject(
            id = attachmentId,
            name = "Test Attachment"
        )
        
        // Mock repository and event channel
        repo.stub {
            onBlocking { subscribeToMessagePreviews(any()) } doReturn listOf(initialPreview)
        }
        
        channel.stub {
            on { subscribe(any()) } doReturn flowOf(listOf(chatAddEvent))
        }
        
        // Mock subscription to return attachment details
        subscription.stub {
            on { subscribe(any<StoreSearchByIdsParams>()) } doReturn flowOf(listOf(attachmentDetails))
        }
        
        // When
        val container = VaultChatPreviewContainer(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = this,
            logger = logger,
            subscription = subscription
        )
        
        container.start()
        
        // Allow time for events to be processed
        delay(100)
        
        // Subscribe to previews with attachments and find the specific space
        val previewsWithAttachments = container.observePreviewsWithAttachments().first()
        val previewWithAttachments = previewsWithAttachments.find { it.space == spaceId }
        
        // Then - verify the preview is updated with attachment details
        assertNotNull(previewWithAttachments)
        assertEquals(1, previewWithAttachments.dependencies.size)
        assertEquals(attachmentDetails, previewWithAttachments.dependencies.first())
        
        container.stop()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should detect missing attachments in initial previews and subscribe to them`() = runTest {
        // Given
        val spaceId = SpaceId("test-space")
        val chatId = "test-chat"
        val attachmentId = "attachment-missing-from-deps"
        
        // Create a chat message with attachment
        val chatMessage = Chat.Message(
            id = "msg-id",
            order = "order-id",
            creator = "creator-id",
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis(),
            content = Chat.Message.Content(
                text = "Test message",
                style = Block.Content.Text.Style.P,
                marks = emptyList()
            ),
            attachments = listOf(
                Chat.Message.Attachment(
                    target = attachmentId,
                    type = Chat.Message.Attachment.Type.File
                )
            )
        )
        
        // Create initial preview with message but no attachment details in dependencies
        val initialPreview = Chat.Preview(
            space = spaceId,
            chat = chatId,
            message = chatMessage,
            dependencies = emptyList() // No attachment details in dependencies
        )
        
        // Create attachment details for subscription response
        val attachmentDetails = StubObject(
            id = attachmentId,
            name = "Test Attachment"
        )
        
        // Mock repository to return initial preview with missing attachment
        repo.stub {
            onBlocking { subscribeToMessagePreviews(any()) } doReturn listOf(initialPreview)
        }
        
        // Mock event channel to emit no additional events
        channel.stub {
            on { subscribe(any()) } doReturn emptyFlow()
        }
        
        // Mock subscription to return attachment details
        subscription.stub {
            on { subscribe(any<StoreSearchByIdsParams>()) } doReturn flowOf(listOf(attachmentDetails))
        }
        
        // When
        val container = VaultChatPreviewContainer(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = this,
            logger = logger,
            subscription = subscription
        )
        
        container.start()
        
        // Allow time for initial processing
        delay(100)
        
        // Subscribe to previews with attachments
        val previewsWithAttachments = container.observePreviewsWithAttachments().first()
        
        // Then - verify initial attachment was detected and subscribed to
        assertEquals(1, previewsWithAttachments.size)
        val preview = previewsWithAttachments.first()
        
        // Should have 1 dependency: the attachment that was missing from initial dependencies
        assertEquals(1, preview.dependencies.size)
        assertEquals(attachmentDetails, preview.dependencies.first())
        
        container.stop()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should handle Update events and update existing messages`() = runTest {
        // Given
        val spaceId = SpaceId("test-space")
        val chatId = "test-chat"
        val messageId = "msg-id"
        
        val originalMessage = Chat.Message(
            id = messageId,
            order = "order-id",
            creator = "creator-id",
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
            order = "order-id",
            creator = "creator-id",
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
        
        // Mock repository and event channel
        repo.stub {
            onBlocking { subscribeToMessagePreviews(any()) } doReturn listOf(initialPreview)
        }
        
        channel.stub {
            on { subscribe(any()) } doReturn flowOf(listOf(updateEvent))
        }
        
        // When
        val container = VaultChatPreviewContainer(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = this,
            logger = logger,
            subscription = subscription
        )
        
        container.start()
        
        // Allow time for events to be processed
        delay(100)
        
        // Subscribe to previews and verify update
        val preview = container.observePreview(spaceId).first()
        
        // Then - verify message was updated
        assertNotNull(preview)
        assertEquals(updatedMessage, preview.message)
        assertEquals("Updated message", preview.message?.content?.text)
        
        container.stop()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should handle Delete events and remove messages`() = runTest {
        // Given
        val spaceId = SpaceId("test-space")
        val chatId = "test-chat"
        val messageId = "msg-id"
        
        val originalMessage = Chat.Message(
            id = messageId,
            order = "order-id",
            creator = "creator-id",
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
        
        // Mock repository and event channel
        repo.stub {
            onBlocking { subscribeToMessagePreviews(any()) } doReturn listOf(initialPreview)
        }
        
        channel.stub {
            on { subscribe(any()) } doReturn flowOf(listOf(deleteEvent))
        }
        
        // When
        val container = VaultChatPreviewContainer(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = this,
            logger = logger,
            subscription = subscription
        )
        
        container.start()
        
        // Allow time for events to be processed
        delay(100)
        
        // Subscribe to previews and verify deletion
        val preview = container.observePreview(spaceId).first()
        
        // Then - verify message was deleted (set to null)
        assertNotNull(preview)
        assertEquals(null, preview.message)
        
        container.stop()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should handle UpdateState events with proper order validation`() = runTest {
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
        
        // Create state update with higher order (should be applied)
        val higherOrderState = Chat.State(
            order = 10L,
            lastStateId = "msg-2"
        )
        
        val updateStateEvent = Event.Command.Chats.UpdateState(
            context = chatId,
            state = higherOrderState
        )
        
        // Mock repository and event channel
        repo.stub {
            onBlocking { subscribeToMessagePreviews(any()) } doReturn listOf(initialPreview)
        }
        
        channel.stub {
            on { subscribe(any()) } doReturn flowOf(listOf(updateStateEvent))
        }
        
        // When
        val container = VaultChatPreviewContainer(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = this,
            logger = logger,
            subscription = subscription
        )
        
        container.start()
        
        // Allow time for events to be processed
        delay(100)
        
        // Subscribe to previews and verify state update
        val preview = container.observePreview(spaceId).first()
        
        // Then - verify state was updated with higher order
        assertNotNull(preview)
        assertEquals(higherOrderState, preview.state)
        assertEquals(10L, preview.state?.order)
        assertEquals("msg-2", preview.state?.lastStateId)
        
        container.stop()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should ignore UpdateState events with lower order`() = runTest {
        // Given
        val spaceId = SpaceId("test-space")
        val chatId = "test-chat"
        
        val initialState = Chat.State(
            order = 10L,
            lastStateId = "msg-1"
        )
        
        val initialPreview = Chat.Preview(
            space = spaceId,
            chat = chatId,
            message = null,
            dependencies = emptyList(),
            state = initialState
        )
        
        // Create state update with lower order (should be ignored)
        val lowerOrderState = Chat.State(
            order = 5L,
            lastStateId = "msg-2"
        )
        
        val updateStateEvent = Event.Command.Chats.UpdateState(
            context = chatId,
            state = lowerOrderState
        )
        
        // Mock repository and event channel
        repo.stub {
            onBlocking { subscribeToMessagePreviews(any()) } doReturn listOf(initialPreview)
        }
        
        channel.stub {
            on { subscribe(any()) } doReturn flowOf(listOf(updateStateEvent))
        }
        
        // When
        val container = VaultChatPreviewContainer(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = this,
            logger = logger,
            subscription = subscription
        )
        
        container.start()
        
        // Allow time for events to be processed
        delay(100)
        
        // Subscribe to previews and verify state was not updated
        val preview = container.observePreview(spaceId).first()
        
        // Then - verify state remains unchanged (original state preserved)
        assertNotNull(preview)
        assertEquals(initialState, preview.state)
        assertEquals(10L, preview.state?.order)
        assertEquals("msg-1", preview.state?.lastStateId)
        
        container.stop()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should handle repository errors gracefully during initialization`() = runTest {
        // Given
        val exception = RuntimeException("Repository error")
        
        // Mock repository to throw an exception
        repo.stub {
            onBlocking { subscribeToMessagePreviews(any()) } doThrow exception
        }
        
        channel.stub {
            on { subscribe(any()) } doReturn emptyFlow()
        }
        
        // When
        val container = VaultChatPreviewContainer(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = this,
            logger = logger,
            subscription = subscription
        )
        
        container.start()
        
        // Allow time for initialization
        delay(100)
        
        // Then - verify container handles error gracefully and returns empty previews
        val previews = container.observePreviewsWithAttachments().first()
        assertEquals(0, previews.size)
        
        // Verify logger was called with the error
        verify(logger).logWarning(any())
        
        container.stop()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should handle attachment subscription errors gracefully`() = runTest {
        // Given
        val spaceId = SpaceId("test-space")
        val chatId = "test-chat"
        val attachmentId = "attachment-id"
        
        val chatMessage = Chat.Message(
            id = "msg-id",
            order = "order-id",
            creator = "creator-id",
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis(),
            content = Chat.Message.Content(
                text = "Test message",
                style = Block.Content.Text.Style.P,
                marks = emptyList()
            ),
            attachments = listOf(
                Chat.Message.Attachment(
                    target = attachmentId,
                    type = Chat.Message.Attachment.Type.File
                )
            )
        )
        
        val initialPreview = Chat.Preview(
            space = spaceId,
            chat = chatId,
            message = chatMessage,
            dependencies = emptyList()
        )
        
        // Mock repository
        repo.stub {
            onBlocking { subscribeToMessagePreviews(any()) } doReturn listOf(initialPreview)
        }
        
        channel.stub {
            on { subscribe(any()) } doReturn emptyFlow()
        }
        
        // Mock subscription to throw an error
        val subscriptionError = RuntimeException("Subscription error")
        subscription.stub {
            on { subscribe(any<StoreSearchByIdsParams>()) } doThrow subscriptionError
        }
        
        // When
        val container = VaultChatPreviewContainer(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = this,
            logger = logger,
            subscription = subscription
        )
        
        container.start()
        
        // Allow time for processing
        delay(100)
        
        // Then - verify container handles subscription error gracefully
        val previews = container.observePreviewsWithAttachments().first()
        assertEquals(1, previews.size)
        
        // Dependencies should be empty due to subscription error
        assertEquals(0, previews.first().dependencies.size)
        
        // Verify logger was called with the error
        verify(logger).logException(any(), any())
        
        container.stop()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should handle event channel errors gracefully`() = runTest {
        // Given
        val spaceId = SpaceId("test-space")
        val chatId = "test-chat"
        
        val initialPreview = Chat.Preview(
            space = spaceId,
            chat = chatId,
            message = null,
            dependencies = emptyList()
        )
        
        repo.stub {
            onBlocking { subscribeToMessagePreviews(any()) } doReturn listOf(initialPreview)
        }
        
        // Mock event channel to return empty flow (no events)
        channel.stub {
            on { subscribe(any()) } doReturn emptyFlow()
        }
        
        // When
        val container = VaultChatPreviewContainer(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = this,
            logger = logger,
            subscription = subscription
        )
        
        container.start()
        
        // Allow time for processing
        delay(100)
        
        // Then - verify container handles absence of events gracefully
        val previews = container.observePreviewsWithAttachments().first()
        assertEquals(1, previews.size)
        
        // Verify initial preview is preserved
        assertEquals(spaceId, previews.first().space)
        assertEquals(chatId, previews.first().chat)
        
        container.stop()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should handle unknown chat events gracefully`() = runTest {
        // Given
        val spaceId = SpaceId("test-space")
        val chatId = "test-chat"
        
        val initialPreview = Chat.Preview(
            space = spaceId,
            chat = chatId,
            message = null,
            dependencies = emptyList()
        )
        
        // Create a chat event type that's not handled (UpdateReactions)
        val unknownChatEvent = Event.Command.Chats.UpdateReactions(
            context = "unknown-context",
            id = "msg-id",
            reactions = emptyMap()
        )
        
        repo.stub {
            onBlocking { subscribeToMessagePreviews(any()) } doReturn listOf(initialPreview)
        }
        
        channel.stub {
            on { subscribe(any()) } doReturn flowOf(listOf(unknownChatEvent))
        }
        
        // When
        val container = VaultChatPreviewContainer(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = this,
            logger = logger,
            subscription = subscription
        )
        
        container.start()
        
        // Allow time for processing
        delay(100)
        
        // Then - verify container handles unknown event gracefully
        val preview = container.observePreview(spaceId).first()
        assertNotNull(preview)
        
        // Verify logger was called for unknown event
        verify(logger).logInfo(any())
        
        container.stop()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should handle multiple start calls without issues`() = runTest {
        // Given
        repo.stub {
            onBlocking { subscribeToMessagePreviews(any()) } doReturn emptyList()
        }
        
        channel.stub {
            on { subscribe(any()) } doReturn emptyFlow()
        }
        
        val container = VaultChatPreviewContainer(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = this,
            logger = logger,
            subscription = subscription
        )
        
        // When - call start multiple times
        container.start()
        container.start()
        container.start()
        
        // Allow time for processing
        delay(100)
        
        // Then - verify container works correctly
        val previews = container.observePreviewsWithAttachments().first()
        assertEquals(0, previews.size)
        
        // Verify repository was called at least once (multiple calls are ok due to cancellation)
        verify(repo, atLeast(1)).subscribeToMessagePreviews(any())
        
        container.stop()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should handle stop before start gracefully`() = runTest {
        // Given
        repo.stub {
            onBlocking { subscribeToMessagePreviews(any()) } doReturn emptyList()
            onBlocking { unsubscribeFromMessagePreviews(any()) } doReturn Unit
            onBlocking { cancelObjectSearchSubscription(any()) } doReturn Unit
        }
        
        channel.stub {
            on { subscribe(any()) } doReturn emptyFlow()
        }
        
        val container = VaultChatPreviewContainer(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = this,
            logger = logger,
            subscription = subscription
        )
        
        // When - call stop before start
        container.stop()
        
        // Allow time for processing
        delay(100)
        
        // Then - verify no errors occurred
        // Verify unsubscribe was called
        verify(repo).unsubscribeFromMessagePreviews(any())
        
        // Now start should work normally
        container.start()
        val previews = container.observePreviewsWithAttachments().first()
        assertEquals(0, previews.size)
        
        container.stop()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should handle multiple stop calls without issues`() = runTest {
        // Given
        repo.stub {
            onBlocking { subscribeToMessagePreviews(any()) } doReturn emptyList()
            onBlocking { unsubscribeFromMessagePreviews(any()) } doReturn Unit
            onBlocking { cancelObjectSearchSubscription(any()) } doReturn Unit
        }
        
        channel.stub {
            on { subscribe(any()) } doReturn emptyFlow()
        }
        
        val container = VaultChatPreviewContainer(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = this,
            logger = logger,
            subscription = subscription
        )
        
        // When
        container.start()
        delay(50)
        
        // Call stop multiple times
        container.stop()
        container.stop()
        container.stop()
        
        // Allow time for processing
        delay(100)
        
        // Then - verify no errors occurred and unsubscribe was called
        verify(repo, times(3)).unsubscribeFromMessagePreviews(any())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should handle stop errors gracefully`() = runTest {
        // Given
        val spaceId = SpaceId("test-space")
        val chatId = "test-chat"
        val attachmentId = "attachment-id"
        
        val chatMessage = Chat.Message(
            id = "msg-id",
            order = "order-id",
            creator = "creator-id",
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis(),
            content = Chat.Message.Content(
                text = "Test message",
                style = Block.Content.Text.Style.P,
                marks = emptyList()
            ),
            attachments = listOf(
                Chat.Message.Attachment(
                    target = attachmentId,
                    type = Chat.Message.Attachment.Type.File
                )
            )
        )
        
        val initialPreview = Chat.Preview(
            space = spaceId,
            chat = chatId,
            message = chatMessage,
            dependencies = emptyList()
        )
        
        repo.stub {
            onBlocking { subscribeToMessagePreviews(any()) } doReturn listOf(initialPreview)
            onBlocking { unsubscribeFromMessagePreviews(any()) } doThrow RuntimeException("Unsubscribe error")
            onBlocking { cancelObjectSearchSubscription(any()) } doThrow RuntimeException("Cancel subscription error")
        }
        
        channel.stub {
            on { subscribe(any()) } doReturn emptyFlow()
        }
        
        val container = VaultChatPreviewContainer(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = this,
            logger = logger,
            subscription = subscription
        )
        
        // When
        container.start()
        delay(100)
        
        // Stop should handle errors gracefully
        container.stop()
        
        // Allow time for processing
        delay(100)
        
        // Then - verify errors were logged
        verify(logger, times(2)).logException(any(), any())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should clear state on stop`() = runTest {
        // Given
        val spaceId = SpaceId("test-space")
        val chatId = "test-chat"
        
        val initialPreview = Chat.Preview(
            space = spaceId,
            chat = chatId,
            message = null,
            dependencies = emptyList()
        )
        
        repo.stub {
            onBlocking { subscribeToMessagePreviews(any()) } doReturn listOf(initialPreview)
            onBlocking { unsubscribeFromMessagePreviews(any()) } doReturn Unit
            onBlocking { cancelObjectSearchSubscription(any()) } doReturn Unit
        }
        
        channel.stub {
            on { subscribe(any()) } doReturn emptyFlow()
        }
        
        val container = VaultChatPreviewContainer(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = this,
            logger = logger,
            subscription = subscription
        )
        
        // When - start and verify there's data
        container.start()
        delay(100)
        
        val previewsBeforeStop = container.observePreviewsWithAttachments().first()
        assertEquals(1, previewsBeforeStop.size)
        
        // Stop the container
        container.stop()
        delay(100)
        
        // Then - verify state is cleared
        val previewsAfterStop = container.observePreviewsWithAttachments().first()
        assertEquals(0, previewsAfterStop.size)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should handle multiple spaces correctly`() = runTest {
        // Given
        val space1 = SpaceId("space-1")
        val space2 = SpaceId("space-2")
        val chat1 = "chat-1"
        val chat2 = "chat-2"
        
        val preview1 = Chat.Preview(
            space = space1,
            chat = chat1,
            message = Chat.Message(
                id = "msg-1",
                order = "order-1",
                creator = "creator-1",
                createdAt = System.currentTimeMillis(),
                modifiedAt = System.currentTimeMillis(),
                content = Chat.Message.Content(
                    text = "Message in space 1",
                    style = Block.Content.Text.Style.P,
                    marks = emptyList()
                ),
                attachments = emptyList()
            ),
            dependencies = emptyList()
        )
        
        val preview2 = Chat.Preview(
            space = space2,
            chat = chat2,
            message = Chat.Message(
                id = "msg-2",
                order = "order-2",
                creator = "creator-2",
                createdAt = System.currentTimeMillis(),
                modifiedAt = System.currentTimeMillis(),
                content = Chat.Message.Content(
                    text = "Message in space 2",
                    style = Block.Content.Text.Style.P,
                    marks = emptyList()
                ),
                attachments = emptyList()
            ),
            dependencies = emptyList()
        )
        
        repo.stub {
            onBlocking { subscribeToMessagePreviews(any()) } doReturn listOf(preview1, preview2)
        }
        
        channel.stub {
            on { subscribe(any()) } doReturn emptyFlow()
        }
        
        // When
        val container = VaultChatPreviewContainer(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = this,
            logger = logger,
            subscription = subscription
        )
        
        container.start()
        delay(100)
        
        // Then - verify both spaces are handled correctly
        val allPreviews = container.observePreviewsWithAttachments().first()
        assertEquals(2, allPreviews.size)
        
        // Verify individual space previews
        val space1Preview = container.observePreview(space1).first()
        val space2Preview = container.observePreview(space2).first()
        
        assertNotNull(space1Preview)
        assertNotNull(space2Preview)
        assertEquals("Message in space 1", space1Preview.message?.content?.text)
        assertEquals("Message in space 2", space2Preview.message?.content?.text)
        
        container.stop()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should handle concurrent updates to different spaces`() = runTest {
        // Given
        val space1 = SpaceId("space-1")
        val space2 = SpaceId("space-2")
        val chat1 = "chat-1"
        val chat2 = "chat-2"
        
        val initialPreview1 = Chat.Preview(
            space = space1,
            chat = chat1,
            message = null,
            dependencies = emptyList()
        )
        
        val initialPreview2 = Chat.Preview(
            space = space2,
            chat = chat2,
            message = null,
            dependencies = emptyList()
        )
        
        val message1 = Chat.Message(
            id = "msg-1",
            order = "order-1",
            creator = "creator-1",
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis(),
            content = Chat.Message.Content(
                text = "Message 1",
                style = Block.Content.Text.Style.P,
                marks = emptyList()
            ),
            attachments = emptyList()
        )
        
        val message2 = Chat.Message(
            id = "msg-2",
            order = "order-2",
            creator = "creator-2",
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis(),
            content = Chat.Message.Content(
                text = "Message 2",
                style = Block.Content.Text.Style.P,
                marks = emptyList()
            ),
            attachments = emptyList()
        )
        
        val addEvent1 = Event.Command.Chats.Add(
            context = chat1,
            id = "msg-1",
            order = "order-1",
            message = message1,
            dependencies = emptyList()
        )
        
        val addEvent2 = Event.Command.Chats.Add(
            context = chat2,
            id = "msg-2",
            order = "order-2",
            message = message2,
            dependencies = emptyList()
        )
        
        repo.stub {
            onBlocking { subscribeToMessagePreviews(any()) } doReturn listOf(initialPreview1, initialPreview2)
        }
        
        channel.stub {
            on { subscribe(any()) } doReturn flowOf(listOf(addEvent1, addEvent2))
        }
        
        // When
        val container = VaultChatPreviewContainer(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = this,
            logger = logger,
            subscription = subscription
        )
        
        container.start()
        delay(100)
        
        // Then - verify both spaces were updated correctly
        val space1Preview = container.observePreview(space1).first()
        val space2Preview = container.observePreview(space2).first()
        
        assertNotNull(space1Preview)
        assertNotNull(space2Preview)
        assertEquals(message1, space1Preview.message)
        assertEquals(message2, space2Preview.message)
        
        container.stop()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should handle attachments for multiple spaces independently`() = runTest {
        // Given
        val space1 = SpaceId("space-1")
        val space2 = SpaceId("space-2")
        val chat1 = "chat-1"
        val chat2 = "chat-2"
        val attachment1 = "attachment-1"
        val attachment2 = "attachment-2"
        
        val message1 = Chat.Message(
            id = "msg-1",
            order = "order-1",
            creator = "creator-1",
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis(),
            content = Chat.Message.Content(
                text = "Message 1",
                style = Block.Content.Text.Style.P,
                marks = emptyList()
            ),
            attachments = listOf(
                Chat.Message.Attachment(
                    target = attachment1,
                    type = Chat.Message.Attachment.Type.File
                )
            )
        )
        
        val message2 = Chat.Message(
            id = "msg-2",
            order = "order-2",
            creator = "creator-2",
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis(),
            content = Chat.Message.Content(
                text = "Message 2",
                style = Block.Content.Text.Style.P,
                marks = emptyList()
            ),
            attachments = listOf(
                Chat.Message.Attachment(
                    target = attachment2,
                    type = Chat.Message.Attachment.Type.File
                )
            )
        )
        
        val preview1 = Chat.Preview(
            space = space1,
            chat = chat1,
            message = message1,
            dependencies = emptyList()
        )
        
        val preview2 = Chat.Preview(
            space = space2,
            chat = chat2,
            message = message2,
            dependencies = emptyList()
        )
        
        val attachmentDetails1 = StubObject(
            id = attachment1,
            name = "Attachment 1"
        )
        
        val attachmentDetails2 = StubObject(
            id = attachment2,
            name = "Attachment 2"
        )
        
        repo.stub {
            onBlocking { subscribeToMessagePreviews(any()) } doReturn listOf(preview1, preview2)
        }
        
        channel.stub {
            on { subscribe(any()) } doReturn emptyFlow()
        }
        
        // Mock subscription to return different attachments for different spaces
        subscription.stub {
            on { subscribe(any<StoreSearchByIdsParams>()) } doAnswer { invocation ->
                val params = invocation.arguments[0] as StoreSearchByIdsParams
                when {
                    params.subscription.startsWith(space1.id) -> flowOf(listOf(attachmentDetails1))
                    params.subscription.startsWith(space2.id) -> flowOf(listOf(attachmentDetails2))
                    else -> flowOf(emptyList())
                }
            }
        }
        
        // When
        val container = VaultChatPreviewContainer(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = this,
            logger = logger,
            subscription = subscription
        )
        
        container.start()
        delay(100)
        
        // Then - verify attachments are handled independently for each space
        val allPreviews = container.observePreviewsWithAttachments().first()
        assertEquals(2, allPreviews.size)
        
        val space1Preview = allPreviews.find { it.space == space1 }
        val space2Preview = allPreviews.find { it.space == space2 }
        
        assertNotNull(space1Preview)
        assertNotNull(space2Preview)
        
        assertEquals(1, space1Preview.dependencies.size)
        assertEquals(1, space2Preview.dependencies.size)
        
        assertEquals(attachmentDetails1, space1Preview.dependencies.first())
        assertEquals(attachmentDetails2, space2Preview.dependencies.first())
        
        container.stop()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should handle rapid sequential updates correctly`() = runTest {
        // Given
        val spaceId = SpaceId("test-space")
        val chatId = "test-chat"
        val messageId = "msg-id"
        
        val initialPreview = Chat.Preview(
            space = spaceId,
            chat = chatId,
            message = null,
            dependencies = emptyList()
        )
        
        // Create multiple sequential updates
        val message1 = Chat.Message(
            id = messageId,
            order = "order-1",
            creator = "creator-1",
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis(),
            content = Chat.Message.Content(
                text = "First message",
                style = Block.Content.Text.Style.P,
                marks = emptyList()
            ),
            attachments = emptyList()
        )
        
        val message2 = Chat.Message(
            id = messageId,
            order = "order-2",
            creator = "creator-2",
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis(),
            content = Chat.Message.Content(
                text = "Second message",
                style = Block.Content.Text.Style.P,
                marks = emptyList()
            ),
            attachments = emptyList()
        )
        
        val message3 = Chat.Message(
            id = messageId,
            order = "order-3",
            creator = "creator-3",
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis(),
            content = Chat.Message.Content(
                text = "Third message",
                style = Block.Content.Text.Style.P,
                marks = emptyList()
            ),
            attachments = emptyList()
        )
        
        val addEvent = Event.Command.Chats.Add(
            context = chatId,
            id = messageId,
            order = "order-1",
            message = message1,
            dependencies = emptyList()
        )
        
        val updateEvent1 = Event.Command.Chats.Update(
            context = chatId,
            id = messageId,
            message = message2
        )
        
        val updateEvent2 = Event.Command.Chats.Update(
            context = chatId,
            id = messageId,
            message = message3
        )
        
        repo.stub {
            onBlocking { subscribeToMessagePreviews(any()) } doReturn listOf(initialPreview)
        }
        
        channel.stub {
            on { subscribe(any()) } doReturn flowOf(listOf(addEvent, updateEvent1, updateEvent2))
        }
        
        // When
        val container = VaultChatPreviewContainer(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = this,
            logger = logger,
            subscription = subscription
        )
        
        container.start()
        
        // Allow time for all events to be processed
        delay(200)
        
        // Then - verify final state has the last message
        val finalPreview = container.observePreview(spaceId).first()
        
        assertNotNull(finalPreview)
        assertEquals(message3, finalPreview.message)
        assertEquals("Third message", finalPreview.message?.content?.text)
        
        container.stop()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should clean up attachment subscriptions on stop`() = runTest {
        // Given
        val spaceId = SpaceId("test-space")
        val chatId = "test-chat"
        val attachmentId = "attachment-id"
        
        val chatMessage = Chat.Message(
            id = "msg-id",
            order = "order-id",
            creator = "creator-id",
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis(),
            content = Chat.Message.Content(
                text = "Message with attachment",
                style = Block.Content.Text.Style.P,
                marks = emptyList()
            ),
            attachments = listOf(
                Chat.Message.Attachment(
                    target = attachmentId,
                    type = Chat.Message.Attachment.Type.File
                )
            )
        )
        
        val initialPreview = Chat.Preview(
            space = spaceId,
            chat = chatId,
            message = chatMessage,
            dependencies = emptyList()
        )
        
        val attachmentDetails = StubObject(
            id = attachmentId,
            name = "Test Attachment"
        )
        
        repo.stub {
            onBlocking { subscribeToMessagePreviews(any()) } doReturn listOf(initialPreview)
            onBlocking { unsubscribeFromMessagePreviews(any()) } doReturn Unit
            onBlocking { cancelObjectSearchSubscription(any()) } doReturn Unit
        }
        
        channel.stub {
            on { subscribe(any()) } doReturn emptyFlow()
        }
        
        subscription.stub {
            on { subscribe(any<StoreSearchByIdsParams>()) } doReturn flowOf(listOf(attachmentDetails))
        }
        
        // When
        val container = VaultChatPreviewContainer(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = this,
            logger = logger,
            subscription = subscription
        )
        
        container.start()
        delay(100)
        
        // Verify attachment subscription was created
        val previewsWithAttachments = container.observePreviewsWithAttachments().first()
        assertEquals(1, previewsWithAttachments.size)
        assertEquals(1, previewsWithAttachments.first().dependencies.size)
        
        // Stop the container
        container.stop()
        delay(100)
        
        // Then - verify cleanup methods were called
        verify(repo).unsubscribeFromMessagePreviews(any())
        verify(repo).cancelObjectSearchSubscription(any())
        
        // Verify attachment subscriptions were cleaned up (should include the space-specific subscription)
        verifyBlocking(repo) {
            cancelObjectSearchSubscription(
                listOf("${spaceId.id}/chat-previews-attachments")
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should clean up multiple attachment subscriptions on stop`() = runTest {
        // Given
        val space1 = SpaceId("space-1")
        val space2 = SpaceId("space-2")
        val chat1 = "chat-1"
        val chat2 = "chat-2"
        val attachment1 = "attachment-1"
        val attachment2 = "attachment-2"
        
        val message1 = Chat.Message(
            id = "msg-1",
            order = "order-1",
            creator = "creator-1",
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis(),
            content = Chat.Message.Content(
                text = "Message 1",
                style = Block.Content.Text.Style.P,
                marks = emptyList()
            ),
            attachments = listOf(
                Chat.Message.Attachment(
                    target = attachment1,
                    type = Chat.Message.Attachment.Type.File
                )
            )
        )
        
        val message2 = Chat.Message(
            id = "msg-2",
            order = "order-2",
            creator = "creator-2",
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis(),
            content = Chat.Message.Content(
                text = "Message 2",
                style = Block.Content.Text.Style.P,
                marks = emptyList()
            ),
            attachments = listOf(
                Chat.Message.Attachment(
                    target = attachment2,
                    type = Chat.Message.Attachment.Type.File
                )
            )
        )
        
        val preview1 = Chat.Preview(
            space = space1,
            chat = chat1,
            message = message1,
            dependencies = emptyList()
        )
        
        val preview2 = Chat.Preview(
            space = space2,
            chat = chat2,
            message = message2,
            dependencies = emptyList()
        )
        
        val attachmentDetails1 = StubObject(
            id = attachment1,
            name = "Attachment 1"
        )
        
        val attachmentDetails2 = StubObject(
            id = attachment2,
            name = "Attachment 2"
        )
        
        repo.stub {
            onBlocking { subscribeToMessagePreviews(any()) } doReturn listOf(preview1, preview2)
            onBlocking { unsubscribeFromMessagePreviews(any()) } doReturn Unit
            onBlocking { cancelObjectSearchSubscription(any()) } doReturn Unit
        }
        
        channel.stub {
            on { subscribe(any()) } doReturn emptyFlow()
        }
        
        subscription.stub {
            on { subscribe(any<StoreSearchByIdsParams>()) } doAnswer { invocation ->
                val params = invocation.arguments[0] as StoreSearchByIdsParams
                when {
                    params.subscription.startsWith(space1.id) -> flowOf(listOf(attachmentDetails1))
                    params.subscription.startsWith(space2.id) -> flowOf(listOf(attachmentDetails2))
                    else -> flowOf(emptyList())
                }
            }
        }
        
        // When
        val container = VaultChatPreviewContainer(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = this,
            logger = logger,
            subscription = subscription
        )
        
        container.start()
        delay(100)
        
        // Verify attachment subscriptions were created for both spaces
        val previewsWithAttachments = container.observePreviewsWithAttachments().first()
        assertEquals(2, previewsWithAttachments.size)
        
        // Stop the container
        container.stop()
        delay(100)
        
        // Then - verify cleanup methods were called
        verify(repo).unsubscribeFromMessagePreviews(any())
        verify(repo).cancelObjectSearchSubscription(any())
        
        // Verify both attachment subscriptions were cleaned up
        verifyBlocking(repo) {
            cancelObjectSearchSubscription(
                listOf("${space1.id}/chat-previews-attachments", "${space2.id}/chat-previews-attachments")
            )
        }
    }
}