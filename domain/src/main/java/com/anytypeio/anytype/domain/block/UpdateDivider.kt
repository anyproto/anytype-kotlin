package com.anytypeio.anytype.domain.block

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.core_models.Payload

/**
 * Use-case for updating a divider style
 */
open class UpdateDivider(
    private val repo: BlockRepository
) : BaseUseCase<Payload, UpdateDivider.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.updateDivider(
            command = Command.UpdateDivider(
                style = params.style,
                context = params.context,
                targets = params.targets
            )
        )
    }

    /**
     * @property context context id
     * @property targets id of the target blocks, whose style we need to update.
     * @property style new style for the target blocks.
     */
    data class Params(
        val context: Id,
        val targets: List<Id>,
        val style: Block.Content.Divider.Style
    )
}