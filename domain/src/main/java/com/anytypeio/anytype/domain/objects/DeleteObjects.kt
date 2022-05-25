package com.anytypeio.anytype.domain.objects

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

/**
 * Use-case for deleting objects.
 * @see SetObjectIsArchived
 */
class DeleteObjects(
    private val repo: BlockRepository
) : BaseUseCase<Unit, DeleteObjects.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.deleteObjects(params.targets)
    }

    /**
     * @property [targets] id of the objects to delete.
     */
    class Params(val targets: List<Id>)
}