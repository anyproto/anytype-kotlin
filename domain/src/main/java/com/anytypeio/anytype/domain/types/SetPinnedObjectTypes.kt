package com.anytypeio.anytype.domain.types

import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import javax.inject.Inject

class SetPinnedObjectTypes @Inject constructor(
    private val repo: UserSettingsRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<SetPinnedObjectTypes.Params, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Params) {
        repo.setPinnedObjectTypes(
            space = params.space,
            types = params.types
        )
    }

    class Params(
        val space: SpaceId,
        val types: List<TypeId>
    )
}