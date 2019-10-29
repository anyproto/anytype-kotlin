package com.agileburo.anytype.domain.auth.interactor

import com.agileburo.anytype.domain.auth.repo.AuthRepository
import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either

/**
 * Use case for recovering wallet via keychain phrase (mnemonic).
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