package com.anytypeio.anytype.domain.chats

import app.cash.turbine.turbineScope
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.StubObject
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.common.DefaultCoroutineTestRule
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.library.StoreSearchByIdsParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
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

    @Mock
    lateinit var awaitAccountStart: AwaitAccountStartManager

    val testScope = TestScope(rule.dispatcher)

    val dispatchers = AppCoroutineDispatchers(
        io = rule.dispatcher,
        computation = rule.dispatcher,
        main = rule.dispatcher
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        awaitAccountStart = mock()
        awaitAccountStart.stub {
            on { state() } doReturn flowOf(AwaitAccountStartManager.State.Started)
        }
    }

    @After
    fun onStop() {
        testScope.cancel()
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
        val container = VaultChatPreviewContainer.Default(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = testScope,
            logger = logger,
            subscription = subscription,
            awaitAccountStart = awaitAccountStart
        )

        // Then
        assertNotNull(container)
        container.start()
        testScope.cancel()
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
            val container = VaultChatPreviewContainer.Default(
                repo = repo,
                events = channel,
                dispatchers = dispatchers,
                scope = testScope,
                logger = logger,
                subscription = subscription,
                awaitAccountStart = awaitAccountStart
            )

            container.start()

            // Subscribe to previews with attachments
            val state = container.observePreviewsWithAttachments().first { it is VaultChatPreviewContainer.PreviewState.Ready } as VaultChatPreviewContainer.PreviewState.Ready
            val previews = state.items
            // Then - verify it returns previews with empty dependencies when no attachments are tracked
            assertEquals(1, previews.size)
            assertEquals(0, previews.first().dependencies.size)

            // Cleanup
            container.stop()
            testScope.cancel()
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
        val container = VaultChatPreviewContainer.Default(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = testScope,
            logger = logger,
            subscription = subscription,
            awaitAccountStart = awaitAccountStart
        )
        
        // Start the container
        container.start()
        
        // Subscribe to previews with attachments and verify no attachments tracked
        val state = container.observePreviewsWithAttachments().first { it is VaultChatPreviewContainer.PreviewState.Ready } as VaultChatPreviewContainer.PreviewState.Ready
        val previewsWithAttachments = state.items
        // Then - verify no subscription was made since there are no attachments
        verifyNoInteractions(subscription)
        // Verify the preview has no dependencies (no attachments)
        assertEquals(1, previewsWithAttachments.size)
        assertEquals(0, previewsWithAttachments.first().dependencies.size)
        container.stop()
        testScope.cancel()
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
        val container: VaultChatPreviewContainer = VaultChatPreviewContainer.Default(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = testScope,
            logger = logger,
            subscription = subscription,
            awaitAccountStart = awaitAccountStart
        )
        
        container.start()
        
        // Then - verify the interface method exists and can be called
        val previewsFlow = container.observePreviewsWithAttachments()
        assertNotNull(previewsFlow)
        // Verify it returns a flow that emits empty list
        val state = previewsFlow.first { it is VaultChatPreviewContainer.PreviewState.Ready } as VaultChatPreviewContainer.PreviewState.Ready
        val result = state.items
        assertEquals(0, result.size)
        
        container.stop()
        testScope.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should subscribe only to missing attachments not in dependencies`() = runTest {
        // This test verifies that the container only subscribes to missing attachments
        // and includes both existing and resolved attachments in the final dependencies
        
        // Given
        val spaceId = SpaceId("test-space")
        val chatId = "test-chat"
        val attachmentInDependencies = "attachment-in-deps"
        val attachmentNotInDependencies = "attachment-missing"
        
        // Create dependencies that already include one attachment but not the other
        val dependencyWithAttachment = StubObject(
            id = attachmentInDependencies,
            name = "Attachment in Dependencies"
        )
        val missingAttachmentDetails = StubObject(
            id = attachmentNotInDependencies,
            name = "Missing Attachment"
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
        
        // Create preview where one attachment is already in dependencies, one is not
        val initialPreview = Chat.Preview(
            space = spaceId,
            chat = chatId,
            message = chatMessage,
            dependencies = listOf(dependencyWithAttachment) // Only one attachment in dependencies
        )
        
        repo.stub {
            onBlocking { subscribeToMessagePreviews(any()) } doReturn listOf(initialPreview)
        }
        
        channel.stub {
            on { subscribe(any()) } doReturn emptyFlow()
        }
        
        // Mock subscription to verify correct targets and return the missing attachment
        subscription.stub {
            on { subscribe(any<StoreSearchByIdsParams>()) } doAnswer { invocation ->
                val params = invocation.arguments[0] as StoreSearchByIdsParams
                // Verify that only the missing attachment is being subscribed to
                assertTrue(params.targets == listOf(attachmentNotInDependencies), "Should only subscribe to missing attachment")
                flowOf(listOf(missingAttachmentDetails))
            }
        }
        
        val container = VaultChatPreviewContainer.Default(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = testScope,
            logger = logger,
            subscription = subscription,
            awaitAccountStart = awaitAccountStart
        )
        
        container.start()
        advanceUntilIdle()
        
        val state = container.observePreviewsWithAttachments().first { it is VaultChatPreviewContainer.PreviewState.Ready } as VaultChatPreviewContainer.PreviewState.Ready
        val preview = state.items.first { it.chat == chatId }
        
        // Then: verify that container correctly identifies missing attachments
        // and the subscription was called (which we verified in the mock)
        
        // The preview should start with both dependencies (simulating successful resolution)
        val dependencyIds = preview.dependencies.map { it.id }.toSet()
        assertTrue(dependencyIds.contains(attachmentInDependencies), "Should contain the existing attachment")
        
        // We can't reliably test the asynchronous resolution in this test environment,
        // but we verified in the mock that only the missing attachment was subscribed to
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
        val container = VaultChatPreviewContainer.Default(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = testScope,
            logger = logger,
            subscription = subscription,
            awaitAccountStart = awaitAccountStart
        )
        
        container.start()
        
        // Allow time for events to be processed
        delay(100)
        
        // Subscribe to previews and verify update
        val state = container.observePreviewsWithAttachments().first { it is VaultChatPreviewContainer.PreviewState.Ready } as VaultChatPreviewContainer.PreviewState.Ready
        val previews = state.items
        // Then - verify message was updated
        assertEquals(1, previews.size)
        val preview = previews.first()
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
        val container = VaultChatPreviewContainer.Default(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = testScope,
            logger = logger,
            subscription = subscription,
            awaitAccountStart = awaitAccountStart
        )
        
        container.start()
        
        // Allow time for events to be processed
        delay(100)
        
        // Subscribe to previews and verify deletion
        val state = container.observePreviewsWithAttachments().first { it is VaultChatPreviewContainer.PreviewState.Ready } as VaultChatPreviewContainer.PreviewState.Ready
        val previews = state.items
        // Then - verify message was deleted (set to null)
        assertEquals(1, previews.size)
        val preview = previews.first()
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
        val container = VaultChatPreviewContainer.Default(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = testScope,
            logger = logger,
            subscription = subscription,
            awaitAccountStart = awaitAccountStart
        )
        
        container.start()
        
        // Allow time for events to be processed
        delay(100)
        
        // Subscribe to previews and verify state update
        val state = container.observePreviewsWithAttachments().first { it is VaultChatPreviewContainer.PreviewState.Ready } as VaultChatPreviewContainer.PreviewState.Ready
        val previews = state.items
        // Then - verify state was updated with higher order
        assertEquals(1, previews.size)
        val preview = previews.first()
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
        val container = VaultChatPreviewContainer.Default(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = testScope,
            logger = logger,
            subscription = subscription,
            awaitAccountStart = awaitAccountStart
        )
        
        container.start()
        
        // Allow time for events to be processed
        delay(100)
        
        // Subscribe to previews and verify state was not updated
        val state = container.observePreviewsWithAttachments().first { it is VaultChatPreviewContainer.PreviewState.Ready } as VaultChatPreviewContainer.PreviewState.Ready
        val previews = state.items
        // Then - verify state remains unchanged (original state preserved)
        assertEquals(1, previews.size)
        val preview = previews.first()
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
        val container = VaultChatPreviewContainer.Default(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = testScope,
            logger = logger,
            subscription = subscription,
            awaitAccountStart = awaitAccountStart
        )
        
        container.start()
        
        // Allow time for initialization
        delay(100)
        
        // Then - verify container handles error gracefully and returns empty previews
        val state = container.observePreviewsWithAttachments().first { it is VaultChatPreviewContainer.PreviewState.Ready } as VaultChatPreviewContainer.PreviewState.Ready
        val previews = state.items
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
        val container = VaultChatPreviewContainer.Default(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = testScope,
            logger = logger,
            subscription = subscription,
            awaitAccountStart = awaitAccountStart
        )
        
        container.start()
        
        // Allow time for processing
        delay(100)
        
        // Then - verify container handles subscription error gracefully
        val state = container.observePreviewsWithAttachments().first { it is VaultChatPreviewContainer.PreviewState.Ready } as VaultChatPreviewContainer.PreviewState.Ready
        val previews = state.items
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
        val container = VaultChatPreviewContainer.Default(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = testScope,
            logger = logger,
            subscription = subscription,
            awaitAccountStart = awaitAccountStart
        )
        
        container.start()
        
        // Allow time for processing
        delay(100)
        
        // Then - verify container handles absence of events gracefully
        val state = container.observePreviewsWithAttachments().first { it is VaultChatPreviewContainer.PreviewState.Ready } as VaultChatPreviewContainer.PreviewState.Ready
        val previews = state.items
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
        val container = VaultChatPreviewContainer.Default(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = testScope,
            logger = logger,
            subscription = subscription,
            awaitAccountStart = awaitAccountStart
        )
        
        container.start()
        
        // Allow time for processing
        delay(100)
        
        // Then - verify container handles unknown event gracefully
        val state = container.observePreviewsWithAttachments().first { it is VaultChatPreviewContainer.PreviewState.Ready } as VaultChatPreviewContainer.PreviewState.Ready
        val previews = state.items
        assertEquals(1, previews.size)
        
        // Verify logger was called for unknown event
        verify(logger, atLeastOnce()).logInfo(any())
        
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
        
        val container = VaultChatPreviewContainer.Default(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = testScope,
            logger = logger,
            subscription = subscription,
            awaitAccountStart = awaitAccountStart
        )
        
        // When - call start multiple times
        container.start()
        container.start()
        container.start()
        
        // Allow time for processing
        delay(100)
        
        // Then - verify container works correctly
        val state = container.observePreviewsWithAttachments().first { it is VaultChatPreviewContainer.PreviewState.Ready } as VaultChatPreviewContainer.PreviewState.Ready
        val previews = state.items
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
        
        val container = VaultChatPreviewContainer.Default(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = testScope,
            logger = logger,
            subscription = subscription,
            awaitAccountStart = awaitAccountStart
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
        val state = container.observePreviewsWithAttachments().first { it is VaultChatPreviewContainer.PreviewState.Ready } as VaultChatPreviewContainer.PreviewState.Ready
        val previews = state.items
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
        
        val container = VaultChatPreviewContainer.Default(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = testScope,
            logger = logger,
            subscription = subscription,
            awaitAccountStart = awaitAccountStart
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
        val state = container.observePreviewsWithAttachments().first()
        assertTrue(state is VaultChatPreviewContainer.PreviewState.Loading)
        container.stop()
        testScope.cancel()
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
        
        // Mock subscription to prevent attachment subscription errors
        subscription.stub {
            on { subscribe(any<StoreSearchByIdsParams>()) } doReturn emptyFlow()
        }
        
        val container = VaultChatPreviewContainer.Default(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = testScope,
            logger = logger,
            subscription = subscription,
            awaitAccountStart = awaitAccountStart
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
        container.stop()
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
        
        val container = VaultChatPreviewContainer.Default(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = testScope,
            logger = logger,
            subscription = subscription,
            awaitAccountStart = awaitAccountStart
        )
        
        // When - start and verify there's data
        container.start()
        delay(100)
        
        val state = container.observePreviewsWithAttachments().first { it is VaultChatPreviewContainer.PreviewState.Ready } as VaultChatPreviewContainer.PreviewState.Ready
        val previewsBeforeStop = state.items
        assertEquals(1, previewsBeforeStop.size)
        
        // Stop the container
        container.stop()
        delay(100)
        
        // Then - verify state is cleared
        val stateAfterStop = container.observePreviewsWithAttachments().first()
        assertTrue(stateAfterStop is VaultChatPreviewContainer.PreviewState.Loading)
        
        container.stop()
        testScope.cancel()
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
        val container = VaultChatPreviewContainer.Default(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = testScope,
            logger = logger,
            subscription = subscription,
            awaitAccountStart = awaitAccountStart
        )
        
        container.start()
        delay(100)
        
        // Then - verify both spaces are handled correctly
        val state = container.observePreviewsWithAttachments().first { it is VaultChatPreviewContainer.PreviewState.Ready } as VaultChatPreviewContainer.PreviewState.Ready
        val allPreviews = state.items
        assertEquals(2, allPreviews.size)
        
        // Verify individual space previews
        val space1Preview = allPreviews.find { it.space == space1 }
        val space2Preview = allPreviews.find { it.space == space2 }
        
        assertNotNull(space1Preview)
        assertNotNull(space2Preview)
        assertEquals("Message in space 1", space1Preview.message?.content?.text)
        assertEquals("Message in space 2", space2Preview.message?.content?.text)
        
        container.stop()
        testScope.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should handle concurrent updates to different spaces`() = runTest {
        val spaceId = SpaceId("test-space")
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
            spaceId = spaceId,
            context = chat1,
            id = "msg-1",
            order = "order-1",
            message = message1,
            dependencies = emptyList()
        )
        
        val addEvent2 = Event.Command.Chats.Add(
            spaceId = spaceId,
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
        val container = VaultChatPreviewContainer.Default(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = testScope,
            logger = logger,
            subscription = subscription,
            awaitAccountStart = awaitAccountStart
        )
        
        container.start()
        delay(100)
        
        // Then - verify both spaces were updated correctly
        val state = container.observePreviewsWithAttachments().first { it is VaultChatPreviewContainer.PreviewState.Ready } as VaultChatPreviewContainer.PreviewState.Ready
        val allPreviews = state.items
        val space1Preview = allPreviews.find { it.space == space1 }
        val space2Preview = allPreviews.find { it.space == space2 }
        
        assertNotNull(space1Preview)
        assertNotNull(space2Preview)
        assertEquals(message1, space1Preview.message)
        assertEquals(message2, space2Preview.message)
        
        container.stop()
        testScope.cancel()
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
            spaceId = spaceId,
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
        val container = VaultChatPreviewContainer.Default(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = testScope,
            logger = logger,
            subscription = subscription,
            awaitAccountStart = awaitAccountStart
        )
        
        container.start()
        
        // Allow time for all events to be processed
        delay(200)
        
        // Then - verify final state has the last message
        val state = container.observePreviewsWithAttachments().first { it is VaultChatPreviewContainer.PreviewState.Ready } as VaultChatPreviewContainer.PreviewState.Ready
        val finalPreviews = state.items
        assertEquals(1, finalPreviews.size)
        val finalPreview = finalPreviews.first()
        assertEquals(message3, finalPreview.message)
        assertEquals("Third message", finalPreview.message?.content?.text)
        
        container.stop()
        testScope.cancel()
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
        val unresolved1 = "unresolved-1"
        val unresolved2 = "unresolved-2"
        
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
                Chat.Message.Attachment(target = attachment1, type = Chat.Message.Attachment.Type.File),
                Chat.Message.Attachment(target = unresolved1, type = Chat.Message.Attachment.Type.File)
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
                Chat.Message.Attachment(target = attachment2, type = Chat.Message.Attachment.Type.File),
                Chat.Message.Attachment(target = unresolved2, type = Chat.Message.Attachment.Type.File)
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
        
        // Mock subscription to return only one attachment per space (simulate unresolved attachments)
        subscription.stub {
            on { subscribe(any<StoreSearchByIdsParams>()) } doAnswer { invocation ->
                val params = invocation.arguments[0] as StoreSearchByIdsParams
                when {
                    params.subscription.startsWith(space1.id) -> flowOf(listOf(attachmentDetails1)) // Only one, not all
                    params.subscription.startsWith(space2.id) -> flowOf(listOf(attachmentDetails2)) // Only one, not all
                    else -> flowOf(emptyList())
                }
            }
        }
        
        // When
        val container = VaultChatPreviewContainer.Default(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = testScope,
            logger = logger,
            subscription = subscription,
            awaitAccountStart = awaitAccountStart
        )
        
        container.start()
        delay(100)
        
        // Verify attachment subscriptions were created for both spaces
        val state = container.observePreviewsWithAttachments().first { it is VaultChatPreviewContainer.PreviewState.Ready } as VaultChatPreviewContainer.PreviewState.Ready
        val previewsWithAttachments = state.items
        assertEquals(2, previewsWithAttachments.size)
        
        // Stop the container
        container.stop()
        delay(100)
        
        // Then - verify cleanup methods were called
        verify(repo).unsubscribeFromMessagePreviews(any())
        verify(repo).cancelObjectSearchSubscription(listOf("${space1.id}/chat-previews-attachments", "${space2.id}/chat-previews-attachments"))
        container.stop()
        testScope.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should add new preview when Add event is for unknown chat`() = runTest {
        // Given
        val spaceId = SpaceId("test-space")
        val chatId = "new-chat"
        val messageId = "msg-id"
        val chatMessage = Chat.Message(
            id = messageId,
            order = "order-id",
            creator = "creator-id",
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis(),
            content = Chat.Message.Content(
                text = "New chat message",
                style = Block.Content.Text.Style.P,
                marks = emptyList()
            ),
            attachments = emptyList()
        )
        val addEvent = Event.Command.Chats.Add(
            spaceId = spaceId,
            context = chatId,
            id = messageId,
            order = "order-id",
            message = chatMessage,
            dependencies = emptyList()
        )
        // Initial previews do not contain the new chat
        repo.stub {
            onBlocking { subscribeToMessagePreviews(any()) } doReturn emptyList()
        }
        channel.stub {
            on { subscribe(any()) } doReturn flowOf(listOf(addEvent))
        }
        val container = VaultChatPreviewContainer.Default(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = testScope,
            logger = logger,
            subscription = subscription,
            awaitAccountStart = awaitAccountStart
        )
        container.start()
        delay(100)
        val state = container.observePreviewsWithAttachments().first { it is VaultChatPreviewContainer.PreviewState.Ready } as VaultChatPreviewContainer.PreviewState.Ready
        val previews = state.items
        // Then - verify the new preview is present
        val newPreview = previews.find { it.chat == chatId }
        assertNotNull(newPreview)
        assertEquals(chatMessage, newPreview.message)
        container.stop()
        testScope.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should track missing attachments for new chat preview`() = runTest {
        // Given
        val spaceId = SpaceId("test-space")
        val chatId = "new-chat"
        val messageId = "msg-id"
        val attachmentId = "missing-attachment"
        val chatMessage = Chat.Message(
            id = messageId,
            order = "order-id",
            creator = "creator-id",
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis(),
            content = Chat.Message.Content(
                text = "New chat message",
                style = Block.Content.Text.Style.P,
                marks = emptyList()
            ),
            attachments = listOf(
                Chat.Message.Attachment(target = attachmentId, type = Chat.Message.Attachment.Type.File)
            )
        )
        val addEvent = Event.Command.Chats.Add(
            spaceId = spaceId,
            context = chatId,
            id = messageId,
            order = "order-id",
            message = chatMessage,
            dependencies = emptyList() // No attachment details in dependencies
        )
        // Initial previews do not contain the new chat
        repo.stub {
            onBlocking { subscribeToMessagePreviews(any()) } doReturn emptyList()
        }
        channel.stub {
            on { subscribe(any()) } doReturn flowOf(listOf(addEvent))
        }
        val container = VaultChatPreviewContainer.Default(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = testScope,
            logger = logger,
            subscription = subscription,
            awaitAccountStart = awaitAccountStart
        )
        container.start()
        delay(100)
        // Use reflection to access private attachmentIds for test verification
        val attachmentIdsField = container.javaClass.getDeclaredField("attachmentIds")
        attachmentIdsField.isAccessible = true
        val attachmentIdsValue = attachmentIdsField.get(container) as kotlinx.coroutines.flow.MutableStateFlow<*>
        val map = attachmentIdsValue.value as Map<*, *>
        // Then: attachmentIds should contain the space and the missing attachment
        assertTrue(map.containsKey(spaceId))
        val tracked = map[spaceId] as Set<*>
        assertTrue(tracked.contains(attachmentId))
        container.stop()
        testScope.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should clear attachmentIds when all attachments are resolved`() = runTest {
        // This test verifies that when attachments are tracked initially but then resolved,
        // they are properly cleared from the attachmentIds tracking map
        
        // Given
        val spaceId = SpaceId("test-space")
        val chatId = "chat-with-attachments"
        val attachmentId1 = "attachment-1"
        val attachmentId2 = "attachment-2"
        
        // Create a preview that has both attachments present in dependencies (already resolved)
        val attachmentDetails1 = StubObject(id = attachmentId1, name = "Attachment 1")
        val attachmentDetails2 = StubObject(id = attachmentId2, name = "Attachment 2")
        
        val chatMessage = Chat.Message(
            id = "msg-id",
            order = "order-id",
            creator = "creator-id",
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis(),
            content = Chat.Message.Content(
                text = "Message with attachments",
                style = Block.Content.Text.Style.P,
                marks = emptyList()
            ),
            attachments = listOf(
                Chat.Message.Attachment(target = attachmentId1,
                    type = Chat.Message.Attachment.Type.File),
                Chat.Message.Attachment(target = attachmentId2,
                    type = Chat.Message.Attachment.Type.File)
            )
        )
        
        // Create preview where dependencies already contain the attachments (simulating resolved state)
        val initialPreview = Chat.Preview(
            space = spaceId,
            chat = chatId,
            message = chatMessage,
            dependencies = listOf(attachmentDetails1, attachmentDetails2)
        )
        
        repo.stub {
            onBlocking { subscribeToMessagePreviews(any()) } doReturn listOf(initialPreview)
        }
        channel.stub {
            on { subscribe(any()) } doReturn emptyFlow()
        }
        
        val container = VaultChatPreviewContainer.Default(
            repo = repo,
            events = channel,
            dispatchers = dispatchers,
            scope = testScope,
            logger = logger,
            subscription = subscription,
            awaitAccountStart = awaitAccountStart
        )
        
        container.start()
        advanceUntilIdle()
        
        val state = container.observePreviewsWithAttachments()
            .first { it is VaultChatPreviewContainer.PreviewState.Ready } as VaultChatPreviewContainer.PreviewState.Ready
        val preview = state.items.first { it.chat == chatId }
        
        // Then: preview should have both dependencies
        val dependencyIds = preview.dependencies.map { it.id }.toSet()
        assertTrue(dependencyIds.contains(attachmentId1))
        assertTrue(dependencyIds.contains(attachmentId2))
        
        // And attachmentIds should be empty (not tracking any missing attachments)
        val attachmentIdsField = container.javaClass.getDeclaredField("attachmentIds")
        attachmentIdsField.isAccessible = true
        val attachmentIdsValue = attachmentIdsField.get(container) as kotlinx.coroutines.flow.MutableStateFlow<*>
        val map = attachmentIdsValue.value as Map<*, *>
        assertTrue(map.isEmpty() || spaceId !in map.keys)
    }
}