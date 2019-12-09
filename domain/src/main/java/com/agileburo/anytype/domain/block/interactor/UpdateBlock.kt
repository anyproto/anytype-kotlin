package com.agileburo.anytype.domain.block.interactor

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.block.model.Command
import com.agileburo.anytype.domain.block.repo.BlockRepository

class UpdateBlock(
    private val repo: BlockRepository
) : BaseUseCase<Unit, UpdateBlock.Params>() {

    override suspend fun run(params: Params) = try {
        repo.update(
            update = Command.Update(
                contextId = params.contextId,
                blockId = params.blockId,
                text = params.text
            )
        ).let {
            Either.Right(it)
        }
    } catch (t: Throwable) {
        Either.Left(t)
    }

    class Params(
        val contextId: String,
        val blockId: String,
        val text: String
    )

}