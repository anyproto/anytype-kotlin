package com.anytypeio.anytype.domain.dataview.interactor

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class SetActiveViewer(private val repo: BlockRepository, dispatchers: AppCoroutineDispatchers) :
    ResultInteractor<SetActiveViewer.Params, Payload>(dispatchers.io) {

    override suspend fun doWork(params: Params): Payload {
        val command = Command.DataViewSetActiveView(
            ctx = params.ctx,
            viewerId = params.viewer,
            dataViewId = params.dv
        )
        return repo.dataViewSetActiveView(command)
    }

    data class Params(
        val ctx: Id,
        val viewer: Id,
        val dv: Id
    )
}