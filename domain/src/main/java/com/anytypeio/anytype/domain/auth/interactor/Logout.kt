package com.anytypeio.anytype.domain.auth.interactor

import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.Interactor
import com.anytypeio.anytype.domain.config.ConfigStorage

/**
 * Use case for logging out.
 */
class Logout(
    private val repo: AuthRepository,
    private val provider: ConfigStorage
) : Interactor<Logout.Params>() {

    override suspend fun run(params: Params) {
        repo.logout(params.clearLocalRepositoryData)
        provider.clear()
    }

    class Params(
        val clearLocalRepositoryData: Boolean = false
    )
}