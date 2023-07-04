package com.anytypeio.anytype.domain.auth.interactor

import com.anytypeio.anytype.core_models.Account
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor

class GetAccount(
    private val repo: AuthRepository,
    dispatcher: AppCoroutineDispatchers
) : ResultInteractor<Unit, Account>(dispatcher.io) {

    override suspend fun doWork(params: Unit): Account {
        return repo.getCurrentAccount()
    }
}