package com.anytypeio.anytype.domain.auth.interactor

import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either

/**
 * Use case for selecting user account.
 */
class StartAccount(
    private val repository: AuthRepository
) : BaseUseCase<String, StartAccount.Params>() {

    override suspend fun run(params: Params) = try {
        repository.startAccount(
            id = params.id,
            path = params.path
        ).let { account ->
            with(repository) {
                saveAccount(account)
                setCurrentAccount(account.id)
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