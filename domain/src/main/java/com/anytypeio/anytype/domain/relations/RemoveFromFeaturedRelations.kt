package com.anytypeio.anytype.domain.relations

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

/**
 * Use-case for removing one or more relations from featured relations list.
 */
class RemoveFromFeaturedRelations(
    private val repo: BlockRepository
) : BaseUseCase<Payload, RemoveFromFeaturedRelations.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.removeFromFeaturedRelations(
            ctx = params.ctx,
            relations = params.relations
        )
    }

    /**
     * @property [relations] list of ids to remove from existing ones.
     */
    class Params(
        val ctx: Id,
        val relations: List<Id>
    )
}