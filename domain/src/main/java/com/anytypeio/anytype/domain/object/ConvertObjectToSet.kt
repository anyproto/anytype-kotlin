package com.anytypeio.anytype.domain.`object`

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class ConvertObjectToSet(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<ConvertObjectToSet.Params, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Params) =
        repo.objectToSet(
            ctx = params.ctx,
            source = params.sources
        )

    data class Params(
        val ctx: Id,
        val sources: List<String>
    )
}