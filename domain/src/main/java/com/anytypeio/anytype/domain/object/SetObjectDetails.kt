package com.anytypeio.anytype.domain.`object`

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class SetObjectDetails @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<SetObjectDetails.Params, Payload>(dispatchers.io) {

    override suspend fun doWork(params: Params): Payload = repo.setObjectDetails(
        ctx = params.ctx,
        details = params.details
    )

    data class Params(
        val ctx: Id,
        val details: Struct
    )
}