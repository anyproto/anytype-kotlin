package com.anytypeio.anytype.domain.workspace

import com.anytypeio.anytype.core_models.FileLimitsEvent
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import kotlinx.coroutines.flow.Flow

class InterceptFileLimitEvents(
    private val fileLimitsEventChannel: FileLimitsEventChannel,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<Unit, Flow<List<FileLimitsEvent>>>(dispatchers.io) {

    override suspend fun doWork(params: Unit): Flow<List<FileLimitsEvent>> {
        return fileLimitsEventChannel.observe()
    }
}