package com.anytypeio.anytype.domain.auth.interactor

import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.BaseUseCase.None
import com.anytypeio.anytype.domain.base.Interactor

/**
 * Use case for logging out.
 */
class Logout(
    private val repo: AuthRepository
) : Interactor<None>() {
    override suspend fun run(params: None) {
        repo.logout()
    }
}