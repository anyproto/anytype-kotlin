package com.anytypeio.anytype.domain.auth.interactor

import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import javax.inject.Inject

/**
 * Use case that answers whether the device holds at least one account.
 *
 * [CheckAuthorizationStatus] cannot answer this: it reports `UNAUTHORIZED` both when no account
 * exists and when an account exists without a mnemonic. A caller that must tell those two states
 * apart — for example to decide whether a lost wallet deserves a recovery prompt — needs this
 * use case instead.
 */
class HasAccount @Inject constructor(
    private val repository: AuthRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<Unit, Boolean>(dispatchers.io) {

    override suspend fun doWork(params: Unit): Boolean = repository.getAccounts().isNotEmpty()
}
