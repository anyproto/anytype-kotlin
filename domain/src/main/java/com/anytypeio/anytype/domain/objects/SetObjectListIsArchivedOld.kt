package com.anytypeio.anytype.domain.objects

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

/**
 * Use-case for archiving (or restoring from archive) a list of objects.
 */
@Deprecated(
    "Use SetObjectListIsArchived instead",
    replaceWith = ReplaceWith(
        "SetObjectListIsArchived"
    )
)
class SetObjectListIsArchivedOld(
    private val repo: BlockRepository
) : BaseUseCase<Unit, SetObjectListIsArchivedOld.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.setObjectListIsArchived(
            isArchived = params.isArchived,
            targets = params.targets
        )
    }


    /**
     * Params for archiving a list of objects.
     * @property [targets] id of the objects to archive/restore.
     */
    data class Params(
        val targets: List<Id>,
        val isArchived: Boolean
    )
}