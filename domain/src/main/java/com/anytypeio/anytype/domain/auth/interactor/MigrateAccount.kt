package com.anytypeio.anytype.domain.auth.interactor

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.device.PathProvider
import javax.inject.Inject

class MigrateAccount @Inject constructor(
    private val repo: AuthRepository,
    private val path: PathProvider,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<MigrateAccount.Params, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Params) {
        when(params) {
            is Params.Current -> {
                val acc = repo.getCurrentAccount()
                val path = path.providePath()
                repo.migrateAccount(
                    account = acc.id,
                    path = path
                )
            }
            is Params.Other -> {
                val path = path.providePath()
                repo.migrateAccount(
                    account = params.acc,
                    path = path
                )
            }
        }
    }

    sealed class Params {
        data object Current : Params()
        data class Other(val acc: Id) : Params()
    }
}

