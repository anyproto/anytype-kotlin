package com.anytypeio.anytype.domain.auth.interactor

import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either

/**
 * Use case for logging out.
 */
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