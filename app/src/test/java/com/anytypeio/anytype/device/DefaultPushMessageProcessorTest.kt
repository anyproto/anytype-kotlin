package com.anytypeio.anytype.device

import android.os.Build
import android.util.Base64
import com.anytypeio.anytype.core_models.DecryptedPushContent
import com.anytypeio.anytype.domain.notifications.NotificationBuilder
import com.anytypeio.anytype.presentation.notifications.DecryptionPushContentService
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class DefaultPushMessageProcessorTest {

    private lateinit var decryptionService: DecryptionPushContentService
    private lateinit var notificationBuilder: NotificationBuilder
    private lateinit var processor: DefaultPushMessageProcessor

    @Before
    fun setup() {
        decryptionService = mock()
        notificationBuilder = mock()
        processor = DefaultPushMessageProcessor(
            decryptionService = decryptionService,
            notificationBuilder = notificationBuilder
        )
    }


    @Test
    fun `process should return false when payload is missing`() {
        // Given
        val messageData = mapOf(
            "x-any-type" to "normal",
            "x-any-group-id" to "test-group",
            "x-any-key-id" to "test-key-id"
        )

        // When
        val result = processor.process(messageData, isAppInForeground = false)

        // Then
        assertFalse(result)
        verifyNoInteractions(decryptionService)
        verifyNoInteractions(notificationBuilder)
    }

    @Test
    fun `process should return false when key id is missing`() {
        // Given
        val messageData = mapOf(
            "x-any-type" to "normal",
            "x-any-group-id" to "test-group",
            "x-any-payload" to "test-payload"
        )

        // When
        val result = processor.process(messageData, isAppInForeground = false)

        // Then
        assertFalse(result)
        verifyNoInteractions(decryptionService)
        verifyNoInteractions(notificationBuilder)
    }

    @Test
    fun `process should return false when decryption fails`() {
        // Given
        val messageData = mapOf(
            "x-any-payload" to "test-payload",
            "x-any-key-id" to "test-key-id"
        )
        whenever(decryptionService.decryptAndVerifySignature(any(), any(), any())).thenReturn(null)

        // When
        val result = processor.process(messageData, isAppInForeground = false)

        // Then
        assertFalse(result)
        verifyNoInteractions(notificationBuilder)
    }

    @Test
    fun `process should return true and show notification when decryption succeeds`() {
        // Given
        val payload = "test-payload"
        val keyId = "test-key-id"
        val messageData = mapOf(
            "x-any-type" to "normal",
            "x-any-group-id" to "test-group",
            "x-any-payload" to payload,
            "x-any-key-id" to keyId
        )
        
        val decryptedContent = DecryptedPushContent(
            spaceId = "test-space-id",
            type = 1,
            senderId = "test-sender-id",
            newMessage = DecryptedPushContent.Message(
                chatId = "test-chat-id",
                msgId = "test-msg-id",
                text = "Test message",
                spaceName = "Test Space",
                senderName = "Test Sender",
                hasAttachments = true
            )
        )

        whenever(decryptionService.decryptAndVerifySignature(Base64.decode(payload, Base64.DEFAULT), keyId, null))
            .thenReturn(decryptedContent)

        // When
        val result = processor.process(messageData, isAppInForeground = false)

        // Then
        assertTrue(result)
        verify(notificationBuilder).buildAndNotify(
            message = decryptedContent.newMessage,
            spaceId = decryptedContent.spaceId,
            groupId = "test-group"
        )
    }

    @Test
    fun `process should return true and show notification when decryption with signature succeeds`() {
        // Given
        val payload = "test-payload"
        val keyId = "test-key-id"
        val signature = "test-signature"
        val messageData = mapOf(
            "x-any-type" to "normal",
            "x-any-group-id" to "test-group",
            "x-any-payload" to payload,
            "x-any-key-id" to keyId,
            "x-any-signature" to signature
        )
        
        val decryptedContent = DecryptedPushContent(
            spaceId = "test-space-id",
            type = 1,
            senderId = "test-sender-id",
            newMessage = DecryptedPushContent.Message(
                chatId = "test-chat-id",
                msgId = "test-msg-id",
                text = "Test message",
                spaceName = "Test Space",
                senderName = "Test Sender",
                hasAttachments = true
            )
        )

        whenever(decryptionService.decryptAndVerifySignature(Base64.decode(payload, Base64.DEFAULT), keyId, signature))
            .thenReturn(decryptedContent)

        // When
        val result = processor.process(messageData, isAppInForeground = false)

        // Then
        assertTrue(result)
        verify(notificationBuilder).buildAndNotify(
            message = decryptedContent.newMessage,
            spaceId = decryptedContent.spaceId,
            groupId = "test-group"
        )
    }

    @Test
    fun `process should return false when signature verification fails`() {
        // Given
        val payload = "test-payload"
        val keyId = "test-key-id"
        val signature = "invalid-signature"
        val messageData = mapOf(
            "x-any-type" to "normal",
            "x-any-group-id" to "test-group",
            "x-any-payload" to payload,
            "x-any-key-id" to keyId,
            "x-any-signature" to signature
        )

        whenever(decryptionService.decryptAndVerifySignature(Base64.decode(payload, Base64.DEFAULT), keyId, signature))
            .thenReturn(null)

        // When
        val result = processor.process(messageData, isAppInForeground = false)

        // Then
        assertFalse(result)
        verifyNoInteractions(notificationBuilder)
    }

    @Test
    fun `process should return true and clear notifications for silent push`() {
        // Given
        val messageData = mapOf(
            "x-any-type" to "silent",
            "x-any-group-id" to "test-group"
        )

        // When
        val result = processor.process(messageData, isAppInForeground = false)

        // Then
        assertTrue(result)
        verify(notificationBuilder).clearNotificationsByGroupId("test-group")
        verifyNoInteractions(decryptionService)
    }

    @Test
    fun `process should return false when type is missing`() {
        // Given
        val messageData = mapOf(
            "x-any-group-id" to "test-group"
        )

        // When
        val result = processor.process(messageData, isAppInForeground = false)

        // Then
        assertFalse(result)
        verifyNoInteractions(notificationBuilder)
        verifyNoInteractions(decryptionService)
    }

    @Test
    fun `process should return false when group id is missing`() {
        // Given
        val messageData = mapOf(
            "x-any-type" to "normal"
        )

        // When
        val result = processor.process(messageData, isAppInForeground = false)

        // Then
        assertFalse(result)
        verifyNoInteractions(notificationBuilder)
        verifyNoInteractions(decryptionService)
    }
} 