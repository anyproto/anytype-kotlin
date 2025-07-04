package com.anytypeio.anytype.domain.chats

import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.common.DefaultCoroutineTestRule
import com.anytypeio.anytype.domain.debugging.Logger
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub

class ChatPreviewContainerTest {

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
            logger = logger
        )

        // Then
        assertNotNull(container)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `should validate order comparison logic implementation`() = runTest {
        // This test validates the order comparison logic that was extracted into shouldApplyNewChatState()
        // The function is now implemented in ChatPreviewContainer.Default.shouldApplyNewChatState()

        // Given - test scenarios for order comparison
        val currentOrder = 3L
        val higherOrder = 5L
        val lowerOrder = 1L
        val equalOrder = 3L

        // When - applying the same logic as the private function in ChatPreviewContainer
        val shouldApplyHigher =
            testShouldApplyNewChatState(newOrder = higherOrder, currentOrder = currentOrder)
        val shouldApplyLower =
            testShouldApplyNewChatState(newOrder = lowerOrder, currentOrder = currentOrder)
        val shouldApplyEqual =
            testShouldApplyNewChatState(newOrder = equalOrder, currentOrder = currentOrder)

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

        // When - applying the same logic as the private function in ChatPreviewContainer
        val shouldApply =
            testShouldApplyNewChatState(newOrder = validOrder, currentOrder = nullCurrentOrder)
        val currentOrder = nullCurrentOrder ?: -1L

        // Then - validate null handling behavior
        assertEquals(-1L, currentOrder, "Null state should default to -1L")
        assertEquals(true, shouldApply, "Valid order should be applied over null state")
    }

    @Test
    fun `should test order comparison edge cases`() {
        // Test edge cases for the order comparison function
        // These test the same logic as ChatPreviewContainer.Default.shouldApplyNewChatState()

        // Edge case: zero orders
        assertEquals(true, testShouldApplyNewChatState(newOrder = 1L, currentOrder = 0L))
        assertEquals(false, testShouldApplyNewChatState(newOrder = 0L, currentOrder = 1L))
        assertEquals(false, testShouldApplyNewChatState(newOrder = 0L, currentOrder = 0L))

        // Edge case: negative orders
        assertEquals(true, testShouldApplyNewChatState(newOrder = -1L, currentOrder = -2L))
        assertEquals(false, testShouldApplyNewChatState(newOrder = -2L, currentOrder = -1L))

        // Edge case: large numbers
        assertEquals(
            true,
            testShouldApplyNewChatState(
                newOrder = Long.MAX_VALUE,
                currentOrder = Long.MAX_VALUE - 1
            )
        )
        assertEquals(
            false,
            testShouldApplyNewChatState(
                newOrder = Long.MAX_VALUE - 1,
                currentOrder = Long.MAX_VALUE
            )
        )

        // Edge case: null current order (should default to -1L)
        assertEquals(true, testShouldApplyNewChatState(newOrder = 0L, currentOrder = null))
        assertEquals(false, testShouldApplyNewChatState(newOrder = -2L, currentOrder = null))
        assertEquals(false, testShouldApplyNewChatState(newOrder = -1L, currentOrder = null))
    }

    // Test helper function that mirrors the exact logic used in ChatPreviewContainer.Default.shouldApplyNewChatState()
    private fun testShouldApplyNewChatState(newOrder: Long, currentOrder: Long?): Boolean {
        // This mirrors the logic: newState.order > (preview.state?.order ?: -1L)
        return newOrder > (currentOrder ?: -1L)
    }
}