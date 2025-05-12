package com.anytypeio.anytype.presentation.notifications

import org.junit.Assert.assertArrayEquals
import org.junit.Before
import org.junit.Test
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class CryptoServiceTest {

    private lateinit var cryptoService: CryptoService
    private lateinit var key: ByteArray
    private lateinit var validEncryptedData: ByteArray

    @Before
    fun setup() {
        cryptoService = CryptoServiceImpl()

        // Generate a test key
        key = "testKey123456789".toByteArray()

        // Create test data and encrypt it
        val testData = "Hello, World!".toByteArray()
        validEncryptedData = encryptTestData(testData, key)
    }

    @Test
    fun `decryptAESGCM should successfully decrypt valid data`() {
        // Given
        val expectedData = "Hello, World!".toByteArray()

        // When
        val decryptedData = cryptoService.decryptAESGCM(validEncryptedData, key)

        // Then
        assertArrayEquals(expectedData, decryptedData)
    }

    @Test(expected = CryptoError.DecryptionFailed::class)
    fun `decryptAESGCM should throw DecryptionFailed when key is invalid`() {
        // Given
        val invalidKey = "invalidKey123456".toByteArray()

        // When
        cryptoService.decryptAESGCM(validEncryptedData, invalidKey)

        // Then - expect exception
    }

    @Test(expected = CryptoError.DecryptionFailed::class)
    fun `decryptAESGCM should throw DecryptionFailed when data is too short`() {
        // Given
        val invalidData = ByteArray(10) // Too short to contain nonce + ciphertext

        // When
        cryptoService.decryptAESGCM(invalidData, key)

        // Then - expect exception
    }

    @Test(expected = CryptoError.DecryptionFailed::class)
    fun `decryptAESGCM should throw DecryptionFailed when data is corrupted`() {
        // Given
        val corruptedData = validEncryptedData.copyOf()
        corruptedData[20] = corruptedData[20].inc() // Corrupt one byte

        // When
        cryptoService.decryptAESGCM(corruptedData, key)

        // Then - expect exception
    }

    private fun encryptTestData(data: ByteArray, key: ByteArray): ByteArray {
        val keySpec = SecretKeySpec(key, "AES")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")

        // Generate random nonce
        val nonce = ByteArray(12).apply {
            java.security.SecureRandom().nextBytes(this)
        }

        val gcmSpec = GCMParameterSpec(128, nonce)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec)

        // Encrypt the data
        val encryptedData = cipher.doFinal(data)

        // Combine nonce and encrypted data
        return nonce + encryptedData
    }
} 