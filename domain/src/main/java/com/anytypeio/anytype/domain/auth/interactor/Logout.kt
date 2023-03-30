package com.anytypeio.anytype.domain.auth.interactor

import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.Interactor
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.UserSettingsRepository

/**
 * Use case for logging out.
 */
class Logout(
    private val repo: AuthRepository,
    private val config: ConfigStorage,
    private val user: UserSettingsRepository,
    dispatchers: AppCoroutineDispatchers,
) : Interactor<Logout.Params>(context = dispatchers.io) {

    override suspend fun run(params: Params) {
        repo.logout(params.clearLocalRepositoryData)
        user.clear()
        config.clear()
    }

    class Params(
        val clearLocalRepositoryData: Boolean = false
    )
}