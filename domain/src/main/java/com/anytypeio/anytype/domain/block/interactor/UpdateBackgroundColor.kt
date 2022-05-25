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
open class UpdateBackgroundColor(
    private val repo: BlockRepository
) : BaseUseCase<Payload, UpdateBackgroundColor.Params>() {

    override suspend fun run(params: Params) = try {
        repo.updateBackgroundColor(
            command = Command.UpdateBackgroundColor(
                context = params.context,
                targets = params.targets,
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
     * @property targets id of the target block, whose background color we need to update.
     * @property color new color (hex)
     */
    data class Params(
        val context: Id,
        val targets: List<Id>,
        val color: String
    )
}