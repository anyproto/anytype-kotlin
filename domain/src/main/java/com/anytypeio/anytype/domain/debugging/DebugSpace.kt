package com.anytypeio.anytype.domain.debugging

import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.workspace.SpaceManager
import javax.inject.Inject

class DebugSpace @Inject constructor(
    private val repo: BlockRepository,
    private val spaceManager: SpaceManager,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<Unit, String>(dispatchers.io) {

    override suspend fun doWork(params: Unit): String = repo.debugSpace(
        space = spaceManager.get().let { id ->
            if (id.isEmpty())
                throw IllegalStateException("Space not found")
            else
                SpaceId(id)
        }
    )
}