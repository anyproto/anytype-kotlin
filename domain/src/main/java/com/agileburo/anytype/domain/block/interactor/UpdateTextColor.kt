package com.agileburo.anytype.domain.block.interactor

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.block.model.Command
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.common.Id

/**
 * Use-case for updating the whole block's text color.
 */
open class UpdateTextColor(
    private val repo: BlockRepository
) : BaseUseCase<Unit, UpdateTextColor.Params>() {

    override suspend fun run(params: Params) = try {
        repo.updateTextColor(
            command = Command.UpdateTextColor(
                context = params.context,
                target = params.target,
                color = params.color
            )
        ).let {
            Either.Right(it)
        }
    } catch (t: Throwable) {
        Either.Left(t)
    }

    /**
     * Params for updating the whole block's text color.
     * @property context context id
     * @property target id of the target block, whose color we need to update.
     * @property color new color (hex)
     */
    data class Params(
        val context: Id,
        val target: Id,
        val color: String
    )
}