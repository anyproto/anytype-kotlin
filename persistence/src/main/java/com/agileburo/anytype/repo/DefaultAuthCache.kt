package com.agileburo.anytype.repo

import android.content.SharedPreferences
import com.agileburo.anytype.data.auth.model.AccountEntity
import com.agileburo.anytype.data.auth.repo.AuthCache
import com.agileburo.anytype.db.AnytypeDatabase
import com.agileburo.anytype.model.AccountTable
import timber.log.Timber

class DefaultAuthCache(
    private val db: AnytypeDatabase,
    private val prefs: SharedPreferences
) : AuthCache {

    override suspend fun saveAccount(account: AccountEntity) {
        db.accountDao().insert(
            AccountTable(
                id = account.id,
                name = account.name
            )
        )
    }

    override suspend fun saveMnemonic(mnemonic: String) {
        Timber.d("Saving mnemonic: $mnemonic")
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

    companion object {
        const val MNEMONIC_KEY = "mnemonic"
    }
}