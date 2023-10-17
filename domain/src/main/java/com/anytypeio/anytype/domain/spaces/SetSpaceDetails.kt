package com.anytypeio.anytype.domain.spaces

import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class SetSpaceDetails @Inject constructor(
    dispatchers: AppCoroutineDispatchers,
    private val repo: BlockRepository
) : ResultInteractor<SetSpaceDetails.Params, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Params) {
        repo.setSpaceDetails(
            space = params.space,
            details = params.details
        )
    }

    class Params(val space: SpaceId, val details: Struct)
}