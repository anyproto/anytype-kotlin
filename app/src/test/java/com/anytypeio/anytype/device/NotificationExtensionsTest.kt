package com.anytypeio.anytype.device

import com.anytypeio.anytype.core_models.DecryptedPushContent
import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationExtensionsTest {

    @Test
    fun `formatNotificationBody returns raw text when no attachments`() {
        val message = createMessage(text = "Hello world", hasAttachments = false)
        assertEquals("Hello world", message.formatNotificationBody("attachment"))
    }

    @Test
    fun `formatNotificationBody appends attachment indicator when only attachments present`() {
        val message = createMessage(text = "", hasAttachments = true)
        assertEquals("\uD83D\uDCCEattachment", message.formatNotificationBody("attachment"))
    }

    @Test
    fun `formatNotificationBody appends attachment indicator after text when both present`() {
        val message = createMessage(text = "Hello world", hasAttachments = true)
        assertEquals("Hello world \uD83D\uDCCEattachment", message.formatNotificationBody("attachment"))
    }

    @Test
    fun `formatNotificationBody trims whitespace from text`() {
        val message = createMessage(text = "  Hello world  ", hasAttachments = false)
        assertEquals("Hello world", message.formatNotificationBody("attachment"))
    }

    private fun createMessage(
        text: String,
        hasAttachments: Boolean
    ) = DecryptedPushContent.Message(
        text = text,
        hasAttachments = hasAttachments,
        chatId = "test-chat",
        senderName = "Test User",
        spaceName = "Test Space",
        msgId = "test-msg"
    )
} 