package com.anytypeio.anytype.domain.block.interactor

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.interactor.CreateBlock.Params
import com.anytypeio.anytype.domain.block.repo.BlockRepository

/**
 * Use-case for creating a block.
 * @see Params
 */
open class CreateBlock(
    private val repo: BlockRepository
) : ResultInteractor<Params, Pair<Id, Payload>>() {

    override suspend fun doWork(params: Params): Pair<Id, Payload> =
        repo.create(
            command = Command.Create(
                context = params.context,
                target = params.target,
                prototype = params.prototype,
                position = params.position
            )
        )

    /**
     * Params for creating a block
     * @property context id of the context of the block (i.e. page, dashboard or something else)
     * @property target id of the block associated with the block we need to create
     * @property position position of the block that we need to create in relation with the target block
     * @property prototype a prototype of the block we would like to create
     */
    data class Params(
        val context: Id,
        val target: Id,
        val position: Position,
        val prototype: Block.Prototype
    )
}