package com.anytypeio.anytype.domain.icon

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.block.repo.BlockRepository

/**
 * Use-case for remove icon from object.
 */
class RemoveDocumentIcon(
    private val repo: BlockRepository
) : RemoveIcon<Id>() {

    override suspend fun run(params: Id) = safe {
        repo.removeDocumentIcon(params)
    }
}