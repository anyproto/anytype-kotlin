package com.agileburo.anytype.domain.block.interactor

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.block.model.Command
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.common.Id

/**
 * Use-case for replacing target block by a new block (created from prototype)
 */
class ReplaceBlock(
    private val repo: BlockRepository
) : BaseUseCase<Id, ReplaceBlock.Params>() {

    override suspend fun run(params: Params) = try {
        repo.replace(
            command = Command.Replace(
                context = params.context,
                target = params.target,
                prototype = params.prototype
            )
        ).let {
            Either.Right(it)
        }
    } catch (e: Throwable) {
        Either.Left(e)
    }

    /**
     * Params for replacing target block by a new block (created from prototype)
     * @property context id of the context
     * @property target id of the block, which we need to replace
     * @property prototype prototype of the new block
     */
    data class Params(
        val context: Id,
        val target: Id,
        val prototype: Block.Prototype
    )
}