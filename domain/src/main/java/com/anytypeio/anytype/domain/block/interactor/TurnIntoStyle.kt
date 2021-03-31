package com.anytypeio.anytype.domain.block.interactor

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class TurnIntoStyle(
    private val repo: BlockRepository
) : BaseUseCase<Payload, TurnIntoStyle.Params>() {

    override suspend fun run(
        params: Params
    ) = safe {
        repo.turnInto(
            context = params.context,
            targets = params.targets,
            style = params.style
        )
    }

    data class Params(
        val context: Id,
        val targets: List<Id>,
        val style: Block.Content.Text.Style
    )
}