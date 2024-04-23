package com.anytypeio.anytype.domain.auth.interactor

import com.anytypeio.anytype.core_models.Account
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.platform.MetricsProvider
import com.anytypeio.anytype.domain.workspace.SpaceManager
import javax.inject.Inject

/**
 * Creates an account, then stores it and sets as current user account.
 */
open class CreateAccount @Inject constructor(
    private val repository: AuthRepository,
    // TODO rename config storage
    private val configStorage: ConfigStorage,
    private val metricsProvider: MetricsProvider,
    private val awaitAccountStartManager: AwaitAccountStartManager,
    private val spaceManager: SpaceManager,
    dispatcher: AppCoroutineDispatchers
) : ResultInteractor<CreateAccount.Params, Account>(dispatcher.io) {

    override suspend fun doWork(params: Params): Account {
        repository.setMetrics(
            version = metricsProvider.getVersion(),
            platform = metricsProvider.getPlatform()
        )

        val networkMode = repository.getNetworkMode()

        val command = Command.AccountCreate(
            name = params.name,
            avatarPath = params.avatarPath,
            icon = params.iconGradientValue,
            networkMode = networkMode.networkMode,
            networkConfigFilePath = networkMode.storedFilePath,
            preferYamuxTransport = networkMode.useReserveMultiplexLib
        )
        val setup = repository.createAccount(command)
        with(repository) {
            saveAccount(setup.account)
            setCurrentAccount(setup.account.id)
        }
        configStorage.set(setup.config)
        spaceManager.set(setup.config.space)
        awaitAccountStartManager.setIsStarted(true)
        return setup.account
    }

    /**
     * @property avatarPath optional avatar image file path
     * @property name username
     * @property iconGradientValue random icon gradient value for new account/space background
     */
    class Params(
        val name: String,
        val avatarPath: String? = null,
        val iconGradientValue: Int
    )
}