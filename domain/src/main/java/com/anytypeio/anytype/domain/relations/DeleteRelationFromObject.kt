package com.anytypeio.anytype.domain.relations

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository

/**
 * Use-case for deleting a relation from an object.
 */
class DeleteRelationFromObject(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<DeleteRelationFromObject.Params, Payload>(dispatchers.io) {

    override suspend fun doWork(params: Params): Payload {
        return repo.deleteRelationFromObject(
            ctx = params.ctx,
            relations = params.relations
        )
    }

    /**
     * @param ctx id of the object
     * @param relations relation keys
     */
    class Params(
        val ctx: Id,
        val relations: List<Key>
    )
}