package com.agileburo.anytype.feature_login.ui.login.data

import com.agileburo.anytype.feature_login.ui.login.domain.model.Account
import com.agileburo.anytype.feature_login.ui.login.domain.repository.UserRepository
import kotlinx.coroutines.delay

class UserDataRepository : UserRepository {

    override suspend fun getAccounts(): List<Account> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun createAccount(name: String) {
        delay(3000)
    }

    override suspend fun getCurrentAccount(): Account {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun selectAccount(id: String) {
        delay(2000)
    }
}