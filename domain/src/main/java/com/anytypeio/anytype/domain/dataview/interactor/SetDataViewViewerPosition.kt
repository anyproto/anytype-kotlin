package com.anytypeio.anytype.domain.dataview.interactor

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class SetDataViewViewerPosition(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<SetDataViewViewerPosition.Params, Payload>(dispatchers.io) {

    override suspend fun doWork(params: Params): Payload = repo.setDataViewViewerPosition(
        ctx = params.ctx,
        view = params.viewer,
        dv = params.dv,
        pos = params.pos
    )

    data class Params(
        val ctx: Id,
        val dv: Id,
        val viewer: Id,
        val pos: Int
    )
}