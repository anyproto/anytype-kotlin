package com.anytypeio.anytype.domain.account

import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import javax.inject.Inject

class UpdateReserveMultiplexSetting @Inject constructor(
    private val repository: AuthRepository,
    dispatchers: AppCoroutineDispatchers,
) : ResultInteractor<UpdateReserveMultiplexSetting.Params, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Params) {
        repository.updateReserveMultiplexLibrary(params.useReserve)
    }

    data class Params(val useReserve: Boolean)
}

class FetchReserveMultiplexingSetting @Inject constructor(
    private val repository: AuthRepository,
    dispatchers: AppCoroutineDispatchers,
) : ResultInteractor<Unit, Boolean>(dispatchers.io) {

    override suspend fun doWork(params: Unit): Boolean {
        return repository.fetchReserveMultiplexLibrary()
    }
}