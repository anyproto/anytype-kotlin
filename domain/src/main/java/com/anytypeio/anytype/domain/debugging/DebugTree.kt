package com.anytypeio.anytype.domain.debugging

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class DebugTree(private val repo: BlockRepository) : ResultInteractor<DebugTree.Params, Id>() {

    override suspend fun doWork(params: Params): Id =
        repo.debugTree(objectId = params.objectId, path = params.path)

    data class Params(
        val objectId: Id,
        val path: String
    )
}