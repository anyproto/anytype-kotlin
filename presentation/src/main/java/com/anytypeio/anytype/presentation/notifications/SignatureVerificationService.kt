package com.anytypeio.anytype.presentation.notifications

import com.anytype.crypto.AccountDecoder
import com.anytype.crypto.Ed25519PubKey
import org.bouncycastle.util.encoders.Hex
import timber.log.Timber

interface SignatureVerificationService {
    
    /**
     * Verifies Ed25519 signature for notification content
     * @param accountAddress The account address to verify against
     * @param data The original data that was signed
     * @param signature The signature to verify (hex-encoded)
     * @return true if signature is valid, false otherwise
     */
    fun verifyNotificationSignature(
        accountAddress: String,
        data: ByteArray,
        signature: String
    ): Boolean
    
    /**
     * Verifies Ed25519 signature using raw public key
     * @param publicKey The raw 32-byte Ed25519 public key
     * @param data The original data that was signed
     * @param signature The signature to verify (hex-encoded)
     * @return true if signature is valid, false otherwise
     */
    fun verifySignatureWithRawKey(
        publicKey: ByteArray,
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
            val signatureBytes = Hex.decode(signature)
            
            if (signatureBytes.size != 64) {
                Timber.w("Invalid signature length: ${signatureBytes.size}, expected 64 bytes")
                return false
            }
            
            val isValid = pubKey.verify(data, signatureBytes)
            
            if (isValid) {
                Timber.d("Signature verification successful for account: $accountAddress")
            } else {
                Timber.w("Signature verification failed for account: $accountAddress")
            }
            
            isValid
        } catch (e: Exception) {
            Timber.e(e, "Error verifying signature for account: $accountAddress")
            false
        }
    }
    
    override fun verifySignatureWithRawKey(
        publicKey: ByteArray,
        data: ByteArray,
        signature: String
    ): Boolean {
        return try {
            if (publicKey.size != 32) {
                Timber.w("Invalid public key length: ${publicKey.size}, expected 32 bytes")
                return false
            }
            
            val signatureBytes = Hex.decode(signature)
            
            if (signatureBytes.size != 64) {
                Timber.w("Invalid signature length: ${signatureBytes.size}, expected 64 bytes")
                return false
            }
            
            val pubKey = Ed25519PubKey(publicKey)
            val isValid = pubKey.verify(data, signatureBytes)
            
            if (isValid) {
                Timber.d("Signature verification successful with raw key")
            } else {
                Timber.w("Signature verification failed with raw key")
            }
            
            isValid
        } catch (e: Exception) {
            Timber.e(e, "Error verifying signature with raw key")
            false
        }
    }
}