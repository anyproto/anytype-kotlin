package com.anytypeio.anytype.domain.`object`

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class ConvertObjectToCollection(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<ConvertObjectToCollection.Params, Id>(dispatchers.io) {

    override suspend fun doWork(params: Params): Id =
        repo.objectToCollection(ctx = params.ctx)

    data class Params(val ctx: Id)
}