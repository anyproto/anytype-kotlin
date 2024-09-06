package com.anytypeio.anytype.domain.search

import com.anytypeio.anytype.core_models.GlobalSearchCache
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import javax.inject.Inject

class RestoreGlobalSearch @Inject constructor(
    private val settings: UserSettingsRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<RestoreGlobalSearch.Params, RestoreGlobalSearch.Response>(dispatchers.io) {

    override suspend fun doWork(params: Params): Response {
        val globalSearchCache = settings.getLatestGlobalSearch(
            space = params.spaceId
        )
        return Response(globalSearchCache)
    }

    data class Params(
        val spaceId: SpaceId
    )

    data class Response(
        val globalSearchCache: GlobalSearchCache?
    )
}