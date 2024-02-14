package com.anytypeio.anytype.domain.auth.interactor

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.domain.platform.MetricsProvider
import com.anytypeio.anytype.domain.workspace.SpaceManager
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers

/**
 * Sets current account for current application session.
 */
class LaunchAccount @Inject constructor(
    private val repository: AuthRepository,
    private val pathProvider: PathProvider,
    private val context: CoroutineContext = Dispatchers.IO,
    private val configStorage: ConfigStorage,
    private val spaceManager: SpaceManager,
    private val metricsProvider: MetricsProvider,
    private val settings: UserSettingsRepository,
    private val awaitAccountStartManager: AwaitAccountStartManager
) : BaseUseCase<Id, BaseUseCase.None>(context) {

    override suspend fun run(params: None) = safe {
        repository.setMetrics(
            version = metricsProvider.getVersion(),
            platform = metricsProvider.getPlatform()
        )

        val networkMode = repository.getNetworkMode()

        val command = Command.AccountSelect(
            id = repository.getCurrentAccountId(),
            path = pathProvider.providePath(),
            networkMode = networkMode.networkMode,
            networkConfigFilePath = networkMode.storedFilePath
        )

        repository.selectAccount(command).let { setup ->
            repository.updateAccount(setup.account)
            configStorage.set(config = setup.config)
            val lastSessionSpace = settings.getCurrentSpace()
            if (lastSessionSpace != null) {
                val result = spaceManager.set(lastSessionSpace.id)
                if (result.isFailure) {
                    // Falling back to the default space
                    spaceManager.set(setup.config.space)
                }
            } else {
                spaceManager.set(setup.config.space)
            }
            awaitAccountStartManager.setIsStarted(true)
            setup.config.analytics
        }
    }
}