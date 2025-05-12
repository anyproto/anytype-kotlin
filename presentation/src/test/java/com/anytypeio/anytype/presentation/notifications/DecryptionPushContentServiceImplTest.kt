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
        val actualEncryptedPayload = "OllD4bCyF0VbI0VrEsz0aYFuj+X8cnsRvm1wDYJC6aCzIyBu99NhHJi3xbIX565cUvIB6tlCdFzRUDc1WJqV8/0dbaB5PZozwLwbv9Pk+Ozxgsu6AspYT8MAR67exZ2ekD3dSo3hoeqlD50bJYQQnWvTgRUns5WzOzDanwwMMXJncxERlB2BdiqC7S2LmU47dgxoMytwBaJXemw9wHiU7dPnICSDAbnNlJU6DAGTn0Rqc38GpMbDg8+u2ksa1gb7P+P8XwTn9AFRPFz4Ay/mM/5jxcignyRGm3PObxBfUCP8NDwl7jH+55Q2VUgC2SX7vVEBLb5mlNJu3DwkhJvB7iRssulypiQ8I1w+mJ+Xh3TG2RYbgjb4l48mNoecblL/hvaRh560T3OTqlWlVNh0c5wRd/eo5YH5zoXrQydk2JXO6vReEWaJQt+bPU2y6N6IUbpLlw2q7OQu9jRIF5T35R3XO8GU8CmyKmhlJK4xAvhOiKIc8X47BGfApY6hl3TSPea9dSEnb0+EB0YsC7DyRc7y3NL588+Yc0sfHLA5Mp2oWs9a"
        val actualEncryptedData = Base64.decode(actualEncryptedPayload, Base64.DEFAULT)
        
        // Use the actual push key from the logs
        val actualKeyValue = "RT1gb7DyUW5tc5qCF92Jc3IlEQVOgxxBo6x2BP5T5mU="
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