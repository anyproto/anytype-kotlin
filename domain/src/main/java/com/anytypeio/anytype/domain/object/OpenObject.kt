package com.anytypeio.anytype.domain.`object`

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import javax.inject.Inject

class OpenObject @Inject constructor(
    private val repo: BlockRepository,
    private val settings: UserSettingsRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<OpenObject.Params, ObjectView>(dispatchers.io) {

    override suspend fun doWork(params: Params) = repo.openObject(params.obj).also {
        if (params.saveAsLastOpened) {
            val obj = ObjectWrapper.Basic(it.details[params.obj].orEmpty())
            val space = obj.targetSpaceId
            if (!space.isNullOrEmpty()) {
                settings.setLastOpenedObject(
                    id = params.obj,
                    space = SpaceId(space)
                )
            }
        }
        else {
            val givenSpace = params.spaceId
            if (givenSpace != null && givenSpace.id.isNotEmpty()) {
                settings.clearLastOpenedObject(
                    SpaceId(givenSpace.id)
                )
            }
        }
    }

    data class Params(
        val obj: Id,
        val saveAsLastOpened: Boolean = true,
        val spaceId: SpaceId? = null
    )
}