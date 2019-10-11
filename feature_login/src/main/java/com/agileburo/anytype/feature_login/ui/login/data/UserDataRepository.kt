package com.agileburo.anytype.feature_login.ui.login.data

import com.agileburo.anytype.feature_login.ui.login.domain.model.Account
import com.agileburo.anytype.feature_login.ui.login.domain.model.Image
import com.agileburo.anytype.feature_login.ui.login.domain.repository.UserRepository
import com.agileburo.anytype.middleware.Event
import com.agileburo.anytype.middleware.EventProxy
import com.agileburo.anytype.middleware.interactor.Middleware
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

class UserDataRepository(
    private val middleware: Middleware,
    private val proxy: EventProxy
) : UserRepository {

    override fun observeAccounts(): Flow<Account> {
        return proxy
            .flow()
            .onEach { event ->
                Timber.d("Event : $event")
            }
            .map { event ->

                if (event is Event.AccountAdd) {
                    Account(
                        id = event.id,
                        name = event.name,
                        image = Image(
                            id = "Id",
                            size = Image.ImageSize.SMALL
                        )
                    )
                } else
                    TODO()
            }
    }

    override suspend fun createAccount(name: String): Account {
        return middleware.createAccount(name).let { response ->
            Account(
                id = response.id,
                name = response.name
            )
        }
    }

    override suspend fun recoverAccount() {
        middleware.recoverAccount()
    }

    override suspend fun selectAccount(id: String, path: String): Account {
        return middleware.selectAccount(id, path).let { response ->
            Account(
                id = response.id,
                name = response.name
            )
        }
    }

    override suspend fun saveAccount(account: Account) {
        // TODO
    }
}