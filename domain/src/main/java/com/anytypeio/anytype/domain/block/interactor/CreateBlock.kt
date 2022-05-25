package com.anytypeio.anytype.domain.block.interactor

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.interactor.CreateBlock.Params
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload

/**
 * Use-case for creating a block.
 * @see Params
 */
open class CreateBlock(
    private val repo: BlockRepository
) : BaseUseCase<Pair<Id, Payload>, Params>() {

    override suspend fun run(params: Params) = try {
        repo.create(
            command = Command.Create(
                context = params.context,
                target = params.target,
                prototype = params.prototype,
                position = params.position
            )
        ).let {
            Either.Right(it)
        }
    } catch (t: Throwable) {
        Either.Left(t)
    }

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