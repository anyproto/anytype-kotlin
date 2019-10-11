package com.agileburo.anytype.feature_login.ui.login.domain.interactor

import com.agileburo.anytype.core_utils.common.Either
import com.agileburo.anytype.feature_login.ui.login.domain.common.BaseUseCase
import com.agileburo.anytype.feature_login.ui.login.domain.repository.UserRepository

class RecoverAccount(
    private val repository: UserRepository
) : BaseUseCase<Unit, RecoverAccount.Params>() {

    override suspend fun run(params: Params) = try {
        repository.recoverAccount().let {
            Either.Right(it)
        }
    } catch (e: Throwable) {
        Either.Left(e)
    }

    class Params
}