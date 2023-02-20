package com.anytypeio.anytype.domain.objects

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.objects.SetObjectListIsArchived.Params

/**
 * Use-case for archiving (or restoring from archive) a list of objects.
 */
class SetObjectListIsArchived(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<Params, Unit>(dispatchers.io) {

    /**
     * Params for archiving a list of objects.
     * @property [targets] id of the objects to archive/restore.
     */
    data class Params(
        val targets: List<Id>,
        val isArchived: Boolean
    )

    override suspend fun doWork(params: Params) = repo.setObjectListIsArchived(
        params.targets, params.isArchived
    )
}