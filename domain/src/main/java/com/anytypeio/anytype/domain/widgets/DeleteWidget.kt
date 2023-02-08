package com.anytypeio.anytype.domain.widgets

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.ResultatInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class DeleteWidget(
    private val repo: BlockRepository
) : ResultatInteractor<DeleteWidget.Params, Payload>() {

    override suspend fun execute(params: Params) = repo.unlink(
        command = Command.Unlink(
            context = params.ctx,
            targets = params.targets
        )
    )

    /**
     * Params for deleting widget blocks from widget object
     * @property [ctx] id of the widget object
     * @property [targets] ids of widget blocks to delete from widget object
     */
    data class Params(
        val ctx: Id,
        val targets: List<Id>
    )
}