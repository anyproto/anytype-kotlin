package com.anytypeio.anytype.domain.auth.interactor

import com.anytypeio.anytype.domain.auth.model.AuthStatus
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either

/**
 * Use case for checking authorisation status.
 * User can be either authorized or unauthorized based on number of available accounts.
 * @param repository repository containing information about available accounts
 */
class CheckAuthorizationStatus(
    private val repository: AuthRepository
) : BaseUseCase<AuthStatus, Unit>() {

    override suspend fun run(params: Unit) = try {
        repository.getAccounts().let { accounts ->
            if (accounts.isNotEmpty())
                Either.Right(AuthStatus.AUTHORIZED)
            else
                Either.Right(AuthStatus.UNAUTHORIZED)
        }
    } catch (e: Throwable) {
        Either.Left(e)
    }
}