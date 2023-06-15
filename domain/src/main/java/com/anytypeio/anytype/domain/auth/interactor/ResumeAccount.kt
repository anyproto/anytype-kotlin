package com.anytypeio.anytype.domain.auth.interactor

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.FeaturesConfigProvider
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.domain.platform.MetricsProvider
import com.anytypeio.anytype.domain.workspace.WorkspaceManager

/**
 * Blocking use-case for resuming account session when application is killed by OS.
 */
class ResumeAccount(
    private val repository: AuthRepository,
    private val pathProvider: PathProvider,
    private val configStorage: ConfigStorage,
    private val featuresConfigProvider: FeaturesConfigProvider,
    private val workspaceManager: WorkspaceManager,
    private val metricsProvider: MetricsProvider
) : BaseUseCase<Id, BaseUseCase.None>() {

    override suspend fun run(params: None) = proceedWithResuming()

    private suspend fun proceedWithResuming() = safe {
        repository.setMetrics(
            version = metricsProvider.getVersion(),
            platform = metricsProvider.getPlatform()
        )
        repository.recoverWallet(
            path = pathProvider.providePath(),
            mnemonic = repository.getMnemonic()
        )
        repository.selectAccount(
            id = repository.getCurrentAccountId(),
            path = pathProvider.providePath()
        ).let { setup ->
            featuresConfigProvider.set(
                enableDataView = setup.features.enableDataView ?: false,
                enableDebug = setup.features.enableDebug ?: false,
                enableChannelSwitch = setup.features.enablePrereleaseChannel ?: false,
                enableSpaces = setup.features.enableSpaces ?: false
            )
            configStorage.set(config = setup.config)
            workspaceManager.setCurrentWorkspace(setup.config.workspace)
            setup.account.id
        }
    }
}