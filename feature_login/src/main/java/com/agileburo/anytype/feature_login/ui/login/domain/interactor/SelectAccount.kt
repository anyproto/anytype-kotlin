package com.agileburo.anytype.feature_login.ui.login.domain.interactor

import com.agileburo.anytype.core_utils.common.Either
import com.agileburo.anytype.feature_login.ui.login.domain.common.BaseUseCase
import com.agileburo.anytype.feature_login.ui.login.domain.repository.UserRepository

class SelectAccount(
    private val userRepository: UserRepository
) : BaseUseCase<Unit, SelectAccount.Params>() {

    override suspend fun run(params: Params) = try {
        userRepository.selectAccount(
            id = params.id,
            path = params.path
        ).let { account ->
            userRepository.saveAccount(account)
        }.let {
            Either.Right(it)
        }
    } catch (e: Throwable) {
        Either.Left(e)
    }

    class Params(
        val id: String,
        val path: String
    )
}