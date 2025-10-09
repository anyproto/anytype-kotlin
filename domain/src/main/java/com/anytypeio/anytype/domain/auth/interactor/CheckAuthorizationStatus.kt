package com.anytypeio.anytype.domain.auth.interactor

import com.anytypeio.anytype.core_models.Account
import com.anytypeio.anytype.domain.auth.model.AuthStatus
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import javax.inject.Inject

/**
 * Use case for checking authorisation status.
 * User can be either authorized or unauthorized based on number of available accounts.
 * @param repository repository containing information about available accounts
 */
class CheckAuthorizationStatus @Inject constructor(
    private val repository: AuthRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<Unit, Pair<AuthStatus, Account?>>(dispatchers.io) {

    override suspend fun doWork(params: Unit): Pair<AuthStatus, Account?> {
        return repository.getAccounts().let { accounts ->
            val mnemonic = repository.getMnemonic()
            if (accounts.isNotEmpty() && !mnemonic.isNullOrBlank())
                AuthStatus.AUTHORIZED to accounts.firstOrNull()
            else
                AuthStatus.UNAUTHORIZED to null
        }
    }
}