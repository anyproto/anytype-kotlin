package com.anytypeio.anytype.domain.objects

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

/**
 * Use-case for archiving (or restoring from archive) an object.
 */
@Deprecated(
    "Use SetObjectListIsArchived instead",
    replaceWith = ReplaceWith("SetObjectListIsArchived")
)
class SetObjectIsArchived(
    private val repo: BlockRepository
) : BaseUseCase<Unit, SetObjectIsArchived.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.setObjectIsArchived(
            ctx = params.context,
            isArchived = params.isArchived
        )
    }


    /**
     * Params for archiving a document
     * @property context id of the context
     */
    data class Params(
        val context: Id,
        val isArchived: Boolean
    )
}