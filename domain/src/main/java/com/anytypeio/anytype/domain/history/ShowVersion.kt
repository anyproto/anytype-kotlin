package com.anytypeio.anytype.domain.history

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.history.ShowVersionResponse
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class ShowVersion @Inject constructor(
    dispatchers: AppCoroutineDispatchers,
    private val repo: BlockRepository
) : ResultInteractor<ShowVersion.Params, ShowVersionResponse>(dispatchers.io) {

    override suspend fun doWork(params: Params): ShowVersionResponse {
        val command = Command.ShowVersion(
            objectId = params.objectId,
            versionId = params.versionId,
            traceId = params.traceId
        )
        return repo.showVersion(command)
    }

    data class Params(
        val objectId: Id,
        val versionId: Id,
        val traceId: Id
    )
}