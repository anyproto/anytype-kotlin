package com.anytypeio.anytype.feature_chats.ui

import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.Test

/**
 * DROID-4556. The chat list is reversed, so an arriving message and a freshly loaded page of
 * older-than-newest messages both land at index 0. Only the size of the change tells them
 * apart, and following the tail after a page load would scroll the user past the very
 * messages they scrolled down to read.
 */
class ChatTailFollowTest {

    private val viewport = 12

    @Test
    fun `should follow the tail when a single message arrives`() {
        assertTrue(
            shouldFollowChatTail(
                wasAtBottom = true,
                isPerformingScrollIntent = false,
                previousItemCount = 40,
                newItemCount = 41,
                viewportItemCount = viewport
            )
        )
    }

    @Test
    fun `should follow the tail when a message arrives together with a date section`() {
        assertTrue(
            shouldFollowChatTail(
                wasAtBottom = true,
                isPerformingScrollIntent = false,
                previousItemCount = 40,
                newItemCount = 42,
                viewportItemCount = viewport
            )
        )
    }

    @Test
    fun `should follow the tail when a small burst of messages arrives`() {
        assertTrue(
            shouldFollowChatTail(
                wasAtBottom = true,
                isPerformingScrollIntent = false,
                previousItemCount = 40,
                newItemCount = 44,
                viewportItemCount = viewport
            )
        )
    }

    @Test
    fun `should not follow the tail when a whole page is loaded`() {
        assertFalse(
            shouldFollowChatTail(
                wasAtBottom = true,
                isPerformingScrollIntent = false,
                previousItemCount = 40,
                newItemCount = 140,
                viewportItemCount = viewport
            )
        )
    }

    @Test
    fun `should not follow the tail when more messages arrive than fit on screen`() {
        assertFalse(
            shouldFollowChatTail(
                wasAtBottom = true,
                isPerformingScrollIntent = false,
                previousItemCount = 40,
                newItemCount = 40 + viewport + 1,
                viewportItemCount = viewport
            )
        )
    }

    @Test
    fun `should follow the tail on the first population of the list`() {
        assertTrue(
            shouldFollowChatTail(
                wasAtBottom = true,
                isPerformingScrollIntent = false,
                previousItemCount = 0,
                newItemCount = 100,
                viewportItemCount = 0
            )
        )
    }

    @Test
    fun `should follow the tail when the window shrank`() {
        assertTrue(
            shouldFollowChatTail(
                wasAtBottom = true,
                isPerformingScrollIntent = false,
                previousItemCount = 40,
                newItemCount = 39,
                viewportItemCount = viewport
            )
        )
    }

    @Test
    fun `should not follow the tail when the user was not at the bottom`() {
        assertFalse(
            shouldFollowChatTail(
                wasAtBottom = false,
                isPerformingScrollIntent = false,
                previousItemCount = 40,
                newItemCount = 41,
                viewportItemCount = viewport
            )
        )
    }

    @Test
    fun `should not follow the tail while a scroll intent is being performed`() {
        assertFalse(
            shouldFollowChatTail(
                wasAtBottom = true,
                isPerformingScrollIntent = true,
                previousItemCount = 40,
                newItemCount = 41,
                viewportItemCount = viewport
            )
        )
    }

    @Test
    fun `should follow the tail when the viewport has not been measured yet`() {
        assertTrue(
            shouldFollowChatTail(
                wasAtBottom = true,
                isPerformingScrollIntent = false,
                previousItemCount = 40,
                newItemCount = 41,
                viewportItemCount = 0
            )
        )
    }
}
