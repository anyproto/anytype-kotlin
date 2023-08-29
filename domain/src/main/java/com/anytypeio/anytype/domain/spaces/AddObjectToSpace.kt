package com.anytypeio.anytype.domain.spaces

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository

/**
 * Use-case for adding one object to a space.
 * Returns id of the object added to the given space.
 */
class AddObjectToSpace(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
): ResultInteractor<AddObjectToSpace.Params, Id>(dispatchers.io) {

    override suspend fun doWork(params: Params): Id = repo.addObjectToSpace(
        obj = params.obj,
        space = params.space
    )

    data class Params(
        val obj: Id,
        val space: Id
    )
}

typealias AddObjectTypeToSpace = AddObjectToSpace