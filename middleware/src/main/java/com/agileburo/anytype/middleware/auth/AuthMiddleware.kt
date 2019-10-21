package com.agileburo.anytype.middleware.auth

import com.agileburo.anytype.data.auth.model.AccountEntity
import com.agileburo.anytype.data.auth.model.WalletEntity
import com.agileburo.anytype.data.auth.repo.AuthRemote
import com.agileburo.anytype.middleware.Event
import com.agileburo.anytype.middleware.EventProxy
import com.agileburo.anytype.middleware.interactor.Middleware
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

class AuthMiddleware(
    private val middleware: Middleware,
    private val events: EventProxy
) : AuthRemote {

    override suspend fun selectAccount(
        id: String, path: String
    ) = middleware.selectAccount(id, path).let { response ->
        AccountEntity(
            id = response.id,
            name = response.name
        )
    }

    override suspend fun createAccount(
        name: String
    ) = middleware.createAccount(name).let { response ->
        AccountEntity(
            id = response.id,
            name = response.name
        )
    }

    override suspend fun recoverAccount() {
        middleware.recoverAccount()
    }

    override fun observeAccounts() = events
        .flow()
        .filter { event -> event is Event.AccountAdd }
        .map { event -> event as Event.AccountAdd }
        .map { event ->
            AccountEntity(
                id = event.id,
                name = event.name
            )
        }

    override suspend fun createWallet(
        path: String
    ) = WalletEntity(mnemonic = middleware.createWallet(path).mnemonic)

    override suspend fun recoverWallet(path: String, mnemonic: String) {
        middleware.recoverWallet(path, mnemonic)
    }
}