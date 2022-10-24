package com.anytypeio.anytype.domain.page

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.Result
import com.anytypeio.anytype.domain.base.ResultInteractor

open class OpenPage(
    private val repo: BlockRepository,
    private val auth: AuthRepository
) : ResultInteractor<Id, Result<Payload>>() {

    override suspend fun doWork(params: Id): Result<Payload> {
        return repo.openPage(params).also {
            auth.saveLastOpenedObjectId(params)
        }
    }
}