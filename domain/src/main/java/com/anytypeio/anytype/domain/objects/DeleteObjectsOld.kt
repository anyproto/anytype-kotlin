package com.anytypeio.anytype.domain.objects

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

/**
 * Use-case for deleting objects.
 * @see SetObjectIsArchived
 */
@Deprecated(
    "Use DeleteObjectsOld instead",
    replaceWith = ReplaceWith(
        "DeleteObjectsOld"
    )
)
class DeleteObjectsOld(
    private val repo: BlockRepository
) : BaseUseCase<Unit, DeleteObjectsOld.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.deleteObjects(params.targets)
    }

    /**
     * @property [targets] id of the objects to delete.
     */
    class Params(val targets: List<Id>)
}