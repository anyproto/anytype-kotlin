package com.anytypeio.anytype.domain.auth.interactor

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.domain.platform.InitialParamsProvider
import com.anytypeio.anytype.domain.workspace.SpaceManager
import javax.inject.Inject

/**
 * Blocking use-case for resuming account session when application is killed by OS.
 */
class ResumeAccount @Inject constructor(
    private val repository: AuthRepository,
    private val pathProvider: PathProvider,
    private val configStorage: ConfigStorage,
    private val initialParamsProvider: InitialParamsProvider,
    private val awaitAccountStartManager: AwaitAccountStartManager,
    private val settings: UserSettingsRepository,
    private val spaceManager: SpaceManager
) : BaseUseCase<Id, BaseUseCase.None>() {

    override suspend fun run(params: None) = proceedWithResuming()

    private suspend fun proceedWithResuming() = safe {
        repository.setInitialParams(initialParamsProvider.toCommand())
        val mnemonic = repository.getMnemonic()
        if (mnemonic.isNullOrBlank()) throw IllegalStateException("Mnemonic is empty")
        repository.recoverWallet(
            path = pathProvider.providePath(),
            mnemonic = mnemonic
        )

        val networkMode = repository.getNetworkMode()

        val currentAccountId = repository.getCurrentAccountId()

        val command = Command.AccountSelect(
            id = currentAccountId,
            path = pathProvider.providePath(),
            networkMode = networkMode.networkMode,
            networkConfigFilePath = networkMode.storedFilePath
        )
        val result = repository.selectAccount(command).let { setup ->
            configStorage.set(config = setup.config, accountId = currentAccountId)
            val lastSessionSpace = settings.getCurrentSpace()
            if (lastSessionSpace != null) {
                val result = spaceManager.set(lastSessionSpace.id)
                if (result.isFailure) {
                    // Falling back to the default space
                    spaceManager.set(setup.config.space)
                }
            }
            setup.account.id
        }
        awaitAccountStartManager.setState(AwaitAccountStartManager.State.Started)
        result
    }
}