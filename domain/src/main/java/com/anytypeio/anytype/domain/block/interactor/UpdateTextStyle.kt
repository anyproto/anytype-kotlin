package com.anytypeio.anytype.domain.block.interactor

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.model.Block.Content.Text
import com.anytypeio.anytype.domain.block.model.Command
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.common.Id
import com.anytypeio.anytype.domain.event.model.Payload

/**
 * Use-case for udpating a block's text style
 */
open class UpdateTextStyle(
    private val repo: BlockRepository
) : BaseUseCase<Payload, UpdateTextStyle.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.updateTextStyle(
            command = Command.UpdateStyle(
                style = params.style,
                context = params.context,
                targets = params.targets
            )
        )
    }

    /**
     * @property context context id
     * @property targets id of the target blocks, whose style we need to update.
     * @property style new style for the target block.
     */
    data class Params(
        val context: Id,
        val targets: List<Id>,
        val style: Text.Style
    )
}