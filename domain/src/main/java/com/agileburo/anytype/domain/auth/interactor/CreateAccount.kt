package com.agileburo.anytype.domain.auth.interactor

import com.agileburo.anytype.domain.auth.repo.AuthRepository
import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either

/**
 * Creates an account, then stores it and sets as current user account.
 */
open class CreateAccount(
    private val repository: AuthRepository
) : BaseUseCase<Unit, CreateAccount.Params>() {

    override suspend fun run(params: Params) = try {
        repository.createAccount(
            name = params.name,
            avatarPath = params.avatarPath,
            invitationCode = params.invitationCode
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
     * @property avatarPath optional avatar image file path
     * @property name username
     */
    class Params(
        val name: String,
        val avatarPath: String? = null,
        val invitationCode: String
    )
}