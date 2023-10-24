package com.anytypeio.anytype.domain.auth.interactor

import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.Interactor
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.workspace.SpaceManager
import javax.inject.Inject

/**
 * Use case for logging out.
 */
class Logout @Inject constructor(
    private val repo: AuthRepository,
    private val config: ConfigStorage,
    private val user: UserSettingsRepository,
    private val spaceManager: SpaceManager,
    dispatchers: AppCoroutineDispatchers,
) : Interactor<Logout.Params>(context = dispatchers.io) {

    override suspend fun run(params: Params) {
        repo.logout(clearLocalRepositoryData = params.clearLocalRepositoryData)
        user.clear()
        config.clear()
        spaceManager.clear()
    }

    class Params(
        val clearLocalRepositoryData: Boolean = false
    )
}