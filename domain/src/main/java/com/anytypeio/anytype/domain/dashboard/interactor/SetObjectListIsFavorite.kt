package com.anytypeio.anytype.domain.dashboard.interactor

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class SetObjectListIsFavorite(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<SetObjectListIsFavorite.Params, Unit>(dispatchers.io) {

    data class Params(
        val objectIds: List<Id>,
        val isFavorite: Boolean
    )

    override suspend fun doWork(params: Params) =
        repo.setObjectListIsFavorite(params.objectIds, params.isFavorite)
}