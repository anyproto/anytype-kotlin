package com.anytypeio.anytype.domain.auth.interactor

import com.anytypeio.anytype.core_models.AccountStatus
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.FeaturesConfigProvider
import com.anytypeio.anytype.domain.platform.MetricsProvider
import javax.inject.Inject

/**
 * Use case for selecting user account.
 */
class SelectAccount @Inject constructor(
    private val repository: AuthRepository,
    private val configStorage: ConfigStorage,
    private val featuresConfigProvider: FeaturesConfigProvider,
    private val metricsProvider: MetricsProvider,
    private val awaitAccountStartManager: AwaitAccountStartManager
) : BaseUseCase<StartAccountResult, SelectAccount.Params>() {

    override suspend fun run(params: Params) = safe {
        repository.setMetrics(
            version = metricsProvider.getVersion(),
            platform = metricsProvider.getPlatform()
        )

        val networkMode = repository.getNetworkMode()

        val command = Command.AccountSelect(
            id = params.id,
            path = params.path,
            networkMode = networkMode.networkMode,
            networkConfigFilePath = networkMode.storedFilePath,
            preferYamuxTransport = networkMode.useReserveMiltiplexLibrary
        )
        val setup = repository.selectAccount(command)
        with(repository) {
            saveAccount(setup.account)
            setCurrentAccount(setup.account.id)
        }
        featuresConfigProvider.set(
            enableDataView = setup.features.enableDataView ?: false,
            enableDebug = setup.features.enableDebug ?: false,
            enableChannelSwitch = setup.features.enablePrereleaseChannel ?: false,
            enableSpaces = setup.features.enableSpaces ?: false
        )
        configStorage.set(config = setup.config)
        awaitAccountStartManager.setIsStarted(true)
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