package com.anytypeio.anytype.domain.history

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.history.Version
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class GetVersions @Inject constructor(
    dispatchers: AppCoroutineDispatchers,
    private val repo: BlockRepository
) : ResultInteractor<GetVersions.Params, List<Version>>(dispatchers.io) {

    override suspend fun doWork(params: Params): List<Version> {
        val command = Command.VersionHistory.GetVersions(
            objectId = params.objectId,
            lastVersion = params.lastVersion,
            limit = params.limit
        )
        return repo.getVersions(command)
    }

    data class Params(
        val objectId: Id,
        val lastVersion: Id? = null,
        val limit: Int = 300
    )
}