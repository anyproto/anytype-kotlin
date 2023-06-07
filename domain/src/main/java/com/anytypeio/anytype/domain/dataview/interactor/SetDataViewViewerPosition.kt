package com.anytypeio.anytype.domain.dataview.interactor

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository

/**
 * Use-case for ordering views of the data view.
 */
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

    /**
     * @property [ctx] set or collection id
     * @property [dv] data view block id
     * @property [viewer] view id, whose position is changed
     * @property [pos] new position of the [viewer]
     */
    data class Params(
        val ctx: Id,
        val dv: Id,
        val viewer: Id,
        val pos: Int
    )
}