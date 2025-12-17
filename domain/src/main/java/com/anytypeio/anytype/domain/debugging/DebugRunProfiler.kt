package com.anytypeio.anytype.domain.debugging

import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import javax.inject.Inject

class DebugRunProfiler @Inject constructor(
    private val repo: AuthRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<DebugRunProfiler.Params, String>(dispatchers.io) {

    override suspend fun doWork(params: Params): String {
        return repo.debugRunProfiler(params.durationInSeconds)
    }

    data class Params(val durationInSeconds: Int)
}
