package com.anytypeio.anytype.domain.page

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.Result
import com.anytypeio.anytype.domain.base.ResultInteractor

open class OpenPage(
    private val repo: BlockRepository,
    private val auth: AuthRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<OpenPage.Params, Result<Payload>>(dispatchers.io) {

    override suspend fun doWork(params: Params): Result<Payload> {
        return repo.openPage(params.obj).also {
            if (params.saveAsLastOpened) {
                auth.saveLastOpenedObjectId(params.obj)
            } else {
                auth.clearLastOpenedObject()
            }
        }
    }

    data class Params(
        val obj: Id,
        val saveAsLastOpened: Boolean
    )
}