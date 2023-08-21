package com.anytypeio.anytype.domain.spaces

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class CreateSpace(
    private val repo: BlockRepository,
    private val dispatchers: AppCoroutineDispatchers
) : ResultInteractor<CreateSpace.Params, Id>(dispatchers.io) {

    override suspend fun doWork(params: Params): Id = repo.createWorkspace(
        params.details
    )

    data class Params(val details: Struct)
}