package com.anytypeio.anytype.domain.relations

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

/**
 * Use-case for adding one or more relations to featured relations list.
 */
class AddToFeaturedRelations(
    private val repo: BlockRepository
) : BaseUseCase<Payload, AddToFeaturedRelations.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.addToFeaturedRelations(
            ctx = params.ctx,
            relations = params.relations
        )
    }

    /**
     * @property [relations] list of ids to add to existing ones.
     */
    class Params(
        val ctx: Id,
        val relations: List<Id>
    )
}