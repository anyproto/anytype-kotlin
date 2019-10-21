package com.agileburo.anytype.domain.auth.interactor

import com.agileburo.anytype.domain.auth.repo.AuthRepository
import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either

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