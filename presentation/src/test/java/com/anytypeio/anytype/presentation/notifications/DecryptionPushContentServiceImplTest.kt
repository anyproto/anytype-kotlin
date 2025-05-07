package com.anytypeio.anytype.presentation.notifications

import android.os.Build
import android.util.Base64
import com.anytypeio.anytype.core_models.DecryptedPushContent
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
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
    private lateinit var json: Json
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
        json = Json { ignoreUnknownKeys = true }
        decryptionService = DecryptionPushContentServiceImpl(
            pushKeyProvider = pushKeyProvider,
            cryptoService = cryptoService,
            json = json
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
                senderName = "Test Sender"
            )
        )
    }

    private fun encryptTestData(content: DecryptedPushContent): ByteArray {
        // Convert content to JSON
        val jsonString = json.encodeToString(DecryptedPushContent.serializer(), content)
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