package com.anytypeio.anytype.domain.auth.interactor

import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.config.FlavourConfigProvider

/**
 * Use case for selecting user account.
 */
class StartAccount(
    private val repository: AuthRepository,
    private val flavourConfigProvider: FlavourConfigProvider
) : BaseUseCase<String, StartAccount.Params>() {

    override suspend fun run(params: Params) = try {
        repository.startAccount(
            id = params.id,
            path = params.path
        ).let { pair ->
            val (account, config) = pair
            with(repository) {
                saveAccount(account)
                setCurrentAccount(account.id)
                flavourConfigProvider.set(
                    enableDataView = config.enableDataView ?: false,
                    enableDebug = config.enableDebug ?: false,
                    enableChannelSwitch = config.enableChannelSwitch ?: false
                )
            }
            account.id
        }.let { accountId ->
            Either.Right(accountId)
        }
    } catch (e: Throwable) {
        Either.Left(e)
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