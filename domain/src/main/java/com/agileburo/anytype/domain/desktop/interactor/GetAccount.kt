package com.agileburo.anytype.domain.desktop.interactor

import com.agileburo.anytype.domain.auth.model.Account
import com.agileburo.anytype.domain.auth.repo.AuthRepository
import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either

/** Use case for getting currently selected user account.
 * @property repository repository containing user account
 */
class GetAccount(
    private val repository: AuthRepository
) : BaseUseCase<Account, BaseUseCase.None>() {

    override suspend fun run(params: None) = try {
        repository.getAccount().let {
            Either.Right(it)
        }
    } catch (t: Throwable) {
        Either.Left(t)
    }
}