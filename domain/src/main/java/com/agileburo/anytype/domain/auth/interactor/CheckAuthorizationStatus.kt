package com.agileburo.anytype.domain.auth.interactor

import com.agileburo.anytype.domain.auth.model.AuthStatus
import com.agileburo.anytype.domain.auth.repo.AuthRepository
import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either

/**
 * Created by Konstantin Ivanov
 * email :  ki@agileburo.com
 * on 2019-10-21.
 */
class CheckAuthorizationStatus(
    private val repository: AuthRepository
) : BaseUseCase<AuthStatus, Unit>() {

    override suspend fun run(params: Unit) = try {
        repository.getAvailableAccounts().let { accounts ->
            if (accounts.isNotEmpty())
                Either.Right(AuthStatus.AUTHORIZED)
            else
                Either.Right(AuthStatus.UNAUTHORIZED)
        }
    } catch (e: Throwable) {
        Either.Left(e)
    }
}