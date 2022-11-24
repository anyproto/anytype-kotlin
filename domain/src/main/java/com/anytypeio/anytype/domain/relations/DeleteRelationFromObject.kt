package com.anytypeio.anytype.domain.relations

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

/**
 * Use-case for deleting a relation from an object.
 */
class DeleteRelationFromObject(
    private val repo: BlockRepository
) : BaseUseCase<Payload, DeleteRelationFromObject.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.deleteRelationFromObject(
            ctx = params.ctx,
            relation = params.relation
        )
    }

    /**
     * @param ctx id of the object
     * @param relation relation key
     */
    class Params(
        val ctx: Id,
        val relation: Key
    )
}