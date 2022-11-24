package com.anytypeio.anytype.domain.relations

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

/**
 * Use-case for adding a relation to an object.
 */
class AddRelationToObject(
    private val repo: BlockRepository
) : BaseUseCase<Payload, AddRelationToObject.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.addRelationToObject(
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