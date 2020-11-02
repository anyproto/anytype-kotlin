package com.anytypeio.anytype.domain.auth.interactor

import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either

class ConvertWallet(
    private val authRepository: AuthRepository
) : BaseUseCase<String, ConvertWallet.Request>() {

    override suspend fun run(params: Request): Either<Throwable, String> = safe {
        val mnemonic = authRepository.convertWallet(entropy = params.entropy)
        authRepository.saveMnemonic(mnemonic)
        mnemonic
    }

    data class Request(val entropy: String)
}