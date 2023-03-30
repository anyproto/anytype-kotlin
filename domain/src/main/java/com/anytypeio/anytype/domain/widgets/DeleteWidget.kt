package com.anytypeio.anytype.domain.widgets

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class DeleteWidget @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<DeleteWidget.Params, Payload>(dispatchers.io) {

    override suspend fun doWork(params: Params): Payload = repo.unlink(
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