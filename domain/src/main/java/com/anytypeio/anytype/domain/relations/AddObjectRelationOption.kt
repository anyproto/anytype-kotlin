package com.anytypeio.anytype.domain.relations

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

/**
 * Use-case for adding a option to a relation.
 */
class AddObjectRelationOption(
    private val repo: BlockRepository
) : BaseUseCase<Pair<Payload, Id?>, AddObjectRelationOption.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.addObjectRelationOption(
            ctx = params.ctx,
            relation = params.relation,
            name = params.name,
            color = params.color
        )
    }

    /**
     * @property [ctx] operation context
     * @property [relation] relation id or relation key
     * @property [name] name for new option
     */
    class Params(
        val ctx: Id,
        val relation: Id,
        val name: String,
        val color: String
    )
}