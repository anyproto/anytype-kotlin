package com.anytypeio.anytype.domain.spaces

import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import javax.inject.Inject

class SaveCurrentSpace @Inject constructor(
    dispatchers: AppCoroutineDispatchers,
    private val repo: UserSettingsRepository
) : ResultInteractor<SaveCurrentSpace.Params, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Params) {
        repo.setCurrentSpace(params.space)
    }

    class Params(val space: SpaceId)
}