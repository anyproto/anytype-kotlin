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
) : BaseUseCase<Unit, CreateBlock.Params>() {

    override suspend fun run(params: Params) = try {
        repo.create(
            command = Command.Create(
                contextId = params.contextId,
                targetId = params.targetId,
                block = params.block,
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
     * @property block a prototype of the block we would like to create
     */
    class Params(
        val contextId: String,
        val targetId: String,
        val position: Position,
        val block: Block
    ) {
        companion object {
            fun empty(
                contextId: String,
                targetId: String
            ): Params = Params(
                contextId = contextId,
                targetId = targetId,
                block = Block(
                    id = "",
                    children = emptyList(),
                    fields = Block.Fields(emptyMap()),
                    content = Block.Content.Text(
                        text = "",
                        marks = emptyList(),
                        style = Block.Content.Text.Style.P
                    )
                ),
                position = Position.AFTER
            )
        }
    }

}