package com.agileburo.anytype.domain.auth.interactor

import com.agileburo.anytype.domain.auth.repo.AuthRepository
import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either

/**
 * Creates an account, then stores it.
 */
open class CreateAccount(
    private val repository: AuthRepository
) : BaseUseCase<Unit, CreateAccount.Params>() {

    override suspend fun run(params: Params) = try {
        repository.createAccount(
            name = params.name
        ).let { account ->
            repository.saveAccount(account)
        }.let {
            Either.Right(it)
        }
    } catch (e: Throwable) {
        Either.Left(e)
    }

    class Params(val name: String)
}