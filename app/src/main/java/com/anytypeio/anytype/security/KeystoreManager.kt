package com.anytypeio.anytype.security

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.security.KeyStoreException
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.anytypeio.anytype.di.main.ENCRYPTED_PREFS_NAME
import java.security.KeyStore
import timber.log.Timber

/**
 * Handles initialization and recovery for EncryptedSharedPreferences
 * using AndroidX Security Crypto (v1.0.0).
 *
 * This implementation:
 * - Uses the default stable MasterKeys setup
 * - Handles known keystore corruption errors (e.g. InvalidKeyBlob)
 * - Deletes and regenerates the key only in scoped recovery
 *
 * Alias used by MasterKeys.getOrCreate(...) is:
 * "_androidx_security_master_key_"
 * (verified from source: https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:security/security-crypto/src/main/java/androidx/security/crypto/MasterKeys.java)
 */
class KeystoreManager(private val context: Context) {

    companion object {
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val DEFAULT_KEY_ALIAS = "_androidx_security_master_key_"
    }

    fun initializeEncryptedPreferences(): SharedPreferences {
        return try {
            EncryptedSharedPreferences.create(
                ENCRYPTED_PREFS_NAME,
                MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            return if (isKeystoreCorruptionError(e)) {
                Timber.e(e, "Keystore key corruption detected. Attempting recovery.")
                recoverEncryptedPreferences()
            } else {
                Timber.e(e, "Unexpected error initializing encrypted prefs. Not recovering.")
                throw e
            }
        }
    }

    private fun recoverEncryptedPreferences(): SharedPreferences {
        try {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
            keyStore.load(null)
            if (keyStore.containsAlias(DEFAULT_KEY_ALIAS)) {
                keyStore.deleteEntry(DEFAULT_KEY_ALIAS)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete corrupted keystore alias: $DEFAULT_KEY_ALIAS")
        }

        context.getSharedPreferences(ENCRYPTED_PREFS_NAME, Context.MODE_PRIVATE)
            .edit().clear().commit()

        return EncryptedSharedPreferences.create(
            ENCRYPTED_PREFS_NAME,
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun isKeystoreCorruptionError(e: Exception): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false
        return (e is KeyStoreException && e.message?.contains("Invalid key blob", ignoreCase = true) == true) ||
                (e.message?.contains("Failed to create operation", ignoreCase = true) == true)
    }}
