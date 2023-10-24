package com.anytypeio.anytype.domain.workspace

import com.anytypeio.anytype.core_models.FileLimits
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class FileSpaceUsage(
    private val repo: BlockRepository,
    private val spaceManager: SpaceManager,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<Unit, FileLimits>(dispatchers.io) {

    override suspend fun doWork(params: Unit): FileLimits {
        return repo.fileSpaceUsage(space = SpaceId(spaceManager.get()))
    }
}