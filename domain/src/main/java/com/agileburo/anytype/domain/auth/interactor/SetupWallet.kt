package com.agileburo.anytype.domain.auth.interactor

import com.agileburo.anytype.domain.auth.repo.AuthRepository
import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either

/**
 * Use case for creating new wallet.
 */
open class SetupWallet(
    private val repository: AuthRepository
) : BaseUseCase<Unit, SetupWallet.Params>() {

    override suspend fun run(params: Params) = try {
        val wallet = repository.createWallet(
            path = params.path
        )
        repository.saveMnemonic(wallet.mnemonic).let {
            Either.Right(it)
        }
    } catch (e: Throwable) {
        Either.Left(e)
    }

    /**
     * @property path repository path.
     */
    class Params(val path: String)
}