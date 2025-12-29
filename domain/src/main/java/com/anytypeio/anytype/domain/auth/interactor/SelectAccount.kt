package com.anytypeio.anytype.domain.auth.interactor

import com.anytypeio.anytype.core_models.AccountStatus
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.platform.InitialParamsProvider
import javax.inject.Inject

/**
 * Use case for selecting user account.
 */
class SelectAccount @Inject constructor(
    private val repository: AuthRepository,
    private val configStorage: ConfigStorage,
    private val initialParamsProvider: InitialParamsProvider,
    private val awaitAccountStartManager: AwaitAccountStartManager
) : BaseUseCase<StartAccountResult, SelectAccount.Params>() {

    override suspend fun run(params: Params) = safe {
        repository.setInitialParams(initialParamsProvider.toCommand())

        val networkMode = repository.getNetworkMode()

        val command = Command.AccountSelect(
            id = params.id,
            path = params.path,
            networkMode = networkMode.networkMode,
            networkConfigFilePath = networkMode.storedFilePath,
            preferYamuxTransport = networkMode.useReserveMultiplexLib
        )
        val setup = repository.selectAccount(command)
        with(repository) {
            saveAccount(setup.account)
            setCurrentAccount(setup.account.id)
        }
        configStorage.set(config = setup.config, accountId = setup.account.id)
        awaitAccountStartManager.setState(AwaitAccountStartManager.State.Started)
        StartAccountResult(setup.config.analytics, setup.status)
    }

    /**
     * @property id account id
     * @property path path for restored account's repository
     */
    class Params(
        val id: String,
        val path: String
    )
}

typealias StartAccountResult = Pair<Id, AccountStatus>