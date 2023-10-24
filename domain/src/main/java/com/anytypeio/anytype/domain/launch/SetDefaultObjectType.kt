package com.anytypeio.anytype.domain.launch

import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.config.UserSettingsRepository

class SetDefaultObjectType(
    private val repo: UserSettingsRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<SetDefaultObjectType.Params, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Params) {
        repo.setDefaultObjectType(
            space = params.space,
            type = params.type
        )
    }

    data class Params(
        val space: SpaceId,
        val type: TypeId,
    )
}