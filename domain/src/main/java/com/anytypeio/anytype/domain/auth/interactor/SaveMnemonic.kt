package com.anytypeio.anytype.domain.auth.interactor

import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either

/**
 * Use case for saving current user's mnemonic.
 */
class SaveMnemonic(
    private val repository: AuthRepository
) : BaseUseCase<Unit, SaveMnemonic.Params>() {

    override suspend fun run(params: Params) = try {
        repository.saveMnemonic(
            mnemonic = params.mnemonic
        ).let {
            Either.Right(it)
        }
    } catch (e: Throwable) {
        Either.Left(e)
    }

    class Params(val mnemonic: String)
}