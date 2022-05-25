package com.anytypeio.anytype.domain.block.interactor

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class AddRelationToBlock(
    private val repo: BlockRepository
) : BaseUseCase<Payload, AddRelationToBlock.Params>() {

    override suspend fun run(params: AddRelationToBlock.Params) = safe {
        repo.addRelationToBlock(
            command = Command.AddRelationToBlock(
                contextId = params.context,
                blockId = params.target,
                relation = params.relation
            )
        )
    }

    data class Params(
        val context: Id,
        val target: Id,
        val relation: Relation
    )
}