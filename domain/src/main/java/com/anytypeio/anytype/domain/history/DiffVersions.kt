package com.anytypeio.anytype.domain.history

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.history.DiffVersionResponse
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class DiffVersions @Inject constructor(
    dispatchers: AppCoroutineDispatchers,
    private val repo: BlockRepository
) : ResultInteractor<DiffVersions.Params, DiffVersionResponse>(dispatchers.io) {

    override suspend fun doWork(params: Params): DiffVersionResponse {
        val command = Command.DiffVersions(
            ctx = params.ctx,
            objectId = params.objectId,
            spaceId = params.spaceId,
            currentVersion = params.currentVersion,
            previousVersion = params.previousVersion
        )
        return repo.diffVersions(command)
    }

    data class Params(
        val ctx: Id,
        val objectId: Id,
        val spaceId: Id,
        val currentVersion: Id,
        val previousVersion: Id
    )
}