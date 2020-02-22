package com.agileburo.anytype.domain.block.interactor

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.block.model.Command
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.common.Id

/**
 * Use-case for updating the whole block's text color.
 */
open class UpdateBackgroundColor(
    private val repo: BlockRepository
) : BaseUseCase<Unit, UpdateBackgroundColor.Params>() {

    override suspend fun run(params: Params) = try {
        repo.updateBackgroundColor(
            command = Command.UpdateBackgroundColor(
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
     * Params for updating background color for the whole block.
     * @property context context id
     * @property target id of the target block, whose background color we need to update.
     * @property color new color (hex)
     */
    data class Params(
        val context: Id,
        val target: Id,
        val color: String
    )
}