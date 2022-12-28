package com.anytypeio.anytype.domain.`object`

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultatInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import kotlinx.coroutines.withContext

class OpenObject(
    private val repo: BlockRepository,
    private val auth: AuthRepository,
    private val dispatchers: AppCoroutineDispatchers
) : ResultatInteractor<Id, ObjectView>() {
    override suspend fun execute(params: Id): ObjectView = withContext(dispatchers.io) {
        repo.openObject(params).also {
            auth.saveLastOpenedObjectId(params)
        }
    }
}