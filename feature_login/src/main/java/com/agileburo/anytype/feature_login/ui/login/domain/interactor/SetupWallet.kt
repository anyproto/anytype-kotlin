package com.agileburo.anytype.feature_login.ui.login.domain.interactor

import com.agileburo.anytype.core_utils.common.Either
import com.agileburo.anytype.feature_login.ui.login.domain.common.BaseUseCase
import com.agileburo.anytype.feature_login.ui.login.domain.repository.AuthRepository

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

    class Params(val path: String)
}