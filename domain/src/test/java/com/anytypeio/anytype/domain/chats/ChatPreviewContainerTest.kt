package com.anytypeio.anytype.domain.chats

import app.cash.turbine.turbineScope
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.StubObject
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.core_models.primitives.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.common.DefaultCoroutineTestRule
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.library.StoreSearchByIdsParams
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.delay
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
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
        val container = ChatPreviewContainer.Default(
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
            val container = ChatPreviewContainer.Default(
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
        val container = ChatPreviewContainer.Default(
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
        val container: ChatPreviewContainer = ChatPreviewContainer.Default(
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
        val container = ChatPreviewContainer.Default(
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
        val container = ChatPreviewContainer.Default(
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
        val container = ChatPreviewContainer.Default(
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

}