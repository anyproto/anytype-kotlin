package com.anytypeio.anytype.domain.auth.interactor

import com.anytypeio.anytype.core_models.AccountStatus
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.FeaturesConfigProvider

/**
 * Use case for selecting user account.
 */
class StartAccount(
    private val repository: AuthRepository,
    private val configStorage: ConfigStorage,
    private val featuresConfigProvider: FeaturesConfigProvider
) : BaseUseCase<StartAccountResult, StartAccount.Params>() {

    override suspend fun run(params: Params) = safe {
        val setup = repository.startAccount(
            id = params.id,
            path = params.path
        )
        with(repository) {
            saveAccount(setup.account)
            setCurrentAccount(setup.account.id)
            featuresConfigProvider.set(
                enableDataView = setup.features.enableDataView ?: false,
                enableDebug = setup.features.enableDebug ?: false,
                enableChannelSwitch = setup.features.enablePrereleaseChannel ?: false,
                enableSpaces = setup.features.enableSpaces ?: false
            )
            configStorage.set(config = setup.config)
        }
        StartAccountResult(setup.account.id, setup.status)
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