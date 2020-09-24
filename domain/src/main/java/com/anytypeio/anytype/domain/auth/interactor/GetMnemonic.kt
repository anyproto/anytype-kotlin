package com.anytypeio.anytype.domain.auth.interactor

import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either

/**
 * Use case for retrieving mnemonic associated with current user.
 */
class GetMnemonic(
    private val repository: AuthRepository
) : BaseUseCase<String, Unit>() {

    override suspend fun run(params: Unit) = try {
        repository.getMnemonic().let { mnemonic ->
            Either.Right(mnemonic)
        }
    } catch (e: Throwable) {
        Either.Left(e)
    }
}