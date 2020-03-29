package com.agileburo.anytype.persistence.repo

import android.content.SharedPreferences
import com.agileburo.anytype.data.auth.model.AccountEntity
import com.agileburo.anytype.data.auth.repo.AuthCache
import com.agileburo.anytype.persistence.db.AnytypeDatabase
import com.agileburo.anytype.persistence.mapper.toEntity
import com.agileburo.anytype.persistence.mapper.toTable

class DefaultAuthCache(
    private val db: AnytypeDatabase,
    private val prefs: SharedPreferences
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

    override suspend fun getCurrentAccountId(): String {
        val id: String? = prefs.getString(CURRENT_ACCOUNT_ID_KEY, null)
        return id ?: throw IllegalStateException("Current account not set")
    }

    override suspend fun saveMnemonic(mnemonic: String) {
        prefs.edit().putString(MNEMONIC_KEY, mnemonic).apply()
    }

    override suspend fun getMnemonic(): String {
        return prefs.getString(MNEMONIC_KEY, null)
            ?: throw IllegalStateException("Mnemonic is missing.")
    }

    override suspend fun logout() {
        db.accountDao().clear()
        prefs.edit().putString(MNEMONIC_KEY, null).apply()
    }

    override suspend fun getAccounts() = db.accountDao().getAccounts().map { it.toEntity() }


    override suspend fun setCurrentAccount(id: String) {
        prefs.edit().putString(CURRENT_ACCOUNT_ID_KEY, id).apply()
    }

    companion object {
        const val MNEMONIC_KEY = "mnemonic"
        const val CURRENT_ACCOUNT_ID_KEY = "current_account"
    }
}