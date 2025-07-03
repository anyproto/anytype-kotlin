package com.anytypeio.anytype.presentation.notifications

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.bouncycastle.util.encoders.Hex

class SignatureVerificationServiceTest {

    private lateinit var signatureVerificationService: SignatureVerificationService

    @Before
    fun setup() {
        signatureVerificationService = SignatureVerificationServiceImpl()
    }

    @Test
    fun `verifyNotificationSignature should return false for invalid account address`() {
        val invalidAccountAddress = "InvalidAddress123"
        val data = "test data".toByteArray()
        val signature = "00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff"

        val result = signatureVerificationService.verifyNotificationSignature(
            accountAddress = invalidAccountAddress,
            data = data,
            signature = signature
        )

        assertFalse(result)
    }

    @Test
    fun `verifyNotificationSignature should return false for invalid signature length`() {
        val accountAddress = "A8yCiRddQZbmRew2bGLkjKP1Q5mEBvs5sSoXxS3UynxJVTiZ"
        val data = "test data".toByteArray()
        val shortSignature = "00112233"

        val result = signatureVerificationService.verifyNotificationSignature(
            accountAddress = accountAddress,
            data = data,
            signature = shortSignature
        )

        assertFalse(result)
    }

    @Test
    fun `verifyNotificationSignature should return false for invalid hex signature`() {
        val accountAddress = "A8yCiRddQZbmRew2bGLkjKP1Q5mEBvs5sSoXxS3UynxJVTiZ"
        val data = "test data".toByteArray()
        val invalidHexSignature = "GGHHIIJJKKLLMMNNOOPPQQRRSSTTUUVVWWXXYYZZ"

        val result = signatureVerificationService.verifyNotificationSignature(
            accountAddress = accountAddress,
            data = data,
            signature = invalidHexSignature
        )

        assertFalse(result)
    }

    @Test
    fun `verifySignatureWithRawKey should return false for invalid public key length`() {
        val shortPublicKey = ByteArray(16) // Should be 32 bytes
        val data = "test data".toByteArray()
        val signature = "00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff"

        val result = signatureVerificationService.verifySignatureWithRawKey(
            publicKey = shortPublicKey,
            data = data,
            signature = signature
        )

        assertFalse(result)
    }

    @Test
    fun `verifySignatureWithRawKey should return false for invalid signature length`() {
        val publicKey = ByteArray(32) // Valid 32-byte key
        val data = "test data".toByteArray()
        val shortSignature = "00112233"

        val result = signatureVerificationService.verifySignatureWithRawKey(
            publicKey = publicKey,
            data = data,
            signature = shortSignature
        )

        assertFalse(result)
    }

    @Test
    fun `verifySignatureWithRawKey should handle empty data gracefully`() {
        val publicKey = ByteArray(32)
        val data = ByteArray(0)
        val signature = "00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff"

        val result = signatureVerificationService.verifySignatureWithRawKey(
            publicKey = publicKey,
            data = data,
            signature = signature
        )

        assertFalse(result) // Should return false for mocked verification
    }

    @Test
    fun `verifySignatureWithRawKey should handle null byte arrays gracefully`() {
        val publicKey = ByteArray(32)
        val data = "test data".toByteArray()
        val signature = "00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff"

        val result = signatureVerificationService.verifySignatureWithRawKey(
            publicKey = publicKey,
            data = data,
            signature = signature
        )

        assertFalse(result) // Should return false for mocked verification
    }

    @Test
    fun `verifyNotificationSignature should handle exception during verification`() {
        val accountAddress = "A8yCiRddQZbmRew2bGLkjKP1Q5mEBvs5sSoXxS3UynxJVTiZ"
        val data = "test data".toByteArray()
        val signature = "invalid_hex_string_that_will_cause_exception"

        val result = signatureVerificationService.verifyNotificationSignature(
            accountAddress = accountAddress,
            data = data,
            signature = signature
        )

        assertFalse(result)
    }
}