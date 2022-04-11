package com.anytypeio.anytype.domain.auth.interactor

import com.anytypeio.anytype.core_models.AccountStatus
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.config.FlavourConfigProvider

/**
 * Use case for selecting user account.
 */
class StartAccount(
    private val repository: AuthRepository,
    private val flavourConfigProvider: FlavourConfigProvider
) : BaseUseCase<Pair<Id, AccountStatus>, StartAccount.Params>() {

    override suspend fun run(params: Params) = safe {
        val (account, config, status) = repository.startAccount(
            id = params.id,
            path = params.path
        )
        with(repository) {
            saveAccount(account)
            setCurrentAccount(account.id)
            flavourConfigProvider.set(
                enableDataView = config.enableDataView ?: false,
                enableDebug = config.enableDebug ?: false,
                enableChannelSwitch = config.enableChannelSwitch ?: false,
                enableSpaces = config.enableSpaces ?: false
            )
        }
        Pair(account.id, status)
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