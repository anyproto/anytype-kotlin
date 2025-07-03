package com.anytypeio.anytype.presentation.notifications

import android.util.Base64
import com.anytypeio.anytype.core_models.DecryptedPushContent
import com.anytypeio.anytype.domain.notifications.PushKeyProvider
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import timber.log.Timber
import javax.inject.Inject

interface DecryptionPushContentService {
    fun decrypt(encryptedData: ByteArray, keyId: String): DecryptedPushContent?
    fun decryptAndVerifySignature(encryptedData: ByteArray, keyId: String, signature: String?): DecryptedPushContent?
}

class DecryptionPushContentServiceImpl @Inject constructor(
    private val pushKeyProvider: PushKeyProvider,
    private val cryptoService: CryptoService,
    private val signatureVerificationService: SignatureVerificationService,
    private val json: Json
) : DecryptionPushContentService {

    @OptIn(ExperimentalSerializationApi::class)
    override fun decrypt(encryptedData: ByteArray, keyId: String): DecryptedPushContent? {
        return try {
            // Get the encryption key from provider
            val pushKeys = pushKeyProvider.getPushKey()
            val key = pushKeys[keyId] ?: run {
                Timber.w("No encryption key found for keyId: $keyId")
                return null
            }

            // Decode the key from Base64
            val keyData = try {
                Base64.decode(key.value, Base64.DEFAULT)
            } catch (e: IllegalArgumentException) {
                Timber.e(e, "Failed to decode key from Base64 for keyId: $keyId")
                return null
            }

            // Decrypt the data
            val decryptedData = try {
                cryptoService.decryptAESGCM(data = encryptedData, keyData = keyData)
            } catch (e: CryptoError.DecryptionFailed) {
                Timber.e(e, "Failed to decrypt data for keyId: $keyId")
                return null
            }

            // Parse the decrypted JSON
            try {
                json.decodeFromStream<DecryptedPushContent>(decryptedData.inputStream())
            } catch (e: Exception) {
                Timber.e(e, "Failed to parse decrypted data for keyId: $keyId")
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error during decryption for keyId: $keyId")
            null
        }
    }

    override fun decryptAndVerifySignature(
        encryptedData: ByteArray,
        keyId: String,
        signature: String?
    ): DecryptedPushContent? {
        return try {
            // First decrypt the content
            val decryptedContent = decrypt(encryptedData, keyId) ?: return null

            // Check if signature verification is required
            if (signature == null) {
                return decryptedContent
            }

            // Extract sender's account address from decrypted content
            val senderAccountAddress = decryptedContent.senderId

            // Verify signature against the encryptedData
            val isSignatureValid = signatureVerificationService.verifyNotificationSignature(
                accountAddress = senderAccountAddress,
                data = encryptedData,
                signature = signature
            )

            if (isSignatureValid) {
                decryptedContent
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error during signature verification for keyId: $keyId")
            null
        }
    }
}