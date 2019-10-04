package com.agileburo.anytype.feature_login.ui.login.domain.interactor

import com.agileburo.anytype.core_utils.common.Either
import com.agileburo.anytype.feature_login.ui.login.domain.common.BaseUseCase
import com.agileburo.anytype.feature_login.ui.login.domain.repository.AuthRepository

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