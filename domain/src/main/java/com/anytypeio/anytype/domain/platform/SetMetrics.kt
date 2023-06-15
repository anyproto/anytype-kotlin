package com.anytypeio.anytype.domain.platform

import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor

class SetMetrics(
    private val auth: AuthRepository,
    dispatchers: AppCoroutineDispatchers,
) : ResultInteractor<SetMetrics.Params, Unit>(dispatchers.io) {
    override suspend fun doWork(params: Params) {
        auth.setMetrics(
            platform = params.platform,
            version = params.version
        )
    }
    data class Params(
        val platform: String,
        val version: String
    )
}