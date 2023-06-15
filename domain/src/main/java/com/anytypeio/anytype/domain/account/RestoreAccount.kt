package com.anytypeio.anytype.domain.account

import com.anytypeio.anytype.core_models.AccountStatus
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.BaseUseCase

class RestoreAccount(
    private val repo: AuthRepository,
    dispatchers: AppCoroutineDispatchers,
) : BaseUseCase<AccountStatus, BaseUseCase.None>(context = dispatchers.io) {

    override suspend fun run(params: None) = safe {
        repo.restoreAccount()
    }
}