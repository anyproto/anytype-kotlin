package com.anytypeio.anytype.persistence.repo

import android.content.SharedPreferences
import com.anytypeio.anytype.data.auth.model.AccountEntity
import com.anytypeio.anytype.data.auth.repo.AuthCache
import com.anytypeio.anytype.persistence.db.AnytypeDatabase
import com.anytypeio.anytype.persistence.mapper.toEntity
import com.anytypeio.anytype.persistence.mapper.toTable

class DefaultAuthCache(
    private val db: AnytypeDatabase,
    private val defaultPrefs: SharedPreferences,
    private val encryptedPrefs: SharedPreferences
) : AuthCache {

    override suspend fun saveAccount(account: AccountEntity) {
        db.accountDao().insert(account.toTable())
    }

    override suspend fun updateAccount(account: AccountEntity) {
        db.accountDao().update(account.toTable())
    }

    override suspend fun getCurrentAccount() = getCurrentAccountId().let { id ->
        db.accountDao().getAccount(id)?.toEntity()
            ?: throw IllegalStateException("Account with the following id not found: $id")
    }

    /**
     * N.B. Migrating sensitive data from default to encrypted prefs.
     */
    override suspend fun getCurrentAccountId(): String {
        val nonEncryptedId = defaultPrefs.getString(CURRENT_ACCOUNT_ID_KEY, null)
        return if (nonEncryptedId != null) {
            encryptedPrefs.edit().putString(CURRENT_ACCOUNT_ID_KEY, nonEncryptedId).apply()
            defaultPrefs.edit().remove(CURRENT_ACCOUNT_ID_KEY).apply()
            nonEncryptedId
        } else {
            val encryptedId = encryptedPrefs.getString(CURRENT_ACCOUNT_ID_KEY, null)
            encryptedId ?: throw IllegalStateException("Current account not set")
        }
    }

    /**
     * N.B. Migrating sensitive data from default to encrypted prefs.
     */
    override suspend fun saveMnemonic(mnemonic: String) {
        defaultPrefs.edit().remove(MNEMONIC_KEY).apply()
        encryptedPrefs.edit().putString(MNEMONIC_KEY, mnemonic).apply()
    }

    /**
     * N.B. Migrating sensitive data from default to encrypted prefs.
     */
    override suspend fun getMnemonic(): String {
        val nonEncryptedMnemonic = defaultPrefs.getString(MNEMONIC_KEY, null)
        return if (nonEncryptedMnemonic != null) {
            encryptedPrefs.edit().putString(MNEMONIC_KEY, nonEncryptedMnemonic).apply()
            defaultPrefs.edit().remove(MNEMONIC_KEY).apply()
            nonEncryptedMnemonic
        } else {
            val encryptedMnemonic = encryptedPrefs.getString(MNEMONIC_KEY, null)
            encryptedMnemonic ?: throw IllegalStateException("Mnemonic is missing")
        }
    }

    override suspend fun logout() {
        db.accountDao().clear()
        defaultPrefs
            .edit()
            .remove(MNEMONIC_KEY)
            .remove(CURRENT_ACCOUNT_ID_KEY)
            .apply()
        encryptedPrefs
            .edit()
            .remove(MNEMONIC_KEY)
            .remove(LAST_OPENED_OBJECT_KEY)
            .remove(CURRENT_ACCOUNT_ID_KEY)
            .apply()
    }

    override suspend fun getAccounts() = db.accountDao().getAccounts().map { it.toEntity() }

    /**
     * N.B. Migrating sensitive data from default to encrypted prefs.
     */
    override suspend fun setCurrentAccount(id: String) {
        defaultPrefs.edit().remove(CURRENT_ACCOUNT_ID_KEY).apply()
        encryptedPrefs.edit().putString(CURRENT_ACCOUNT_ID_KEY, id).apply()
    }

    override suspend fun saveLastOpenedObject(id: String) {
        encryptedPrefs.edit().putString(LAST_OPENED_OBJECT_KEY, id).apply()
    }

    override suspend fun getLastOpenedObject(): String? {
        return encryptedPrefs.getString(LAST_OPENED_OBJECT_KEY, null)
    }

    override suspend fun clearLastOpenedObject() {
        encryptedPrefs.edit().remove(LAST_OPENED_OBJECT_KEY).apply()
    }

    companion object {
        const val MNEMONIC_KEY = "mnemonic"
        const val LAST_OPENED_OBJECT_KEY = "last_opened_object"
        const val CURRENT_ACCOUNT_ID_KEY = "current_account"
    }
}