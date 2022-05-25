package com.anytypeio.anytype.domain.auth.interactor

import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either

/**
 * Use case for starting loading remote accounts associated with current wallet.
 */
class StartLoadingAccounts(
    private val repository: AuthRepository
) : BaseUseCase<Unit, StartLoadingAccounts.Params>() {

    override suspend fun run(params: Params) = try {
        repository.startLoadingAccounts().let {
            Either.Right(it)
        }
    } catch (e: Throwable) {
        Either.Left(e)
    }

    class Params
}