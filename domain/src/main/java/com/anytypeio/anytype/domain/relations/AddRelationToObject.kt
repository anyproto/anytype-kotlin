package com.anytypeio.anytype.domain.relations

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository

/**
 * Use-case for adding a relation to an object.
 */
class AddRelationToObject(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<AddRelationToObject.Params, Payload?>(dispatchers.io) {

    override suspend fun doWork(params: Params): Payload? {
        return repo.addRelationToObject(
            ctx = params.ctx,
            relation = params.relationKey
        )
    }

    /**
     * @param ctx id of the object
     * @param relationKey relation key
     */
    class Params(
        val ctx: Id,
        val relationKey: Key
    )
}