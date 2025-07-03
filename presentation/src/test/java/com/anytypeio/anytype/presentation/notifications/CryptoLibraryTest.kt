package com.anytypeio.anytype.presentation.notifications

import android.os.Build
import android.util.Base64
import com.anytype.crypto.AccountDecoder
import com.anytype.crypto.Ed25519PubKey
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.security.SecureRandom

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class CryptoLibraryTest {

    @Test
    fun `test crypto library basic functionality`() {
        // Generate a key pair for testing
        val keyPairGenerator = Ed25519KeyPairGenerator()
        keyPairGenerator.init(Ed25519KeyGenerationParameters(SecureRandom()))
        val keyPair = keyPairGenerator.generateKeyPair()
        
        val privateKey = keyPair.private as Ed25519PrivateKeyParameters
        val publicKey = keyPair.public as Ed25519PublicKeyParameters
        
        // Create PubKey from raw bytes
        val pubKey = Ed25519PubKey(publicKey.encoded)
        
        // Get account address
        val accountAddress = pubKey.account()
        println("Account Address: $accountAddress")
        
        // Sign some data
        val message = "Test message".toByteArray()
        val signer = Ed25519Signer()
        signer.init(true, privateKey)
        signer.update(message, 0, message.size)
        val signature = signer.generateSignature()
        
        // Decode account address back to public key
        val decodedPubKey = AccountDecoder.decodeAccountAddress(accountAddress)
        
        // Verify signature
        val isValid = decodedPubKey.verify(message, signature)
        assertTrue("Signature should be valid", isValid)
    }

    @Test
    fun `test crypto library with Base64 encoded signature`() {
        // Generate a key pair for testing
        val keyPairGenerator = Ed25519KeyPairGenerator()
        keyPairGenerator.init(Ed25519KeyGenerationParameters(SecureRandom()))
        val keyPair = keyPairGenerator.generateKeyPair()
        
        val privateKey = keyPair.private as Ed25519PrivateKeyParameters
        val publicKey = keyPair.public as Ed25519PublicKeyParameters
        
        // Create PubKey from raw bytes
        val pubKey = Ed25519PubKey(publicKey.encoded)
        
        // Get account address
        val accountAddress = pubKey.account()
        
        // Sign some data
        val message = "Test message".toByteArray()
        val signer = Ed25519Signer()
        signer.init(true, privateKey)
        signer.update(message, 0, message.size)
        val signatureBytes = signer.generateSignature()
        
        // Encode signature to Base64 (like in our notifications)
        val signatureBase64 = Base64.encodeToString(signatureBytes, Base64.DEFAULT)
        
        // Test our SignatureVerificationService
        val verificationService = SignatureVerificationServiceImpl()
        val isValid = verificationService.verifyNotificationSignature(
            accountAddress = accountAddress,
            data = message,
            signature = signatureBase64
        )
        
        assertTrue("Signature should be valid with our service", isValid)
    }

    @Test
    fun `test with different message formats`() {
        // Generate a key pair for testing
        val keyPairGenerator = Ed25519KeyPairGenerator()
        keyPairGenerator.init(Ed25519KeyGenerationParameters(SecureRandom()))
        val keyPair = keyPairGenerator.generateKeyPair()
        
        val privateKey = keyPair.private as Ed25519PrivateKeyParameters
        val publicKey = keyPair.public as Ed25519PublicKeyParameters
        
        // Create PubKey from raw bytes
        val pubKey = Ed25519PubKey(publicKey.encoded)
        val accountAddress = pubKey.account()
        
        val testMessages = listOf(
            "Test345",
            "bafyreiejccv5j62jpbqhir5w3n4a3cg2kqti5thxgdaksqjy2thah5b3z4:Test345",
            """{"text":"Test345"}""",
            ""
        )
        
        val verificationService = SignatureVerificationServiceImpl()
        
        for (message in testMessages) {
            val messageBytes = message.toByteArray(Charsets.UTF_8)
            
            // Sign the message
            val signer = Ed25519Signer()
            signer.init(true, privateKey)
            signer.update(messageBytes, 0, messageBytes.size)
            val signatureBytes = signer.generateSignature()
            val signatureBase64 = Base64.encodeToString(signatureBytes, Base64.DEFAULT)
            
            // Verify with our service
            val isValid = verificationService.verifyNotificationSignature(
                accountAddress = accountAddress,
                data = messageBytes,
                signature = signatureBase64
            )
            
            assertTrue("Signature should be valid for message: '$message'", isValid)
        }
    }

    @Test
    fun `test library owner example format`() {
        // Test using the exact pattern from the library owner's example
        val keyPairGenerator = Ed25519KeyPairGenerator()
        keyPairGenerator.init(Ed25519KeyGenerationParameters(SecureRandom()))
        val keyPair = keyPairGenerator.generateKeyPair()
        
        val privateKey = keyPair.private as Ed25519PrivateKeyParameters
        val publicKey = keyPair.public as Ed25519PublicKeyParameters
        
        // Create PubKey from raw bytes
        val pubKey = Ed25519PubKey(publicKey.encoded)
        
        // Get account address
        val accountAddress = pubKey.account()
        println("Generated Account Address: $accountAddress")
        
        // Sign some data (following the library owner's example pattern)
        val message = "hello".toByteArray()
        val signer = Ed25519Signer()
        signer.init(true, privateKey)
        signer.update(message, 0, message.size)
        val signature = signer.generateSignature()
        
        // Decode account address back to public key
        val decodedPubKey = AccountDecoder.decodeAccountAddress(accountAddress)
        
        // Verify signature using library directly (should work)
        val isValid = decodedPubKey.verify(message, signature)
        assertTrue("Direct library verification should work", isValid)
        
        // Test our SignatureVerificationService with Base64-encoded signature
        val verificationService = SignatureVerificationServiceImpl()
        val signatureBase64 = Base64.encodeToString(signature, Base64.DEFAULT)
        
        val isValidWithService = verificationService.verifyNotificationSignature(
            accountAddress = accountAddress,
            data = message,
            signature = signatureBase64
        )
        
        assertTrue("Our service should also verify successfully", isValidWithService)
    }

    @Test
    fun `test iOS implementation pattern - sign encrypted data`() {
        // Test the pattern used in iOS: sign the encrypted data, not the message content
        val keyPairGenerator = Ed25519KeyPairGenerator()
        keyPairGenerator.init(Ed25519KeyGenerationParameters(SecureRandom()))
        val keyPair = keyPairGenerator.generateKeyPair()
        
        val privateKey = keyPair.private as Ed25519PrivateKeyParameters
        val publicKey = keyPair.public as Ed25519PublicKeyParameters
        
        // Create PubKey from raw bytes
        val pubKey = Ed25519PubKey(publicKey.encoded)
        val accountAddress = pubKey.account()
        
        // Simulate encrypted notification data (this would be the AES-GCM encrypted payload)
        val encryptedData = "simulated_encrypted_push_notification_data".toByteArray()
        
        // Sign the encrypted data (this is what the server would do)
        val signer = Ed25519Signer()
        signer.init(true, privateKey)
        signer.update(encryptedData, 0, encryptedData.size)
        val signature = signer.generateSignature()
        val signatureBase64 = Base64.encodeToString(signature, Base64.DEFAULT)
        
        // Verify signature using our service (this is what our Android implementation does)
        val verificationService = SignatureVerificationServiceImpl()
        val isValid = verificationService.verifyNotificationSignature(
            accountAddress = accountAddress,
            data = encryptedData,  // Sign/verify the encrypted data, not decrypted content
            signature = signatureBase64
        )
        
        assertTrue("Signature verification against encrypted data should work", isValid)
        
        // Also verify directly with the library to ensure our service works correctly
        val decodedPubKey = AccountDecoder.decodeAccountAddress(accountAddress)
        val isValidDirect = decodedPubKey.verify(encryptedData, signature)
        assertTrue("Direct verification should also work", isValidDirect)
        
        println("✅ iOS pattern test passed - signing encrypted data works correctly")
    }
}