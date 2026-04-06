package com.anytypeio.anytype.feature_chats.ui

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChatBubbleTimestampPlaceholderTest {

    @Test
    fun `placeholder should wrap time with LRE and PDF markers`() {
        val result = buildTimestampPlaceholder(formattedTime = "14:30")

        assertEquals("\u202A 14:30\u202C", result)
    }

    @Test
    fun `placeholder with edited label should wrap entire text with LRE and PDF markers`() {
        val result = buildTimestampPlaceholder(
            formattedTime = "14:30",
            editedLabel = "edited"
        )

        assertEquals("\u202A edited 14:30\u202C", result)
    }

    @Test
    fun `placeholder should start with LRE marker`() {
        val result = buildTimestampPlaceholder(formattedTime = "09:15")

        assertTrue(result.startsWith("\u202A"))
    }

    @Test
    fun `placeholder should end with PDF marker`() {
        val result = buildTimestampPlaceholder(formattedTime = "09:15")

        assertTrue(result.endsWith("\u202C"))
    }

    @Test
    fun `placeholder without edited label should contain formatted time`() {
        val result = buildTimestampPlaceholder(formattedTime = "23:59")

        assertTrue(result.contains("23:59"))
    }

    @Test
    fun `placeholder with edited label should contain both label and time`() {
        val result = buildTimestampPlaceholder(
            formattedTime = "08:00",
            editedLabel = "edited"
        )

        assertTrue(result.contains("edited"))
        assertTrue(result.contains("08:00"))
    }

    @Test
    fun `placeholder with spaces in time string should work correctly`() {
        val result = buildTimestampPlaceholder(formattedTime = "02:30 PM")

        assertEquals("\u202A 02:30 PM\u202C", result)
    }
}
