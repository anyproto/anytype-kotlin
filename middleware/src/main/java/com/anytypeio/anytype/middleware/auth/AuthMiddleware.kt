package com.anytypeio.anytype.middleware.auth

import com.anytypeio.anytype.core_models.AccountSetup
import com.anytypeio.anytype.core_models.AccountStatus
import com.anytypeio.anytype.data.auth.model.WalletEntity
import com.anytypeio.anytype.data.auth.repo.AuthRemote
import com.anytypeio.anytype.middleware.EventProxy
import com.anytypeio.anytype.middleware.converters.toAccountEntity
import com.anytypeio.anytype.middleware.interactor.Middleware
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AuthMiddleware(
    private val middleware: Middleware,
    private val events: EventProxy
) : AuthRemote {

    override suspend fun selectAccount(
        id: String, path: String
    ): AccountSetup = middleware.accountSelect(id, path)

    override suspend fun createAccount(
        name: String,
        avatarPath: String?,
        iconGradientValue: Int,
    ) : AccountSetup = middleware.accountCreate(
        name = name,
        path = avatarPath,
        iconGradientValue = iconGradientValue
    )

    override suspend fun deleteAccount(): AccountStatus = middleware.accountDelete()
    override suspend fun restoreAccount(): AccountStatus = middleware.accountRestore()

    override suspend fun recoverAccount() = withContext(Dispatchers.IO) {
        middleware.accountRecover()
    }

    override fun observeAccounts() = events
        .flow()
        .filter { event ->
            event.messages.any { message ->
                message.accountShow != null
            }
        }
        .map { event ->
            event.messages.filter { message ->
                message.accountShow != null
            }
        }
        .flatMapConcat { messages -> messages.asFlow() }
        .map {
            val event = it.accountShow
            checkNotNull(event)
            event.toAccountEntity()
        }

    override suspend fun createWallet(
        path: String
    ) = WalletEntity(mnemonic = middleware.walletCreate(path).mnemonic)

    override suspend fun recoverWallet(path: String, mnemonic: String) {
        middleware.walletRecover(path, mnemonic)
    }

    override suspend fun convertWallet(entropy: String): String = middleware.walletConvert(entropy)

    override suspend fun logout(clearLocalRepositoryData: Boolean) {
        middleware.accountStop(clearLocalRepositoryData)
    }

    override suspend fun getVersion(): String {
        return middleware.versionGet().version
    }

    override suspend fun setMetrics(platform: String, version: String) {
        middleware.metricsSetParameters(
            platform = platform,
            version = version
        )
    }
}