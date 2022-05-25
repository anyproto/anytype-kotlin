package com.anytypeio.anytype.domain.`object`

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class DuplicateObject(
    private val repo: BlockRepository
) : BaseUseCase<Id, Id>() {

    override suspend fun run(params: Id) = safe {
        repo.duplicateObject(params)
    }
}