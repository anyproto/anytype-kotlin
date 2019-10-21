package com.agileburo.anytype.domain.auth.interactor

import com.agileburo.anytype.domain.auth.repo.AuthRepository
import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either

class Logout(
    private val repository: AuthRepository
) : BaseUseCase<Unit, BaseUseCase.None>() {

    override suspend fun run(params: None) = try {
        repository.logout().let {
            Either.Right(it)
        }
    } catch (e: Throwable) {
        Either.Left(e)
    }
}