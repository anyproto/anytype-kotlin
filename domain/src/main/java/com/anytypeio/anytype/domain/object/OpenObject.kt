package com.anytypeio.anytype.domain.`object`

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class OpenObject(
    private val repo: BlockRepository,
    private val auth: AuthRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<OpenObject.Params, ObjectView>(dispatchers.io) {

    override suspend fun doWork(params: Params) = repo.openObject(params.obj).also {
        if (params.saveAsLastOpened)
            auth.saveLastOpenedObjectId(params.obj)
        else
            auth.clearLastOpenedObject()
    }

    data class Params(
        val obj: Id,
        val saveAsLastOpened: Boolean = true
    )
}