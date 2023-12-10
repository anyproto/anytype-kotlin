package com.anytypeio.anytype.domain.networkmode

import com.anytypeio.anytype.core_models.NetworkModeConfig
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import javax.inject.Inject

class SetNetworkMode @Inject constructor(
    private val repository: AuthRepository,
    dispatchers: AppCoroutineDispatchers,
) : ResultInteractor<SetNetworkMode.Params, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Params) {
        repository.setNetworkMode(params.networkModeConfig)
    }

    data class Params(val networkModeConfig: NetworkModeConfig)
}