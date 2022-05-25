package com.anytypeio.anytype.domain.block.interactor

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

/**
 * Use-case for updating the whole block's text color.
 */
open class UpdateTextColor(
    private val repo: BlockRepository
) : BaseUseCase<Payload, UpdateTextColor.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.updateTextColor(
            command = Command.UpdateTextColor(
                context = params.context,
                targets = params.targets,
                color = params.color
            )
        )
    }

    /**
     * Params for updating the whole block's text color.
     * @property context context id
     * @property targets id of the target blocks, whose color we need to update.
     * @property color new color (hex)
     */
    data class Params(
        val context: Id,
        val targets: List<Id>,
        val color: String
    )
}