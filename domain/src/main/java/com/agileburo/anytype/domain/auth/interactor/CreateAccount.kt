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
            name = params.name,
            avatarPath = params.avatarPath
        ).let { account ->
            repository.saveAccount(account)
        }.let {
            Either.Right(it)
        }
    } catch (e: Throwable) {
        Either.Left(e)
    }

    /**
     * @property avatarPath optional avatar image file path
     * @property name username
     */
    class Params(
        val name: String,
        val avatarPath: String? = null
    )
}