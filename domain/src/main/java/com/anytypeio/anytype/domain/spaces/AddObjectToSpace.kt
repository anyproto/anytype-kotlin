package com.anytypeio.anytype.domain.spaces

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

/**
 * Use-case for adding one object to a space.
 * Returns id of the object added to the given space.
 */
class AddObjectToSpace @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<AddObjectToSpace.Params, AddObjectToSpace.Result>(dispatchers.io) {

    override suspend fun doWork(params: Params): Result {
        val command = Command.AddObjectToSpace(
            objectId = params.obj,
            space = params.space
        )
        val result = repo.addObjectToSpace(command)
        return Result(
            id = result.first,
            type = result.second
        )
    }

    data class Params(
        val obj: Id,
        val space: Id
    )

    data class Result(
        val id: Id,
        val type: Struct?
    )
}

typealias AddObjectTypeToSpace = AddObjectToSpace