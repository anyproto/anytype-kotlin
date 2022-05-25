package com.anytypeio.anytype.domain.auth.interactor

import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.Interactor

/**
 * Use case for logging out.
 */
class Logout(
    private val repo: AuthRepository
) : Interactor<Logout.Params>() {

    override suspend fun run(params: Params) {
        repo.logout(params.clearLocalRepositoryData)
    }

    class Params(
        val clearLocalRepositoryData: Boolean = false
    )
}