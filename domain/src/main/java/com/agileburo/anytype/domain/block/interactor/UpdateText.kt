package com.agileburo.anytype.domain.block.interactor

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.block.model.Command
import com.agileburo.anytype.domain.block.repo.BlockRepository

open class UpdateText(
    private val repo: BlockRepository
) : BaseUseCase<Unit, UpdateText.Params>() {

    override suspend fun run(params: Params) = try {
        repo.updateText(
            command = Command.UpdateText(
                contextId = params.contextId,
                blockId = params.blockId,
                text = params.text,
                marks = params.marks
            )
        ).let {
            Either.Right(it)
        }
    } catch (t: Throwable) {
        Either.Left(t)
    }

    data class Params(
        val contextId: String,
        val blockId: String,
        val text: String,
        val marks: List<Block.Content.Text.Mark>
    )

}