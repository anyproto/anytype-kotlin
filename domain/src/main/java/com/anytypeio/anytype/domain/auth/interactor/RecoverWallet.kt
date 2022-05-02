package com.anytypeio.anytype.domain.auth.interactor

import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either

/**
 * Use case for recovering wallet via recovery (aka keychain) phrase (mnemonic).
 */
class RecoverWallet(
    private val repository: AuthRepository
) : BaseUseCase<Unit, RecoverWallet.Params>() {

    override suspend fun run(params: Params) = try {
        repository.recoverWallet(
            path = params.path,
            mnemonic = params.mnemonic
        ).let {
            Either.Right(it)
        }
    } catch (e: Throwable) {
        Either.Left(e)
    }

    class Params(val path: String, val mnemonic: String)
}