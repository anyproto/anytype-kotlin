package com.anytypeio.anytype.domain.icon

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.icon.RemoveObjectIcon.Params

class RemoveObjectIcon(
    private val repository: BlockRepository,
    dispatchers: AppCoroutineDispatchers
): ResultInteractor<Params, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Params) {
        repository.removeDocumentIcon(ctx = params.objectId)
    }

    data class Params(val objectId: Id)
}