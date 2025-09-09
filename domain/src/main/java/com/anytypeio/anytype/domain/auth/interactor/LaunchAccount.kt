package com.anytypeio.anytype.domain.auth.interactor

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.domain.platform.InitialParamsProvider
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
    private val configStorage: ConfigStorage,
    private val spaceManager: SpaceManager,
    private val initialParamsProvider: InitialParamsProvider,
    private val settings: UserSettingsRepository,
    private val awaitAccountStartManager: AwaitAccountStartManager,
    context: CoroutineContext = Dispatchers.IO
    ) : BaseUseCase<Pair<String, String>, BaseUseCase.None>(context) {

    override suspend fun run(params: None) = safe {
        repository.setInitialParams(initialParamsProvider.toCommand())

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
                spaceManager.set(lastSessionSpace.id)
            }
            awaitAccountStartManager.setState(AwaitAccountStartManager.State.Started)
            setup.config.analytics to setup.config.network
        }
    }
}