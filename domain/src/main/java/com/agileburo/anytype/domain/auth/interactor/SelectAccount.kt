package com.agileburo.anytype.domain.auth.interactor

import com.agileburo.anytype.domain.auth.repo.AuthRepository
import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either

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

    class Params(
        val id: String,
        val path: String
    )
}