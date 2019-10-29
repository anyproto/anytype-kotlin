package com.agileburo.anytype.domain.auth.interactor

import com.agileburo.anytype.domain.auth.repo.AuthRepository
import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either

/**
 * Use case for selecting user account.
 */
class SelectAccount(
    private val repository: AuthRepository
) : BaseUseCase<Unit, SelectAccount.Params>() {

    override suspend fun run(params: Params) = try {
        repository.selectAccount(
            id = params.id,
            path = params.path
        ).let { account ->
            repository.saveAccount(account)
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