package com.anytypeio.anytype.domain.dataview.interactor

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectOrder
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository

/**
 * Use-case for changing the order of objects within data view groups
 * (e.g. reordering cards inside a Kanban column).
 */
class SetDataViewObjectOrder(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<SetDataViewObjectOrder.Params, Payload>(dispatchers.io) {

    override suspend fun doWork(params: Params): Payload = repo.setDataViewObjectOrder(
        ctx = params.ctx,
        dv = params.dv,
        objectOrders = params.objectOrders
    )

    /**
     * @property [ctx] set or collection id
     * @property [dv] data view block id
     * @property [objectOrders] per-group ordered object ids to persist
     */
    data class Params(
        val ctx: Id,
        val dv: Id,
        val objectOrders: List<ObjectOrder>
    )
}
