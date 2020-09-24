package com.anytypeio.anytype.domain.block.interactor

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.model.Block
import com.anytypeio.anytype.domain.block.model.Command
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.common.Id

open class UpdateText(
    private val repo: BlockRepository
) : BaseUseCase<Unit, UpdateText.Params>() {

    override suspend fun run(params: Params) = try {
        repo.updateText(
            command = Command.UpdateText(
                contextId = params.context,
                blockId = params.target,
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
        val context: Id,
        val target: Id,
        val text: String,
        val marks: List<Block.Content.Text.Mark>
    )
}