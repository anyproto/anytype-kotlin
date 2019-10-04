package com.agileburo.anytype.feature_login.ui.login.domain.interactor

import com.agileburo.anytype.core_utils.Either
import com.agileburo.anytype.feature_login.ui.login.domain.common.BaseUseCase
import com.agileburo.anytype.feature_login.ui.login.domain.repository.UserRepository

open class CreateAccount(
    private val userRepository: UserRepository
) : BaseUseCase<Unit, CreateAccount.Params>() {

    override suspend fun run(params: Params) = try {
        userRepository.createAccount(
            name = params.name
        ).let {
            Either.Right(it)
        }
    } catch (e: Throwable) {
        Either.Left(e)
    }

    class Params(val name: String)
}