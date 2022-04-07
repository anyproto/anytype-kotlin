package com.anytypeio.anytype.domain.auth.interactor

import com.anytypeio.anytype.domain.auth.model.Account
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.BaseUseCase

class GetAccount(
    private val repo: AuthRepository
) : BaseUseCase<Account, BaseUseCase.None>() {
    override suspend fun run(params: None) = safe { repo.getCurrentAccount() }
}