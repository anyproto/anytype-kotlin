package com.anytypeio.anytype.domain.spaces

import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class DeleteSpace @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<SpaceId, Unit>(dispatchers.io)  {

    override suspend fun doWork(params: SpaceId) {
        repo.deleteSpace(space = params)
    }
}