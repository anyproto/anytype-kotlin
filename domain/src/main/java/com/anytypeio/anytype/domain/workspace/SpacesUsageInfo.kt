package com.anytypeio.anytype.domain.workspace

import com.anytypeio.anytype.core_models.NodeUsageInfo
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class SpacesUsageInfo(
    private val repo: BlockRepository,
    private val spaceManager: SpaceManager,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<Unit, NodeUsageInfo>(dispatchers.io) {

    override suspend fun doWork(params: Unit): NodeUsageInfo {
        return repo.nodeUsage()
    }
}