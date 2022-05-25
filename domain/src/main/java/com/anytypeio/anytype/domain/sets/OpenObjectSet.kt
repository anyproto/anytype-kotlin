package com.anytypeio.anytype.domain.sets

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Result
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class OpenObjectSet(
    private val repo: BlockRepository,
    private val auth: AuthRepository
) : BaseUseCase<Result<Payload>, Id>() {

    override suspend fun run(params: Id) = safe {
        repo.openObjectSet(params).also {
            auth.saveLastOpenedObjectId(params)
        }
    }
}