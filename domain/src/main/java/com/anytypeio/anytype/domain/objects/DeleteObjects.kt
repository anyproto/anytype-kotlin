package com.anytypeio.anytype.domain.objects

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository

/**
 * Use-case for deleting objects.
 * @see SetObjectIsArchived
 */
class DeleteObjects(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<DeleteObjects.Params, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Params) = repo.deleteObjects(params.targets)

    /**
     * @property [targets] id of the objects to delete.
     */
    class Params(val targets: List<Id>)
}