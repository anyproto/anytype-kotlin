package com.agileburo.anytype.domain.auth.interactor

import com.agileburo.anytype.domain.auth.repo.AuthRepository
import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either

/**
 * Use case for selecting user account.
 */
class StartAccount(
    private val repository: AuthRepository
) : BaseUseCase<Unit, StartAccount.Params>() {

    override suspend fun run(params: Params) = try {
        repository.startAccount(
            id = params.id,
            path = params.path
        ).let { account ->
            with(repository) {
                saveAccount(account)
                setCurrentAccount(account.id)
            }
        }.let {
            Either.Right(it)
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