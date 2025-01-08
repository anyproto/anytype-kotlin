package com.anytypeio.anytype.domain.debugging

import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import javax.inject.Inject

class DebugExportLogs @Inject constructor(
    private val repo: AuthRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<DebugExportLogs.Params, String>(dispatchers.io) {

    override suspend fun doWork(params: Params): String {
        return repo.debugExportLogs(params.dir)
    }

    data class Params(val dir: String)
}