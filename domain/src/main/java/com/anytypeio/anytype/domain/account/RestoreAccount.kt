package com.anytypeio.anytype.domain.account

import com.anytypeio.anytype.core_models.AccountStatus
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.BaseUseCase

class RestoreAccount(private val repo: AuthRepository) : BaseUseCase<AccountStatus, BaseUseCase.None>() {
    override suspend fun run(params: None) = safe { repo.restoreAccount() }
}