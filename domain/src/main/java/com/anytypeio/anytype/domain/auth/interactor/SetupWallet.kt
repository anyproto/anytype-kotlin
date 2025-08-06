package com.anytypeio.anytype.domain.auth.interactor

import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import javax.inject.Inject

/**
 * Use case for creating new wallet.
 */
open class SetupWallet @Inject constructor(
    private val repository: AuthRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<SetupWallet.Params, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Params) {
        val wallet = repository.createWallet(
            path = params.path
        )
        repository.saveMnemonic(wallet.mnemonic)
    }

    /**
     * @property path repository path.
     */
    class Params(val path: String)
}