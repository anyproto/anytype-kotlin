package com.anytypeio.anytype.domain.block.interactor

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload

/**
 * Use-case for updating the whole block's text color.
 */
open class UpdateTextColor(
    private val repo: BlockRepository
) : BaseUseCase<Payload, UpdateTextColor.Params>() {

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