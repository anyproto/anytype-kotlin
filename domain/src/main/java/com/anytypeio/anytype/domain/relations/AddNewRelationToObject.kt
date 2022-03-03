package com.anytypeio.anytype.domain.relations

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

/**
 * Use-case for adding a relation to an object.
 */
class AddNewRelationToObject(
    private val repo: BlockRepository
) : BaseUseCase<Pair<Id, Payload>, AddNewRelationToObject.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.addNewRelationToObject(
            ctx = params.ctx,
            name = params.name,
            format = params.format,
            limitObjectTypes = params.limitObjectTypes
        )
    }

    /**
     * @param [ctx] id of the object
     * @param [name] name for the new relation
     * @param [format] format of the new relation
     */
    class Params(
        val ctx: Id,
        val name: String,
        val format: RelationFormat,
        val limitObjectTypes: List<Id>
    )
}