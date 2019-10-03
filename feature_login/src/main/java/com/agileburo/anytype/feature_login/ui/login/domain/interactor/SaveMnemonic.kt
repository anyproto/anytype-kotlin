package com.agileburo.anytype.feature_login.ui.login.domain.interactor

import com.agileburo.anytype.core_utils.Either
import com.agileburo.anytype.feature_login.ui.login.domain.common.BaseUseCase
import com.agileburo.anytype.feature_login.ui.login.domain.repository.AuthRepository

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