package com.anytypeio.anytype.domain.auth.interactor

import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.FeaturesConfigProvider
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers

/**
 * Sets current account for current application session.
 */
class LaunchAccount(
    private val repository: AuthRepository,
    private val pathProvider: PathProvider,
    private val context: CoroutineContext = Dispatchers.IO,
    private val configStorage: ConfigStorage,
    private val featuresConfigProvider: FeaturesConfigProvider,
    private val workspaceManager: WorkspaceManager
) : BaseUseCase<String, BaseUseCase.None>(context) {

    override suspend fun run(params: None) = try {
        repository.selectAccount(
            id = repository.getCurrentAccountId(),
            path = pathProvider.providePath()
        ).let { setup ->
            repository.updateAccount(setup.account)
            featuresConfigProvider.set(
                enableDataView = setup.features.enableDataView ?: false,
                enableDebug = setup.features.enableDebug ?: false,
                enableChannelSwitch = setup.features.enablePrereleaseChannel ?: false,
                enableSpaces = setup.features.enableSpaces ?: false
            )
            configStorage.set(config = setup.config)
            workspaceManager.setCurrentWorkspace(setup.config.workspace)
            Either.Right(setup.account.id)
        }
    } catch (e: Throwable) {
        Either.Left(e)
    }
}