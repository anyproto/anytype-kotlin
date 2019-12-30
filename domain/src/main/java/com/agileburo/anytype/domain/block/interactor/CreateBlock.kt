package com.agileburo.anytype.domain.block.interactor

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.block.interactor.CreateBlock.Params
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.block.model.Command
import com.agileburo.anytype.domain.block.model.Position
import com.agileburo.anytype.domain.block.repo.BlockRepository

/**
 * Use-case for creating a block.
 * @see Params
 */
class CreateBlock(
    private val repo: BlockRepository
) : BaseUseCase<Unit, Params>() {

    override suspend fun run(params: Params) = try {
        repo.create(
            command = Command.Create(
                contextId = params.contextId,
                targetId = params.targetId,
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
     * @property contextId id of the context of the block (i.e. page, dashboard or something else)
     * @property targetId id of the block associated with the block we need to create
     * @property position position of the block that we need to create in relation with the target block
     * @property prototype a prototype of the block we would like to create
     */
    class Params(
        val contextId: String,
        val targetId: String,
        val position: Position,
        val prototype: Block.Prototype
    )

}