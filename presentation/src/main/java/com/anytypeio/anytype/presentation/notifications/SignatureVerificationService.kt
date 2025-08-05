package com.anytypeio.anytype.presentation.notifications

import android.util.Base64
import com.anytype.crypto.AccountDecoder
import timber.log.Timber

interface SignatureVerificationService {
    
    /**
     * Verifies Ed25519 signature for notification content
     * @param accountAddress The account address to verify against
     * @param data The original data that was signed
     * @param signature The signature to verify (Base64-encoded)
     * @return true if signature is valid, false otherwise
     */
    fun verifyNotificationSignature(
        accountAddress: String,
        data: ByteArray,
        signature: String
    ): Boolean
}

class SignatureVerificationServiceImpl : SignatureVerificationService {
    
    override fun verifyNotificationSignature(
        accountAddress: String,
        data: ByteArray,
        signature: String
    ): Boolean {
        return try {
            val pubKey = AccountDecoder.decodeAccountAddress(accountAddress)
            val signatureBytes = Base64.decode(signature, Base64.DEFAULT)
            
            if (signatureBytes.size != 64) {
                Timber.w("Invalid signature length: ${signatureBytes.size}, expected 64 bytes")
                return false
            }
            
            pubKey.verify(data, signatureBytes)
        } catch (e: Exception) {
            Timber.e(e, "Exception during signature verification")
            false
        }
    }
}