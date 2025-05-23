package com.anytypeio.anytype.presentation.notifications

import android.os.Build
import android.util.Base64
import com.anytypeio.anytype.core_models.DecryptedPushContent
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class DecryptionPushContentServiceImplTest {

    private lateinit var pushKeyProvider: PushKeyProvider
    private lateinit var cryptoService: CryptoService
    private lateinit var decryptionService: DecryptionPushContentService

    private val testKeyId = "test-key-id"
    private val testKey = "testKey123456789"
    private val testSpaceId = "test-space-id"
    private val testSenderId = "test-sender-id"
    private val testChatId = "test-chat-id"
    private val testMsgId = "test-msg-id"

    @Before
    fun setup() {
        pushKeyProvider = mock()
        cryptoService = CryptoServiceImpl()
        decryptionService = DecryptionPushContentServiceImpl(
            pushKeyProvider = pushKeyProvider,
            cryptoService = cryptoService
        )
    }

    @Test
    fun `decrypt should successfully decrypt and parse valid data`() {
        // Given
        val keyAsBytes = testKey.toByteArray()
        val value = Base64.encodeToString(keyAsBytes, Base64.DEFAULT)
        val expectedContent = createTestContent()
        val encryptedData = encryptTestData(expectedContent)
        whenever(pushKeyProvider.getPushKey()).thenReturn(
            mapOf(testKeyId to PushKey(id = testKeyId, value = value))
        )

        // When
        val result = decryptionService.decrypt(encryptedData, testKeyId)

        // Then
        assertEquals(expectedContent, result)
    }

    @Test
    fun `decrypt should return null when key not found`() {
        // Given
        val encryptedData = ByteArray(100)
        whenever(pushKeyProvider.getPushKey()).thenReturn(emptyMap())

        // When
        val result = decryptionService.decrypt(encryptedData, testKeyId)

        // Then
        assertNull(result)
    }

    @Test
    fun `decrypt should return null when key is invalid base64`() {
        // Given
        val encryptedData = ByteArray(100)
        whenever(pushKeyProvider.getPushKey()).thenReturn(
            mapOf(testKeyId to PushKey(id = testKeyId, value = "invalid-base64"))
        )

        // When
        val result = decryptionService.decrypt(encryptedData, testKeyId)

        // Then
        assertNull(result)
    }

    @Test
    fun `decrypt should return null when decryption fails`() {
        // Given
        val encryptedData = ByteArray(100)
        whenever(pushKeyProvider.getPushKey()).thenReturn(
            mapOf(
                testKeyId to PushKey(
                    id = testKeyId,
                    value = Base64.encodeToString("wrong-key".toByteArray(), Base64.DEFAULT)
                )
            )
        )

        // When
        val result = decryptionService.decrypt(encryptedData, testKeyId)

        // Then
        assertNull(result)
    }

    @Test
    fun `decrypt should return null when json parsing fails`() {
        // Given
        val invalidJson = "invalid-json".toByteArray()
        whenever(pushKeyProvider.getPushKey()).thenReturn(
            mapOf(
                testKeyId to PushKey(
                    id = testKeyId,
                    value = Base64.encodeToString(testKey.toByteArray(), Base64.DEFAULT)
                )
            )
        )

        // When
        val result = decryptionService.decrypt(invalidJson, testKeyId)

        // Then
        assertNull(result)
    }

    @Test
    fun `decrypt should successfully decrypt actual RemoteMessage data`() {
        // Given
        val actualKeyId = "626166797265696376626f79757979696a6c66636235677461336665736c6f716132656f646b707377766133326b6d6c6b76336870637366756971e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        val actualKeyValue = "RT1gb7DyUW5tc5qCF92Jc3IlEQVOgxxBo6x2BP5T5mU="
        
        // Create test content with the same values as in the original test
        val testContent = DecryptedPushContent(
            spaceId = "test-space",
            type = 1,
            senderId = "test-sender",
            newMessage = DecryptedPushContent.Message(
                chatId = "test-chat",
                msgId = "test-msg",
                text = "ooo",
                spaceName = "Спейсдля пушей",
                senderName = "Test not",
                hasAttachments = false
            )
        )
        
        // Encrypt the test content
        val keyBytes = Base64.decode(actualKeyValue, Base64.DEFAULT)
        val keySpec = SecretKeySpec(keyBytes, "AES")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val nonce = ByteArray(12).apply {
            java.security.SecureRandom().nextBytes(this)
        }
        val gcmSpec = GCMParameterSpec(128, nonce)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec)
        
        val jsonString = Json.encodeToString(DecryptedPushContent.serializer(), testContent)
        val ciphertext = cipher.doFinal(jsonString.toByteArray())
        val actualEncryptedData = nonce + ciphertext
        
        whenever(pushKeyProvider.getPushKey()).thenReturn(
            mapOf(actualKeyId to PushKey(id = actualKeyId, value = actualKeyValue))
        )

        // When
        val result = decryptionService.decrypt(actualEncryptedData, actualKeyId)

        // Then
        assertNotNull(result)
        assertEquals(1, result?.type)
        assertNotNull(result?.newMessage)
        assertEquals("Спейсдля пушей", result?.newMessage?.spaceName)
        assertEquals("Test not", result?.newMessage?.senderName)
        assertEquals("ooo", result?.newMessage?.text)
        assertEquals(false, result?.newMessage?.hasAttachments)
    }

    @Test
    fun `decrypt should successfully decrypt message with attachments`() {
        // Given
        val keyId = "test-key-id"
        val key = "testKey123456789"
        val keyAsBytes = key.toByteArray()
        val value = Base64.encodeToString(keyAsBytes, Base64.DEFAULT)
        
        val content = DecryptedPushContent(
            spaceId = "test-space",
            type = 1,
            senderId = "test-sender",
            newMessage = DecryptedPushContent.Message(
                chatId = "test-chat",
                msgId = "test-msg",
                text = "Test message with attachments",
                spaceName = "Test Space",
                senderName = "Test Sender",
                hasAttachments = true
            )
        )
        
        val encryptedData = encryptTestData(content)
        whenever(pushKeyProvider.getPushKey()).thenReturn(
            mapOf(keyId to PushKey(id = keyId, value = value))
        )

        // When
        val result = decryptionService.decrypt(encryptedData, keyId)

        // Then
        assertNotNull(result)
        assertEquals(1, result?.type)
        assertNotNull(result?.newMessage)
        assertEquals("Test Space", result?.newMessage?.spaceName)
        assertEquals("Test Sender", result?.newMessage?.senderName)
        assertEquals("Test message with attachments", result?.newMessage?.text)
        assertEquals(true, result?.newMessage?.hasAttachments)
    }

    private fun createTestContent(): DecryptedPushContent {
        return DecryptedPushContent(
            spaceId = testSpaceId,
            type = 1,
            senderId = testSenderId,
            newMessage = DecryptedPushContent.Message(
                chatId = testChatId,
                msgId = testMsgId,
                text = "Test message",
                spaceName = "Test Space",
                senderName = "Test Sender",
                hasAttachments = false
            )
        )
    }

    private fun encryptTestData(content: DecryptedPushContent): ByteArray {
        // Convert content to JSON
        val jsonString = Json.encodeToString(DecryptedPushContent.serializer(), content)
        val plaintext = jsonString.toByteArray()

        // Generate random nonce
        val nonce = ByteArray(12).apply {
            java.security.SecureRandom().nextBytes(this)
        }

        // Initialize cipher
        val keySpec = SecretKeySpec(testKey.toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(128, nonce)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec)

        // Encrypt
        val ciphertext = cipher.doFinal(plaintext)

        // Combine nonce and ciphertext
        return nonce + ciphertext
    }
} 