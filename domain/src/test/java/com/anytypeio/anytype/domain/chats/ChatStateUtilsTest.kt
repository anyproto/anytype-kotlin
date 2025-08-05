package com.anytypeio.anytype.domain.chats

import kotlin.test.assertEquals
import org.junit.Test

class ChatStateUtilsTest {

    @Test
    fun `should apply new state with higher order - nullable version`() {
        assertEquals(true, ChatStateUtils.shouldApplyNewChatState(newOrder = 5L, currentOrder = 3L))
        assertEquals(true, ChatStateUtils.shouldApplyNewChatState(newOrder = 1L, currentOrder = null))
    }

    @Test
    fun `should reject new state with lower or equal order - nullable version`() {
        assertEquals(false, ChatStateUtils.shouldApplyNewChatState(newOrder = 3L, currentOrder = 5L))
        assertEquals(false, ChatStateUtils.shouldApplyNewChatState(newOrder = 3L, currentOrder = 3L))
        assertEquals(false, ChatStateUtils.shouldApplyNewChatState(newOrder = -1L, currentOrder = null))
    }

    @Test
    fun `should apply new state with higher order - non-nullable version`() {
        assertEquals(true, ChatStateUtils.shouldApplyNewChatState(newOrder = 5L, currentOrder = 3L))
        assertEquals(true, ChatStateUtils.shouldApplyNewChatState(newOrder = 0L, currentOrder = -1L))
    }

    @Test
    fun `should reject new state with lower or equal order - non-nullable version`() {
        assertEquals(false, ChatStateUtils.shouldApplyNewChatState(newOrder = 3L, currentOrder = 5L))
        assertEquals(false, ChatStateUtils.shouldApplyNewChatState(newOrder = 3L, currentOrder = 3L))
    }

    @Test
    fun `should handle edge cases correctly`() {
        // Edge case: null defaults to -1L
        assertEquals(true, ChatStateUtils.shouldApplyNewChatState(newOrder = 0L, currentOrder = null))
        assertEquals(false, ChatStateUtils.shouldApplyNewChatState(newOrder = -2L, currentOrder = null))
        
        // Edge case: large numbers
        assertEquals(true, ChatStateUtils.shouldApplyNewChatState(newOrder = Long.MAX_VALUE, currentOrder = Long.MAX_VALUE - 1))
        assertEquals(false, ChatStateUtils.shouldApplyNewChatState(newOrder = Long.MAX_VALUE - 1, currentOrder = Long.MAX_VALUE))
        
        // Edge case: negative numbers
        assertEquals(true, ChatStateUtils.shouldApplyNewChatState(newOrder = -1L, currentOrder = -2L))
        assertEquals(false, ChatStateUtils.shouldApplyNewChatState(newOrder = -2L, currentOrder = -1L))
    }
}