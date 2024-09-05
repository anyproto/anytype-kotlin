package com.anytypeio.anytype.domain.search

import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import javax.inject.Inject

class GetLastSearchQuery @Inject constructor(
    private val settings: UserSettingsRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<GetLastSearchQuery.Params, String>(dispatchers.io) {

    override suspend fun doWork(params: Params): String {
        return settings.getLastSearchQuery(space = params.space)
    }

    data class Params(val space: SpaceId)
}